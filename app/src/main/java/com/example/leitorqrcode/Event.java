package com.example.leitorqrcode;

public class Event {
    private int id;
    private String eventName;
    private String description;
    private String eventDate;
    private long organizerId; // Foreign Key

    // Construtor vazio
    public Event() {}

    // Construtor com todos os campos
    public Event(int id, String eventName, String description, String eventDate, long organizerId) {
        this.id = id;
        this.eventName = eventName;
        this.description = description;
        this.eventDate = eventDate;
        this.organizerId = organizerId;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(long id) {
        this.id = (int) id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(long organizerId) {
        this.organizerId = organizerId;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", eventName='" + eventName + '\'' +
                ", description='" + description + '\'' +
                ", eventDate='" + eventDate + '\'' +
                ", organizerId=" + organizerId +
                '}';
    }
}
