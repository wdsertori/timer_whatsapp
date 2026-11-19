package com.wall.timer25;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String a = intent.getAction();
        if (Timer.ACTION_FIRE.equals(a)) Timer.ring(ctx);
        else if (Timer.ACTION_STOP.equals(a)) Timer.stopRinging(ctx);
    }
}
