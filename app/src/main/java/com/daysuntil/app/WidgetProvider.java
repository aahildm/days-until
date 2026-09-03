package com.daysuntil.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
        scheduleMidnightUpdate(context);
    }

    @Override
    public void onEnabled(Context context) {
        scheduleMidnightUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = getAlarmPendingIntent(context);
        am.cancel(pi);
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("days_until_prefs", Context.MODE_PRIVATE);
        String eventName = prefs.getString("event_name", "REMAINING").toUpperCase();
        long targetMillis = prefs.getLong("target_date", System.currentTimeMillis());

        TimeZone tz = TimeZone.getDefault();

        Calendar today = Calendar.getInstance(tz);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar target = Calendar.getInstance(tz);
        target.setTimeInMillis(targetMillis);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long days = TimeUnit.MILLISECONDS.toDays(
                target.getTimeInMillis() - today.getTimeInMillis());
        String daysText = String.valueOf(Math.max(days, 0));

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String dateStr = sdf.format(target.getTime());

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widgetLabel, eventName);
        views.setTextViewText(R.id.widgetDays, daysText);
        views.setTextViewText(R.id.widgetDate, dateStr);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void scheduleMidnightUpdate(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Calendar midnight = Calendar.getInstance();
        midnight.add(Calendar.DAY_OF_YEAR, 1);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 5);
        midnight.set(Calendar.MILLISECOND, 0);

        PendingIntent pi = getAlarmPendingIntent(context);

        try {
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    midnight.getTimeInMillis(),
                    pi);
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    midnight.getTimeInMillis(),
                    pi);
        }
    }

    private static PendingIntent getAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, MidnightAlarmReceiver.class);
        return PendingIntent.getBroadcast(
                context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
