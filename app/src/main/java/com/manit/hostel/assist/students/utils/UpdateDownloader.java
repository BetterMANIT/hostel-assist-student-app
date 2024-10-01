package com.manit.hostel.assist.students.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.manit.hostel.assist.students.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateDownloader {

    private Context context;
    private String apkDownloadLink;

    public UpdateDownloader(Context context, String apkDownloadLink) {
        this.context = context;
        this.apkDownloadLink = apkDownloadLink;
    }

    public void startDownload() {
        showDownloadDialog();
    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Downloading Update")
                .setMessage("The update is being downloaded...")
                .setCancelable(false);

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View customLayout = inflater.inflate(R.layout.dialog_download, null);
        builder.setView(customLayout);

        ProgressBar progressBar = customLayout.findViewById(R.id.progressBar);
        TextView progressText = customLayout.findViewById(R.id.progressText);

        Dialog dialog = builder.create();
        dialog.show();

        new DownloadFileAsync(progressBar, progressText, dialog).execute(apkDownloadLink);
    }

    private class DownloadFileAsync extends AsyncTask<String, Integer, String> {
        private ProgressBar progressBar;
        private TextView progressText;
        private Dialog dialog;

        public DownloadFileAsync(ProgressBar progressBar, TextView progressText, Dialog dialog) {
            this.progressBar = progressBar;
            this.progressText = progressText;
            this.dialog = dialog;
        }

        @Override
        protected String doInBackground(String... urls) {
            String apkPath = context.getExternalCacheDir() + "/update.apk";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // Check for a valid response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }

                // Download the file
                int fileLength = connection.getContentLength();
                InputStream input = new BufferedInputStream(connection.getInputStream());
                FileOutputStream output = new FileOutputStream(apkPath);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Publish the progress
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                return e.toString();
            }

            return apkPath;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
            progressText.setText(progress[0] + "%");
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            if (result.startsWith("Server returned HTTP")) {
                // Handle error
                new AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            } else {
                // Start installation
                installApk(result);
            }
        }
    }

    private void installApk(String apkPath) {
        File apkFile = new File(apkPath);
        Uri apkUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(context, "your.package.name.fileprovider", apkFile);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
