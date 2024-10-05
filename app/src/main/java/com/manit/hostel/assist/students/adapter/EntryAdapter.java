package com.manit.hostel.assist.students.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manit.hostel.assist.students.data.EntryDetail;
import com.manit.hostel.assist.students.databinding.EntryListBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {
    private ArrayList<EntryDetail> entryDetailsList;

    // Constructor
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
        // Get current entry detail item
        EntryDetail entryDetail = entryDetailsList.get(position);
        // Bind data to the view using View Binding
        holder.binding.tvPurpose.setText(entryDetail.getPurpose());
        holder.binding.tvDate.setText(entryDetail.getOpenTime().split(" ")[0]);
        holder.binding.tvStartTime.setText(entryDetail.getOpenTime());
        holder.binding.tvEndTime.setText(entryDetail.getCloseTime());

        // Check if closeTime is not null
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
            // Hide the totalTime TextView if closeTime is null or empty
            holder.binding.totalTime.setVisibility(View.GONE);
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
