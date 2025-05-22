package com.example.leitorqrcode;

public class Organizer {
    private long id;
    private String username;
    private String password;
    private String email;

    // Construtor vazio
    public Organizer() {}

    // Construtor com todos os campos
    public Organizer(long id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Organizer{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' + // Em um sistema real, evite logar senhas!
                ", email='" + email + '\'' +
                '}';
    }
}
