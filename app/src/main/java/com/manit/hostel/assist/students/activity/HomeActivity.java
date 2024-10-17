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
import com.manit.hostel.assist.students.data.HostelTable;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityHomeBinding;
import com.manit.hostel.assist.students.utils.SlipBottomSheet;
import com.manit.hostel.assist.students.utils.Utility;

import java.util.ArrayList;

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

        dbConnection.getTablesForHostel(loggedInStudent.getHostelName(), new MariaDBConnection.TablesStatusCallback() {
            @Override
            public void onSuccess(ArrayList<HostelTable> table) {
                ArrayList<String> tableNames = new ArrayList<>();
                lb.gradientBack.setupGradient();
                for (HostelTable hostelTable : table) {
                    tableNames.add(hostelTable.getPurpose().toUpperCase());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_dropdown_item_1line, tableNames);
                lb.placeSpinner.setAdapter(adapter);
                lb.placeSpinner.setOnItemClickListener((parent, view, position, id) -> {
                    Log.d(HomeActivity.this.toString(), "onItemSelected");
                    lb.goout.setEnabled(true);
                    placeSelected = tableNames.get(position);
                    lb.goout.setText("Exit for " + placeSelected);
                    lb.goout.setOnClickListener(v -> {
                        AppPref.setCurrentPlaceWent(HomeActivity.this, placeSelected);
                        showGeneratingSlipDialog(table, position);
                        lb.goout.setEnabled(false);
                    });
                });
            }

            @Override
            public void onError(String error) {

            }
        });

    }

    private void showGeneratingSlipDialog(ArrayList<HostelTable> table, int position) {
        lb.goout.setEnabled(false);
        SlipBottomSheet slipBottomSheet = new SlipBottomSheet(this, table.get(position), new SlipBottomSheet.SlipListener() {
            @Override
            public void onSlipGenerated() {
                addNewEntry(table.get(position));
            }

            @Override
            public void onSlipCancelled() {
                lb.goout.setEnabled(true);
            }
        },false, findViewById(R.id.view_overlay));
        slipBottomSheet.show();

    }


    private void addNewEntry(HostelTable selectedTable) {
        dbConnection.openNewEntry(loggedInStudent, selectedTable, new MariaDBConnection.AddNewEntryCallback() {
            @Override
            public void onAddedSuccess(String scholarNo) {
                checkStatusOfStudent();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                lb.goout.setEnabled(true);
            }

            @Override
            public void networkError() {
                Utility.showNoInternetDialog(HomeActivity.this);
                lb.goout.setEnabled(true);
                Toast.makeText(HomeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addClickLogic() {
        lb.goout.setOnClickListener((view) -> {
            Toast.makeText(this, "select place you are going!", Toast.LENGTH_SHORT).show();
        });
        lb.setting.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        lb.latestSlip.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

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
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Log.d(LoginActivity.class.getSimpleName(), "Logged in student: " + loggedInStudent);
                finish();
            }
        });
    }

    private void openSlipActivity(EntryDetail entryDetail) {
        Intent intent = new Intent(this, EntryExitSlipActivityActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}