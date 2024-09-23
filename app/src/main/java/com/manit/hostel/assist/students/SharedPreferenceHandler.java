package com.manit.hostel.assist.students;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferenceHandler {
    public SharedPreferenceHandler(Context mContext){

    }

    private static SharedPreferences getSharedPreferences(@NonNull Context mContext){
        return mContext.getSharedPreferences(PreferenceManager.getDefaultSharedPreferencesName(mContext), Context.MODE_PRIVATE);
    }

    private static final String SHARED_PREFERENCES_KEY_MAC_ADDRESS = "MAC_ADDRESSES";
    public static Set<String> getListOfAlreadyExistingMACAddresses(@NonNull Context mContext){
      return getSharedPreferences(mContext).getStringSet(SHARED_PREFERENCES_KEY_MAC_ADDRESS, new HashSet<>());
    }


}
