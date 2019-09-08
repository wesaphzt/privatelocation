package com.wesaphzt.privatelocation.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;

import com.wesaphzt.privatelocation.R;
import com.wesaphzt.privatelocation.service.LocationProvider;
import com.wesaphzt.privatelocation.service.LocationService;
import com.wesaphzt.privatelocation.widget.LocationWidgetProvider;

import static com.wesaphzt.privatelocation.service.LocationService.isRunning;
import static com.wesaphzt.privatelocation.service.LocationService.mCountDown;

public class ActionReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("location_service");

        if(action.equals("service_notification")){
            Intent stopIntent  = new Intent(context, LocationService.class);
            stopIntent.setAction(LocationService.ACTION_STOP_FOREGROUND_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(stopIntent);
            } else {
                context.startService(stopIntent);
            }
        }
    }
}