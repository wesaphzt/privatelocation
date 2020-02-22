package com.wesaphzt.privatelocation.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;

import com.wesaphzt.privatelocation.MainActivity;
import com.wesaphzt.privatelocation.R;
import com.wesaphzt.privatelocation.receivers.ActionReceiver;
import com.wesaphzt.privatelocation.widget.LocationWidgetProvider;

import java.util.Random;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class LocationService extends Service {

    LocationProvider mockNetwork;
    LocationProvider mockGps;
    double DEF_LOCATION_LNG = MainActivity.DEFAULT_LNG;
    double DEF_LOCATION_LAT = MainActivity.DEFAULT_LAT;
    //notifications
    public static PendingIntent pendingIntent;
    public static PendingIntent pendingCloseIntent;

    public static final int NOTIFICATION_ID = 100;

    Notification notification;
    NotificationManager notificationManager;

    public static final String CHANNEL_ID = "location_notification_channel_id";
    public static final String CHANNEL_NAME = "Location Notification Service";

    Context context;
    SharedPreferences sharedPreferences;

    public static boolean disabled = true;

    //randomize
    public static CountDownTimer mCountDown;
    public static boolean isRunning = false;
    private static int RANDOMIZE_LOCATION_INTERVAL;//间隔时间
    private static int RANDOMIZE_LOCATION_ROUTE_RADIUS;//偏差距离
    private static int RANDOMIZE_LOCATION_RANGE_RADIUS;//范围半径

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (intent != null) {
            String action = intent.getAction();

            LocationWidgetProvider locationWidgetProvider = new LocationWidgetProvider();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    setNotification();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //create foreground service
                        startForeground(NOTIFICATION_ID, notification);
                        pushLocation(intent);
                        disabled = false;
                        locationWidgetProvider.setWidgetStart(context);
                    } else {
                        notificationManager.notify(NOTIFICATION_ID, notification);
                        pushLocation(intent);
                        disabled = false;
                        locationWidgetProvider.setWidgetStart(context);
                    }

                    break;

                case ACTION_STOP_FOREGROUND_SERVICE:
                    shutdown();
                    stopService(intent);
                    disabled = true;
                    locationWidgetProvider.setWidgetStop(context);

                    break;
            }
        } else {
            return LocationService.START_REDELIVER_INTENT;
        }

        return LocationService.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setNotification() {
        //open main activity when clicked
        pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);

        //action when notification button clicked
        Intent intentAction = new Intent(context, ActionReceiver.class);
        intentAction.putExtra("location_service", "service_notification");
        pendingCloseIntent = PendingIntent.getBroadcast(context, 0, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notification = notificationBuilder
                    .setSmallIcon(R.drawable.ic_notification_pin_drop_white_24dp)
                    .setContentTitle(getString(R.string.app_name) + " is running")
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(getString(R.string.app_name) + " is running")
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", pendingCloseIntent)
                    .setOngoing(true)
                    .setPriority(PRIORITY_LOW)
                    .build();

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N | Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            notification = notificationBuilder
                    .setSmallIcon(R.drawable.ic_notification_pin_drop_white_24dp)
                    .setContentTitle(getString(R.string.app_name) + " is running")
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setColor(getColor(R.color.colorPrimary))
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(getString(R.string.app_name) + " is running")
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", pendingCloseIntent)
                    .setOngoing(true)
                    .setPriority(PRIORITY_LOW)
                    .build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            notification = notificationBuilder
                    .setSmallIcon(R.drawable.ic_notification_pin_drop_white_24dp)
                    .setContentTitle(getString(R.string.app_name) + " is running")
                    .setPriority(PRIORITY_MIN)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setColor(getColor(R.color.colorPrimary))
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(getString(R.string.app_name) + " is running")
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", pendingCloseIntent)
                    .setOngoing(true)
                    .build();
        }
    }

    double lat = DEF_LOCATION_LAT;
    double lng = DEF_LOCATION_LNG;

    private void pushLocation(Intent intent) {
        try {
            if (intent.hasExtra("lat") && intent.hasExtra("lng")) {
                lat = intent.getDoubleExtra("lat", 37.0951691945 );
                lng = intent.getDoubleExtra("lng",  79.9454498291);

                mockNetwork = new LocationProvider(LocationManager.NETWORK_PROVIDER, context);
                mockGps = new LocationProvider(LocationManager.GPS_PROVIDER, context);

                mockNetwork.pushLocation(lat, lng);
                mockGps.pushLocation(lat, lng);

                if (sharedPreferences.getBoolean("RANDOMIZE_LOCATION", false)) {
                    randomize();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String id = "location_notification_channel_id";
            notificationManager.deleteNotificationChannel(id);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
        }

        try {
            disabled = true;
            isRunning = false;

            if (mockNetwork != null)
                mockNetwork.shutdown();
            if (mockGps != null)
                mockGps.shutdown();
            if (isRunning)
                mCountDown.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------
    private void randomize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            RANDOMIZE_LOCATION_INTERVAL = Integer.parseInt(prefs.getString("RANDOMIZE_LOCATION_INTERVAL", "60"));

            float randomLat = randomLat();
            float randomLng = randomLng();
            Log.d("Location", "Lat: " + randomLat + "  Lng:  " + randomLng);
            if (sharedPreferences.getBoolean("RANDOMIZE_RANGE", false)) {//随机路线
                float[] value = randomRange(prefs);
                randomLat = value[0];
                randomLng = value[1];
                Log.e("Location randomRout", "Lat: " + randomLat + "  Lng:  " + randomLng);
            }

            mockNetwork = new LocationProvider(LocationManager.NETWORK_PROVIDER, context);
            mockGps = new LocationProvider(LocationManager.GPS_PROVIDER, context);

            mockNetwork.pushLocation(randomLat, randomLng);
            mockGps.pushLocation(randomLat, randomLng);

            randomizeTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void randomizeTimer() {
        mCountDown = new CountDownTimer(RANDOMIZE_LOCATION_INTERVAL * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                isRunning = true;
            }

            public void onFinish() {
                isRunning = false;
                randomize();
            }
        }.start();
    }

    private float randomLat() {
        Random r = new Random();
        //return r.nextFloat() * (180) - 90;
        return (float) (lat+r.nextFloat());
    }

    private float randomLng() {
        Random r = new Random();
        //return r.nextFloat() * (360) - 180;
        return (float) (lng+r.nextFloat());
    }

    private float[] randomRange(SharedPreferences prefs) {
        //int[] angles = new int[]{30, 60, 45};
        Random random = new Random();
        float radius =Float.valueOf(prefs.getString("RANDOMIZE_RANGE_RADIUS","0.1f"));
        float distance = random.nextFloat() * radius;
        //double randomValue = sin(angles[random.nextInt(angles.length)]) * distance;
        float nextLng = (float) (lng + distance);
        float nextLat = (float) (lat + distance);
        return new float[]{nextLat,nextLng};
    }

    double sin(int num) {
        return Math.sin(num * Math.PI / 180);
    }

    double cos(int num) {
        return Math.cos(num * Math.PI / 180);
    }
}