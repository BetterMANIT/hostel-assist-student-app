package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.databinding.ActivityHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    @NonNull
    ActivityHomeBinding lb;
    StudentInfo loggedInStudent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        lb.quickCard.setVisibility(View.GONE);
        loggedInStudent = AppPref.getLoggedInStudent(this);
        if (loggedInStudent != null) {
            updateInfo(loggedInStudent);
        }
        addClickLogic();
    }

    private void addClickLogic() {
        lb.setting.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void updateInfo(StudentInfo studentInfo) {
        lb.scholarNo.setText("Scholar No: " + studentInfo.getScholarNo());
        lb.studentName.setText(studentInfo.getName());
        lb.roomNo.setText("Room No: " + studentInfo.getRoomNo());
        lb.hostelName.setText("Hostel Name: " + studentInfo.getHostelName());
        lb.phoneNo.setText("Phone: " + studentInfo.getPhoneNo());
        lb.section.setText("Section: " + studentInfo.getSection());
        lb.guardianNo.setText("Guardian: " + studentInfo.getGuardianNo());
        lb.quickCard.setVisibility(View.VISIBLE);
        // Use Glide to load the student's photo into ImageView
        Glide.with(this)
                .load(studentInfo.getPhotoUrl()) // Assuming `photoUrl` is a valid URL or local URI
                .placeholder(R.drawable.img) // Optional placeholder
                .into(lb.studentPhoto);
    }
}