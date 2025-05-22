package com.example.leitorqrcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ParticipantAuthActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private Button buttonGenerateQrCode;
    private TextView textViewEventName;
    private ImageView imageViewQrCode;
    private Button buttonExportPdf;
    private AttendanceDbHelper dbHelper;
    private long eventId;
    private static final String TAG = "ParticipantAuthActivity";
    private Button btnBack; // <-- MANTIDO: O botão "Voltar" customizado

    private Bitmap generatedQrCodeBitmap; // Armazena o bitmap gerado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_auth);

        // --- Habilitar o botão Voltar na ActionBar ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gerar QR Code do Participante"); // Título para esta tela
            Log.d(TAG, "onCreate: Botão Voltar da ActionBar habilitado.");
        } else {
            Log.w(TAG, "onCreate: getSupportActionBar() retornou nulo. Botão Voltar da ActionBar NÃO habilitado!");
        }
        // -------------------------------------------

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonGenerateQrCode = findViewById(R.id.buttonGenerateQrCode);
        textViewEventName = findViewById(R.id.textViewEventName);
        imageViewQrCode = findViewById(R.id.imageViewQrCode);
        buttonExportPdf = findViewById(R.id.buttonExportPdf);

        dbHelper = new AttendanceDbHelper(this);

        // RE-ADICIONADO E REFORÇADO: Inicializa e configura o botão "Voltar" customizado
        btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(view -> {
                Log.d(TAG, "Botão 'Voltar' customizado clicado. Finalizando Activity.");
                finish();
            });
            Log.d(TAG, "Botão 'Voltar' customizado encontrado e listener configurado.");
        } else {
            Log.w(TAG, "Botão 'Voltar' customizado (btnBack) NÃO encontrado no layout.");
        }


        // eventId virá da EventSelectionActivity
        eventId = getIntent().getLongExtra("eventId", -1); // Use -1 como default para indicar que precisa de um ID válido
        Log.d(TAG, "ParticipantAuthActivity: eventId recebido: " + eventId); // Adicionado para depuração
        if (eventId == -1) {
            Toast.makeText(this, "Erro: ID do evento não recebido. Retornando.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "ID do evento não recebido na ParticipantAuthActivity. Finalizando.");
            finish();
            return;
        }

        Event event = dbHelper.getEventById(eventId);
        if (event != null) {
            textViewEventName.setText("Evento: " + event.getEventName());
            Log.d(TAG, "Evento encontrado: " + event.getEventName() + " (ID: " + event.getId() + ")"); // Adicionado para depuração
        } else {
            textViewEventName.setText("Evento: Desconhecido (ID: " + eventId + ")");
            Toast.makeText(this, "Erro: Evento com ID " + eventId + " não encontrado.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Evento com ID " + eventId + " não encontrado no banco de dados."); // Adicionado para depuração
            // Considere desabilitar os botões de geração de QR Code e exportação se o evento não for encontrado
            buttonGenerateQrCode.setEnabled(false);
            buttonExportPdf.setEnabled(false);
        }


        buttonGenerateQrCode.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira nome e email.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uniqueQrCodeId = UUID.randomUUID().toString();
            Log.d(TAG, "Generated QR Code ID: " + uniqueQrCodeId);

            long participantId = dbHelper.addParticipant(name, email, eventId, uniqueQrCodeId);
            if (participantId != -1) {
                generatedQrCodeBitmap = generateQrCode(uniqueQrCodeId);
                if (generatedQrCodeBitmap != null) {
                    imageViewQrCode.setImageBitmap(generatedQrCodeBitmap);
                    imageViewQrCode.setVisibility(View.VISIBLE);
                    buttonGenerateQrCode.setEnabled(false);
                    buttonExportPdf.setEnabled(true);
                    Toast.makeText(this, "QR Code gerado e participante registrado!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Erro ao gerar QR Code.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Falha ao gerar o Bitmap do QR Code para o ID: " + uniqueQrCodeId);
                }
            } else {
                Toast.makeText(this, "Erro ao registrar participante.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha ao adicionar participante no banco de dados. Nome: " + name + ", Email: " + email + ", Event ID: " + eventId);
            }
        });

        buttonExportPdf.setOnClickListener(v -> exportQrCodeToPdf());
    }

    private Bitmap generateQrCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            Log.e(TAG, "Erro ao gerar QR Code", e);
            return null;
        }
    }

    private void exportQrCodeToPdf() {
        if (generatedQrCodeBitmap == null) {
            Toast.makeText(this, "Nenhum QR Code para exportar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String participantName = editTextName.getText().toString().trim();
        String fileNameSuffix = participantName.isEmpty() ? String.valueOf(System.currentTimeMillis()) : participantName.replaceAll("[^a-zA-Z0-9.-]", "_"); // Sanitize filename
        String pdfFileName = "QRCode_Participante_" + fileNameSuffix + ".pdf";

        File pdfFile = new File(getExternalFilesDir(null), pdfFileName);

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            generatedQrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image image = Image.getInstance(stream.toByteArray());

            // Ajusta o tamanho da imagem para caber na página
            float pageWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            float pageHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();

            if (image.getWidth() > pageWidth || image.getHeight() > pageHeight) {
                image.scaleToFit(pageWidth, pageHeight);
            }
            image.setAlignment(Image.ALIGN_CENTER);
            document.add(image);

            String name = (editTextName != null && editTextName.getText() != null) ? editTextName.getText().toString().trim() : "N/A";
            String email = (editTextEmail != null && editTextEmail.getText() != null) ? editTextEmail.getText().toString().trim() : "N/A";
            String eventName = (textViewEventName != null && textViewEventName.getText() != null) ?
                    textViewEventName.getText().toString().replace("Evento: ", "").trim() : "N/A";


            Paragraph details = new Paragraph("Nome: " + name +
                    "\nEmail: " + email +
                    "\nEvento: " + eventName);
            details.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(details);

            document.close();

            Toast.makeText(this, "PDF salvo em " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "PDF salvo em: " + pdfFile.getAbsolutePath());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(
                    this,
                    "com.example.leitorqrcode.fileprovider",
                    pdfFile
            );

            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir QR Code PDF"));

        } catch (IOException e) {
            Log.e(TAG, "Erro de I/O ao exportar PDF: ", e);
            Toast.makeText(this, "Erro de armazenamento ao exportar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Erro inesperado ao exportar PDF: ", e);
            Toast.makeText(this, "Erro geral ao exportar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- Método para lidar com o clique no botão Voltar da ActionBar (mantido) ---
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Botão Voltar da ActionBar clicado.");
            finish(); // Finaliza esta Activity e retorna à anterior na pilha
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