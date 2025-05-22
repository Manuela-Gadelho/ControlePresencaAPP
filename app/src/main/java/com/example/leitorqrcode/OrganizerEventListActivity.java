package com.example.leitorqrcode;

import android.content.Context; // Importação adicionada
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color; // Importação adicionada
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextThemeWrapper; // Importação adicionada

import java.util.List;

public class OrganizerEventListActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerEventList";

    private RecyclerView eventRecyclerView;
    private EventListAdapter eventListAdapter;
    private List<Event> eventList;
    private AttendanceDbHelper dbHelper;
    private Button buttonVoltar;
    private long organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_list);

        dbHelper = new AttendanceDbHelper(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Meus Eventos");
        }

        organizerId = getIntent().getLongExtra("organizerId", -1);

        if (organizerId == -1) {
            Toast.makeText(this, "Erro: ID do organizador não encontrado.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erro: organizerId não foi passado para OrganizerEventListActivity");
            finish();
            return;
        }

        buttonVoltar = findViewById(R.id.button_voltar_eventos_organizador);
        eventRecyclerView = findViewById(R.id.event_list_recycler_view);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        loadEvents();
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
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        eventList = dbHelper.getEventsByOrganizerId(organizerId);

        if (eventList == null) {
            eventList = new java.util.ArrayList<>();
            Log.w(TAG, "eventList retornado como nulo de dbHelper.getEventsByOrganizerId.");
        }

        if (eventList.isEmpty()) {
            Toast.makeText(this, "Nenhum evento encontrado para este organizador.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Nenhum evento encontrado para o organizador com ID: " + organizerId);
        }

        eventListAdapter = new EventListAdapter(eventList, new EventListAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrganizerEventListActivity.this);
                builder.setTitle("Opções do Evento: " + event.getEventName());
                builder.setItems(new CharSequence[]{"Escanear QR Code", "Ver Lista de Presença", "Excluir Evento"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) { // Escanear QR Code
                            Intent intent = new Intent(OrganizerEventListActivity.this, QrScannerActivity.class);
                            intent.putExtra("eventId", (long) event.getId());
                            Log.d(TAG, "Iniciando QrScannerActivity com eventId: " + event.getId());
                            startActivity(intent);
                        } else if (which == 1) { // Ver Lista de Presença
                            Intent intent = new Intent(OrganizerEventListActivity.this, AttendanceListActivity.class);
                            intent.putExtra("eventId", (long) event.getId());
                            Log.d(TAG, "Iniciando AttendanceListActivity com eventId: " + event.getId());
                            startActivity(intent);
                        } else if (which == 2) { // Excluir Evento
                            // Criando o AlertDialog com um tema específico para os botões
                            // Aqui, usamos um tema padrão do Android para Alert Dialogs que permite estilização dos botões
                            // Ou, mais robustamente, você poderia criar um estilo customizado em styles.xml
                            AlertDialog confirmationDialog = new AlertDialog.Builder(OrganizerEventListActivity.this)
                                    .setTitle("Confirmar Exclusão")
                                    .setMessage("Tem certeza que deseja excluir o evento '" + event.getEventName() + "'?\n\nATENÇÃO: Isso também excluirá todos os participantes e presenças associados a ele.")
                                    .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            boolean deleted = dbHelper.deleteEvent(event.getId());
                                            if (deleted) {
                                                Toast.makeText(OrganizerEventListActivity.this, "Evento excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "Evento excluído: " + event.getEventName() + " (ID: " + event.getId() + ")");
                                                loadEvents();
                                            } else {
                                                Toast.makeText(OrganizerEventListActivity.this, "Erro ao excluir evento.", Toast.LENGTH_SHORT).show();
                                                Log.e(TAG, "Falha ao excluir evento: " + event.getEventName() + " (ID: " + event.getId() + ")");
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Log.d(TAG, "Exclusão de evento cancelada.");
                                        }
                                    })
                                    .show(); // Chama show() para exibir o diálogo

                            // Após chamar show(), podemos acessar os botões e mudar a cor do texto
                            Button positiveButton = confirmationDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            if (positiveButton != null) {
                                positiveButton.setTextColor(getResources().getColor(R.color.red_error)); // Define a cor vermelha para "Excluir"
                            }
                            Button negativeButton = confirmationDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            if (negativeButton != null) {
                                negativeButton.setTextColor(getResources().getColor(R.color.gray_cancel)); // Define a cor cinza para "Cancelar"
                            }
                        }
                    }
                });
                builder.show();
            }
        });
        eventRecyclerView.setAdapter(eventListAdapter);
        eventListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}