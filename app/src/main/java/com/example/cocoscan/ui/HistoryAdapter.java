package com.example.cocoscan.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cocoscan.R;
import com.example.cocoscan.data.DetectionRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final Context context;
    private List<DetectionRecord> records = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    public HistoryAdapter(Context context) {
        this.context = context;
    }

    public void setRecords(List<DetectionRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetectionRecord record = records.get(position);
        holder.tvCount.setText(record.totalCoconuts + " Coconuts");
        holder.tvConf.setText(String.format("%d%% Conf.", (int)(record.averageConfidence * 100)));
        holder.tvDate.setText(dateFormat.format(new Date(record.timestamp)));

        Glide.with(context)
                .load(record.imagePath)
                .centerCrop()
                .into(holder.ivThumbnail);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvCount;
        TextView tvConf;
        TextView tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvCount = itemView.findViewById(R.id.tv_count);
            tvConf = itemView.findViewById(R.id.tv_confidence);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
