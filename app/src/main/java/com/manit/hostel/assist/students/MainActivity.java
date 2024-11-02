package com.manit.hostel.assist.students;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.manit.hostel.assist.students.activity.EntryExitSlipActivityActivity;
import com.manit.hostel.assist.students.activity.HomeActivity;
import com.manit.hostel.assist.students.activity.LoginActivity;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.utils.UpdateDownloader;
import com.manit.hostel.assist.students.utils.Utility;
import com.onesignal.OneSignal;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "not a web app";
    private String HOSTEL_WIFI_MAC_ADDRESS = "00:00:00:00";
    private boolean allowInVicinity = true;
    private boolean isPresentAtGate = false;
    private WebView webview;
    private LinearLayout splash;
    boolean isComingBack = false;
    final Handler wifiScannerHandler = new Handler();
    StudentInfo loggedInStudent;

    MariaDBConnection dbConnection;

    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = findViewById(R.id.webview);
        splash = findViewById(R.id.splash);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(36000).build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        fetchRemoteConfig();
        /*  requestPermissions();
        Log.d(MainActivity.class.getSimpleName(), "Wifi Name list : " + new WifiScanner(this).getWifiList(this).toString());
        if (isLocationEnabled()) {
            // Start Wi-Fi scanning
            showWifiCheckDialog();
        } else {
            promptEnableLocation();
        }
        setupOneSignal();*/
    }

    private void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {

                dbConnection = new MariaDBConnection(this);
                String BASE_URL = dbConnection.getBaseURL();
                Log.d(MainActivity.class.getSimpleName(), "BASE_URL: " + BASE_URL);


                    if (!isInternetAvailable(this)) {
                        showNoInternetDialog(this);
                    } else {
                        checkForLatestVersion();
                    }

            } else {
                Utility.showNoInternetDialog(this);
            }
        });
    }

    private void checkForLatestVersion() {
        dbConnection.checkForUpdate(new MariaDBConnection.UpdateCallback() {
            @Override
            public void onUpdateAvailable(boolean b, String link) {
                if (b) {
                    showUpdateDialog(link);
                } else {
                    checkForStudentOut();
                }
            }

            @Override
            public void onError(String message) {
                if (message != null)
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                checkForStudentOut();
            }

            @Override
            public void networkError() {
                Utility.showNoInternetDialog(MainActivity.this);
            }
        });
    }

    private void checkForStudentOut() {
        loggedInStudent = AppPref.getLoggedInStudent(this);
        if (loggedInStudent == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            setupOneSignal();
            checkStatusOfStudent();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
       /* if (isComingBack) {
            isComingBack = false;
            if (isLocationEnabled()) {
                showWifiCheckDialog();
            } else {
                promptEnableLocation();
            }
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
       /* wifiScannerHandler.removeCallbacksAndMessages(null);
        isComingBack = true;
        isPresentAtGate = false;*/
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
                ((TextView) dialog.findViewById(R.id.wifi_status)).setText("Scanning Hostel Wifi");
                checkForWifi();  // Function to scan Wi-Fi
                if (!isPresentAtGate) {
                    wifiScannerHandler.postDelayed(this, 3000); // Repeat every 5 seconds
                } else {
                    ((TextView) dialog.findViewById(R.id.wifi_status)).setText("Found Hostel Wifi");
                    dialog.findViewById(R.id.success_icon).setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.success_icon).animate().alpha(1).setDuration(300).start();

                    dialog.findViewById(R.id.wifi_progress).setVisibility(View.GONE);
                    dialog.findViewById(R.id.wifi_progress).postDelayed(() -> {
                        loadWebview();
                        dialog.dismiss();
                    }, 1000);
                }
            }
        };
        wifiScannerHandler.post(runnable);
    }


    private void setupOneSignal() {
        OneSignal.login(loggedInStudent.getScholarNo());
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
        PermissionX.init(this).permissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET).request(new RequestCallback() {
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
            new AlertDialog.Builder(this).setTitle("Enable Location").setMessage("Location is required for scanning Wi-Fi networks. Please enable it in settings.").setPositiveButton("Enable", (dialogInterface, i) -> {
                // Direct user to location settings
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                isComingBack = true;
            }).setNegativeButton("Cancel", null).show();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void showNoInternetDialog(Context context) {
        runOnUiThread(()->{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Cant reach the server").setMessage("Please check your internet connection or try again later").setCancelable(false).setNegativeButton("Exit", (dialog, which) -> {
                finish();
            }).setPositiveButton("try again", (dialog, which) -> {
                dialog.dismiss();
                if (!isInternetAvailable(this)) {
                    showNoInternetDialog(this);
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    private void checkStatusOfStudent() {
        dbConnection.getStudentStatus(loggedInStudent.getScholarNo(), new MariaDBConnection.StatusCallback() {
            @Override
            public void outsideHostel(EntryDetail entryDetail) {
                openSlipActivity(entryDetail);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void networkError() {

            }

            @Override
            public void insideHostel(String message) {
                openHome();
            }
        });
    }

    private void openHome() {
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        Log.d(LoginActivity.class.getSimpleName(), "Logged in student: " + loggedInStudent);
        finish();
    }

    private void openSlipActivity(EntryDetail entryDetail) {
        Intent intent = new Intent(this, EntryExitSlipActivityActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void showUpdateDialog(String link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Available").setMessage("A new version is available. Do you want to update?").setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadApk(link);
            }
        }).setNegativeButton("Cancel", null).show();
    }

    private void downloadApk(String link) {
        ; // Replace with your APK URL
        UpdateDownloader customDownloader = new UpdateDownloader(this, link);
        customDownloader.startDownload();
    }
}