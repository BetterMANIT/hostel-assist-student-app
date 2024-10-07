package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.HostelTable;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityEntryExitSlipBinding;
import com.manit.hostel.assist.students.utils.SlipBottomSheet;

import org.json.JSONException;
import org.json.JSONObject;

public class EntryExitSlipActivityActivity extends AppCompatActivity {
    @NonNull
    ActivityEntryExitSlipBinding lb;
    MariaDBConnection dbConnection;
    StudentInfo loggedInStudent;
    Boolean enteredBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbConnection = new MariaDBConnection(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        lb = ActivityEntryExitSlipBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        loggedInStudent = AppPref.getLoggedInStudent(this);
        hideUi();
        if (loggedInStudent != null) {
            showDetails();
        } else {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            Log.d(EntryExitSlipActivityActivity.class.getSimpleName(), "Logged in student: " + loggedInStudent);
            finish();
        }
    }

    private void hideUi() {
        lb.slipframe.setVisibility(View.GONE);
        lb.infoLayout.setVisibility(View.GONE);
        lb.timeLayout.setVisibility(View.GONE);
        lb.infoLayout.setAlpha(0);
        lb.timeLayout.setAlpha(0);
        lb.enterAgain.setVisibility(View.GONE);
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
                if (getIntent().hasExtra("LATEST")) {
                    Log.d(EntryExitSlipActivityActivity.class.getSimpleName(), "fetching latest slip ");
                    try {
                        EntryDetail entryDetail = AppPref.getLastEntryDetails(EntryExitSlipActivityActivity.this);
                        fillDetails(entryDetail);
                        enteredAgainUI(entryDetail);
                    } catch (JSONException e) {
                        failTheActivity();
                    }
                } else if (getIntent().hasExtra("VIEW")) {
                    Log.d(EntryExitSlipActivityActivity.class.getSimpleName(), "fetching latest slip ");
                    try {
                        EntryDetail entryDetail = EntryDetail.fromJSON(getIntent().getStringExtra("VIEW"));
                        fillDetails(entryDetail);
                        enteredAgainUI(entryDetail);
                    } catch (JSONException e) {
                        failTheActivity();
                    }
                } else {
                    startActivity(new Intent(EntryExitSlipActivityActivity.this, HomeActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }
        });
    }

    private void failTheActivity() {
        Toast.makeText(EntryExitSlipActivityActivity.this, "Error in fetching slip", Toast.LENGTH_SHORT).show();
        finish();
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
        lb.title.setText(String.format("%s Exit Slip", AppPref.getCurrentPlaceWent(this)));
        lb.entryNo.setText(String.format("Entry No: %s", entryDetail.getId()));
        lb.roomNo.setText(String.format("Room: %s", entryDetail.getRoomNo()));
        try {
            lb.date.setText(String.format("Date: %s", entryDetail.getOpenTime().split(" ")[0])); // Assuming date comes as part of openTime
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
        lb.mobileNo.setText(String.format("Mobile: %s", loggedInStudent.getPhoneNo()));

        doUiAnim();
        Glide.with(this).load(loggedInStudent.getPhotoUrl()).placeholder(R.drawable.img).into(lb.stuPic);

        lb.enterAgain.setOnClickListener(v -> {
            SlipBottomSheet slipBottomSheet = new SlipBottomSheet(EntryExitSlipActivityActivity.this, new HostelTable(entryDetail.getPurpose()), new SlipBottomSheet.SlipListener() {

                @Override
                public void onSlipGenerated() {
                    enterAgain(entryDetail);
                }

                @Override
                public void onSlipCancelled() {

                }
            }, true);
            slipBottomSheet.show();

        });
    }

    private void enterAgain(EntryDetail entryDetail) {
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
    }

    private void doUiAnim() {

        lb.infoLayout.setVisibility(View.GONE);
        lb.timeLayout.setVisibility(View.GONE);
        lb.infoLayout.setAlpha(0);
        lb.timeLayout.setAlpha(0);
        lb.enterAgain.setVisibility(View.GONE);
        lb.slipframe.setVisibility(View.VISIBLE);
        lb.cardView.post(() -> lb.cardView.setupGradient());
        lb.cardView.setTranslationY(-500);
        lb.cardView.animate().translationY(-50).setDuration(500).start();
        lb.cardView.postDelayed(() -> {
            lb.infoLayout.setVisibility(View.VISIBLE);
            lb.timeLayout.setVisibility(View.VISIBLE);
            lb.timeLayout.animate().alpha(1).setDuration(300).start();
            lb.infoLayout.animate().alpha(1).setDuration(300).start();
            lb.enterAgain.setVisibility(View.VISIBLE);
        }, 700);


        Animation watermarkAnimation = AnimationUtils.loadAnimation(EntryExitSlipActivityActivity.this, R.anim.watermark_animation);
        lb.watermark.startAnimation(watermarkAnimation);
    }

    private void enteredAgainUI(EntryDetail entryDetail) {
        enteredBack = true;
        try {
            lb.entryTime.setText(entryDetail.getCloseTime().split(" ")[1]);
        } catch (Exception e) {
            lb.entryTime.setText("-----");
        }
        lb.enterAgain.setText("Show Above to Guard");
        lb.enterAgain.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{Color.GREEN}));
        lb.enterAgain.setOnClickListener(v -> {
            startActivity(new Intent(EntryExitSlipActivityActivity.this, HomeActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
        Toast.makeText(EntryExitSlipActivityActivity.this, "Entry Closed", Toast.LENGTH_SHORT).show();
        lb.watermark.setText("Entered back");
        lb.watermark.setTextColor(Color.GREEN);
        lb.imgBorder.setCardBackgroundColor(Color.green(Color.GREEN));
    }

    @Override
    public void onBackPressed() {
        if (enteredBack) {
            startActivity(new Intent(EntryExitSlipActivityActivity.this, HomeActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}