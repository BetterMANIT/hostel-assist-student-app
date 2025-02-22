package com.manit.hostel.assist.students.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.manit.hostel.assist.students.data.EntryDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Utility {
    public static void showNoInternetDialog(Activity context, Runnable runnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cannot Reach The Server").setMessage("Please check your internet connection or try again later.").setCancelable(false).setPositiveButton("OK", (dialog, which) -> {
            if(runnable != null) runnable.run();
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static ArrayList<EntryDetail> sortByOpenTime(ArrayList<EntryDetail> entriesList) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Collections.sort(entriesList, (e1, e2) -> {
            try {
                Date openTime1 = dateFormat.parse(e1.getOpenTime());
                Date openTime2 = dateFormat.parse(e2.getOpenTime());
                return openTime2.compareTo(openTime1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
        return entriesList;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(ContentResolver contentResolver) {
        String deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        Log.d("Utility", "getDeviceId: " + deviceId);
        return deviceId;
    }

    public static void coolButton(LinearLayout googleSignIn) {
        googleSignIn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    googleSignIn.setElevation(10);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    googleSignIn.setElevation(5);
                    googleSignIn.performClick();
                }
                return false;
            }
        });
    }
}