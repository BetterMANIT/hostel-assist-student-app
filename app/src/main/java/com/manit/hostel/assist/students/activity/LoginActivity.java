package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.manit.hostel.assist.students.R;
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
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dbConnection = new MariaDBConnection(this);
        lb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
//      allowAllSSL();
        loggedInStudent = AppPref.getLoggedInStudent(this);
        if (loggedInStudent == null) {
            initGoogleLogin();
        } else {
            checkStatusOfStudent();
        }
        Utility.coolButton(lb.googleSignIn);
    }

    private void initGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.default_web_client_id))
                .setHostedDomain("stu.manit.ac.in")
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        lb.googleSignIn.setOnClickListener(v -> {
            lb.loader.setVisibility(View.VISIBLE);
            lb.googleSignIn.setEnabled(false);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                lb.loader.setVisibility(View.GONE);
                if (user != null) {
                    Log.e("TAG", "firebaseAuthWithGoogle:" + user.getEmail());
                    Log.e("TAG", "firebaseAuthWithGoogle:" + user.getDisplayName());
                    Log.e("TAG", "firebaseAuthWithGoogle:" + user.getUid());
                    lb.welcome.setText("Welcome " + user.getDisplayName());
                    Toast.makeText(this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    loginUser(user.getEmail().substring(0, user.getEmail().indexOf("@")),user.getUid());
                }
            } else {
                lb.loader.setVisibility(View.VISIBLE);
                task.addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            lb.googleSignIn.setEnabled(true);
        });
    }

    private void loginUser(String scholarNo, String uid) {
        dbConnection.loginUser(scholarNo, uid, new MariaDBConnection.Callback() {
            @Override
            public void onResponse(String result) {
                fetchLoginDetails(scholarNo);
            }

            @Override
            public void onErrorResponse(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                Log.d(LoginActivity.class.getSimpleName(), "Error: " + error);
                FirebaseUser user = mAuth.getCurrentUser();
                mAuth.signOut();
            }
        });
    }

    private void fetchLoginDetails(String schNo) {
        if (!schNo.isEmpty()) {
            dbConnection.fetchStudentInfo(schNo, new MariaDBConnection.StudentCallback() {
                @Override
                public void onStudentInfoReceived(StudentInfo student) {
                    runOnUiThread(() -> loginStudent(student));
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void networkError() {
                    Utility.showNoInternetDialog(LoginActivity.this,null);
                }
            });
        } else {
            Toast.makeText(this, "Scholar no. is wrong", Toast.LENGTH_SHORT).show();
        }
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