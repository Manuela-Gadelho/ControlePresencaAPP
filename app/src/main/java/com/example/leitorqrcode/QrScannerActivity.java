package com.example.leitorqrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrScannerActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private TextureView textureView;
    private BarcodeScanner scanner;
    private AttendanceDbHelper dbHelper;
    private long lastScanTime = 0;
    private static final long SCAN_DELAY_MS = 2000;
    private long eventId; // Garantido como long
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private Surface mySurface;
    private SurfaceTexture mySurfaceTexture;
    private Executor cameraExecutor;

    private static final String TAG = "QrScannerActivity";
    public static final int QR_SCAN_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Escanear QR Code");
        }

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        dbHelper = new AttendanceDbHelper(this);

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        scanner = BarcodeScanning.getClient(options);

        // O eventId é crucial para a validação.
        eventId = getIntent().getLongExtra("eventId", -1L); // Use -1L para indicar um long
        if (eventId == -1L) { // Compare com -1L
            Toast.makeText(this, "Erro: ID do evento não fornecido.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ID do evento não recebido em QrScannerActivity.");
            finish();
            return;
        }
        Log.d(TAG, "QrScannerActivity iniciada para Event ID: " + eventId);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            if (textureView.isAvailable()) {
                startCamera();
            }
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                preview.setSurfaceProvider(new Preview.SurfaceProvider() {
                    @Override
                    public void onSurfaceRequested(@NonNull SurfaceRequest request) {
                        mySurfaceTexture = textureView.getSurfaceTexture();
                        if (mySurfaceTexture != null) {
                            mySurface = new Surface(mySurfaceTexture);
                            request.provideSurface(mySurface, cameraExecutor, result -> {
                                if (result.getResultCode() != SurfaceRequest.Result.RESULT_REQUEST_CANCELLED) {
                                    Log.d(TAG, "Surface provided successfully.");
                                } else {
                                    Log.w(TAG, "Surface request cancelled or failed: " + result.getResultCode());
                                }
                            });
                        } else {
                            Log.e(TAG, "SurfaceTexture is null. Cannot provide surface for preview.");
                            request.willNotProvideSurface();
                        }
                    }
                });

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        if (System.currentTimeMillis() - lastScanTime < SCAN_DELAY_MS) {
                            imageProxy.close();
                            return;
                        }

                        android.media.Image mediaImage = imageProxy.getImage();
                        if (mediaImage == null) {
                            imageProxy.close();
                            return;
                        }

                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                        scanner.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    for (Barcode barcode : barcodes) {
                                        String rawValue = barcode.getRawValue();
                                        if (rawValue != null) {
                                            if (isValidUUID(rawValue)) {
                                                registrarPresenca(rawValue);
                                                lastScanTime = System.currentTimeMillis();
                                                break;
                                            } else {
                                                handleOtherDataTypes(barcode);
                                                lastScanTime = System.currentTimeMillis();
                                                break;
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erro ao escanear QR Code: ", e);
                                })
                                .addOnCompleteListener(task -> {
                                    imageProxy.close();
                                });
                    }
                });

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erro ao iniciar a câmera: ", e);
                runOnUiThread(() -> Toast.makeText(this, "Erro ao iniciar a câmera.", Toast.LENGTH_SHORT).show());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean isValidUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void registrarPresenca(String participantQrCodeId) {
        Log.d(TAG, "Tentando registrar presença para QR Code: " + participantQrCodeId);
        Log.d(TAG, "Event ID atual na QrScannerActivity (da Intent): " + eventId);

        Participant participant = dbHelper.getParticipantByQrCode(participantQrCodeId);

        if (participant != null) {
            Log.d(TAG, "Participante encontrado no BD: " + participant.getName() + " (ID: " + participant.getId() + ")");
            Log.d(TAG, "Event ID do participante no BD: " + participant.getEventId());

            // AQUI ESTÁ A VALIDAÇÃO CRÍTICA
            if (participant.getEventId() != eventId) { // Ambos são long e vêm das fontes corretas
                runOnUiThread(() -> Toast.makeText(this, "QR Code inválido para este evento.", Toast.LENGTH_LONG).show());
                Log.w(TAG, "Mismatched Event IDs: QR Code's eventId (" + participant.getEventId() + ") != Current eventId (" + eventId + ")");
                return;
            }

            // Lógica de Entrada/Saída, só será executada se o eventId for válido
            String lastCheckInType = dbHelper.getLastCheckInType(participant.getId(), eventId);
            String newCheckInType;

            if (lastCheckInType == null || lastCheckInType.equals("Saída")) {
                newCheckInType = "Entrada";
            } else if (lastCheckInType.equals("Entrada")) {
                newCheckInType = "Saída";
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Estado de check-in inválido para " + participant.getName(), Toast.LENGTH_SHORT).show());
                Log.e(TAG, "Tipo de check-in inesperado: " + lastCheckInType + " para participante " + participant.getId());
                return;
            }

            long newCheckInId = dbHelper.addCheckInWithType(participant.getId(), eventId, newCheckInType);

            if (newCheckInId != -1) {
                runOnUiThread(() -> Toast.makeText(this, newCheckInType + " de " + participant.getName() + " registrada!", Toast.LENGTH_SHORT).show());
                Log.d(TAG, newCheckInType + " de " + participant.getName() + " (ID: " + participant.getId() + ") registrada para evento " + eventId);

                // Retorna o resultado para a Activity que chamou (EventSelectionActivity)
                Intent resultIntent = new Intent();
                resultIntent.putExtra("participanteNome", participant.getName());
                resultIntent.putExtra("participanteEmail", participant.getEmail());
                resultIntent.putExtra("tipoCheckIn", newCheckInType);
                resultIntent.putExtra("timestamp", System.currentTimeMillis());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Erro ao registrar " + newCheckInType + ".", Toast.LENGTH_SHORT).show());
                Log.e(TAG, "Falha ao registrar " + newCheckInType + " para participante " + participant.getId() + ", evento " + eventId);
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "QR Code não reconhecido ou participante não registrado.", Toast.LENGTH_LONG).show());
            Log.w(TAG, "Participante NÃO encontrado para QR Code: " + participantQrCodeId);
        }
    }

    private void handleOtherDataTypes(Barcode barcode) {
        runOnUiThread(() -> {
            switch (barcode.getValueType()) {
                case Barcode.TYPE_URL:
                    openUrl(barcode.getUrl().getUrl());
                    break;
                case Barcode.TYPE_CONTACT_INFO:
                    showContactDialog(barcode.getContactInfo());
                    break;
                case Barcode.TYPE_CALENDAR_EVENT:
                    showCalendarEventDialog(barcode.getCalendarEvent());
                    break;
                case Barcode.TYPE_WIFI:
                    showWifiDialog(barcode.getWifi());
                    break;
                default:
                    mostrarDialogoGenerico(barcode.getRawValue());
            }
        });
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir URL: ", e);
            Toast.makeText(this, "Erro ao abrir URL.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showContactDialog(Barcode.ContactInfo contactInfo) {
        if (contactInfo != null) {
            StringBuilder contactDetails = new StringBuilder();

            if (contactInfo.getName() != null) {
                contactDetails.append("Nome: ").append(contactInfo.getName().getFormattedName()).append("\n");
            }

            if (contactInfo.getPhones() != null) {
                contactDetails.append("Telefones:\n");
                for (Barcode.Phone phone : contactInfo.getPhones()) {
                    contactDetails.append("  Tipo: ").append(getPhoneTypeString(phone.getType()))
                            .append(", Número: ").append(phone.getNumber()).append("\n");
                }
            }

            if (contactInfo.getEmails() != null) {
                contactDetails.append("Emails:\n");
                for (Barcode.Email email : contactInfo.getEmails()) {
                    contactDetails.append("  Tipo: ").append(getEmailTypeString(email.getType()))
                            .append(", Endereço: ").append(email.getAddress()).append("\n");
                }
            }

            new AlertDialog.Builder(this)
                    .setTitle("Informações de Contato")
                    .setMessage(contactDetails.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private String getPhoneTypeString(int type) {
        switch (type) {
            case Barcode.Phone.TYPE_HOME:
                return "Residencial";
            case Barcode.Phone.TYPE_WORK:
                return "Trabalho";
            case Barcode.Phone.TYPE_MOBILE:
                return "Celular";
            default:
                return "Outro";
        }
    }

    private String getEmailTypeString(int type) {
        switch (type) {
            case Barcode.Email.TYPE_HOME:
                return "Residencial";
            case Barcode.Email.TYPE_WORK:
                return "Trabalho";
            default:
                return "Outro";
        }
    }

    private void showCalendarEventDialog(Barcode.CalendarEvent event) {
        if (event != null) {
            StringBuilder eventDetails = new StringBuilder();
            eventDetails.append("Evento: ").append(event.getSummary()).append("\n")
                    .append("Descrição: ").append(event.getDescription()).append("\n");
            eventDetails.append("Início: ").append(event.getStart() != null ? event.getStart().getDay() : "N/A").append("\n")
                    .append("Fim: ").append(event.getEnd() != null ? event.getEnd().getDay() : "N/A");

            new AlertDialog.Builder(this)
                    .setTitle("Evento de Calendário")
                    .setMessage(eventDetails.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void showWifiDialog(Barcode.WiFi wifi) {
        if (wifi != null) {
            String wifiInfo = "SSID: " + wifi.getSsid() + "\n" +
                    "Senha: " + wifi.getPassword() + "\n" +
                    "Tipo de Criptografia: " + wifi.getEncryptionType();

            new AlertDialog.Builder(this)
                    .setTitle("Informações de Wi-Fi")
                    .setMessage(wifiInfo)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void mostrarDialogoGenerico(String valor) {
        new AlertDialog.Builder(this)
                .setTitle("Conteúdo do QR Code")
                .setMessage(valor)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        if (mySurfaceTexture == null) {
            mySurfaceTexture = surface;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        if (mySurface != null) {
            mySurface.release();
            mySurface = null;
        }
        mySurfaceTexture = null;
        Log.d(TAG, "onSurfaceTextureDestroyed: Camera resources released.");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (textureView.isAvailable()) {
                    startCamera();
                }
            } else {
                Toast.makeText(this, "Permissão da câmera necessária para escanear QR Codes.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable() && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.close();
            Log.d(TAG, "onDestroy: BarcodeScanner fechado.");
        }
        if (cameraExecutor instanceof ExecutorService) {
            ((ExecutorService) cameraExecutor).shutdown();
            Log.d(TAG, "onDestroy: CameraExecutor desligado.");
        }
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "onDestroy: DbHelper fechado.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void startQrScannerActivityForResult(AppCompatActivity activity, long eventId, int requestCode) {
        Intent intent = new Intent(activity, QrScannerActivity.class);
        intent.putExtra("eventId", eventId);
        activity.startActivityForResult(intent, requestCode);
    }
}