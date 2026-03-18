package com.cmpt276.studbuds.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmpt276.studbuds.models.Deck;
import com.cmpt276.studbuds.models.FlashCard;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class FlashcardController {
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/decks/{id}/cards")
    public String addCard(Model model, HttpSession session, @PathVariable long id,
                         @RequestParam("question") String question,
                         @RequestParam("answer") String answer) 
    {
        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return "redirect:/login";

        List<Deck> userDeckList = user.getDecks();
        Deck deck = userDeckList.stream()
                                .filter(d -> d.getId() == id)
                                .findFirst()
                                .orElse(null);
        if(deck == null) return "redirect:/login";

        FlashCard card = new FlashCard(question, answer, deck);
        List<FlashCard> flashcards = deck.getFlashcards();
        flashcards.add(card);

        userRepository.save(user);

        return "redirect:/decks/{id}";
    }

    @PostMapping("/decks/{deckId}/cards/{cardId}/delete") 
    public String deleteCard(HttpSession session, 
                             @PathVariable("deckId") long deckId,
                             @PathVariable("cardId") long cardId) {
        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return "redirect:/login";

        List<Deck> userDeckList = user.getDecks();
        Deck deck = userDeckList.stream()
                                .filter(d -> d.getId() == deckId)
                                .findFirst()
                                .orElse(null);
        if(deck == null) return "redirect:/login";
        
        List<FlashCard> userFlashcards = deck.getFlashcards();
        userFlashcards.removeIf(f -> f.getId() == cardId);
        userRepository.save(user);

        return "redirect:/decks/{deckId}";                   
    }

    @PostMapping("/decks/{deckId}/cards/{cardId}/edit")
    public String editCard(HttpSession session, 
                           @PathVariable("deckId") long deckId,
                           @PathVariable("cardId") long cardId,
                           @RequestParam("question") String question,
                           @RequestParam("answer") String answer) 
    {
        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return "redirect:/login";

        List<Deck> userDeckList = user.getDecks();
        Deck deck = userDeckList.stream()
                                .filter(d -> d.getId() == deckId)
                                .findFirst()
                                .orElse(null);
        if(deck == null) return "redirect:/login";

        List<FlashCard> userFlashcards = deck.getFlashcards();
        FlashCard card = userFlashcards.stream()
                                       .filter(f -> f.getId() == cardId)
                                       .findFirst()
                                       .orElse(null);
        if(card == null) return "redirect:/login";

        card.setQuestion(question);
        card.setAnswer(answer);
        userRepository.save(user);

        return "redirect:/decks/{deckId}";
    } 
    

}   
