package com.wesaphzt.privatelocation.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;

import com.wesaphzt.privatelocation.service.LocationProvider;

public class ActionReceiver extends BroadcastReceiver {

    LocationProvider mockNetwork;
    LocationProvider mockGPS;

    private static final int NOTIFICATION = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("location_service");

        if(action.equals("service_notification")){
            mockNetwork = new LocationProvider(LocationManager.NETWORK_PROVIDER, context);
            mockGPS = new LocationProvider(LocationManager.GPS_PROVIDER, context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String id = "location_notification_channel_id";
                notificationManager.deleteNotificationChannel(id);
            } else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIFICATION);
            }

            try {
                if (mockNetwork != null)
                    mockNetwork.shutdown();
                if (mockGPS != null)
                    mockGPS.shutdown();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}