package com.manit.hostel.assist.students.database;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.manit.hostel.assist.students.data.StudentInfo;

import org.json.JSONException;
import org.json.JSONObject;

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
        StringRequest mStringRequest = new StringRequest(Request.Method.GET, url,
                response -> callback.onResponse(response),
                error -> callback.onErrorResponse(error)
        );
        mQueue.add(mStringRequest);
    }

    // Fetch student info by scholar number
    public void fetchStudentInfo(String scholarNo, StudentCallback callback) {
        String url = BASE_URL + "/API/get_student_info.php?scholar_no=" + scholarNo;
        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Extract the 'data' object
                        Log.d("Response", response.toString());

                        if(response.getString("status").equals("success")){
                            JSONObject data = response.getJSONObject("data");
                            // Parsing the data into StudentInfo
                            StudentInfo student = new StudentInfo(
                                    data.getString("scholar_no"),
                                    data.getString("name"),
                                    data.getString("room_no"),
                                    data.getString("hostel_name"),
                                    data.getString("photo_url"),
                                    data.getString("phone_no"),
                                    data.getString("section"),
                                    data.getString("guardian_no"),
                                    data.getString("entry_exit_table_name")
                            );

                            // Pass the parsed student data to the callback
                            callback.onStudentInfoReceived(student);
                        } else if (response.getString("status").equals("error")) {
                            callback.onError(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.getMessage())
        );
        mQueue.add(jsonObjectRequest);
    }

    public void sendOtp(String string, OtpCallBack otpCallBack) {
        if (!string.isEmpty()) {
            otpCallBack.otpSent("1234");
        } else {
            otpCallBack.onError("Mobile Number is required");
        }
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
    }

    public interface OtpCallBack {
        void otpSent(String otpId);

        void onError(String error);
    }
}
