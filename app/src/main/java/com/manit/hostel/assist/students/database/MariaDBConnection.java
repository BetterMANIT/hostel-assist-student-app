package com.manit.hostel.assist.students.database;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.BuildConfig;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.StudentInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MariaDBConnection {

    private static final String BASE_URL = "http:/4.186.57.254/";


    private RequestQueue mQueue;
    private AppCompatActivity mAppCompatActivity;

    public MariaDBConnection(AppCompatActivity mAppCompatActivity) {
        mQueue = Volley.newRequestQueue(mAppCompatActivity);
        this.mAppCompatActivity = mAppCompatActivity;
    }

    // Fetch entry/exit list method
    public void fetchEntryExitList(Callback callback) {
        String url = BASE_URL + "/API/fetch_latest_entries.php";
        StringRequest mStringRequest = new StringRequest(Request.Method.GET, url, response -> callback.onResponse(response), error -> callback.onErrorResponse(error));
        mQueue.add(mStringRequest);
    }

    // Fetch student info by scholar number
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
                callback.onError(e.getMessage());
            }
        }, error -> {
            if (error.networkResponse.statusCode == 404) {
                callback.networkError();
            }
        });
        mQueue.add(jsonObjectRequest);
    }

    public void updateStudentInfo(StudentInfo studentInfo, StudentCallback callback) {
        String url = BASE_URL + "/API/set_student_info.php?scholar_no=" + studentInfo.getScholarNo();
        url = url + "&room_no=" + studentInfo.getRoomNo();
        url = url + "&guardian_no=" + studentInfo.getGuardianNo();
        url = url + "&section=" + studentInfo.getSection();


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
        }, error -> callback.onError(error.getMessage()));
        mQueue.add(jsonObjectRequest);
    }

    public void sendOtp(String mobile, OtpCallBack otpCallBack) {
        //===/API/send_otp_to_phone_no.php?phone_no=8021229292
        if (!mobile.isEmpty()) {
            String url = BASE_URL + "/API/send_otp_to_phone_no.php?phone_no=" + mobile;


            Log.d("URL", url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    // Extract the 'data' object
                    Log.d("Response", response.toString());

                    if (response.getString("otp") != null) {
                        otpCallBack.otpSent(response.getString("otp"));
                    } else {
                        otpCallBack.onError("Error in sending OTP");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    otpCallBack.onError(e.getMessage());
                }
            }, error -> otpCallBack.onError(error.getMessage()));
            mQueue.add(jsonObjectRequest);
        } else {
            otpCallBack.onError("Mobile Number is required");
        }
    }

    public void getStudentStatus(String scholarNo, StatusCallback statusCallback) {
        if (!scholarNo.isEmpty()) {
            String url = BASE_URL + "API/get_specific_entry_details_by_scholar_no.php?scholar_no=" + scholarNo;

            Log.d("URL", url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    Log.d("Response", response.toString());

                    if (response.getString("status").equals("success")) {
                        String entryExitTableName = response.optString("entry_exit_table_name");

                        if (entryExitTableName.isEmpty()) {
                            // Student is currently inside the hostel
                            statusCallback.insideHostel("Student is currently in hostel.");
                        } else {
                            // Parse the 'data' object
                            JSONObject dataObject = response.getJSONObject("data");
                            EntryDetail entryDetail = new EntryDetail(dataObject.getString("id"), dataObject.getString("scholar_no"), dataObject.getString("name"), dataObject.getString("room_no"), dataObject.getString("photo_url"), dataObject.getString("phone_no"), dataObject.getString("section"), dataObject.getString("open_time"), dataObject.optString("close_time"), dataObject.getString("updated_at"));
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
            }, error -> statusCallback.onError(error.getMessage()));

            mQueue.add(jsonObjectRequest);
        } else {
            statusCallback.onError("Scholar Number is required");
        }
    }


    public void checkForUpdate(UpdateCallback updateCallback) {
        // Assuming you fetch the app version from an API
        String url = BASE_URL + "/API/latest_version_android.php";  // Replace with your actual API URL

        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                // Assuming the response contains a 'version_name' field
                String serverVersion = response.getString("latest_version");
                String link = response.getString("link");
                String installedVersion = BuildConfig.VERSION_NAME;  // Get the installed app version

                Log.d("Installed Version", installedVersion);
                Log.d("Server Version", serverVersion);

                if (serverVersion != null && installedVersion != null) {
                    if (installedVersion.compareTo(serverVersion) < 0) {
                        updateCallback.onUpdateAvailable(true, link);
                    } else {
                        updateCallback.onUpdateAvailable(false, link);
                    }
                } else {
                    updateCallback.onError("Version information missing");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                updateCallback.onError(e.getMessage());
            }
        }, error -> updateCallback.onError(error.getMessage()));
        mQueue.add(jsonObjectRequest);
    }

    public void openNewEntry(StudentInfo studentInfo, AddNewEntryCallback callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + "API/open_new_entry.php";

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, response -> {
            // Handle successful response
            Log.d("Response", response);
            callback.onAddedSuccess(studentInfo.getScholarNo());
        }, error -> {
            // Handle error response
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
                params.put("section", studentInfo.getSection());
                params.put("hostel_name", studentInfo.getHostelName());
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";  // Proper content type for sending form data
            }
        };

        // Add the request to the RequestQueue
        mQueue.add(mStringRequest);
    }

    public void closeEntryStudent(String scholarNo, CloseEntryCallback callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + "API/close_already_existing_entry.php";
        Log.d(MariaDBConnection.class.getSimpleName(), "Sending request to: " + BASE_URL_PLUS_SUFFIX);

        final StringRequest mStringRequest = new StringRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, response -> {
            // Handle successful response
            callback.onSuccess(response);
        }, error -> {
            // Handle error response
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
        };

        // Add the request to the RequestQueue
        mQueue.add(mStringRequest);
    }


    // Interface for the entry-exit list callback
    public interface Callback {
        void onResponse(String result);

        void onErrorResponse(VolleyError error);
    }

    // Interface for the student info callback
    public interface StudentCallback {
        void onStudentInfoReceived(StudentInfo student);

        void onError(String error);

        void networkError();
    }

    public interface OtpCallBack {
        void otpSent(String otpId);

        void onError(String error);

        void networkError();
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

    public interface CloseEntryCallback {
        void onSuccess(String response);

        void onErrorResponse(String error);
    }

}
