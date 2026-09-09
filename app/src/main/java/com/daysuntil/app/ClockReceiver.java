package com.daysuntil.app;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ClockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Update widget clock on every minute tick
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName comp = new ComponentName(context, WidgetProvider.class);
        for (int id : mgr.getAppWidgetIds(comp)) {
            WidgetProvider.updateWidget(context, mgr, id);
        }
    }
}