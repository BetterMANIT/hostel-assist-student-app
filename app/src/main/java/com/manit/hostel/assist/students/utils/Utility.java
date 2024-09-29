package com.manit.hostel.assist.students.utils;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class Utility {
    public static void showNoInternetDialog(Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cannot Reach The Server")
                .setMessage("Please check your internet connection or try again later.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
