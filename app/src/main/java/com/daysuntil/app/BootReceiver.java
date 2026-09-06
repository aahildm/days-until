package com.daysuntil.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WidgetProvider.scheduleMidnightUpdate(context);
        WidgetProvider.refreshAll(context);
        TickService.start(context);
    }
}
