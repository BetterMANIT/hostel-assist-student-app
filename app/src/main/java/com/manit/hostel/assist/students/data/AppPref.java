package com.manit.hostel.assist.students.data;

import static android.content.Context.MODE_PRIVATE;

import com.manit.hostel.assist.students.activity.HomeActivity;
import com.manit.hostel.assist.students.activity.LoginActivity;

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

    public static void loginStudent(LoginActivity loginActivity, StudentInfo student) {
        try {
            loginActivity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(STUDENT_LOGIN, student.getJSON()).apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static StudentInfo getLoggedInStudent(LoginActivity loginActivity) {
        if(!loginActivity.getSharedPreferences(APREF, MODE_PRIVATE).getString(STUDENT_LOGIN, "").isEmpty()){
            try {
                return StudentInfo.fromJSON(loginActivity.getSharedPreferences(APREF, MODE_PRIVATE).getString(STUDENT_LOGIN, ""));
            } catch (JSONException e) {
                return null;
            }
        }else{
            return null;
        }
    }
}
