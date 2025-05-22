package com.example.leitorqrcode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar; // Importe a classe ActionBar

public class MainActivity extends AppCompatActivity {

    private Button buttonOrganizerLogin, buttonParticipantAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- INÍCIO DA MODIFICAÇÃO PARA ESCONDER A ACTION BAR ---
        // Obtém uma referência à Action Bar
        ActionBar actionBar = getSupportActionBar();
        // Verifica se a Action Bar existe antes de tentar manipulá-la
        if (actionBar != null) {
            // Esconde a Action Bar para esta Activity
            actionBar.hide();
        }
        // --- FIM DA MODIFICAÇÃO ---

        setContentView(R.layout.activity_main);

        buttonOrganizerLogin = findViewById(R.id.btnOrganizerLogin);
        buttonParticipantAuth = findViewById(R.id.btnParticipantLogin);

        buttonOrganizerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OrganizerLoginActivity.class);
                startActivity(intent);
            }
        });

        buttonParticipantAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EventSelectionActivity.class);
                startActivity(intent);
            }
        });
    }
}