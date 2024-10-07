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
    private boolean isEntryMode = false; // Added to differentiate between entry and exit

    public interface SlipListener {
        void onSlipGenerated();

        void onSlipCancelled();
    }

    public SlipBottomSheet(Context context, HostelTable hostelTable, SlipListener listener, boolean isEntryMode) {
        this.listener = listener;
        this.selectedTable = hostelTable;
        this.isEntryMode = isEntryMode;
        binding = SlipGeneratingViewBinding.inflate(LayoutInflater.from(context));
        updateUIForMode();
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.setCancelable(false);
        binding.cancelButton.setOnClickListener(view -> {
            isCancelled = true;
            bottomSheetDialog.dismiss();
            listener.onSlipCancelled(); // Notify listener of cancellation
        });
    }

    private void updateUIForMode() {
        if (isEntryMode) {
            binding.title.setText(String.format("Generating Entry Slip for %s", selectedTable.getPurpose()));
            binding.textViewGeneratingSlip.setText("Generating entry slip in 00:05");
            binding.note1.setText("Closing the entry requires you to be present at hostel gate");
        } else {
            binding.title.setText(String.format("Generating Exit Slip for %s", selectedTable.getPurpose()));
            binding.textViewGeneratingSlip.setText("Generating exit slip in 00:05");
        }
    }

    public void show() {
        bottomSheetDialog.show();
        startBufferTimer();
    }

    private void startBufferTimer() {
        countDownTimer = new CountDownTimer(bufferTime, 10) { // Update every 10ms
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                binding.textViewGeneratingSlip.setText(String.format("Generating in 00:0%d", secondsRemaining));

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

    private void generateSlip() {
        if (isEntryMode) {
            binding.textViewGeneratingSlip.setText("Entry Slip Generated!");
        } else {
            binding.textViewGeneratingSlip.setText("Exit Slip Generated!");
        }
        bottomSheetDialog.dismiss();
    }

    public void dismiss() {
        if (bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }
}
