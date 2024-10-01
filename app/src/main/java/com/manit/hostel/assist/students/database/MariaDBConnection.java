package com.manit.hostel.assist.students.database;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.HostelTable;
import com.manit.hostel.assist.students.data.StudentInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

                        if (!response.has("data")) {
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

    public void getTablesForHostel(String hostelName, TablesStatusCallback statusCallback) {
        if (!hostelName.isEmpty()) {
            String url = BASE_URL + "API/student/get_purposes_by_hostel_name.php?hostel_name=" + hostelName;
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
            }, error -> statusCallback.onError(error.getMessage()));

            mQueue.add(jsonObjectRequest);
        } else {
            statusCallback.onError("Scholar Number is required");
        }
    }


    public void checkForUpdate(UpdateCallback updateCallback) {
        // Assuming you fetch the app version from an API
        String url = BASE_URL + "API/student/is_app_update_available.php";  // Replace with your actual API URL

        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, response -> {
            try {
                //{
                //    "status": "success",
                //    "is_update_available": true,
                //    "apk_download_link": "https://example.com/latest_app.apk"
                //}
                Log.e("Response", response.toString());
                if (response.getString("status").equals("success")) {
                    if (response.getBoolean("is_update_available")) {
                        updateCallback.onUpdateAvailable(true, response.getString("apk_download_link"));
                    } else {
                        updateCallback.onUpdateAvailable(false, response.getString("apk_download_link"));
                    }
                } else {
                    updateCallback.onError("Unexpected response from server.");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                updateCallback.onError(e.getMessage());
            }
        }, error -> updateCallback.onError(error.getMessage()))
        {
            @Override
            public byte[] getBody() {
                PackageInfo pInfo = null;
                try {
                    pInfo = mAppCompatActivity.getPackageManager().getPackageInfo(mAppCompatActivity.getPackageName(), 0);
                    int versionCode = pInfo.versionCode;
                    Log.d("Version Code", String.valueOf(versionCode));
                    return ("version_code=" + versionCode).getBytes("UTF-8");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return super.getBody();
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";  // Proper content type for sending form data
            }
        };
        mQueue.add(jsonObjectRequest);
    }

    public void openNewEntry(StudentInfo studentInfo, String table, AddNewEntryCallback callback) {
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
                params.put("table_name", table);
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

    public void uploadPhoto(String scholarNo, File imageFile, PhotoUploadCallBack callback) {
        final String BASE_URL_PLUS_SUFFIX = BASE_URL + "API/student/upload_profile_photo.php";

        // Convert the image file to a Base64 string
        // Create a VolleyMultipartRequest
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, BASE_URL_PLUS_SUFFIX, response -> {
            Log.d("Upload Response", new String(response.data));

        }, error -> {
            Log.e("Upload Error", error.toString());
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("image", new DataPart(imageFile.getName(), getFileData(imageFile)));
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("scholar_no", scholarNo);
                return params;
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

    public interface Callback {
        void onResponse(String result);

        void onErrorResponse(VolleyError error);
    }

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

}
