package com.cmpt276.studbuds.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cmpt276.studbuds.models.Deck;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DeckController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/decks")
    public String getAllDecks(Model model, HttpServletRequest request) {
        
        User user = null;

        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        List<Deck> decks = user.getDecks();

        model.addAttribute("decks", decks);
        return "decks";
    }

    // Not needed for iteration 1
    /* @GetMapping("/decks/add")
    public String addDeckPage() {
        return "decks/add";
    } */
    
    @PostMapping("/decks/add")
    public String addDeck(HttpServletRequest request, @RequestParam("deckName") String name) {
        
        User user = null;

        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        List<Deck> decks = user.getDecks();

        Deck deck = new Deck(name, user);
        decks.add(deck);

        userRepository.save(user);
        return "redirect:/decks";
    }

   
    @GetMapping("/decks/{id}/delete")
    public String deletePage(@PathVariable long id, 
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        
        User user = null;
        Deck deckToDelete = null;

        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        deckToDelete = findDeck(user, id);

        
        redirectAttributes.addFlashAttribute("showPopup", true);
        redirectAttributes.addFlashAttribute("deleteDeck", deckToDelete);
        
        return "redirect:/decks";
    }

    @PostMapping("/decks/{id}/delete")
    public String deleteDeck(@PathVariable long id, HttpServletRequest request) {
    
        User user = null;
        
        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        user.getDecks().removeIf(d -> d.getId() == id);
        userRepository.save(user);

        return "redirect:/decks";
    }


    @GetMapping("/decks/{id}/challenge")
    public String getTimeChallenge(Model model, @PathVariable long id, HttpServletRequest request) {

        User user = null;
        Deck deck = null;

        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        try {
            deck = findDeck(user, id);
        } catch(NullPointerException e) {
            return "redirect:/decks";
        }

        model.addAttribute("deck", deck);

        List<String> questions = new java.util.ArrayList<>();
        List<String> answers = new java.util.ArrayList<>();

        if (deck.getFlashcards() != null) {
            deck.getFlashcards().forEach(card -> {
                questions.add(card.getQuestion());
                answers.add(card.getAnswer());
            });
        }

        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        model.addAttribute("totalCards", questions.size());

        return "timeChallenge";
    }


    @GetMapping("/decks/{id}/cards")
    public String getDeck(Model model, @PathVariable long id, HttpServletRequest request) {
        
        User user = null;
        Deck deck = null;
        try {
            user = findUser(request);
        } catch(NullPointerException e) {
            return "redirect:/login";
        }

        try {
            deck = findDeck(user, id);
        } catch(NullPointerException e) {
            return "redirect:/decks";
        }

        model.addAttribute("deck", deck);

        return "flashcards";
    }
    
    // === Helper Methods === //
    private User findUser(HttpServletRequest request) throws NullPointerException {
        
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) throw new NullPointerException("userId not found");
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) throw new NullPointerException("user not found");

        return user;
    }

    private Deck findDeck(User user, long deckId) throws NullPointerException {
        
        Deck deck = user.getDecks().stream()
                        .filter(d -> d.getId() == deckId)
                        .findFirst()
                        .orElse(null);

        if (deck == null) throw new NullPointerException("Deck not found");

        return deck;
    }
}