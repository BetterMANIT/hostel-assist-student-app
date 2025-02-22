package com.manit.hostel.assist.students.database;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.HostelTable;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MariaDBConnection {


    private final String API_KEY;
    private String BASE_URL;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;


    private RequestQueue mQueue;
    private AppCompatActivity mAppCompatActivity;


    public MariaDBConnection(AppCompatActivity mAppCompatActivity) {
        mQueue = Volley.newRequestQueue(mAppCompatActivity);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        this.mAppCompatActivity = mAppCompatActivity;
        BASE_URL = getBaseURL();
        API_KEY = getApiKey();
    }

    private String getApiKey() {
        return mFirebaseRemoteConfig.getString("X_KEY");
    }


    public String getBaseURL() {
        return mFirebaseRemoteConfig.getString("BASE_URL");
    }


    // Logging in user after google signing in app to server
    public void loginUser(String scholarNo, String uid,Callback callback){
        String url = BASE_URL + "/API/login_user.php?";
        Map<String, String> query = new HashMap<>();
        query.put("device_id", Utility.getDeviceId(mAppCompatActivity.getContentResolver()));
        query.put("uid", uid);
        query.put("scholar_no", scholarNo);
        query.put("key",API_KEY);
        for(Map.Entry<String, String> entry : query.entrySet()){
            url =  url.concat(entry.getKey() + "=" + entry.getValue() + "&");
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                Log.d("Response", response.toString());
                if (response.getString("status").equals("success")) {
                    AppPref.saveAuthToken(mAppCompatActivity, response.getString("token")+uid);
                    callback.onResponse(response.getString("token"));
                } else if (response.getString("status").equals("error")) {
                    callback.onErrorResponse("error" + response.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onErrorResponse("error" + e.getMessage());
            }
        }, error -> {
            error.printStackTrace();
            callback.onErrorResponse("error" + error.getMessage());
        });
        mQueue.add(jsonObjectRequest);
    }


    public void fetchStudentInfo(String scholarNo, StudentCallback callback) {
        String url = BASE_URL + "/API/get_student_info.php?scholar_no=" + scholarNo;
        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                // Extract the 'data' object
                Log.d("Response", response.toString());

                if (response.getString("status").equals("success")) {
                    JSONObject data = response.getJSONObject("data");
                    // Parsing the data into StudentInfo
                    StudentInfo student = new StudentInfo(data.getString("scholar_no"), data.getString("name"), data.getString("room_no"), data.getString("hostel_name"), data.getString("photo_url"), data.getString("phone_no"), data.getString("section"), data.getString("guardian_no"), data.getString("entry_exit_table_name"));

                    // Pass the parsed student data to the callback
                    callback.onStudentInfoReceived(student);
                } else if (response.getString("status").equals("error")) {
                    callback.onError(response.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onError("error" + e.getMessage());
            }
        }, error -> {
            error.printStackTrace();
            callback.onError("error" + error.getMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return getHeadersDefault(scholarNo);
            }
        };
        mQueue.add(jsonObjectRequest);
    }

    public void updateStudentInfo(StudentInfo studentInfo, StudentCallback callback) {
        String url = BASE_URL + "/API/student/set_student_info.php?scholar_no=" + studentInfo.getScholarNo();
        url = url + "&room_no=" + studentInfo.getRoomNo();
        url = url + "&guardian_no=" + studentInfo.getGuardianNo();
        url = url + "&section=" + studentInfo.getSection();
        url = url + "&photo_url=" + studentInfo.getPhotoUrl();
        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                // Extract the 'data' object
                Log.d("Response", response.toString());

                if (response.getString("status").equals("success")) {
                    callback.onStudentInfoReceived(studentInfo);
                } else if (response.getString("status").equals("error")) {
                    callback.onError(response.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onError(e.getMessage());
            }
        }, error -> callback.onError(error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() {
                return getHeadersDefault(studentInfo.getScholarNo());
            }
        };
        mQueue.add(jsonObjectRequest);
    }

    //deprecated
    public void verifyOtp(String phone_no, Context mContext, String otp, String scholar_no, OtpVerificationCallBack otpVerificationCallBack) {

        //http://localhost/hostel-assist-web/API/student/verify_otp.php?phone_no=19209229&otp=123456&scholar_no=129293238
        final String url = BASE_URL + "/API/student/verify_otp.php?phone_no=" + phone_no + "&otp=" + otp + "&scholar_no=" + scholar_no;
        Log.d(MariaDBConnection.class.getSimpleName(), "Creating OTP Request, to URL " + url);
        JsonObjectRequest mJSONObject = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d(MariaDBConnection.class.getSimpleName(), "Response : " + response);
            try {
                if (response.has("status")) {
                    if (response.getString("status").equals("success")) {
                        AppPref.saveAuthToken(mContext, response.getString("token"));
                        otpVerificationCallBack.onSuccessfulVerification();
                    }
                }
            } catch (Exception e) {
                Log.e(MariaDBConnection.class.getSimpleName(), "Error", e);
                e.printStackTrace();
                otpVerificationCallBack.onError(e.getMessage());
            }
        }, error -> {
            Log.d(MariaDBConnection.class.getSimpleName(), "Error message : " + error.getMessage());
            error.printStackTrace();
            otpVerificationCallBack.onError("Error : " + error.getMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("device_id", Utility.getDeviceId(mContext.getContentResolver()));
                return headers;
            }

        };

        mQueue.add(mJSONObject);
    }

    //deprecated
    public void sendOtp(String mobile, String scholarNo, OtpCallBack otpCallBack) {
        //===/API/send_otp_to_phone_no.php?phone_no=8021229292
        if (!mobile.isEmpty()) {
            String url = BASE_URL + "/API/send_otp_to_phone_no.php?phone_no=" + mobile + "&scholar_no=" + scholarNo;
            Log.d("URL", url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    // Extract the 'data' object
                    Log.d("Response", response.toString());
                    if (response.getString("status").equals("success")) otpCallBack.otpSent();
                    else {
                        otpCallBack.onError("Error in sending OTP");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    otpCallBack.onError(e.getMessage());
                }
            }, error -> otpCallBack.onError(error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("device-id", Utility.getDeviceId(mAppCompatActivity.getContentResolver()));
                    return headers;
                }

            };
            mQueue.add(jsonObjectRequest);
        } else {
            otpCallBack.onError("Mobile Number is required");
        }
    }

    public void getStudentStatus(String scholarNo, StatusCallback statusCallback) {
        if (!scholarNo.isEmpty()) {
            String url = BASE_URL + "/API/get_specific_entry_details_by_scholar_no.php?scholar_no=" + scholarNo;

            Log.d("URL", url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    Log.d("Response", response.toString());

                    if (response.getString("status").equals("success")) {

                        if (!response.has("data")) {
                            // Student is currently inside the hostel
                            statusCallback.insideHostel("Student is currently in hostel.");
                        } else {
                            // Parse the 'data' object
                            JSONObject dataObject = response.getJSONObject("data");
                            EntryDetail entryDetail = getEntryDetail(dataObject);
                            // Student is outside the hostel
                            statusCallback.outsideHostel(entryDetail);
                        }
                    } else {
                        statusCallback.onError("Unexpected response from server.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    statusCallback.onError(e.getMessage());
                }
            }, error -> statusCallback.onError(error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() {
                    return getHeadersDefault(scholarNo);
                }

            };
            mQueue.add(jsonObjectRequest);
        } else {
            statusCallback.onError("Scholar Number is required");
        }
    }

    private static @NonNull EntryDetail getEntryDetail(JSONObject dataObject) throws JSONException {
        EntryDetail entryDetail = new EntryDetail(dataObject.getString("id"), dataObject.getString("scholar_no"), dataObject.getString("name"), dataObject.getString("room_no"), dataObject.getString("photo_url"), dataObject.getString("phone_no"), dataObject.getString("section"), dataObject.getString("open_time"), dataObject.optString("close_time"), dataObject.getString("updated_at"), dataObject.getString("purpose"));
        return entryDetail;
    }

    public void getStudentHistory(String scholarNo, HistoryCallback historyCallback) {
        if (!scholarNo.isEmpty()) {
            String url = BASE_URL + "/API/get_history_of_entry_by_scholar_no.php?scholar_no=" + scholarNo;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    Log.d("Response", response.toString());

                    if (response.getString("status").equals("success")) {
                        JSONObject data = response.getJSONObject("data");

                        ArrayList<EntryDetail> entriesList = new ArrayList<>();
                        Iterator<String> keys = data.keys();
                        while (keys.hasNext()) {
                            String tableName = keys.next();
                            JSONArray entries = data.getJSONArray(tableName);

                            for (int i = 0; i < entries.length(); i++) {
                                JSONObject entry = entries.getJSONObject(i);
                                EntryDetail entryDetail = getEntryDetail(entry);
                                entriesList.add(entryDetail);
                                Log.d("TAG", "Entry: " + entryDetail.getJSON());
                            }
                        }
                        entriesList = Utility.sortByOpenTime(entriesList);
                        historyCallback.onSuccess(entriesList);
                    } else {
                        historyCallback.onError("Unexpected response from server.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    historyCallback.onError(e.getMessage());
                }
            }, error -> historyCallback.onError(error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() {
                    return getHeadersDefault(scholarNo);
                }

            };

            mQueue.add(jsonObjectRequest);
        } else {
            historyCallback.onError("Scholar Number is required");
        }
    }

    public void getTablesForHostel(String hostelName,String scholarNo, TablesStatusCallback statusCallback) {
        if (!hostelName.isEmpty()) {
            String url = BASE_URL + "/API/student/get_purposes_by_hostel_name.php?hostel_name=" + hostelName;
            Log.d("URL", url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    Log.d("Response", response.toString());
                    if (response.getString("status").equals("success")) {
                        ArrayList<HostelTable> tables = new ArrayList<>();
                        JSONArray dataArray = response.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject tableObject = dataArray.getJSONObject(i);

                            HostelTable table = new HostelTable(tableObject.getInt("id"), tableObject.getString("table_name"), tableObject.getString("hostel_name"), tableObject.getString("purpose"));

                            tables.add(table);
                        }

                        // Pass the list to the callback
                        statusCallback.onSuccess(tables);
                    } else {
                        statusCallback.onError("Unexpected response from server.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    statusCallback.onError(e.getMessage());
                }
            }, error -> statusCallback.onError(error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() {
                    return getHeadersDefault(scholarNo);
                }

            };

            mQueue.add(jsonObjectRequest);
        } else {
            statusCallback.onError("Scholar Number is required");
        }
    }

    public void checkForUpdate(UpdateCallback updateCallback) {
        String url = BASE_URL + "/API/student/is_app_update_available.php";
        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, response -> {
            try {
                Log.e("Response", response.toString());
                if ("success".equals(response.getString("status"))) {
                    updateCallback.onUpdateAvailable(
                            response.getBoolean("is_update_available"),
                            response.getString("apk_download_link")
                    );
                } else {
                    updateCallback.onError("Unexpected response from server.");
                }
            } catch (JSONException e) {
                updateCallback.onError(e.getMessage());
            }
        }, error -> {
            if (error.networkResponse == null) { // Ensure only unreachable server errors are handled
                updateCallback.networkError();
            }
        }) {
            @Override
            public byte[] getBody() {
                try {
                    PackageInfo pInfo = mAppCompatActivity.getPackageManager().getPackageInfo(mAppCompatActivity.getPackageName(), 0);
                    return ("version_code=" + pInfo.versionCode).getBytes("UTF-8");
                } catch (PackageManager.NameNotFoundException | UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        mQueue.add(jsonObjectRequest);
    }

    public void openNewEntry(StudentInfo studentInfo, HostelTable table, AddNewEntryCallback callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + "/API/open_new_entry.php";

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, response -> {
            Log.d("Response", "open new entry : " + response);
            callback.onAddedSuccess(studentInfo.getScholarNo());
        }, error -> {
            callback.onError("Error: " + error.getMessage());
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", studentInfo.getScholarNo());
                params.put("name", studentInfo.getName());
                params.put("room_no", studentInfo.getRoomNo());
                params.put("photo_url", studentInfo.getPhotoUrl());
                params.put("phone_no", studentInfo.getPhoneNo());
                params.put("table_name", table.getTableName());
                params.put("purpose", table.getPurpose());
                params.put("section", studentInfo.getSection());
                params.put("hostel_name", studentInfo.getHostelName());
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";  // Proper content type for sending form data
            }

            @Override
            public Map<String, String> getHeaders() {
                return getHeadersDefault(studentInfo.getScholarNo());
            }

        };
        mQueue.add(mStringRequest);
    }

    public void closeEntryStudent(String scholarNo, CloseEntryCallback callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + "/API/close_already_existing_entry.php";
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to: " + BASE_URL_PLUS_SUFFIX);
        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, response -> {
            Log.d(MariaDBConnection.class.getSimpleName(), "Response : " + response);
            callback.onSuccess(response);
        }, error -> {
            callback.onErrorResponse("Error: " + error.getMessage());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", scholarNo); // Changed to use the variable without String.valueOf()
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";  // Proper content type for sending form data
            }

            @Override
            public Map<String, String> getHeaders() {
                return getHeadersDefault(scholarNo);
            }

        };
        mQueue.add(mStringRequest);
    }

    public void uploadPhoto(String scholarNo, File imageFile, PhotoUploadCallBack callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + "/API/student/upload_profile_photo.php?scholar_no=" + scholarNo;

        // Convert the image file to a Base64 string
        // Create a VolleyMultipartRequest
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, response -> {
            Log.d("Upload Response", new String(response.data));
            try {
                JSONObject result = new JSONObject(new String(response.data));
                boolean status = result.getBoolean("success");
                if(status){
                    callback.onAddedSuccess(scholarNo);
                }else{
                    callback.onError(result.getString("message"));
                }
            } catch (JSONException error) {
                Log.e("Upload Error", error.toString());
                callback.onError(error.toString());
            }

        }, error -> {
            Log.e("Upload Error", error.toString());
            callback.onError(error.toString());
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("image", new DataPart(imageFile.getName(), getFileData(imageFile),"image/png"));
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", scholarNo);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                return getHeadersDefault(scholarNo);
            }

        };
        mQueue.add(multipartRequest);
    }

    private static byte[] getFileData(File file) {
        byte[] data = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            data = new byte[(int) file.length()];
            fis.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void submitFeedback(String scholarNo, String name, String comments, int stars, String versionCode, Callback callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + "/API/student/record_feedback.php";

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, response -> {
            Log.d("Response", response);
            callback.onResponse(scholarNo);
        }, error -> {
            callback.onErrorResponse(error.getMessage());
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", scholarNo);
                params.put("name", name);
                params.put("comments", comments);
                params.put("stars", String.valueOf(stars));
                params.put("version_code", versionCode);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                return getHeadersDefault(scholarNo);
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";  // Proper content type for sending form data
            }
        };

        // Add the request to the RequestQueue
        mQueue.add(mStringRequest);
    }

    private @NonNull Map<String, String> getHeadersDefault(String scholarNo) {
        Map<String, String> headers = new HashMap<>();
        headers.put("device-id", Utility.getDeviceId(mAppCompatActivity.getContentResolver()));
        headers.put("token", AppPref.getAuthToken(mAppCompatActivity));
        headers.put("scholar-no", scholarNo);
        return headers;
    }


    public interface Callback {
        void onResponse(String result);

        void onErrorResponse(String error);
    }

    public interface StudentCallback {
        void onStudentInfoReceived(StudentInfo student);

        void onError(String error);

        void networkError();
    }

    public interface OtpCallBack {
        void otpSent();

        void onError(String error);

        void networkError();
    }

    public interface OtpVerificationCallBack {
        void onSuccessfulVerification();

        void onError(String error);
    }


    public interface UpdateCallback {
        void onUpdateAvailable(boolean b, String link);

        void onError(String message);

        void networkError();
    }

    public interface StatusCallback {
        void outsideHostel(EntryDetail entryDetail);

        void onError(String message);

        void networkError();

        void insideHostel(String message);
    }

    public interface AddNewEntryCallback {
        void onAddedSuccess(String scholarNo);

        void onError(String message);

        void networkError();
    }

    public interface PhotoUploadCallBack {
        void onAddedSuccess(String scholarNo);

        void onError(String message);

        void networkError();
    }

    public interface CloseEntryCallback {
        void onSuccess(String response);

        void onErrorResponse(String error);
    }

    public interface TablesStatusCallback {
        void onSuccess(ArrayList<HostelTable> table);

        void onError(String error);
    }

    public interface HistoryCallback {
        void onSuccess(ArrayList<EntryDetail> entries);

        void onError(String error);
    }
}
