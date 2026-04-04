package com.cmpt276.studbuds.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;
    private String name;

    @Enumerated(EnumType.STRING)
    private roleType role;
    public enum roleType {USER, ADMIN};

    private String password;

    private LocalDate createdAt;

    private String googleID;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deck> decks = new ArrayList<>();

    public User() {}

    public User(String name, String password, String googleID) {
        this.name = name;
        this.password = password;
        this.googleID = googleID;
        this.createdAt = LocalDate.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
    
    public roleType getRole() {
        return role;
    }

    public void setRole(roleType role) {
        this.role = role;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public List<Deck> getDecks() {
        return this.decks;
    }

    public void setDecks(List<Deck> decks) {
        this.decks = decks;
    }

    public String getGoogleId() {
        return this.googleID;
    }

    public void setGoogleId(String googleID) {
        this.googleID = googleID;
    }
}
