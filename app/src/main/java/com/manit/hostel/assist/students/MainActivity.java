package com.manit.hostel.assist.students;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String HOSTEL_WIFI_MAC_ADDRESS = "00:00:00:00:00:00";
    private boolean allowInVicinity = true;
    private boolean isPresentAtGate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        Log.d(MainActivity.class.getSimpleName(), "Wifi Name list : " + new WifiScanner(this).getWifiList(this).toString());
        checkForWifi();

    }

    private void requestPermissions(){
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
        if(allowInVicinity){
            WifiScanner wifiScanner = new WifiScanner(getApplicationContext());
            List<ScanResult> scanResults = wifiScanner.getWifiList(this);
            for (ScanResult result : scanResults) {
                String ssid = result.SSID;  // Network name
                String bssid = result.BSSID; // MAC address of the network
                Log.d("WiFi", "SSID: " + ssid + ", BSSID (MAC): " + bssid);
                if(result.BSSID == HOSTEL_WIFI_MAC_ADDRESS){
                    isPresentAtGate = true;
                }
            }
        }else{

        }
    }
}