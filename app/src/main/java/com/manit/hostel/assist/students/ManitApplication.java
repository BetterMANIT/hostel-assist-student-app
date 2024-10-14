package com.manit.hostel.assist.students;

import android.app.Application;
import android.util.Log;

import com.manit.hostel.assist.students.data.AppPref;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class ManitApplication extends Application {

    // NOTE: Replace the below with your own ONESIGNAL_APP_ID

    private String ONESIGNAL_APP_ID = "fcbc265e-5f69-4b52-8df2-7ccbf5940739";

    @Override
    public void onCreate() {
        super.onCreate();

        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        OneSignal.getNotifications().requestPermission(false, Continue.none());
        try {
            OneSignal.login(AppPref.getLoggedInStudent(this).getScholarNo());
            Log.d(MainActivity.class.getSimpleName(), "Logged in student: " + AppPref.getLoggedInStudent(this));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(MainActivity.class.getSimpleName(), "Error logging in: " + e.getMessage());
        }
    }
}