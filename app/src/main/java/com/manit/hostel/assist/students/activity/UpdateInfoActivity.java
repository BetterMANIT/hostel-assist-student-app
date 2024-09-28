package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityUpdateInfoBinding;

public class UpdateInfoActivity extends AppCompatActivity {
    @NonNull
    ActivityUpdateInfoBinding lb;
    MariaDBConnection dbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbConnection = new MariaDBConnection(this);
        lb = ActivityUpdateInfoBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        if (AppPref.getLoggedInStudent(this) != null) {
            fillDetails(AppPref.getLoggedInStudent(this));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void fillDetails(StudentInfo studentInfo) {
        lb.schnoEditText.setText(studentInfo.getScholarNo());
        lb.schnoEditText.setEnabled(false);
        lb.nameEditText.setText(studentInfo.getName());
        lb.nameEditText.setEnabled(false);
        lb.roomEditText.setText(studentInfo.getRoomNo());
        lb.sectionEditText.setText(studentInfo.getSection());
        lb.guardianEditText.setText(studentInfo.getGuardianNo());
        lb.loginButton.setOnClickListener(v -> {
            studentInfo.setRoomNo(lb.roomEditText.getText().toString());
            studentInfo.setSection(lb.sectionEditText.getText().toString());
            studentInfo.setGuardianNo(lb.guardianEditText.getText().toString());
            dbConnection.updateStudentInfo(studentInfo, new MariaDBConnection.StudentCallback() {
                @Override
                public void onStudentInfoReceived(StudentInfo student) {
                    AppPref.loginStudent(UpdateInfoActivity.this, student);
                    if (AppPref.getLoggedInStudent(UpdateInfoActivity.this) != null) {
                        Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}