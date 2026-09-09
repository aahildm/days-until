package com.daysuntil.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.AlarmClock;
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
        TickService.start(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            refreshAll(context);
        }
    }

    static void refreshAll(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, WidgetProvider.class);
        int[] ids = appWidgetManager.getAppWidgetIds(component);
        for (int id : ids) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    @Override
    public void onEnabled(Context context) {
        scheduleMidnightUpdate(context);
        TickService.start(context);
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(getAlarmPendingIntent(context));
        }
        context.stopService(new Intent(context, TickService.class));
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("days_until_prefs", Context.MODE_PRIVATE);
        String eventName = prefs.getString("widget_" + appWidgetId + "_name", "REMAINING").toUpperCase();
        long targetMillis = prefs.getLong("widget_" + appWidgetId + "_date", System.currentTimeMillis());

        TimeZone tz = TimeZone.getDefault();
        Calendar now = Calendar.getInstance(tz);

        Calendar today = (Calendar) now.clone();
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

        SimpleDateFormat targetDateFmt = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        String targetDateStr = targetDateFmt.format(target.getTime());

        SimpleDateFormat clockFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String clockStr = clockFmt.format(now.getTime());

        SimpleDateFormat todayFmt = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
        String todayStr = todayFmt.format(now.getTime());

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widgetClock, clockStr);
        views.setTextViewText(R.id.widgetTodayDate, todayStr);
        views.setTextViewText(R.id.widgetLabel, eventName);
        views.setTextViewText(R.id.widgetDays, daysText);
        views.setTextViewText(R.id.widgetDate, targetDateStr);

        PendingIntent clockPendingIntent = buildClockPendingIntent(context);
        if (clockPendingIntent != null) {
            views.setOnClickPendingIntent(R.id.widgetClockSection, clockPendingIntent);
        }

        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(
                context, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetEventSection, appPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static PendingIntent buildClockPendingIntent(Context context) {
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PackageManager pm = context.getPackageManager();
        if (alarmIntent.resolveActivity(pm) != null) {
            return PendingIntent.getActivity(
                    context, 1, alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        // Fallback: try opening the default Clock app directly
        Intent clockAppIntent = pm.getLaunchIntentForPackage("com.android.deskclock");
        if (clockAppIntent != null) {
            return PendingIntent.getActivity(
                    context, 2, clockAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        return null;
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
