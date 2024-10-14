package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.students.BuildConfig;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityLoginBinding;
import com.manit.hostel.assist.students.utils.Utility;

public class LoginActivity extends AppCompatActivity {
    @NonNull
    ActivityLoginBinding lb;
    MariaDBConnection dbConnection;
    StudentInfo loggedInStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dbConnection = new MariaDBConnection(this);
        lb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());

        loggedInStudent = AppPref.getLoggedInStudent(this);
        if (loggedInStudent == null) {
            setScholarNoEnteringView();
        } else {
            checkStatusOfStudent();
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

                    @Override
                    public void networkError() {
                        Utility.showNoInternetDialog(LoginActivity.this);
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

                //check if debug build
                if (BuildConfig.DEBUG) {
                    Toast.makeText(LoginActivity.this, "OTP: " + otpId, Toast.LENGTH_SHORT).show();
                }
                lb.loginButton.setText("Verify OTP");
                lb.otpNoBox.setVisibility(View.VISIBLE);
                lb.forgotPasswordTextView.setVisibility(View.VISIBLE);
                lb.schnoEditText.setEnabled(false);
                lb.loginButton.setOnClickListener(v -> {
                    if (!lb.otpEditText.getText().toString().isEmpty()) {
                        if (lb.otpEditText.getText().toString().equals(otpId)) {
                            loginStudent(student);
                        }
                    }
                });

            }

            @Override
            public void onError(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkError() {
                Utility.showNoInternetDialog(LoginActivity.this);
            }
        });
    }

    private void loginStudent(StudentInfo student) {
        AppPref.loginStudent(this, student);
        if (AppPref.getLoggedInStudent(this) != null) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, UpdateInfoActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
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
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
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