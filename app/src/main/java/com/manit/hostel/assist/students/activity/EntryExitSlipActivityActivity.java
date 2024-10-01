package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.graphics.Color;
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

import org.json.JSONException;
import org.json.JSONObject;

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
        lb.heading.setTranslationY(-500);
        lb.heading.animate().translationY(0).setDuration(1000).start();
        if (loggedInStudent != null) {
            showDetails();
        } else {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            Log.d(EntryExitSlipActivityActivity.class.getSimpleName(), "Logged in student: " + loggedInStudent);
            finish();
        }
    }

    private void showDetails() {
        dbConnection.getStudentStatus(loggedInStudent.getScholarNo(), new MariaDBConnection.StatusCallback() {
            @Override
            public void outsideHostel(EntryDetail entryDetail) {
                save(entryDetail);
                fillDetails(entryDetail);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void networkError() {

            }

            @Override
            public void insideHostel(String message) {
                if(!getIntent().hasExtra("LATEST")){
                    startActivity(new Intent(EntryExitSlipActivityActivity.this, HomeActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }else{
                    Log.d(EntryExitSlipActivityActivity.class.getSimpleName(), "fetching latest slip ");
                    try {
                        EntryDetail entryDetail = AppPref.getLastEntryDetails(EntryExitSlipActivityActivity.this);
                        fillDetails(entryDetail);
                        enteredAgainUI(entryDetail);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void save(EntryDetail entryDetail) {
        try {
            AppPref.saveLastEntryDetails(EntryExitSlipActivityActivity.this, entryDetail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillDetails(EntryDetail entryDetail) {
        // Assuming lb is already initialized in onCreate or elsewhere
        lb.studentName.setText(entryDetail.getName());
        lb.title.setText(AppPref.getCurrentPlaceWent(this) + " Exit Slip");
        lb.entryNo.setText("Entry No: " + entryDetail.getId());
        lb.roomNo.setText("Room: " + entryDetail.getRoomNo());
        try {
            lb.date.setText("Date: " + entryDetail.getOpenTime().split(" ")[0]); // Assuming date comes as part of openTime
            lb.exitTime.setText(entryDetail.getOpenTime().split(" ")[1]); // Time part from openTime
            if (entryDetail.getCloseTime() != null) {
                lb.entryTime.setText(entryDetail.getCloseTime().split(" ")[1]);
            } else {
                lb.entryTime.setText("-----");
            }
        } catch (Exception e) {
            lb.entryTime.setText("-----");
            // Time part from openTime
        }
        lb.scholarNo.setText(entryDetail.getScholarNo());
        lb.watermark.setText(entryDetail.getOpenTime());
        lb.mobileNo.setText("Mobile: " + loggedInStudent.getPhoneNo());
        lb.slipframe.setVisibility(View.VISIBLE);
        lb.slipframe.setAlpha(0f);
        lb.slipframe.animate().alpha(1f).setDuration(500).start();
        lb.slipframe.setTranslationY(500);
        lb.slipframe.animate().translationY(0).setDuration(500).start();
        Animation watermarkAnimation = AnimationUtils.loadAnimation(EntryExitSlipActivityActivity.this, R.anim.watermark_animation);
        lb.watermark.startAnimation(watermarkAnimation);
        // Assuming you're using a URL for the photo and have an image loading library like Glide or Picasso
        Glide.with(this).load(loggedInStudent.getPhotoUrl()).placeholder(R.drawable.img) // Add a placeholder image in case the URL is null or slow to load
                .into(lb.stuPic); // The ImageView bound by LayoutBinding

        lb.enterAgain.setOnClickListener(v -> {
            dbConnection.closeEntryStudent(loggedInStudent.getScholarNo(), new MariaDBConnection.CloseEntryCallback() {
                @Override
                public void onSuccess(String response) {
                    // show time in hh:mm:ss format
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        entryDetail.setCloseTime(jsonObject.getString("close_time"));
                        enteredAgainUI(entryDetail);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onErrorResponse(String error) {
                    Toast.makeText(EntryExitSlipActivityActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void enteredAgainUI(EntryDetail entryDetail) {
        lb.entryTime.setText(entryDetail.getCloseTime().split(" ")[1]);
        lb.enterAgain.setText("Show Above to Guard");
        Toast.makeText(EntryExitSlipActivityActivity.this, "Entry Closed", Toast.LENGTH_SHORT).show();
        lb.watermark.setText("Entered back");
        lb.watermark.setTextColor(Color.GREEN);
        lb.imgBorder.setCardBackgroundColor(Color.green(Color.GREEN));
    }

}