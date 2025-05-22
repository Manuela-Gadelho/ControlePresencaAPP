package com.example.leitorqrcode;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AttendanceCheckActivity extends AppCompatActivity {

    private TextView textViewWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_check);

        textViewWelcome = findViewById(R.id.textViewWelcome);

        long eventId = getIntent().getLongExtra("eventId", -1);
        long participantId = getIntent().getLongExtra("participantId", -1);

        AttendanceDbHelper dbHelper = new AttendanceDbHelper(this);
        Participant participant = dbHelper.getParticipantById(participantId);
        Event event = dbHelper.getEventById(eventId);

        if (participant != null && event != null) {
            textViewWelcome.setText("Bem-vindo, " + participant.getName() + " ao evento: " + event.getEventName());
        } else {
            textViewWelcome.setText("Bem-vindo ao evento!");
        }
    }
}