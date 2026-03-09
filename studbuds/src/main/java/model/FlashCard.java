package model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class FlashCard {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private String question;
    private String answer;

    private Deck deck;

    public FlashCard(){

    }

    public FlashCard(String question , String answer, Deck deck ){
        this.question = question;
        this.answer = answer;
        this.deck = deck;
    }

    public Long getId() {
    return id;
}

public String getQuestion() {
    return question;
}

public void setQuestion(String question) {
    this.question = question;
}

public String getAnswer() {
    return answer;
}

public void setAnswer(String answer) {
    this.answer = answer;
}

public Deck getDeck(){
    return deck;
}

public void setDeck(){
    this.deck = deck;
}


public void setId(){
    this.id = id;
}

}
