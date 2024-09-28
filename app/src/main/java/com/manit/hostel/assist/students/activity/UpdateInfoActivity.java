package com.manit.hostel.assist.students.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.AppPref;
import com.manit.hostel.assist.students.data.StudentInfo;
import com.manit.hostel.assist.students.database.MariaDBConnection;
import com.manit.hostel.assist.students.databinding.ActivityUpdateInfoBinding;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class UpdateInfoActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1223;
    private static final int REQUEST_CAMERA_PERMISSION = 1225;
    private static final int REQUEST_GALLERY = 1224;
    @NonNull
    ActivityUpdateInfoBinding lb;
    MariaDBConnection dbConnection;
    Bitmap profilePhoto;
    private Uri imageUri;

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
        lb.roomEditText.setEnabled(false);
        lb.sectionEditText.setEnabled(false);
        lb.roomEditText.setText(studentInfo.getRoomNo());
        lb.sectionEditText.setText(studentInfo.getSection());
        lb.guardianEditText.setText(studentInfo.getGuardianNo());
        lb.uploadProfileBtn.setOnClickListener(v -> {
            showImagePickerDialog();
        });
        lb.loginButton.setOnClickListener(v -> {
            if ((!lb.guardianEditText.getText().toString().isEmpty()) && (lb.guardianEditText.getText().toString().length() == 10)) {
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
            } else {
                lb.guardianEditText.setError("Please enter guardian number correctly");
                Toast.makeText(getApplicationContext(), "Please enter guardian number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickerDialog() {
        // Show options for camera or gallery
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Camera selected
                    openCamera();
                } else {
                    // Gallery selected
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        if(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        }else{
            // Request camera permission
            Toast.makeText(getApplicationContext(),"Camera Permission Required",Toast.LENGTH_SHORT);
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    // Open gallery to select an image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                // Start crop activity for camera image
                startCrop(imageUri);
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                // Start crop activity for gallery image
                Uri selectedImage = data.getData();
                startCrop(selectedImage);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                // Get the cropped image result
                handleCropResult(data);
            } else if (requestCode == REQUEST_CAMERA_PERMISSION){
                openCamera();
            }
        }
    }

    private void startCrop(Uri sourceUri) {
        // Create a destination URI for the cropped image
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.jpg"));

        UCrop.of(sourceUri, destinationUri).withAspectRatio(7, 9) // Adjust the aspect ratio here
                .withMaxResultSize(420, 540) // Adjust the max size here
                .start(this);
    }

    private void handleCropResult(@Nullable Intent data) {
        if (data != null) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                // Set the cropped image to the ImageView
                lb.profileImage.setImageURI(null);
                lb.profileImage.setImageURI(resultUri);
            }
        }
    }
}