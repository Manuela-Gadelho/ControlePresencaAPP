package com.example.leitorqrcode;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OrganizerRegisterActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerRegister";

    private EditText editTextOrganizerUsername, editTextOrganizerPassword, editTextOrganizerEmail, editTextEventName;
    private Button buttonOrganizerRegister;
    private AttendanceDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registro de Organizador");
            Log.d(TAG, "onCreate: Botão Voltar da ActionBar habilitado.");
        } else {
            Log.w(TAG, "onCreate: getSupportActionBar() retornou nulo. Botão Voltar da ActionBar NÃO habilitado!");
        }

        try {
            editTextOrganizerUsername = findViewById(R.id.editTextUsername);
            editTextOrganizerPassword = findViewById(R.id.editTextPassword);
            editTextOrganizerEmail = findViewById(R.id.organizer_email_edit_text);
            editTextEventName = findViewById(R.id.editTextEventName);
            buttonOrganizerRegister = findViewById(R.id.organizer_register_button);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar elementos da UI: ", e); // Usar Log.e para erros
            Toast.makeText(this, "Erro ao inicializar elementos da UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        dbHelper = new AttendanceDbHelper(this);

        buttonOrganizerRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextOrganizerUsername.getText().toString().trim();
                String password = editTextOrganizerPassword.getText().toString().trim();
                String email = editTextOrganizerEmail.getText().toString().trim();
                String eventName = editTextEventName.getText().toString().trim(); // Pode ser vazio

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(OrganizerRegisterActivity.this, "Por favor, preencha os campos obrigatórios (usuário, senha, email).", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbHelper.checkOrganizerExists(username)) {
                    Toast.makeText(OrganizerRegisterActivity.this, "Nome de usuário já cadastrado.", Toast.LENGTH_SHORT).show();
                    return;
                }

                long organizerId = -1;
                if (!eventName.isEmpty()) {
                    organizerId = dbHelper.insertOrganizerWithEvent(username, password, email, eventName, "Descrição Padrão", "Data Padrão");
                } else {
                    organizerId = dbHelper.addOrganizer(username, password, email);
                }


                if (organizerId != -1) {
                    Toast.makeText(OrganizerRegisterActivity.this, "Organizador e evento (se informado) cadastrados com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrganizerRegisterActivity.this, OrganizerMainActivity.class);
                    intent.putExtra("organizerId", organizerId);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "Organizador registrado com sucesso! ID: " + organizerId);
                } else {
                    Toast.makeText(OrganizerRegisterActivity.this, "Erro ao cadastrar organizador ou evento.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao cadastrar organizador ou evento.");
                }
            }
        });
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