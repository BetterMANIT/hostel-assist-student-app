package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    @NonNull
    ActivityLoginBinding lb;
    MariaDBConnection dbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbConnection = new MariaDBConnection(this);
        lb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        if (AppPref.getLoggedInStudent(this) == null) {
            setScholarNoEnteringView();
        } else {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void setScholarNoEnteringView() {
        lb.loginButton.setText("Send OTP");
        lb.otpNoBox.setVisibility(View.GONE);
        lb.forgotPasswordTextView.setVisibility(View.GONE);
        lb.schnoEditText.setEnabled(true);
        lb.loginButton.setOnClickListener(v -> {
            if (!lb.schnoEditText.getText().toString().isEmpty()) {
                dbConnection.fetchStudentInfo(lb.schnoEditText.getText().toString(), new MariaDBConnection.StudentCallback() {
                    @Override
                    public void onStudentInfoReceived(StudentInfo student) {
                        runOnUiThread(() -> displayConfirmOtpDialog(student));
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                lb.schnoEditText.setError("Scholar no. is required");
            }

        });
    }

    private void displayConfirmOtpDialog(StudentInfo student) {
        //show a alert dialog for confirming otp dialog
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Hello " + student.getName());
        alertDialog.setMessage("Is " + student.getPhoneNo() + " your mobile number?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
            sendOtp(student);
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> {
            Toast.makeText(this, "Please contact admin for changing number", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        alertDialog.show();
    }

    private void sendOtp(StudentInfo student) {
        dbConnection.sendOtp(student.getPhoneNo(), new MariaDBConnection.OtpCallBack() {
            @Override
            public void otpSent(String otpId) {
                lb.loginButton.setText("Verify OTP");
                lb.otpNoBox.setVisibility(View.VISIBLE);
                lb.forgotPasswordTextView.setVisibility(View.VISIBLE);
                lb.schnoEditText.setEnabled(false);
                lb.loginButton.setOnClickListener(v -> {
                    if (!lb.otpEditText.getText().toString().isEmpty()) {
                        // if(lb.otpEditText.getText().toString().equals(otpId)) {
                        loginStudent(student);
                        //}
                    }
                });

            }

            @Override
            public void onError(String error) {

            }
        });
    }

    private void loginStudent(StudentInfo student) {
        AppPref.loginStudent(this, student);
        if (AppPref.getLoggedInStudent(this) != null) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, UpdateInfoActivity.class));
            finish();
        }
    }
}