package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityEntryExitSlipBinding;

public class EntryExitSlipActivityActivity extends AppCompatActivity {
    @NonNull
    ActivityEntryExitSlipBinding lb;
    MariaDBConnection dbConnection;
    StudentInfo loggedInStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbConnection = new MariaDBConnection(this);
        lb = ActivityEntryExitSlipBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        loggedInStudent = AppPref.getLoggedInStudent(this);
        lb.slipframe.setVisibility(View.GONE);
        if (loggedInStudent != null) {
            showDetails();
        } else {
            startActivity(new Intent(this, HomeActivity.class));
            Log.d(EntryExitSlipActivityActivity.class.getSimpleName(), "Logged in student: " + loggedInStudent);
            finish();
        }
    }

    private void showDetails() {
        dbConnection.getStudentStatus(loggedInStudent.getScholarNo(), new MariaDBConnection.StatusCallback() {
            @Override
            public void outsideHostel(EntryDetail entryDetail) {
                fillDetails(entryDetail);
                Animation watermarkAnimation = AnimationUtils.loadAnimation(EntryExitSlipActivityActivity.this, R.anim.watermark_animation);
                lb.watermark.startAnimation(watermarkAnimation);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void networkError() {

            }

            @Override
            public void insideHostel(String message) {
                startActivity(new Intent(EntryExitSlipActivityActivity.this, HomeActivity.class));
            }
        });
    }

    private void fillDetails(EntryDetail entryDetail) {
        // Assuming lb is already initialized in onCreate or elsewhere
        lb.studentName.setText("Name: " + entryDetail.getName());
        lb.entryNo.setText("Entry No: " + entryDetail.getId());
        lb.roomNo.setText("Room: " + entryDetail.getRoomNo());
        lb.date.setText("Date: " + entryDetail.getOpenTime().split(" ")[0]); // Assuming date comes as part of openTime
        lb.exitTime.setText("Time: " + entryDetail.getOpenTime().split(" ")[1]); // Time part from openTime
        lb.scholarNo.setText("Scholar No: " + entryDetail.getScholarNo());
        lb.watermark.setText(entryDetail.getOpenTime());
        lb.slipframe.setVisibility(View.VISIBLE);
        lb.slipframe.setAlpha(0f);
        lb.slipframe.animate().alpha(1f).setDuration(300).start();
        // Assuming you're using a URL for the photo and have an image loading library like Glide or Picasso
        Glide.with(this).load(loggedInStudent.getPhotoUrl()).placeholder(R.drawable.img) // Add a placeholder image in case the URL is null or slow to load
                .into(lb.stuPic); // The ImageView bound by LayoutBinding

        lb.enterAgain.setOnClickListener(v -> {
            dbConnection.closeEntryStudent(loggedInStudent.getScholarNo(), new MariaDBConnection.CloseEntryCallback() {
                @Override
                public void onSuccess(String response) {
                    startActivity(new Intent(EntryExitSlipActivityActivity.this, HomeActivity.class));
                    finish();
                }
                @Override
                public void onErrorResponse(String error) {
                    Toast.makeText(EntryExitSlipActivityActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

}