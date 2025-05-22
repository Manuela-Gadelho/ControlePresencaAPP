package com.example.leitorqrcode;

public class Participant {
    private long id;
    private String name;
    private String email;
    private long eventId; // Tipo ajustado para long
    private String qrCodeId;

    public Participant() {}

    public Participant(long id, String name, String email, long eventId, String qrCodeId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.eventId = eventId;
        this.qrCodeId = qrCodeId;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getEventId() { // Retorna long
        return eventId;
    }

    public void setEventId(long eventId) { // Recebe long
        this.eventId = eventId;
    }

    public String getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", eventId=" + eventId +
                ", qrCodeId='" + qrCodeId + '\'' +
                '}';
    }
}