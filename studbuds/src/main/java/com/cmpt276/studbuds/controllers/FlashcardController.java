package com.cmpt276.studbuds.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmpt276.studbuds.models.Deck;
import com.cmpt276.studbuds.models.FlashCard;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;

import jakarta.servlet.http.HttpServletRequest;


@Controller
public class FlashcardController {
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/decks/{id}/study")
    public String getStudyModePage(Model model, HttpServletRequest request, @PathVariable long id) 
    {
        User user = null;
        Deck deck = null;

        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        try {
            deck = getDeck(user, id);
        } catch(NullPointerException e) {
            return "redirect:/decks";
        }

        List<FlashCard> flashcards = deck.getFlashcards();
        if(flashcards == null) return "redirect:/decks/{id}/cards";

        int totalCards = flashcards.size();
        if(totalCards == 0) return "redirect:/decks/{id}/cards";

        model.addAttribute("deck", deck);
        model.addAttribute("cards", deck.flashcardsJson());
        model.addAttribute("totalCards", flashcards.size());
        
        return "study";
    }
    
    @PostMapping("/decks/{id}/cards")
    public String addCard(HttpServletRequest request, @PathVariable long id,
                         @RequestParam("question") String question,
                         @RequestParam("answer") String answer) 
    {
        if(question.isBlank()) return "redirect:/decks/{id}/cards";
        if(answer.isBlank()) return "redirect:/decks/{id}/cards";

        User user = null;
        Deck deck = null;

        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        try {
            deck = getDeck(user, id);
        } catch(NullPointerException e) {
            return "redirect:/decks";
        }

        FlashCard card = new FlashCard(question, answer, deck);
        deck.getFlashcards().add(card);

        userRepository.save(user);

        return "redirect:/decks/{id}/cards";
    }

    @PostMapping("/decks/{deckId}/cards/{cardId}/delete") 
    public String deleteCard(HttpServletRequest request, 
                             @PathVariable("deckId") long deckId,
                             @PathVariable("cardId") long cardId) {
        User user = null;
        Deck deck = null;
                        
        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }
        
        try {
            deck = getDeck(user, deckId);
        } catch(NullPointerException e) {
            return "redirect:/decks";
        }

        deck.getFlashcards()
            .removeIf(card -> card.getId() == cardId);
    
        userRepository.save(user);

        return "redirect:/decks/{deckId}/cards";                   
    }

    @PostMapping("/decks/{deckId}/cards/{cardId}/edit")
    public String editCard(HttpServletRequest request, 
                           @PathVariable("deckId") long deckId,
                           @PathVariable("cardId") long cardId,
                           @RequestParam("question") String question,
                           @RequestParam("answer") String answer) 
    {
        if(question.isBlank()) return "redirect:/decks/{deckId}/cards";
        if(answer.isBlank()) return "redirect:/decks/{deckId}/cards";

        User user = null;
        Deck deck = null;
        FlashCard card = null;

        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        try {
            deck = getDeck(user, deckId);
        } catch(NullPointerException e) {
            return "redirect:/decks";
        }

        try {
            card = getFlashcard(deck, cardId);
        } catch(NullPointerException e) {
            return "redirect:/decks/{deckId}/cards";
        }

        card.setQuestion(question);
        card.setAnswer(answer);
        userRepository.save(user);

        return "redirect:/decks/{deckId}/cards";
    } 
    
    // === Helper Methods === //
    private User findUser(HttpServletRequest request) throws NullPointerException {
        Integer userId = (Integer) request.getSession()
                                          .getAttribute("userId");
        if(userId == null) throw new NullPointerException();

        User user = userRepository.findById(userId).orElse(null);
        if(user == null) throw new NullPointerException();

        return user;
    }

    private Deck getDeck(User user, long deckId) throws NullPointerException {

        Deck deck = user.getDecks().stream()
                .filter(d -> d.getId() == deckId)
                .findFirst()
                .orElse(null);
        if (deck == null) throw new NullPointerException();

        return deck;
    }

    private FlashCard getFlashcard(Deck deck, long cardId) throws NullPointerException {

        List<FlashCard> flashcards = deck.getFlashcards();
        if(flashcards == null) throw new NullPointerException();

        FlashCard card = flashcards.stream()
                                       .filter(f -> f.getId() == cardId)
                                       .findFirst()
                                       .orElse(null);
        if(card == null) throw new NullPointerException();

        return card;
    }

}   
