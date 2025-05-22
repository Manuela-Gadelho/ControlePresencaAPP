package com.example.leitorqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // <--- Importação necessária para MenuItem
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerLoginActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerLogin";

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegisterLink;
    private AttendanceDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_login);

        // --- Adicionar o botão Voltar na ActionBar ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Opcional: Definir um título para a ActionBar, se não estiver definido no Manifest
            getSupportActionBar().setTitle("Login do Organizador");
            Log.d(TAG, "onCreate: Botão Voltar da ActionBar habilitado.");
        } else {
            Log.w(TAG, "onCreate: getSupportActionBar() retornou nulo. Botão Voltar da ActionBar NÃO habilitado!");
        }
        // ---------------------------------------------

        // Inicializa as views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);

        dbHelper = new AttendanceDbHelper(this);

        // Listener para o botão de login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                loginOrganizer(username, password);
            }
        });

        // Listener para o link de registro
        textViewRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerLoginActivity.this, OrganizerRegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginOrganizer(String username, String password) {
        Organizer organizer = dbHelper.getOrganizer(username);
        if (organizer != null) {
            if (organizer.getPassword().equals(password)) {
                Intent intent = new Intent(OrganizerLoginActivity.this, OrganizerMainActivity.class);
                intent.putExtra("organizerId", organizer.getId());
                startActivity(intent);
                finish();
                Log.d(TAG, "Login bem-sucedido para o organizador: " + username);
            } else {
                Toast.makeText(this, "Senha incorreta.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Tentativa de login com senha incorreta para o organizador: " + username);
            }
        } else {
            Toast.makeText(this, "Organizador não encontrado.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Organizador não encontrado: " + username);
        }
        // Removi o dbHelper.close() daqui, pois ele será fechado no onDestroy
        // para evitar fechar a conexão prematuramente se você tiver outras operações
        // de banco de dados após o login na mesma Activity.
    }

    // --- Método para lidar com o clique no botão Voltar da ActionBar ---
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Verifica se o item selecionado é o botão Home (ID padrão do botão Voltar da ActionBar)
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Botão Voltar da ActionBar clicado.");
            finish(); // Finaliza esta Activity e retorna à anterior na pilha
            return true; // Indica que o evento foi tratado
        }
        return super.onOptionsItemSelected(item); // Deixa que o Android lide com outros itens de menu
    }
    // ------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close(); // Fecha a conexão com o banco de dados
            Log.d(TAG, "onDestroy: DbHelper fechado.");
        }
    }
}