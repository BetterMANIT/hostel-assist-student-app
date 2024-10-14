package com.manit.hostel.assist.students.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityFeedbackBinding;

import java.io.UnsupportedEncodingException;

public class FeedBackActivity extends AppCompatActivity {
    @NonNull
    ActivityFeedbackBinding lb;
    MariaDBConnection dbConnection;
    int stars = 0;
    StudentInfo loggedInStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dbConnection = new MariaDBConnection(this);
        lb = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());

        loggedInStudent = AppPref.getLoggedInStudent(this);
        if (loggedInStudent != null) {
            addClickLogic();
        } else {
            Toast.makeText(this, "Error in fetching logged in student", Toast.LENGTH_SHORT).show();
        }
    }

    private void addClickLogic() {
        lb.sendFeedback.setEnabled(true);
        lb.sendFeedback.setAlpha(1f);
        lb.sendFeedback.setOnClickListener(v -> {
            Log.d(FeedBackActivity.class.getSimpleName(), "Sending feedback");
            if (lb.titleEditText.getText().toString().isEmpty() || lb.bodyEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if(lb.titleEditText.getText().toString().length() > 100){
                Toast.makeText(this, "Title should be less than 100 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if(lb.bodyEditText.getText().toString().length() > 1000){
                Toast.makeText(this, "Body should be less than 1000 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if((int) lb.ratingBar.getRating() == 0){
                Toast.makeText(this, "Please rate your experience", Toast.LENGTH_SHORT).show();
                return;
            }
            stars = (int) lb.ratingBar.getRating();
            lb.sendFeedback.setEnabled(false);
            lb.sendFeedback.setAlpha(0.5f);
            String version_code = "error";
            PackageInfo pInfo = null;
            try {
                pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                int versionCode = pInfo.versionCode;
                version_code = String.valueOf(versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String comments = lb.titleEditText.getText().toString() + " :: " + lb.bodyEditText.getText().toString();
            dbConnection.submitFeedback(loggedInStudent.getScholarNo(), loggedInStudent.getName(), comments, stars, version_code, new MariaDBConnection.Callback() {
                @Override
                public void onResponse(String result) {
                    Toast.makeText(FeedBackActivity.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(FeedBackActivity.this, "Error submitting feedback", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

}