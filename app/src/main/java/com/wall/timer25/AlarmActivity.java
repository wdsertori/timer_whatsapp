package com.wall.timer25;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Tela do alarme: botão grande "Parar e abrir WhatsApp Business". */
public class AlarmActivity extends Activity {
    static final String EXTRA_OPEN_NOW = "open_now";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT < 27) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Veio do botão da notificação: não mostra tela, já para e abre o WhatsApp.
        if (getIntent().getBooleanExtra(EXTRA_OPEN_NOW, false)) {
            stopAndOpenWhatsApp();
            return;
        }

        int pad = dp(24);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(Color.parseColor("#B71C1C"));

        TextView title = new TextView(this);
        title.setText(Timer.minutes(this) + " minutos!\nResponda os clientes");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        Button open = new Button(this);
        open.setText("PARAR E ABRIR\n" + Timer.appLabel(this).toUpperCase());
        open.setTextSize(22);
        open.setOnClickListener(v -> stopAndOpenWhatsApp());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(160));
        lp.topMargin = dp(48);
        root.addView(open, lp);

        Button stop = new Button(this);
        stop.setText("Só parar o alarme");
        stop.setOnClickListener(v -> { Timer.stopRinging(this); finish(); });
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp2.topMargin = dp(24);
        root.addView(stop, lp2);

        setContentView(root);
    }

    private void stopAndOpenWhatsApp() {
        Timer.stopRinging(this);
        KeyguardManager km = getSystemService(KeyguardManager.class);
        if (km != null && km.isKeyguardLocked()) {
            // Pede desbloqueio (digital/PIN) e só depois abre o WhatsApp.
            km.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                @Override public void onDismissSucceeded() { launchWhatsApp(); }
                @Override public void onDismissCancelled() { finish(); }
                @Override public void onDismissError() { launchWhatsApp(); }
            });
        } else {
            launchWhatsApp();
        }
    }

    private void launchWhatsApp() {
        Intent i = getPackageManager().getLaunchIntentForPackage(Timer.appPackage(this));
        if (i == null) i = getPackageManager().getLaunchIntentForPackage(Timer.DEFAULT_APP);
        if (i == null) i = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else {
            Toast.makeText(this, "App escolhido não encontrado", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
