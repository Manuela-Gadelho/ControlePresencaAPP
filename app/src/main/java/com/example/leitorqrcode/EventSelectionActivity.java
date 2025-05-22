package com.example.leitorqrcode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EventSelectionActivity extends AppCompatActivity {

    private static final String TAG = "EventSelectionActivity";
    private ListView listViewEvents;
    private AttendanceDbHelper dbHelper;
    private List<Event> eventList;
    private List<String> eventNames;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Seleção de Evento");
            Log.d(TAG, "onCreate: Botão Voltar da ActionBar e título 'Seleção de Evento' habilitados.");
        } else {
            Log.w(TAG, "onCreate: getSupportActionBar() retornou nulo. ActionBar pode não estar visível ou configurada.");
        }

        listViewEvents = findViewById(R.id.listViewEvents);
        dbHelper = new AttendanceDbHelper(this);

        eventNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventNames);
        listViewEvents.setAdapter(adapter);

        // --- INÍCIO: Listener de Clique NORMAL (PARA ABRIR ParticipantAuthActivity) ---
        listViewEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = eventList.get(position);
                if (selectedEvent != null) {
                    Log.d(TAG, "onItemClick: Evento clicado (normal). Preparando Intent para ParticipantAuthActivity.");
                    Intent intent = new Intent(EventSelectionActivity.this, ParticipantAuthActivity.class);
                    intent.putExtra("eventId", (long) selectedEvent.getId()); // Cast para long aqui

                    Log.d(TAG, "onItemClick: eventId a ser passado: " + selectedEvent.getId());
                    Log.d(TAG, "onItemClick: Chamando startActivity para ParticipantAuthActivity.");

                    startActivity(intent);

                    Log.d(TAG, "onItemClick: startActivity() foi chamado. Verifique o Logcat para ParticipantAuthActivity.");

                    // Log existente que você viu
                    Log.d(TAG, "Evento selecionado para visualização/escaneamento: " + selectedEvent.getEventName());
                } else {
                    Toast.makeText(EventSelectionActivity.this, "Erro: Evento não encontrado (clique normal).", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro: Evento selecionado na posição " + position + " é nulo (clique normal).");
                }
            }
        });
        // --- FIM: Listener de Clique NORMAL ---


        // --- INÍCIO: Listener de CLIQUE LONGO (PARA EXCLUIR EVENTO) ---
        listViewEvents.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Event eventToDelete = eventList.get(position);

                if (eventToDelete != null) {
                    Log.d(TAG, "onItemLongClick: Clique longo detectado para o evento: " + eventToDelete.getEventName());
                    new AlertDialog.Builder(EventSelectionActivity.this)
                            .setTitle("Excluir Evento")
                            .setMessage("Tem certeza que deseja excluir o evento '" + eventToDelete.getEventName() + "'? Esta ação também excluirá todos os participantes e presenças associados a ele.")
                            .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "Confirmação de exclusão. Tentando excluir evento ID: " + eventToDelete.getId());
                                    boolean deleted = dbHelper.deleteEvent(eventToDelete.getId());
                                    if (deleted) {
                                        Toast.makeText(EventSelectionActivity.this, "Evento excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Evento excluído: " + eventToDelete.getEventName() + " (ID: " + eventToDelete.getId() + ")");
                                        loadEvents(); // Recarrega a lista para refletir a exclusão
                                    } else {
                                        Toast.makeText(EventSelectionActivity.this, "Erro ao excluir evento.", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Falha ao excluir evento: " + eventToDelete.getEventName() + " (ID: " + eventToDelete.getId() + ")");
                                    }
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "Exclusão de evento cancelada.");
                                }
                            })
                            .show();
                } else {
                    Log.e(TAG, "Erro: Evento selecionado na posição " + position + " é nulo (clique longo).");
                }
                return true; // Retorna true para indicar que o clique longo foi consumido
            }
        });
        // --- FIM: Listener de CLIQUE LONGO ---
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(); // Recarrega os eventos toda vez que a Activity volta ao primeiro plano
        Log.d(TAG, "onResume: loadEvents() chamado.");
    }

    private void loadEvents() {
        eventList = dbHelper.getAllEvents(); // Obtém todos os eventos do banco de dados

        eventNames.clear(); // Limpa a lista de nomes antes de adicionar novamente
        if (eventList != null && !eventList.isEmpty()) {
            for (Event event : eventList) {
                eventNames.add(event.getEventName());
            }
            adapter.notifyDataSetChanged(); // Notifica o adapter que os dados mudaram
            Log.d(TAG, "loadEvents: " + eventList.size() + " eventos carregados e lista atualizada.");
        } else {
            Toast.makeText(this, "Nenhum evento disponível no momento.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "loadEvents: Nenhum evento encontrado no banco de dados.");
            adapter.notifyDataSetChanged(); // Notifica para limpar a lista se não houver eventos
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Botão Voltar da ActionBar clicado.");
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