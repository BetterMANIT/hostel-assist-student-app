package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.adapter.EntryAdapter;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.data.HostelTable;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityHistoryBinding;
import com.manit.hostel.assist.students.utils.SlipBottomSheet;
import com.manit.hostel.assist.students.utils.Utility;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    @NonNull
    ActivityHistoryBinding lb;
    StudentInfo loggedInStudent;
    MariaDBConnection dbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lb = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        dbConnection = new MariaDBConnection(this);
        loggedInStudent = AppPref.getLoggedInStudent(this);
        if (loggedInStudent != null) {
            displayHistory();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        addClickLogic();

    }

    private void addClickLogic() {
        lb.backbtn.setOnClickListener(v -> finish());
    }


    private void displayHistory() {
        dbConnection.getStudentHistory(loggedInStudent.getScholarNo(), new MariaDBConnection.HistoryCallback() {
            @Override
            public void onSuccess(ArrayList<EntryDetail> entries) {
                if(entries.size()!=0){
                    lb.history.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    lb.history.setAdapter(new EntryAdapter(entries));
                }else{
                    Toast.makeText(HistoryActivity.this, "No History Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }
}