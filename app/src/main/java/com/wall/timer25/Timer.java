package com.wall.timer25;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

/** Toda a lógica do timer num lugar só. */
final class Timer {
    static final int DEFAULT_MINUTES = 25;
    static final String DEFAULT_APP = "com.whatsapp.w4b"; // WhatsApp Business

    static final String ACTION_FIRE = "com.wall.timer25.FIRE";
    static final String ACTION_STOP = "com.wall.timer25.STOP";

    private static final String CHANNEL = "alarm_v1";
    private static final int NOTIF_ID = 25;
    private static final String PREFS = "timer";
    private static final String KEY_END = "end";
    private static final String KEY_MIN = "minutes";
    private static final String KEY_APP = "app";

    // ---------- Configurações ----------
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences(PREFS, 0); }
    static int minutes(Context c) { return prefs(c).getInt(KEY_MIN, DEFAULT_MINUTES); }
    static String appPackage(Context c) { return prefs(c).getString(KEY_APP, DEFAULT_APP); }

    static void saveSettings(Context c, int minutes, String pkg) {
        prefs(c).edit().putInt(KEY_MIN, minutes).putString(KEY_APP, pkg).apply();
    }

    static String appLabel(Context c) {
        PackageManager pm = c.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(appPackage(c), 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "app";
        }
    }

    // ---------- Timer ----------
    /** Zera qualquer timer/alarme em andamento e começa a contar de novo. */
    static void restart(Context ctx) {
        stopRinging(ctx);
        long end = System.currentTimeMillis() + minutes(ctx) * 60_000L;
        schedule(ctx, end);
        prefs(ctx).edit().putLong(KEY_END, end).apply();
    }

    /** Reagenda após reboot, se o timer ainda não tinha terminado. */
    static void restoreAfterBoot(Context ctx) {
        long end = prefs(ctx).getLong(KEY_END, 0);
        if (end > System.currentTimeMillis()) schedule(ctx, end);
    }

    private static void schedule(Context ctx, long end) {
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        // Mesmo PendingIntent => o agendamento anterior é substituído (é isso que "zera").
        // setAlarmClock = prioridade de despertador: dispara mesmo em Doze/economia de bateria.
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(end, null), broadcastPi(ctx, ACTION_FIRE, 0));
    }

    static void ring(Context ctx) {
        prefs(ctx).edit().remove(KEY_END).apply();
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        ensureChannel(nm);
        String app = appLabel(ctx);

        Notification n = new Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(minutes(ctx) + " minutos! Responda os clientes")
                .setContentText("Toque para parar e abrir o " + app)
                .setCategory(Notification.CATEGORY_ALARM)
                .setOngoing(true)
                // Tela cheia por cima do bloqueio; com o celular em uso vira notificação flutuante
                .setFullScreenIntent(alarmScreenPi(ctx, false, 10), true)
                .setContentIntent(alarmScreenPi(ctx, true, 11))
                .addAction(new Notification.Action.Builder(null, "Parar e abrir " + app,
                        alarmScreenPi(ctx, true, 12)).build())
                .addAction(new Notification.Action.Builder(null, "Só parar",
                        broadcastPi(ctx, ACTION_STOP, 3)).build())
                .build();
        n.flags |= Notification.FLAG_INSISTENT; // repete o som até parar
        nm.notify(NOTIF_ID, n);
    }

    static void stopRinging(Context ctx) {
        ctx.getSystemService(NotificationManager.class).cancel(NOTIF_ID);
    }

    private static void ensureChannel(NotificationManager nm) {
        if (nm.getNotificationChannel(CHANNEL) != null) return;
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (sound == null) sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationChannel ch = new NotificationChannel(CHANNEL, "Alarme do timer",
                NotificationManager.IMPORTANCE_HIGH);
        ch.setSound(sound, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)   // usa o volume de alarme
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        ch.enableVibration(true);
        ch.setVibrationPattern(new long[]{0, 800, 400, 800});
        ch.setBypassDnd(true);
        nm.createNotificationChannel(ch);
    }

    private static PendingIntent alarmScreenPi(Context ctx, boolean openNow, int code) {
        Intent i = new Intent(ctx, AlarmActivity.class)
                .putExtra(AlarmActivity.EXTRA_OPEN_NOW, openNow)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        return PendingIntent.getActivity(ctx, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent broadcastPi(Context ctx, String action, int code) {
        Intent i = new Intent(ctx, AlarmReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(ctx, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private Timer() {}
}
