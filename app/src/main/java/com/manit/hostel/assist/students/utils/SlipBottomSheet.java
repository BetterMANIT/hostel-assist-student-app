package com.manit.hostel.assist.students.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.manit.hostel.assist.students.data.HostelTable;
import com.manit.hostel.assist.students.databinding.SlipGeneratingViewBinding;

public class SlipBottomSheet {

    private final HostelTable selectedTable;
    private BottomSheetDialog bottomSheetDialog;
    private SlipGeneratingViewBinding binding;
    private CountDownTimer countDownTimer;
    private long bufferTime = 7000; // 5 seconds buffer time
    private boolean isCancelled = false;
    private SlipListener listener;

    // Interface for events
    public interface SlipListener {
        void onSlipGenerated();

        void onSlipCancelled();
    }

    // Constructor to set up the bottom sheet
    public SlipBottomSheet(Context context, HostelTable hostelTable, SlipListener listener) {
        this.listener = listener;
        this.selectedTable = hostelTable;        // Initialize view binding for the bottom sheet layout
        binding = SlipGeneratingViewBinding.inflate(LayoutInflater.from(context));
        binding.title.setText(String.format("Generating Exit Slip for %s", selectedTable.getPurpose()));
        // Create and configure the BottomSheetDialog
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.setCancelable(false);
        // Handle the cancel button click
        binding.cancelButton.setOnClickListener(view -> {
            isCancelled = true;
            bottomSheetDialog.dismiss();
            listener.onSlipCancelled(); // Notify listener of cancellation
        });
    }

    // Show the bottom sheet dialog and start the timer
    public void show() {
        bottomSheetDialog.show();
        startBufferTimer();
    }

    // Start the countdown for 5 seconds buffer time
    private void startBufferTimer() {
        countDownTimer = new CountDownTimer(bufferTime, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                binding.textViewGeneratingSlip.setText("Generating in 00:0" + secondsRemaining);

                // Update the progress bar
                float progressPercentage = (bufferTime - millisUntilFinished) / (float) bufferTime;
                int progressWidth = (int) (binding.progressTrack.getWidth() * progressPercentage);
                binding.progressBar.getLayoutParams().width = progressWidth;
                binding.progressBar.requestLayout();
            }

            public void onFinish() {
                if (!isCancelled) {
                    listener.onSlipGenerated(); // Notify listener of slip generation
                    generateSlip();
                }
            }
        }.start();
    }

    // Logic for generating the slip
    private void generateSlip() {
        binding.textViewGeneratingSlip.setText("Slip Generated!");
        bottomSheetDialog.dismiss();
    }

    // Dismiss the bottom sheet if needed
    public void dismiss() {
        if (bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }
}
