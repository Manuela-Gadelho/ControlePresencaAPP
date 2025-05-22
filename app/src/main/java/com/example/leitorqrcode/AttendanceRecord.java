package com.example.leitorqrcode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AttendanceRecord {
    private String participantName;
    private String participantEmail;
    private long timestamp; // Mantendo como long para consistência com o banco
    private String checkInType; // "Entrada" ou "Saída"

    public AttendanceRecord(String participantName, String participantEmail, long timestamp, String checkInType) {
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.timestamp = timestamp;
        this.checkInType = checkInType;
    }

    // Getters
    public String getParticipantName() {
        return participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCheckInType() {
        return checkInType;
    }

    // Método para formatar o timestamp para uma string legível
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));
        // Opcional: Defina o fuso horário se precisar de um específico, caso contrário, usa o padrão do dispositivo
        // TimeZone tz = TimeZone.getTimeZone("America/Sao_Paulo"); // Exemplo de fuso horário
        // sdf.setTimeZone(tz);
        return sdf.format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return "AttendanceRecord{" +
                "participantName='" + participantName + '\'' +
                ", participantEmail='" + participantEmail + '\'' +
                ", timestamp=" + timestamp +
                ", checkInType='" + checkInType + '\'' +
                '}';
    }
}