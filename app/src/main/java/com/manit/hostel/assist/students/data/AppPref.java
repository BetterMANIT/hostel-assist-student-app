package com.manit.hostel.assist.students.data;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;

import com.manit.hostel.assist.students.activity.HomeActivity;
import com.manit.hostel.assist.students.activity.LoginActivity;
import com.manit.hostel.assist.students.activity.SettingsActivity;

import org.json.JSONException;

public class AppPref {
    private static final String APREF = "apref";
    private static final String SELECTED_HOSTEL = "selected_hostel";
    private static final String STUDENT_LOGIN = "selected_hostel";

    public static void setSelectedHostel(HomeActivity c, String string) {
        c.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(SELECTED_HOSTEL, string).apply();
    }
    public static String getSelectedHostel(HomeActivity c) {
        return c.getSharedPreferences(APREF, MODE_PRIVATE).getString(SELECTED_HOSTEL, "");
    }

    public static void loginStudent(Activity activity, StudentInfo student) {
        try {
            activity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(STUDENT_LOGIN, student.getJSON()).apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static StudentInfo getLoggedInStudent(Activity activity) {
        if(!activity.getSharedPreferences(APREF, MODE_PRIVATE).getString(STUDENT_LOGIN, "").isEmpty()){
            try {
                return StudentInfo.fromJSON(activity.getSharedPreferences(APREF, MODE_PRIVATE).getString(STUDENT_LOGIN, ""));
            } catch (JSONException e) {
                return null;
            }
        }else{
            return null;
        }
    }

    public static void logoutStudent(Activity activity) {
        try {
            activity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(STUDENT_LOGIN, "").apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
