package com.cmpt276.studbuds.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "xp_logs")
public class XpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private LocalDate date;

    private int xpEarned;

    public XpLog() {}

    public XpLog(User user, LocalDate date, int xpEarned) {
        this.user = user;
        this.date = date;
        this.xpEarned = xpEarned;
    }

    public Long getId() { 
        return id; 
    }

    public User getUser() {
         return user; 
    } 
    public void setUser(User user) { 
        this.user = user; 
    }

    public LocalDate getDate() { 
        return date;
    }
    public void setDate(LocalDate date) { 
        this.date = date; 
    }

    public int getXpEarned() { 
        return xpEarned;
     }
    public void setXpEarned(int xpEarned) { 
        this.xpEarned = xpEarned; 
    }
}
