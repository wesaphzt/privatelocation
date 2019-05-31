package com.wesaphzt.privatelocation.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;

public class LocationProvider {

    private String providerName;
    public Context context;

    LocationProvider(String name, Context context) {
        this.providerName = name;
        this.context = context;

        LocationManager locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        try
        {
            locationManager.addTestProvider(providerName, false, false, false, false, false,
                    true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
            locationManager.setTestProviderEnabled(providerName, true);
        } catch (SecurityException e) {
            throw new SecurityException("Error applying mock location");
        }
    }

    void pushLocation(double lat, double lon) {
        LocationManager locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);

        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(3F);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setSpeed(0.01F);
        mockLocation.setBearing(1F);
        mockLocation.setAccuracy(3F);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mockLocation.setBearingAccuracyDegrees(0.1F);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mockLocation.setVerticalAccuracyMeters(0.1F);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mockLocation.setSpeedAccuracyMetersPerSecond(0.01F);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        locationManager.setTestProviderLocation(providerName, mockLocation);
    }

    void shutdown() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeTestProvider(providerName);
    }
}
