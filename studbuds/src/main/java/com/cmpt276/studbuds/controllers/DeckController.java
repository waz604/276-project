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
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DeckController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/decks")
    public String getAllDecks(Model model, HttpServletRequest request) {
        
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) return "redirect:/login";

        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return "redirect:/login";

        List<Deck> userDeckList = user.getDecks();

        model.addAttribute("decks", userDeckList);
        return "decks";
    }

    // Not needed for iteration 1
    /* @GetMapping("/decks/add")
    public String addDeckPage() {
        return "decks/add";
    } */
    
    @PostMapping("/decks/add")
    public String addDeck(HttpServletRequest request, @RequestParam("deckName") String name) {
        
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) return "redirect:/login";
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return "redirect:/login";

        List<Deck> userDeckList = user.getDecks();
       
        Deck deck = new Deck(name, user);
        userDeckList.add(deck);

        user.setDecks(userDeckList);
        userRepository.save(user);
        return "redirect:/decks";
    }

    // not needed for iteration 1
    /*
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
    */

    @PostMapping("/decks/{id}/delete")
    public String deleteDeck(@PathVariable long id, HttpServletRequest request) {
    
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) return "redirect:/login";
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return "redirect:/login";

        user.getDecks().removeIf(d -> d.getId() == id);
        userRepository.save(user);

        return "redirect:/decks";
    }


    @GetMapping("/decks/{id}/challenge")
    public String getTimeChallenge(Model model, @PathVariable long id, HttpServletRequest request) {

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/login";

        Deck deck = user.getDecks().stream()
                        .filter(d -> d.getId() == id)
                        .findFirst()
                        .orElse(null);

        if (deck == null) return "redirect:/decks";

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
        
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) return "redirect:/login";
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) return "redirect:/login";

        List<Deck> userDeckList = user.getDecks();
        Deck deck = userDeckList.stream()
                                .filter(d -> d.getId() == id)
                                .findFirst()
                                .orElse(null);
        if(deck == null) return "redirect:/login";

        model.addAttribute("deck", deck);

        return "flashcards";
    }
    
}