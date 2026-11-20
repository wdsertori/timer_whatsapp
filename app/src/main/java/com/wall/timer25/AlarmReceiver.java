package com.wall.timer25;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String a = intent.getAction();
        if (Timer.ACTION_FIRE.equals(a)) Timer.ring(ctx);
        else if (Timer.ACTION_STOP.equals(a)) Timer.stopRinging(ctx);
        else if (Timer.ACTION_TAP.equals(a)) {           // toque no widget
            Timer.restart(ctx);
            Toast.makeText(ctx, "Timer " + Timer.minutes(ctx) + " min iniciado", Toast.LENGTH_SHORT).show();
        }
    }
}
