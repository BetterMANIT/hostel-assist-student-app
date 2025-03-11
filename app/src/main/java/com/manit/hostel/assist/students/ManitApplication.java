package com.manit.hostel.assist.students;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.manit.hostel.assist.students.data.AppPref;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class ManitApplication extends Application {

    private static final String ONESIGNAL_APP_ID = "fcbc265e-5f69-4b52-8df2-7ccbf5940739";

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        FirebaseApp.initializeApp(this);

        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        try {
            OneSignal.login(AppPref.getLoggedInStudent(this).getScholarNo());
            Log.d(MainActivity.class.getSimpleName(), "Logged in student: " + AppPref.getLoggedInStudent(this));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(MainActivity.class.getSimpleName(), "Error logging in: " + e.getMessage());
        }

        // Set Global Exception Handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("AppCrash", "Uncaught Exception", throwable);
            showErrorDialog(throwable);
        });
    }

    private void showErrorDialog(Throwable throwable) {
        new Thread(() -> {
            Looper.prepare();
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle("App Crashed")
                    .setMessage(throwable.getMessage())
                    .setCancelable(false)
                    .setPositiveButton("Restart", (dialog, which) -> {
                        restartApp();
                    })
                    .setNegativeButton("Close", (dialog, which) -> {
                        System.exit(1);
                    })
                    .show();
            Looper.loop();
        }).start();
    }

    private void restartApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        System.exit(1);
    }
}
