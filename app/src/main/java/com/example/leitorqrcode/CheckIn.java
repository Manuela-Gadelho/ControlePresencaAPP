package com.example.leitorqrcode;

public class CheckIn {
    private long id;
    private long participantId;
    private long eventId;
    private long timestamp; // Armazena timestamp em milissegundos
    private String type; // "Entrada" ou "Saída"

    public CheckIn() {}

    public CheckIn(long id, long participantId, long eventId, long timestamp, String type) {
        this.id = id;
        this.participantId = participantId;
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "id=" + id +
                ", participantId=" + participantId +
                ", eventId=" + eventId +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                '}';
    }
}