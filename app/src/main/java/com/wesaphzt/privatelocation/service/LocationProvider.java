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

    public LocationProvider(String name, Context context) {
        this.providerName = name;
        this.context = context;

        LocationManager locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        try
        {

            try {
                // addTestProvider fails with an IllegalArgumentException if the existing provider with that name is a test provider
                // within an app it is not possible to find out if a provider is a test provider
                // so we remove any existing provider first and then add it again
                locationManager.removeTestProvider(providerName);
            } catch(IllegalArgumentException e) {
                // removeTestProvider is broken too. It will create an IllegalArgumentException if there is no existing provider with that name
                // or the existing provider is not a test provider
                // so we just ignore that exception
                // see frameworks/base/services/core/java/com/android/server/LocationManagerService.java in the AOSP source for the problematic functions
            }

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

    public void shutdown() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeTestProvider(providerName);
    }
}