package com.example.leitorqrcode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.AttendanceViewHolder> {

    private List<AttendanceRecord> attendanceList;

    public AttendanceListAdapter(List<AttendanceRecord> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false); // Usando um layout com dois TextViews
        return new AttendanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = attendanceList.get(position);
        holder.textViewParticipantName.setText(record.getParticipantName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(new Date(record.getTimestamp()));
        holder.textViewTimestamp.setText("Registrado em: " + formattedDate);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewParticipantName;
        public TextView textViewTimestamp;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewParticipantName = itemView.findViewById(android.R.id.text1);
            textViewTimestamp = itemView.findViewById(android.R.id.text2);
        }
    }
}