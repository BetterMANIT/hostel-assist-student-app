package com.manit.hostel.assist.students.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.databinding.ActivityHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    @NonNull
    ActivityHomeBinding lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        lb = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(lb.getRoot());
        addClickLogic();
        addClickLogicToViewEntries();
        setupHostelSpinner(Arrays.asList("Hostel 10CD","Hostel 4"));
        lb.date.setText(getDateFormated());
    }

    private void addClickLogicToViewEntries() {
        lb.classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lb.viewEntries.setAlpha(1);
                AppPref.setSelectedHostel(HomeActivity.this, parent.getItemAtPosition(position).toString());
                lb.viewEntries.setEnabled(true);
                lb.viewEntries.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), HomeActivity.class)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lb.viewEntries.setEnabled(false);
                lb.viewEntries.setAlpha(0.5f);
            }
        });
    }

    private void addClickLogic() {
        lb.backbtn.setOnClickListener(v -> finish());
    }

    private String getDateFormated() {
        //get todays date in dd-mm-yyyy format
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }
    private void setupHostelSpinner(List<String> classes) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes);
        lb.classSpinner.setAdapter(adapter);
    }


}