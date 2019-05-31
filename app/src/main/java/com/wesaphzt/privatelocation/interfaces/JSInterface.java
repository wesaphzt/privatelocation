package com.wesaphzt.privatelocation.interfaces;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.wesaphzt.privatelocation.MainActivity;

public class JSInterface {

    private Context context;

    private MainActivity mainActivity;
    public JSInterface(Context context, MainActivity mActivity) {
        mainActivity = mActivity;
    }

    public Context getContext() {
        return context;
    }

    //store user location in shared prefs on leaflet move end, unreliable elsewhere
    @JavascriptInterface
    public void setLatLongZoom(final String latlng, int zoom) {
        //separate lat & long
        String lat = latlng.substring(latlng.indexOf('(') + 1, latlng.indexOf(','));
        String lng = latlng.substring(latlng.indexOf(',') + 2, latlng.indexOf(')'));

        mainActivity.setSharedPreferencesLatLng(lat, lng, zoom);
    }

    @JavascriptInterface
    public void setMockLocation(final String location) {
        //separate lat & long
        String lat = location.substring(location.indexOf('(') + 1, location.indexOf(','));
        String lng = location.substring(location.indexOf(',') + 2, location.indexOf(')'));

        //call method to add to shared prefs
        mainActivity.setMockLatLong(lat, lng);
    }

    @JavascriptInterface
    public void addFavoriteLocation(final String favorite) {
        //separate lat & long
        String lat = favorite.substring(favorite.indexOf('(') + 1, favorite.indexOf(','));
        String lng = favorite.substring(favorite.indexOf(',') + 2, favorite.indexOf(')'));

        mainActivity.addFavoriteLocation(lat, lng);
    }

    @JavascriptInterface
    public void onLocationError() {
        mainActivity.getLocationGps();
    }
}
