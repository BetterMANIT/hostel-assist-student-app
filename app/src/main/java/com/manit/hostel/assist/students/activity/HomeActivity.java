package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityHomeBinding;
import com.manit.hostel.assist.students.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {
    @NonNull
    ActivityHomeBinding lb;
    StudentInfo loggedInStudent;
    MariaDBConnection dbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        dbConnection = new MariaDBConnection(this);
        lb.quickCard.setVisibility(View.GONE);
        loggedInStudent = AppPref.getLoggedInStudent(this);
        if (loggedInStudent != null) {
            updateInfo(loggedInStudent);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        addClickLogic();
        updateStudentDetails();
        setupPlacesAdapter();
        checkStatusOfStudent();
    }

    private void checkStatusOfStudent() {
        dbConnection.getStudentStatus(loggedInStudent.getScholarNo(), new MariaDBConnection.StatusCallback() {
            @Override
            public void outsideHostel(EntryDetail entryDetail) {
                openSlipActivity(entryDetail);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void networkError() {

            }

            @Override
            public void insideHostel(String message) {

            }
        });

    }

    private void openSlipActivity(EntryDetail entryDetail) {
        Intent intent = new Intent(this, EntryExitSlipActivityActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateStudentDetails() {
        dbConnection.fetchStudentInfo(loggedInStudent.getScholarNo(), new MariaDBConnection.StudentCallback() {
            @Override
            public void onStudentInfoReceived(StudentInfo student) {
                runOnUiThread(() -> saveInPref(student));
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkError() {
                Utility.showNoInternetDialog(HomeActivity.this);
            }
        });
    }

    private void saveInPref(StudentInfo student) {
        AppPref.loginStudent(this, student);
        updateInfo(loggedInStudent);
    }

    String placeSelected;

    private void setupPlacesAdapter() {

        ArrayList<String> classes = new ArrayList<>(Arrays.asList("Collage", "Market", "Home", "Toorynad", "Maffick"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes);
        lb.placeSpinner.setAdapter(adapter);
        lb.placeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(HomeActivity.this.toString(), "onItemSelected");
            lb.goout.setEnabled(true);
            placeSelected = classes.get(position);
            lb.goout.setText("Exit for " + placeSelected);
            lb.goout.setOnClickListener(v -> {
                addNewEntry(placeSelected);
            });
        });
    }


    private void addNewEntry(String placeSelected) {
        dbConnection.openNewEntry(loggedInStudent, new MariaDBConnection.AddNewEntryCallback() {
            @Override
            public void onAddedSuccess(String scholarNo) {
                checkStatusOfStudent();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkError() {
                Utility.showNoInternetDialog(HomeActivity.this);
            }
        });
    }

    private void addClickLogic() {
        lb.goout.setOnClickListener((view)->{
            Toast.makeText(this, "select place you are going!", Toast.LENGTH_SHORT).show();
        });
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
        Glide.with(this).load(studentInfo.getPhotoUrl()) // Assuming `photoUrl` is a valid URL or local URI
                .placeholder(R.drawable.img) // Optional placeholder
                .into(lb.studentPhoto);
    }
}