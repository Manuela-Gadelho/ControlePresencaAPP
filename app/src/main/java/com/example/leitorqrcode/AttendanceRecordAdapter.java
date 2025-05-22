package com.example.leitorqrcode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AttendanceRecordAdapter extends ArrayAdapter<AttendanceRecord> {

    private static final String TAG = "AttendanceRecordAdapter";

    public AttendanceRecordAdapter(Context context, List<AttendanceRecord> records) {
        super(context, 0, records);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obter o registro de presença para esta posição
        AttendanceRecord currentRecord = getItem(position);

        // Inflar o layout se convertView for nulo
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_attendance, parent, false);
        }

        // Obter as referências dos TextViews do layout
        TextView participantNameTextView = convertView.findViewById(R.id.textViewParticipantName);
        TextView participantEmailTextView = convertView.findViewById(R.id.textViewParticipantEmail);
        TextView checkinTimeTextView = convertView.findViewById(R.id.textViewCheckinTime);
        TextView checkinTypeTextView = convertView.findViewById(R.id.textViewCheckinType);

        // Preencher os TextViews com os dados do AttendanceRecord
        if (currentRecord != null) {
            participantNameTextView.setText(currentRecord.getParticipantName());
            participantEmailTextView.setText(currentRecord.getParticipantEmail());
            // CORREÇÃO AQUI: Use o método getFormattedTimestamp() para obter a String formatada
            checkinTimeTextView.setText(currentRecord.getFormattedTimestamp());
            checkinTypeTextView.setText(currentRecord.getCheckInType());

            // Opcional: Mudar a cor do texto do tipo de check-in
            if ("Entrada".equals(currentRecord.getCheckInType())) {
                checkinTypeTextView.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else if ("Saída".equals(currentRecord.getCheckInType())) {
                checkinTypeTextView.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
            } else {
                checkinTypeTextView.setTextColor(getContext().getResources().getColor(android.R.color.black));
            }
        }

        return convertView;
    }
}