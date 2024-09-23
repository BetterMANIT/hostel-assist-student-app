package com.manit.hostel.assist.students;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.permissionx.guolindev.PermissionX;

import java.util.List;

public class WifiScanner {
    private WifiManager wifiManager;

    public WifiScanner(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public List<ScanResult> getWifiList(AppCompatActivity mAppCompatActivity) {
        if (!sufficientPermissionForWifiAccessAllowed(mAppCompatActivity)) {
            return null;
        }

        List<ScanResult> wifiList = wifiManager.getScanResults();
        return wifiList;
    }

    boolean sufficientPermissionForWifiAccessAllowed(AppCompatActivity mAppCompatActivity){
        if (ActivityCompat.checkSelfPermission(mAppCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return true;
        PermissionX.init(mAppCompatActivity).permissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        // TODO: 23/09/24 Implement the logic
        return true;
    }


}
