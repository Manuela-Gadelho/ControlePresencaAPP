package com.example.leitorqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import java.text.SimpleDateFormat; // Ainda necessário para formatar na UI, mas não no construtor
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AttendanceListActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceListActivity";

    private AttendanceDbHelper dbHelper;
    private long eventId;
    private ListView attendanceListView;
    private AttendanceRecordAdapter attendanceAdapter;
    private List<AttendanceRecord> attendanceRecordList;
    private TextView eventNameDisplayTextView;

    private static final int QR_SCANNER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Presenças");
            actionBar.setDisplayHomeAsUpEnabled(true);
            Log.d(TAG, "onCreate: Botão Voltar da ActionBar habilitado.");
        } else {
            Log.w(TAG, "onCreate: getSupportActionBar() retornou nulo. Botão Voltar da ActionBar NÃO habilitado!");
        }

        dbHelper = new AttendanceDbHelper(this);
        attendanceListView = findViewById(R.id.listViewAttendance);
        eventNameDisplayTextView = findViewById(R.id.event_name_display_textview);

        eventId = getIntent().getLongExtra("eventId", -1L); // Usar -1L para long
        Log.d(TAG, "onCreate: eventId recebido do Intent: " + eventId);

        if (eventId == -1L) { // Comparar com -1L
            Log.e(TAG, "onCreate: Nenhum eventId encontrado na Intent.");
            Toast.makeText(this, "Erro: ID do evento não fornecido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        attendanceRecordList = new ArrayList<>();
        attendanceAdapter = new AttendanceRecordAdapter(this, attendanceRecordList);
        attendanceListView.setAdapter(attendanceAdapter);

        findViewById(R.id.fabScanQr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QrScannerActivity.startQrScannerActivityForResult(AttendanceListActivity.this, eventId, QR_SCANNER_REQUEST_CODE);
            }
        });

        // loadAttendanceList será chamado em onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAttendanceList();
    }

    private void loadAttendanceList() {
        String eventName = dbHelper.getEventNameById(eventId);
        if (eventName != null) {
            if (eventNameDisplayTextView != null) {
                eventNameDisplayTextView.setText("Lista de Presença do Evento: " + eventName);
            }
        } else {
            if (eventNameDisplayTextView != null) {
                eventNameDisplayTextView.setText("Lista de Presença do Evento: [Nome do Evento]");
            }
            Log.w(TAG, "Nome do evento não encontrado para o ID: " + eventId);
        }

        List<AttendanceRecord> currentAttendance = dbHelper.getCheckInsForEvent(eventId);

        attendanceRecordList.clear();
        if (currentAttendance != null && !currentAttendance.isEmpty()) {
            attendanceRecordList.addAll(currentAttendance);
            Log.d(TAG, "Lista de presenças carregada para o evento ID: " + eventId + ". Registros encontrados: " + attendanceRecordList.size());
        } else {
            Toast.makeText(this, "Nenhuma presença registrada para este evento.", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Nenhuma presença registrada para o evento ID: " + eventId);
        }
        attendanceAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_SCANNER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String participantName = data.getStringExtra("participanteNome");
            String participantEmail = data.getStringExtra("participanteEmail");
            String checkInType = data.getStringExtra("tipoCheckIn");
            long timestampMillis = data.getLongExtra("timestamp", 0L); // Obter como long

            if (participantName != null && checkInType != null && timestampMillis != 0L) {
                // CORREÇÃO AQUI: Passe timestampMillis (long) diretamente para o construtor
                AttendanceRecord newRecord = new AttendanceRecord(participantName, participantEmail, timestampMillis, checkInType);
                attendanceRecordList.add(newRecord);
                attendanceAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Presença de " + participantName + " (" + checkInType + ") adicionada.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Dados de presença incompletos do scanner.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "onActivityResult: Dados incompletos do scanner.");
            }
        } else if (requestCode == QR_SCANNER_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Escaneamento de QR Code cancelado.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onActivityResult: Escaneamento cancelado.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "onDestroy: DbHelper fechado.");
        }
    }
}