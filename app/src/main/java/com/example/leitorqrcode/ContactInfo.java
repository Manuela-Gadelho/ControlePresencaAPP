package com.example.leitorqrcode;

import java.util.List;

public class ContactInfo {
    private String name;
    private String email;
    private List<String> phoneNumbers;

    public ContactInfo() {
        // Construtor padrão vazio
    }

    public ContactInfo(String name, String email, List<String> phoneNumbers) {
        this.name = name;
        this.email = email;
        this.phoneNumbers = phoneNumbers;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
