package com.wall.timer25;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

/** Widget 1x1: toque = zera e inicia. Cinza parado, verde contando (com contagem), vermelho tocando. */
public class TimerWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        refresh(ctx);
    }

    /** Redesenha todos os widgets conforme o estado atual do timer. */
    static void refresh(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, TimerWidget.class));
        if (ids == null || ids.length == 0) return;

        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_timer);
        long remaining = Timer.endTime(ctx) - System.currentTimeMillis();
        int color;

        if (Timer.isRinging(ctx)) {
            color = ctx.getColor(R.color.state_ringing);
            showLabel(v, "Responder!");
        } else if (remaining > 0) {
            color = ctx.getColor(R.color.state_running);
            // O próprio Android anima a contagem regressiva; não precisa atualizar a cada segundo.
            v.setChronometer(R.id.chrono, SystemClock.elapsedRealtime() + remaining, null, true);
            v.setChronometerCountDown(R.id.chrono, true);
            v.setViewVisibility(R.id.chrono, View.VISIBLE);
            v.setViewVisibility(R.id.label, View.GONE);
        } else {
            color = ctx.getColor(R.color.state_idle);
            showLabel(v, Timer.minutes(ctx) + " min");
        }
        v.setInt(R.id.bg, "setColorFilter", color);

        Intent tap = new Intent(ctx, AlarmReceiver.class).setAction(Timer.ACTION_TAP);
        v.setOnClickPendingIntent(R.id.root, PendingIntent.getBroadcast(ctx, 50, tap,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        mgr.updateAppWidget(ids, v);
    }

    private static void showLabel(RemoteViews v, String text) {
        v.setChronometer(R.id.chrono, SystemClock.elapsedRealtime(), null, false);
        v.setViewVisibility(R.id.chrono, View.GONE);
        v.setTextViewText(R.id.label, text);
        v.setViewVisibility(R.id.label, View.VISIBLE);
    }
}
