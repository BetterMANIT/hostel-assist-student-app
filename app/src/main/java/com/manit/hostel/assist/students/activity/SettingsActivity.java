package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    @NonNull
    ActivitySettingsBinding lb;
    MariaDBConnection dbConnection;
    StudentInfo student;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbConnection = new MariaDBConnection(this);
        lb = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        if (AppPref.getLoggedInStudent(this) != null) {
            student = AppPref.getLoggedInStudent(this);
            setClickLogic();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void setClickLogic() {
        lb.changeInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, UpdateInfoActivity.class));
        });
        lb.loginButton.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void showLogoutDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Do you want to logout?");
        alertDialog.setMessage("You will be logged out from the app and logging again will require OTP");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
            logoutUser();
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> {
            Toast.makeText(this, "Please contact admin for changing number", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        alertDialog.show();
     }

    private void logoutUser() {
        AppPref.logoutStudent(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}