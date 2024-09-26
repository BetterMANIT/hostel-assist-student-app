package com.manit.hostel.assist.students;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.onesignal.OneSignal;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "http://4.186.57.254/for-app";
    private String HOSTEL_WIFI_MAC_ADDRESS = "80:c5:f2:7b:ff:29";
    private boolean allowInVicinity = true;
    private boolean isPresentAtGate = false;
    private WebView webview;
    private LinearLayout splash;
    boolean isComingBack = false;
    final Handler wifiScannerHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = findViewById(R.id.webview);
        splash = findViewById(R.id.splash);
        requestPermissions();
        Log.d(MainActivity.class.getSimpleName(), "Wifi Name list : " + new WifiScanner(this).getWifiList(this).toString());
        if (isLocationEnabled()) {
            // Start Wi-Fi scanning
            showWifiCheckDialog();
        } else {
            promptEnableLocation();
        }
        setupOneSignal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isComingBack) {
            isComingBack = false;
            if (isLocationEnabled()) {
                showWifiCheckDialog();
            } else {
                promptEnableLocation();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wifiScannerHandler.removeCallbacksAndMessages(null);
        isComingBack = true;
        isPresentAtGate = false;
    }

    private void showWifiCheckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_check_wifi);
        AlertDialog dialog = builder.create();
        dialog.setTitle("Checking WIFI of MANIT");
        dialog.setCancelable(false);
        dialog.show();

        // Schedule a task to scan Wi-Fi every 5 seconds

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dialog.findViewById(R.id.wifi_progress).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.success_icon).setVisibility(View.GONE);
                ((TextView)dialog.findViewById(R.id.wifi_status)).setText("Scanning Hostel Wifi");
                checkForWifi();  // Function to scan Wi-Fi
                if(!isPresentAtGate){
                    wifiScannerHandler.postDelayed(this, 3000); // Repeat every 5 seconds
                }else{
                    ((TextView)dialog.findViewById(R.id.wifi_status)).setText("Found Hostel Wifi");
                    dialog.findViewById(R.id.success_icon).setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.success_icon).animate().alpha(1).setDuration(300).start();

                    dialog.findViewById(R.id.wifi_progress).setVisibility(View.GONE);
                    dialog.findViewById(R.id.wifi_progress).postDelayed(()->{
                        loadWebview();
                        dialog.dismiss();
                    }, 1000);
                }
            }
        };
        wifiScannerHandler.post(runnable);
    }


    private void setupOneSignal() {
        String externalId = "241100112233"; // You will supply the external id to the OneSignal SDK
        OneSignal.login(externalId);
    }

    private void loadWebview() {
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Enable cache
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // Use cache when offline

        // Enable cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webview, true);
        webview.setWebViewClient(new ManitWebClient());

        // Load the URL
        webview.loadUrl(URL);
    }

    private void requestPermissions() {
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET).request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                        if (allGranted) {
                            Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(MainActivity.this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void checkForWifi() {
        if (allowInVicinity) {
            WifiScanner wifiScanner = new WifiScanner(getApplicationContext());
            List<ScanResult> scanResults = wifiScanner.getWifiList(this);
            for (ScanResult result : scanResults) {
                String ssid = result.SSID;  // Network name
                String bssid = result.BSSID; // MAC address of the network
                Log.d("WiFi", "SSID: " + ssid + ", BSSID (MAC): " + bssid);
                if (result.BSSID.equals(HOSTEL_WIFI_MAC_ADDRESS)) {
                    isPresentAtGate = true;
                }
            }
        } else {

        }
    }

    private class ManitWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            splash.animate().alpha(0).setDuration(300).start();
            splash.postDelayed(() -> splash.setVisibility(View.GONE), 300);
            super.onPageFinished(view, url);
        }
    }


    private void promptEnableLocation() {
        if (!isLocationEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Location")
                    .setMessage("Location is required for scanning Wi-Fi networks. Please enable it in settings.")
                    .setPositiveButton("Enable", (dialogInterface, i) -> {
                        // Direct user to location settings
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        isComingBack = true;
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}