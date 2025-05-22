package com.example.leitorqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerMainActivity extends AppCompatActivity {

    private Button buttonCreateEvent;
    private Button buttonViewEvents;
    private Button buttonLogout;
    private AttendanceDbHelper dbHelper;

    private long organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main);

        dbHelper = new AttendanceDbHelper(this);

        buttonCreateEvent = findViewById(R.id.button_create_event);
        buttonViewEvents = findViewById(R.id.button_view_events);
        // buttonScanQrCode = findViewById(R.id.button_scan_qr_code); // Remover inicialização
        // buttonViewAttendanceList = findViewById(R.id.button_view_attendance_list); // Remover inicialização
        buttonLogout = findViewById(R.id.button_logout);

        organizerId = getIntent().getLongExtra("organizerId", -1); // Corrigir a chave para "organizerId"

        if (organizerId == -1) {
            Intent intent = new Intent(OrganizerMainActivity.this, OrganizerLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        buttonCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerMainActivity.this, CreateEventActivity.class);
                intent.putExtra("organizerId", organizerId); // Passar o ID do organizador
                startActivity(intent);
            }
        });

        buttonViewEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerMainActivity.this, OrganizerEventListActivity.class);
                intent.putExtra("organizerId", organizerId); // Passar o ID do organizador
                startActivity(intent);
            }
        });

        // REMOVER OS LISTENERS ABAIXO
        // buttonScanQrCode.setOnClickListener(new View.OnClickListener() { ... });
        // buttonViewAttendanceList.setOnClickListener(new View.OnClickListener() { ... });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerMainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

