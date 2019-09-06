package com.wesaphzt.privatelocation.widget;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.wesaphzt.privatelocation.R;
import com.wesaphzt.privatelocation.service.LocationProvider;
import com.wesaphzt.privatelocation.service.LocationService;

import static com.wesaphzt.privatelocation.MainActivity.DEFAULT_LAT;
import static com.wesaphzt.privatelocation.MainActivity.DEFAULT_LNG;
import static com.wesaphzt.privatelocation.MainActivity.USER_LAT_NAME;
import static com.wesaphzt.privatelocation.MainActivity.USER_LNG_NAME;
import static com.wesaphzt.privatelocation.service.LocationService.CHANNEL_ID;
import static com.wesaphzt.privatelocation.service.LocationService.disabled;
import static com.wesaphzt.privatelocation.service.LocationService.isRunning;
import static com.wesaphzt.privatelocation.service.LocationService.mCountDown;

public class LocationWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
    public boolean SERVICE_STATUS;

    LocationProvider mockNetwork;
    LocationProvider mockGps;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.app_widget);

            //default status
            remoteViews.setTextViewText(R.id.tvWidgetToggle, context.getResources().getString(R.string.widget_start_text));

            Intent intent = new Intent(context, LocationWidgetProvider.class);
            intent.setAction(ACTION_WIDGET_RECEIVER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            remoteViews.setOnClickPendingIntent(R.id.llWidget, pendingIntent);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean value = prefs.getBoolean(context.getString(R.string.widget_prefs_service_id), false);

            if (value) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(context.getString(R.string.widget_prefs_service_id), false);
                editor.apply();
            }

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean value = prefs.getBoolean(context.getString(R.string.widget_prefs_service_id), false);
            SharedPreferences.Editor editor = prefs.edit();

            SERVICE_STATUS = value;

            //if service is running
            if (SERVICE_STATUS) {
                editor.putBoolean(context.getString(R.string.widget_prefs_service_id), false);
                editor.apply();

                mockNetwork = new LocationProvider(LocationManager.NETWORK_PROVIDER, context);
                mockGps = new LocationProvider(LocationManager.GPS_PROVIDER, context);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationManager.deleteNotificationChannel(CHANNEL_ID);
                    //if countdown timer is running (pause), cancel
                    if(isRunning) {
                        mCountDown.cancel(); isRunning = false;
                        disabled = true;
                        cancelLocation();
                        setWidgetStop(context);
                    } else {
                        disabled = true;
                        cancelLocation();
                        setWidgetStop(context);
                    }
                } else {
                    notificationManager.cancel(LocationService.NOTIFICATION_ID);
                    if(isRunning) {
                        mCountDown.cancel(); isRunning = false;
                        disabled = true;
                        cancelLocation();
                        setWidgetStop(context);
                    } else {
                        disabled = true;
                        cancelLocation();
                        setWidgetStop(context);
                    }
                }

                //if service is not running
            } else {
                editor.putBoolean(context.getString(R.string.widget_prefs_service_id), true);
                editor.commit();

                double mLat = 0;
                double mLng = 0;

                //grab last lat/lng or use defaults if app hasn't run yet
                try {
                    mLat = Double.parseDouble(prefs.getString(USER_LAT_NAME, "null"));
                    mLng = Double.parseDouble(prefs.getString(USER_LNG_NAME, "null"));
                } catch (Exception e) {
                    e.printStackTrace();

                    mLat = DEFAULT_LAT;
                    mLng = DEFAULT_LNG;
                }

                Intent i = new Intent(context, LocationService.class);
                i.putExtra("lat", mLat);
                i.putExtra("lng", mLng);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    disabled = false;
                    context.startForegroundService(i);
                    setWidgetStart(context);
                } else {
                    disabled = false;
                    context.startService(i);
                    setWidgetStart(context);
                }
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean value = prefs.getBoolean(context.getString(R.string.widget_prefs_service_id), false);

        if (value) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(context.getString(R.string.widget_prefs_service_id), false);
            editor.apply();
        }

        super.onDeleted(context, appWidgetIds);
    }

    //update widget methods
    public void setWidgetStart(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(context.getString(R.string.widget_prefs_service_id), true).apply();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, LocationWidgetProvider.class);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        remoteViews.setTextViewText(R.id.tvWidgetToggle, context.getResources().getString(R.string.widget_stop_text));
        remoteViews.setImageViewResource(R.id.ivWidgetLocation, R.drawable.ic_widget_location_on_white_24dp);
        remoteViews.setInt(R.id.llWidget, "setBackgroundResource", R.color.colorWidgetStart);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    public void setWidgetStop(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(context.getString(R.string.widget_prefs_service_id), false).apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, LocationWidgetProvider.class);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        remoteViews.setTextViewText(R.id.tvWidgetToggle, context.getResources().getString(R.string.widget_start_text));
        remoteViews.setImageViewResource(R.id.ivWidgetLocation, R.drawable.ic_widget_location_off_white_24dp);
        remoteViews.setInt(R.id.llWidget, "setBackgroundResource", R.color.colorWidgetStop);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    public void cancelLocation() {
        try {
            mockNetwork.shutdown();
            mockGps.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}