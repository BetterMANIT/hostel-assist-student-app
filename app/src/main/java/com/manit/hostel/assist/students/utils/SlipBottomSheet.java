package com.manit.hostel.assist.students.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.manit.hostel.assist.students.R;
import com.manit.hostel.assist.students.data.HostelTable;
import com.manit.hostel.assist.students.databinding.SlipGeneratingViewBinding;

import org.checkerframework.common.subtyping.qual.Bottom;

public class SlipBottomSheet {

    private final HostelTable selectedTable;
    private BottomSheetDialog bottomSheetDialog;
    private SlipGeneratingViewBinding binding;
    private CountDownTimer countDownTimer;
    private long bufferTime = 3000; // 5 seconds buffer time
    private boolean isCancelled = false;
    private SlipListener listener;
    private boolean isEntryMode = false; // Added to differentiate between entry and exit

    public interface SlipListener {
        void onSlipGenerated();

        void onSlipCancelled();
    }

    private View overlayView;
    public SlipBottomSheet(Context context,
                           HostelTable hostelTable,
                           SlipListener listener,
                           boolean isEntryMode,
                           View overlayView) {
        this.listener = listener;
        this.selectedTable = hostelTable;
        this.isEntryMode = isEntryMode;
        binding = SlipGeneratingViewBinding.inflate(LayoutInflater.from(context));
        updateUIForMode();

        this.overlayView = overlayView;
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(overlayView!=null)
                    overlayView.setVisibility(View.GONE);
            }
        });

// Create a FrameLayout for the dark background
//        FrameLayout dialogBackground = new FrameLayout(context);
//        dialogBackground.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT
//        ));

// Set a semi-transparent background color for the dark effect
//        dialogBackground.setBackgroundColor(Color.parseColor("#80000000")); // Dark background

// Create a CardView to hold your content
        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

// Create a drawable with corner radii
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE); // Set the background color of the CardView
        float radius = dpToPx(16, context);
        drawable.setCornerRadii(new float[]{
                radius, radius, // top left
                radius, radius, // top right
                0f, 0f,       // bottom right
                0f, 0f        // bottom left
        });

// Set the background of the CardView to the drawable
        cardView.setBackground(drawable);
        cardView.setCardElevation(dpToPx(8, context)); // Set elevation for shadow
        cardView.addView(binding.getRoot()); // Add your content to the CardView

// Add the CardView to the dialog background
// Set the FrameLayout as the content view for the BottomSheetDialog
        bottomSheetDialog.setContentView(cardView);

// Set the FrameLayout as the content view for the BottomSheetDialog
//        bottomSheetDialog.setContentView(binding.getRoot());

//        bottomSheetDialog.setOnShowListener(dialog -> {
//            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
//            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//            if (bottomSheet != null) {
//                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED); // Expand the bottom sheet
//                bottomSheet.setBackgroundColor(Color.TRANSPARENT); // Make the bottom sheet background transparent
//            }
//        });

//        bottomSheetDialog = new BottomSheetDialog(context);
//
//
//        FrameLayout dialogBackground = new FrameLayout(context);
//        dialogBackground.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT
//        ));
//
//        // Set a semi-transparent background color
//        dialogBackground.setBackgroundColor(Color.parseColor("#80000000")); // Dark background
//
//        // Create a CardView to hold your content
//        CardView cardView = new CardView(context);
//        cardView.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//        ));
//
//        // Create a drawable with corner radii
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setColor(Color.WHITE); // Set the background color of the CardView
//        float radius = dpToPx(16, context);
//        drawable.setCornerRadii(new float[]{
//                radius, radius, // top left
//                radius, radius, // top right
//                0f, 0f, // bottom right
//                0f, 0f, // bottom left
//        });
//
//        // Set the background of the CardView to the drawable
//        cardView.setBackground(drawable);
//        cardView.setCardElevation(dpToPx(8, context)); // Set elevation for shadow
//        cardView.addView(binding.getRoot()); // Add your content to the CardView
//
//        // Add the CardView to the dialog background
//        dialogBackground.addView(cardView);
//
//        // Set the FrameLayout as the content view for the BottomSheetDialog
//        bottomSheetDialog.setContentView(dialogBackground);

//        bottomSheetDialog = new BottomSheetDialog(context);
////        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//
////        bottomSheetDialog.setContentView(binding.getRoot());
//
//        CardView cardView = new CardView(context);
//        cardView.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//        ));
//
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setColor(Color.WHITE); // Set the background color
//        float radius = dpToPx(16, context);
//        drawable.setCornerRadii(new float[]{
//                radius, radius,
//                radius, radius,
//                0f, 0f,
//                0f, 0f,
//                0f, 0f,
//                0f, 0f
//        });
//
//        // Set the background of the CardView to the drawable
//        cardView.setBackground(drawable);
//
//        // Apply the shape appearance model to the CardView
//        cardView.setCardElevation(dpToPx(8, context));
//
//        cardView.setCardBackgroundColor(Color.WHITE);
//
//        cardView.addView(binding.getRoot());
//
//        bottomSheetDialog.setContentView(cardView);


        bottomSheetDialog.setCancelable(false);
        binding.cancelButton.setOnClickListener(view -> {
            isCancelled = true;
            bottomSheetDialog.dismiss();
            listener.onSlipCancelled(); // Notify listener of cancellation
        });
    }

    public static int dpToPx(int dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (metrics.densityDpi / 160f));
    }

    private void updateUIForMode() {
        if (isEntryMode) {
            binding.title.setText(String.format("Generating Entry Slip for %s", selectedTable.getPurpose().toUpperCase()));
            binding.textViewGeneratingSlip.setText("Generating entry slip in 00:05");
            binding.note1.setText("Closing the entry requires you to be present at hostel gate");
        } else {
            binding.title.setText(String.format("Generating Exit Slip for %s", selectedTable.getPurpose()));
            binding.textViewGeneratingSlip.setText("Generating exit slip in 00:05");
        }
    }

    public void show() {
        if(overlayView!=null)
            overlayView.setVisibility(View.VISIBLE);
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
