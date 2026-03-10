package com.cmpt276.studbuds.models;

import jakarta.persistence.*;

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

    public User() { 

    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
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
}
