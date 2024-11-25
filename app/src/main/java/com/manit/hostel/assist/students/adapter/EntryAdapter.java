package com.manit.hostel.assist.students.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manit.hostel.assist.students.activity.EntryExitSlipActivityActivity;
import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.databinding.EntryListBinding;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {
    private ArrayList<EntryDetail> entryDetailsList;

    public EntryAdapter(ArrayList<EntryDetail> entryDetailsList) {
        this.entryDetailsList = entryDetailsList;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using View Binding
        EntryListBinding binding = EntryListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {

        EntryDetail entryDetail = entryDetailsList.get(position);
        holder.binding.tvPurpose.setText(entryDetail.getPurpose());
        holder.binding.tvDate.setText(entryDetail.getOpenTime().split(" ")[0]);
        holder.binding.tvStartTime.setText(entryDetail.getOpenTime());
        holder.binding.tvEndTime.setText(entryDetail.getCloseTime());

        totalTimeLogic(holder, entryDetail);

        dateHeaderLogic(holder, position, entryDetail);
        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EntryExitSlipActivityActivity.class);
            try {
                intent.putExtra("VIEW", entryDetail.getJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            v.getContext().startActivity(intent);
        });
    }

    private static void totalTimeLogic(@NonNull EntryViewHolder holder, EntryDetail entryDetail) {
        if (entryDetail.getCloseTime() != null && !entryDetail.getCloseTime().isEmpty()) {
            // Parse openTime and closeTime
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            try {
                Date openTime = sdf.parse(entryDetail.getOpenTime());
                Date closeTime = sdf.parse(entryDetail.getCloseTime());

                // Calculate time difference in milliseconds
                long differenceInMillis = closeTime.getTime() - openTime.getTime();

                // Convert milliseconds to hours and minutes
                long differenceInMinutes = differenceInMillis / (1000 * 60);
                long hours = differenceInMinutes / 60;
                long minutes = differenceInMinutes % 60;

                // Set the total time in the TextView
                String totalTime = hours + " hrs " + minutes + " mins";
                holder.binding.totalTime.setText(totalTime);

                // Show the totalTime TextView
                holder.binding.totalTime.setVisibility(View.VISIBLE);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            holder.binding.totalTime.setVisibility(View.GONE);
        }
    }

    private void dateHeaderLogic(@NonNull EntryViewHolder holder, int position, EntryDetail entryDetail) {
        String currentDate = entryDetail.getOpenTime().split(" ")[0];
        if (position == 0) {
            holder.binding.tvDateHeader.setVisibility(View.VISIBLE);
            holder.binding.tvDateHeader.setText(currentDate);
        } else {
            String previousDate = entryDetailsList.get(position - 1).getOpenTime().split(" ")[0];
            if (!currentDate.equals(previousDate)) {
                holder.binding.tvDateHeader.setVisibility(View.VISIBLE);
                holder.binding.tvDateHeader.setText(currentDate);
            } else {
                holder.binding.tvDateHeader.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return entryDetailsList.size();
    }

    // ViewHolder class using View Binding
    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        EntryListBinding binding;

        public EntryViewHolder(EntryListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
