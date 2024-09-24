package com.manit.hostel.assist.students;

import android.Manifest;
import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "http://4.186.57.254/index.php";
    private String HOSTEL_WIFI_MAC_ADDRESS = "00:00:00:00:00:00";
    private boolean allowInVicinity = true;
    private boolean isPresentAtGate = false;
    private WebView webview;
    private LinearLayout splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = findViewById(R.id.webview);
        splash = findViewById(R.id.splash);
        requestPermissions();
        Log.d(MainActivity.class.getSimpleName(), "Wifi Name list : " + new WifiScanner(this).getWifiList(this).toString());
        checkForWifi();
        loadWebview();
        setupOneSignal();
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
                if (result.BSSID == HOSTEL_WIFI_MAC_ADDRESS) {
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
}