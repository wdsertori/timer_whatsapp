package com.wall.timer25;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Segurar o ícone > "Atualizar": baixa a versão mais nova do GitHub e abre o instalador. */
public class UpdateActivity extends Activity {
    private static final String BASE =
            "https://github.com/wdsertori/timer_whatsapp/releases/download/latest/";
    private static final String ACTION_STATUS = "com.wall.timer25.INSTALL_STATUS";

    private AlertDialog progress;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (ACTION_STATUS.equals(getIntent().getAction())) {
            handleStatus(getIntent());
            return;
        }
        progress = new AlertDialog.Builder(this)
                .setTitle("Atualização")
                .setMessage("Verificando… (versão atual " + currentVersion() + ")")
                .setCancelable(false)
                .show();
        new Thread(this::checkAndDownload).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_STATUS.equals(intent.getAction())) handleStatus(intent);
    }

    private void checkAndDownload() {
        try {
            int remote = Integer.parseInt(readText(BASE + "version.txt").trim());
            if (remote <= currentVersion()) {
                done("Você já está na versão mais recente (" + currentVersion() + ")");
                return;
            }
            if (!getPackageManager().canRequestPackageInstalls()) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(),
                            "Permita instalar apps e toque em Atualizar de novo",
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + getPackageName())));
                    finish();
                });
                return;
            }
            runOnUiThread(() -> progress.setMessage("Baixando versão " + remote + "…"));
            install(BASE + "TimerZap.apk");
        } catch (Exception e) {
            done("Falha ao atualizar: " + e.getMessage());
        }
    }

    /** Grava o APK numa sessão do PackageInstaller; o Android mostra "Atualizar?" ao usuário. */
    private void install(String url) throws Exception {
        PackageInstaller pi = getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(getPackageName());
        int id = pi.createSession(params);
        try (PackageInstaller.Session s = pi.openSession(id)) {
            HttpURLConnection c = open(url);
            try (InputStream in = c.getInputStream();
                 OutputStream out = s.openWrite("app.apk", 0, -1)) {
                byte[] buf = new byte[65536];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                s.fsync(out);
            }
            Intent status = new Intent(this, UpdateActivity.class).setAction(ACTION_STATUS);
            PendingIntent p = PendingIntent.getActivity(this, 0, status,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            s.commit(p.getIntentSender());
        }
    }

    private void handleStatus(Intent i) {
        int st = i.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        if (st == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = i.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirm != null) startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            if (progress != null) progress.dismiss();
            finish();
        } else if (st == PackageInstaller.STATUS_SUCCESS) {
            done("Atualizado!");
        } else {
            String msg = i.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            done("Instalação não concluída" + (msg != null ? ": " + msg : ""));
        }
    }

    private void done(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            if (progress != null) progress.dismiss();
            finish();
        });
    }

    @SuppressWarnings("deprecation")
    private int currentVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private static HttpURLConnection open(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true); // GitHub redireciona para o servidor de arquivos
        if (c.getResponseCode() != 200) throw new Exception("HTTP " + c.getResponseCode());
        return c;
    }

    private static String readText(String url) throws Exception {
        HttpURLConnection c = open(url);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
            return r.readLine();
        }
    }
}
