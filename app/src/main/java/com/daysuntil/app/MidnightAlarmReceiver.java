package com.daysuntil.app;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MidnightAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, WidgetProvider.class);
        int[] ids = appWidgetManager.getAppWidgetIds(component);

        for (int id : ids) {
            WidgetProvider.updateWidget(context, appWidgetManager, id);
        }

        WidgetProvider.scheduleMidnightUpdate(context);
    }
}
