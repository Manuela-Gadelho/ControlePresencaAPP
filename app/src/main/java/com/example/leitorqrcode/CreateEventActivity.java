package com.example.leitorqrcode;

import android.os.Bundle;
import android.text.Editable; // Importar Editable
import android.text.TextWatcher; // Importar TextWatcher
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateEventActivity extends AppCompatActivity {

    private EditText editEventName, editEventDate;
    private Button buttonCreateEvent, buttonVoltar;
    private AttendanceDbHelper dbHelper;
    private long organizerId;

    // Variável para controlar se a formatação está em andamento para evitar loops infinitos
    private boolean isUpdatingDate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Opcional: Definir o título da Action Bar para esta Activity
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Criar Evento");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar
        }

        dbHelper = new AttendanceDbHelper(this);
        organizerId = getIntent().getLongExtra("organizerId", -1);

        editEventName = findViewById(R.id.edit_event_name);
        editEventDate = findViewById(R.id.edit_event_date);
        buttonCreateEvent = findViewById(R.id.button_create_event);
        buttonVoltar = findViewById(R.id.button_voltar_criar_evento);

        // --- INÍCIO DA MODIFICAÇÃO: Adicionar TextWatcher para formatar a data ---
        editEventDate.addTextChangedListener(new TextWatcher() {
            private String current = ""; // Armazena o texto atual para comparação

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não precisamos de lógica aqui para esta implementação
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Não precisamos de lógica aqui para esta implementação
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingDate) { // Evita loop infinito
                    return;
                }

                String text = s.toString();
                if (text.equals(current)) { // Se o texto não mudou (ex: por causa da nossa própria formatação)
                    return;
                }

                isUpdatingDate = true; // Indica que estamos atualizando o texto

                String cleanText = text.replace("/", ""); // Remove todas as barras
                StringBuilder formattedText = new StringBuilder();

                if (cleanText.length() > 0) {
                    // Limita o tamanho máximo para 8 dígitos (DDMMYYYY)
                    if (cleanText.length() > 8) {
                        cleanText = cleanText.substring(0, 8);
                    }

                    // Adiciona as barras automaticamente
                    for (int i = 0; i < cleanText.length(); i++) {
                        formattedText.append(cleanText.charAt(i));
                        if (i == 1 || i == 3) { // Após o 2º dígito (dia) e 4º dígito (mês)
                            if (i < cleanText.length() - 1) { // Garante que não adiciona barra no final se já completou
                                formattedText.append("/");
                            }
                        }
                    }
                }

                current = formattedText.toString(); // Atualiza o texto atual
                editEventDate.setText(current); // Define o texto formatado
                editEventDate.setSelection(current.length()); // Move o cursor para o final

                isUpdatingDate = false; // Permite novas atualizações
            }
        });
        // --- FIM DA MODIFICAÇÃO ---


        buttonCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = editEventName.getText().toString().trim();
                String eventDate = editEventDate.getText().toString().trim();

                if (eventName.isEmpty() || eventDate.isEmpty()) {
                    Toast.makeText(CreateEventActivity.this, "Por favor, insira nome e data do evento", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Opcional: Adicionar validação de formato de data aqui, se a data não estiver no formato DD/MM/AAAA
                // Exemplo básico de validação de comprimento:
                if (eventDate.length() != 10 || !eventDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    Toast.makeText(CreateEventActivity.this, "Formato de data inválido. Use DD/MM/AAAA.", Toast.LENGTH_SHORT).show();
                    return;
                }

                long eventId = dbHelper.addEvent(eventName, eventDate, organizerId);
                if (eventId != -1) {
                    Toast.makeText(CreateEventActivity.this, "Evento criado com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateEventActivity.this, "Erro ao criar evento", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Para lidar com o botão de voltar da Action Bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Simula o comportamento do botão voltar
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}