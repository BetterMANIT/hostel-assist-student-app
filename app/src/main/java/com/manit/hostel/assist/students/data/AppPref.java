package com.manit.hostel.assist.students.data;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.manit.hostel.assist.students.activity.HomeActivity;

import org.json.JSONException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AppPref {
    private static final String APREF = "apref";
    private static final String SELECTED_HOSTEL = "selected_hostel";
    private static final String STUDENT_LOGIN = "selected_hostel";
    private static final String AUTH_TOKEN_KEY = "auth_token";
    private static final String PLACE_WENT = "place_went";
    private static final String UPDATED_INFO = "info_updated";

    private static SharedPreferences getEncryptedSharedPreferences(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    "secure_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveAuthToken(Context context, String authToken) {
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(context);
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(AUTH_TOKEN_KEY, authToken).apply();
        }
    }

    public static String getAuthToken(Context context) {
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getString(AUTH_TOKEN_KEY, null);
        }
        return null;
    }
    public static void clearAuthToken(Context context) {
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(context);
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(AUTH_TOKEN_KEY).apply();
        }
    }

    public static void setSelectedHostel(HomeActivity c, String string) {
        c.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(SELECTED_HOSTEL, string).apply();
    }

    public static String getSelectedHostel(HomeActivity c) {
        return c.getSharedPreferences(APREF, MODE_PRIVATE).getString(SELECTED_HOSTEL, "");
    }

    public static void loginStudent(Activity activity, StudentInfo student) {
        try {
            activity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(STUDENT_LOGIN, student.getJSON()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static StudentInfo getLoggedInStudent(Context activity) {
        if (!activity.getSharedPreferences(APREF, MODE_PRIVATE).getString(STUDENT_LOGIN, "").isEmpty()) {
            try {
                return StudentInfo.fromJSON(activity.getSharedPreferences(APREF, MODE_PRIVATE).getString(STUDENT_LOGIN, ""));
            } catch (JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void logoutStudent(Activity activity) {
        try {
            activity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(STUDENT_LOGIN, "").apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCurrentPlaceWent(Activity activity, String place) {
        activity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString(PLACE_WENT, place).apply();
    }

    public static String getCurrentPlaceWent(Activity activity) {
        return activity.getSharedPreferences(APREF, MODE_PRIVATE).getString(PLACE_WENT, "");
    }

    public static void saveLastEntryDetails(Activity activity, EntryDetail entryDetail) throws JSONException {
        activity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putString("entry_details", entryDetail.getJSON()).apply();
    }

    public static EntryDetail getLastEntryDetails(Activity activity) throws JSONException {
        return EntryDetail.fromJSON(activity.getSharedPreferences(APREF, MODE_PRIVATE).getString("entry_details", ""));
    }

    public static boolean isStudentInfoUpdated(Activity activity) {
        return activity.getSharedPreferences(APREF, MODE_PRIVATE).getBoolean(UPDATED_INFO, false);
    }
    public static void setStudentInfoUpdated(Activity activity, boolean updated) {
        activity.getSharedPreferences(APREF, MODE_PRIVATE).edit().putBoolean(UPDATED_INFO, updated).apply();
    }

}