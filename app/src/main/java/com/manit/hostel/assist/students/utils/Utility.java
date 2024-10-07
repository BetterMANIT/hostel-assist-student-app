package com.manit.hostel.assist.students.utils;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.manit.hostel.assist.students.data.EntryDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

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
}