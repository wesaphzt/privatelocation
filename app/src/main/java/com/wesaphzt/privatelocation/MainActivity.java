package com.wesaphzt.privatelocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.wesaphzt.privatelocation.fragments.DialogFragmentAddFavorite;
import com.wesaphzt.privatelocation.fragments.DialogFragmentFavorite;
import com.wesaphzt.privatelocation.fragments.DialogFragmentGoTo;
import com.wesaphzt.privatelocation.fragments.FragmentAbout;
import com.wesaphzt.privatelocation.fragments.FragmentDonate;
import com.wesaphzt.privatelocation.fragments.FragmentSettings;
import com.wesaphzt.privatelocation.interfaces.ILatLong;
import com.wesaphzt.privatelocation.interfaces.JSInterface;
import com.wesaphzt.privatelocation.service.LocationService;
import com.wesaphzt.privatelocation.widget.LocationWidgetProvider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.view.MenuItem;
import android.view.Menu;

import android.webkit.WebViewClient;
import android.widget.Toast;

import static com.wesaphzt.privatelocation.service.LocationService.mCountDown;

public class MainActivity extends AppCompatActivity
        implements ILatLong {

    Context context;

    int firstRun = 0;

    //webview
    public WebView webView;
    private String appVersion;
    com.wesaphzt.privatelocation.interfaces.JSInterface JSInterface;
    public static final String LOAD_URL = "file:///android_asset/leaflet.html";

    LocationListener locationListener;

    //nav
    Toolbar toolbar;

    //location
    static Double lat;
    static Double lng;

    //shared prefs
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor sharedPreferencesEditor;

    public static final String USER_LAT_NAME = "USER_LAT";
    public static final String USER_LNG_NAME = "USER_LNG";
    public static final String USER_ZOOM_NAME = "USER_ZOOM";

    public static final Double DEFAULT_LAT = 40.748578;
    public static final Double DEFAULT_LNG = -73.985659;
    public static final int DEFAULT_ZOOM = 11;

    //first run
    final String PREF_VERSION_CODE_KEY = "VERSION_CODE";
    final int DOESNT_EXIST = -1;

    //permissions
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    MainActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        mActivity = this;

        //shared prefs
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //webview
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new customWebView());
        webView.setWebChromeClient(new customChromeClient());
        JSInterface = new JSInterface(this, this);


        toolbar = findViewById(R.id.toolbar);

        getFirstRun();
        setWebView();

        //nav
        setSupportActionBar(toolbar);

        //listeners
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        //toggle back arrow on back stack change
                        if(getSupportFragmentManager().getBackStackEntryCount() > 0){
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        } else {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                            //set title
                            MainActivity.this.setTitle(R.string.app_name);
                        }
                    }
                });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getSupportFragmentManager().getBackStackEntryCount() > 0){
                    getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private class customChromeClient extends WebChromeClient {
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);

            if(isLocationPermissionGranted()){
                findLocation();
            }
        }
    }

    public class customWebView extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            getSharedPreferencesLatLng();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
            return true;
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
            view.getContext().startActivity(intent);
            return true;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.addJavascriptInterface(JSInterface, "Android");
        webView.loadUrl(LOAD_URL);

        //grab package info
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //set custom user agent string as per tos
        String webViewUserAgent = webView.getSettings().getUserAgentString() + ", AppID: " + R.string.app_name + ", AppVersion: " + appVersion +
                ", GitHub: " + getString(R.string.app_github);
        webView.getSettings().setUserAgentString(webViewUserAgent);
    }

    private void findLocation() {
        sendJS("findLocation();");
    }

    //location permission
    public  boolean isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return false;
            }
        } else {
            return true; //permission automatically granted on sdk < 23 upon installation
        }
    }

    public boolean isDevMode() {
        try {
            //24 - 25 (nougat): trouble builds, for some reason these builds always return true regardless of dev options enabled or not (dialog shown on first run for these users)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Settings.Secure.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
            } else {
                //17 - 23: dev options enabled by default but still return default false value, so set default to true here
                return Settings.Secure.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1) != 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Please ensure developer settings are enabled", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void getFirstRun() {
        //get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;
        //get saved version code
        int savedVersionCode = sharedPreferences.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        //check for first run
        if (currentVersionCode == savedVersionCode) {
            //normal run
            return;
        } else if (savedVersionCode == DOESNT_EXIST) {
            firstRun = 1;

            //first run
            Intent myIntent = new Intent(this, IntroActivity.class);
            this.startActivity(myIntent);

            //dialog for nougat build
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog
                        .setTitle(getString(R.string.dialog_developer_settings_nougat_title))
                        .setMessage(getString(R.string.dialog_developer_settings_nougat_message))
                        .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //user cancel
                            }
                        })
                        .setNegativeButton("SETTINGS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS), 0);
                            }
                        });
                alertDialog.create().show();
            }
        }

        //update shared prefs with current version code
        sharedPreferences.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    //get last user location from shared prefs
    public void getSharedPreferencesLatLng(){
        try {
            double mLat = Double.parseDouble(sharedPreferences.getString(USER_LAT_NAME, "null"));
            double mLng = Double.parseDouble(sharedPreferences.getString(USER_LNG_NAME, "null"));
            int mZoom = Integer.parseInt(String.valueOf(sharedPreferences.getInt(USER_ZOOM_NAME, DEFAULT_ZOOM)));

            //set map view, marker, zoom and open popup
            sendJS("map.setView([" + mLat + ", " + mLng + "], " + mZoom + ");");
            sendJS("var marker = L.marker([" + mLat + ", " + mLng + "]).addTo(map);");
            sendJS("popupMoveEnd();");
        } catch (Exception e) {
            //if shared prefs values haven't been created yet set defaults
            e.printStackTrace();
            //set map view, marker, zoom and open popup
            sendJS("map.setView([" + DEFAULT_LAT + ", " + DEFAULT_LNG + "], " + DEFAULT_ZOOM + ");");
            sendJS("var marker = L.marker([" + DEFAULT_LAT + ", " + DEFAULT_LNG + "]).addTo(map);");
            sendJS("popupMoveEnd();");
        }
    }

    //store user location in shared prefs on leaflet move end, unreliable elsewhere
    public void setSharedPreferencesLatLng(String lat, String lng, int zoom) {
        sharedPreferencesEditor = sharedPreferences.edit();

        sharedPreferencesEditor.putString(USER_LAT_NAME, lat);
        sharedPreferencesEditor.putString(USER_LNG_NAME, lng);
        sharedPreferencesEditor.putInt(USER_ZOOM_NAME, zoom);

        sharedPreferencesEditor.apply();
    }

    @SuppressLint("MissingPermission")
    public void getLocationGps(){
        //method called on webview location error, try gps through android
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //called when a new location is found
                if(location != null) {
                    locationManager.removeUpdates(this);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {  }

            @Override
            public void onProviderEnabled(String provider) {  }

            @Override
            public void onProviderDisabled(String provider) {  }
        };

        if(isLocationPermissionGranted()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //add items to action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.action_favorites) {
            FragmentManager fm = getSupportFragmentManager();
            //pass mActivity so we can callback in FavoriteAdapter
            DialogFragmentFavorite dialogFragment = new DialogFragmentFavorite(context, mActivity);
            dialogFragment.show(fm, "FragmentFavorite");
        } else if (id == R.id.action_go_to) {
            FragmentManager fm = getSupportFragmentManager();
            DialogFragmentGoTo dialogFragment = new DialogFragmentGoTo();
            dialogFragment.show(fm, "FragmentGoTo");
        } else if (id == R.id.action_settings) {
            fragment = new FragmentSettings();
        } else if (id == R.id.action_donate) {
            fragment = new FragmentDonate();
        } else if (id == R.id.action_show_intro) {
            Intent myIntent = new Intent(this, IntroActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.action_about) {
            fragment = new FragmentAbout();
        }

        //add fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            ft.add(R.id.content_main, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setLocation(Double lat, Double lng) {
        sendJS("setOnMap(" + lat + ", " + lng + ");");
    }

    public void sendJS(String command) {
        webView.loadUrl("javascript:" + command);
    }

    public void setMockLatLong(String mLat, String mLng) {
        lat = Double.parseDouble(mLat);
        lng = Double.parseDouble(mLng);

        applyLocation();
    }

    public void applyLocation() {
        //check mock location app returns true
        if(checkPermissions()) {

            if(firstRun == 1) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog
                        .setTitle(getString(R.string.dialog_first_run_title))
                        .setMessage(getString(R.string.dialog_first_run_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startService();
                                    LocationWidgetProvider locationWidgetProvider = new LocationWidgetProvider();
                                    locationWidgetProvider.setWidgetStart(context);

                                    //cancel any pause timer that might be running
                                    try {
                                        mCountDown.cancel();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } catch (Exception e) {
                                    startService();
                                    LocationWidgetProvider locationWidgetProvider = new LocationWidgetProvider();
                                    locationWidgetProvider.setWidgetStart(context);
                                }
                                startService();
                            }
                        });
                alertDialog.create().show();

                firstRun = 0;

            } else {
                try {
                    startService();
                    LocationWidgetProvider locationWidgetProvider = new LocationWidgetProvider();
                    locationWidgetProvider.setWidgetStart(context);

                    //cancel any pause timer that might be running
                    try {
                        mCountDown.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    startService();
                    LocationWidgetProvider locationWidgetProvider = new LocationWidgetProvider();
                    locationWidgetProvider.setWidgetStart(context);
                }
                startService();
            }
        }
    }

    public void startService() {
        try {
            Intent intent = new Intent(context, LocationService.class);
            //add data to the intent
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);

            //check android api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                context.startService(intent);
            }

            minimizeApp();

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public boolean checkPermissions() {
        //check developer options & mock location app set
        if(isDevMode()) {
            //dev mode enabled
            if(isMockLocationEnabled()) {
                //mock location app set
                return true;
            } else {
                //mock location app not set

                //tailor message based on build
                String buildMsg;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    buildMsg = getString(R.string.dialog_mock_location_message_m_over);
                } else {
                    buildMsg = getString(R.string.dialog_mock_location_message_under_m);
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog
                        .setTitle(getString(R.string.dialog_mock_location_title))
                        .setMessage(buildMsg)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //user cancel
                            }
                        });
                alertDialog.create().show();
            }
        } else {
            //dev mode disabled
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog
                    .setTitle(getString(R.string.dialog_developer_settings_title))
                    .setMessage(getString(R.string.dialog_developer_settings_message))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS), 0);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //user cancel
                        }
                    });
            alertDialog.create().show();
        }
        return false;
    }

    public boolean isMockLocationEnabled(){
        boolean isMockLocation = false;
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID)== AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(context.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }
        return isMockLocation;
    }

    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void addFavoriteLocation(String mLat, String mLng) {
        lat = Double.parseDouble(mLat);
        lng = Double.parseDouble(mLng);

        FragmentManager fm = getSupportFragmentManager();
        DialogFragmentAddFavorite dialogFragment = new DialogFragmentAddFavorite();

        Bundle args = new Bundle();
        args.putDouble("lat", lat);
        args.putDouble("lng", lng);
        dialogFragment.setArguments(args);

        dialogFragment.show(fm, "FragmentAddFavorite");
    }
}
