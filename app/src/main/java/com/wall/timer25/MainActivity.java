package com.wall.timer25;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

/** Sem tela: toque no ícone => zera e inicia 25 min, mostra um aviso e fecha. */
public class MainActivity extends Activity {
    private static final int REQ = 1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Só na primeira vez (Android 13+): pede permissão para o alarme aparecer/tocar.
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                   != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ);
            return;
        }
        startAndClose();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        startAndClose();
    }

    private void startAndClose() {
        Timer.restart(this);
        Toast.makeText(getApplicationContext(), "Timer " + Timer.minutes(this) + " min iniciado", Toast.LENGTH_SHORT).show();
        // Android 14+: sem esta permissão o alarme não abre a tela cheia (só notificação).
        if (Build.VERSION.SDK_INT >= 34
                && !getSystemService(NotificationManager.class).canUseFullScreenIntent()) {
            Toast.makeText(getApplicationContext(),
                    "Ative 'tela cheia' para o alarme aparecer no bloqueio", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:" + getPackageName())));
        }
        finish();
        overridePendingTransition(0, 0);
    }
}
