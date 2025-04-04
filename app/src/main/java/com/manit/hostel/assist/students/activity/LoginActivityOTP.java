package com.manit.hostel.assist.students.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityLoginOtpBinding;
import com.manit.hostel.assist.students.utils.Utility;

public class LoginActivityOTP extends AppCompatActivity {
    @NonNull
    ActivityLoginOtpBinding lb;
    MariaDBConnection dbConnection;
    StudentInfo loggedInStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dbConnection = new MariaDBConnection(this);
        lb = ActivityLoginOtpBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
//      allowAllSSL();
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
        lb.resendOTPTextView.setVisibility(View.GONE);
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
                        Toast.makeText(LoginActivityOTP.this, error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void networkError() {
                        Utility.showNoInternetDialog(LoginActivityOTP.this,null);
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

    int otp_sent_counter = 0;

    private void sendOtp(StudentInfo student) {
        if (otp_sent_counter >= 2) {
            Toast.makeText(LoginActivityOTP.this, "OTP Send limit exceeded, TRY LATER!", Toast.LENGTH_LONG).show();
            return;
        }
        otp_sent_counter++;
        Toast.makeText(LoginActivityOTP.this, "Sending OTP", Toast.LENGTH_LONG).show();
        dbConnection.sendOtp(student.getPhoneNo(),student.getScholarNo(), new MariaDBConnection.OtpCallBack() {
            @SuppressLint("SetTextI18n")
            @Override
            public void otpSent() {
                lb.loginButton.setText("Verify OTP");
                lb.otpNoBox.setVisibility(View.VISIBLE);
                lb.resendOTPTextView.setVisibility(View.VISIBLE);
                lb.resendOTPTextView.setOnClickListener(v -> sendOtp(student));
                lb.schnoEditText.setEnabled(false);
                lb.loginButton.setOnClickListener(v -> {
                    if (lb.otpEditText.getText() != null && lb.otpEditText.getText().toString().isEmpty()) {
                        lb.otpEditText.requestFocus();
                        lb.otpEditText.setError("OTP Required");
                        return;
                    }
                    Dialog mOTPVerificationDialog = createVerificationDialog(LoginActivityOTP.this);
                    dbConnection.verifyOtp(student.getPhoneNo(), LoginActivityOTP.this, lb.otpEditText.getText().toString(), student.getScholarNo(), new MariaDBConnection.OtpVerificationCallBack() {
                        @Override
                        public void onSuccessfulVerification() {
                            Toast.makeText(LoginActivityOTP.this, "OTP Verified Successfully", Toast.LENGTH_LONG).show();
                            mOTPVerificationDialog.dismiss();
                            loginStudent(student);
                        }

                        @Override
                        public void onError(String error) {
                            mOTPVerificationDialog.dismiss();
                            Toast.makeText(LoginActivityOTP.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
                });

            }

            @Override
            public void onError(String error) {
                Toast.makeText(LoginActivityOTP.this, error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkError() {
                Utility.showNoInternetDialog(LoginActivityOTP.this,null);
            }
        });
    }


    public static AlertDialog createVerificationDialog(Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);  // Padding for better spacing
        layout.setGravity(Gravity.CENTER);

        ProgressBar progressBar = new ProgressBar(activity);
        progressBar.setIndeterminate(true);  // Indeterminate spinner

        TextView textView = new TextView(activity);
        textView.setText("Verifying OTP...");
        textView.setTextSize(18);
        textView.setGravity(Gravity.CENTER);

        layout.addView(progressBar);
        layout.addView(textView);

        // Create an AlertDialog to show the progress layout
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);  // Prevent dialog from being canceled with back button
        builder.setView(layout);  // Set the layout into the dialog

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();  // Show the dialog immediately

        // Return the dialog instance for later use (dismissal, etc.)
        return dialog;
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
                startActivity(new Intent(LoginActivityOTP.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Log.d(LoginActivityOTP.class.getSimpleName(), "Logged in student: " + loggedInStudent);
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