package com.cmpt276.studbuds.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;

import com.cmpt276.studbuds.models.Deck;
import com.cmpt276.studbuds.models.FlashCard;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DeckController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/decks")
    public String getAllDecks(Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("session_user");
        List<Deck> userDeckList = user.getDecks();

        model.addAttribute("decks", userDeckList);
        return "decks";
    }

    
    @GetMapping("/decks/add")
    public String addDeckPage() {
        return "decks/add";
    }
    
    @PostMapping("/decks/add")
    public String addDeck(HttpSession session, @RequestParam String name) {
        
        User user = (User) session.getAttribute("session_user");
        List<Deck> userDeckList = user.getDecks();
       
        Deck deck = new Deck(name);
        userDeckList.add(deck);

        user.setDecks(userDeckList);
        userRepository.save(user);
        return "redirect:/decks";
    }

    @GetMapping("/decks/{id}/delete")
    public String deletePage(@PathVariable long id, Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("session_user");
        List<Deck> userDeckList = user.getDecks();
        Deck deckToDelete = userDeckList.stream()
                                        .filter(d -> d.getId() == id)
                                        .collect(Collectors.toList())
                                        .get(0);
        model.addAttribute("deck", deckToDelete);

        return "decks/delete";
    }

    @PostMapping("/deck/{id}/delete")
    public String deleteDeck(@PathVariable long id) {
    
        User user = (User) session.getAttribute("session_user");
        List<Deck> userDeckList = user.getDecks();
        List<Deck> newDeckList = userDeckList.stream()
                                             .filter(d -> d.getId() != id)
                                             .collect(Collectors.toList());
        user.setDecks(newDeckList);
        userRepository.save(user);

        return "redirect:/decks";
    }


    @GetMapping("/deck/{id}")
    public String getDeck(Model model, @PathVariable long id, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        List<Deck> userDeckList = user.getDecks();
        Deck deck = userDeckList.stream()
                                .filter(d -> d.getId() == id)
                                .collect(Collectors.toList())
                                .get(0);
        List<FlashCard> flashcardList = deck.getFlashcards();
        model.addAttribute("flashcardList", flashcardList);

        return "flashcards";
    }
    
    
    
}