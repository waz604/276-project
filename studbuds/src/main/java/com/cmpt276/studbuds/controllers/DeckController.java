package com.cmpt276.studbuds.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cmpt276.studbuds.exceptions.NullDeckException;
import com.cmpt276.studbuds.exceptions.NullUserException;
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
        
        User user = findUser(request);
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
        
        User user = findUser(request);
        List<Deck> decks = user.getDecks();
        if(decks == null) throw new NullDeckException("Deck collection not found");

        Deck deck = new Deck(name, user);
        decks.add(deck);

        userRepository.save(user);
        return "redirect:/decks";
    }

   
    @GetMapping("/decks/{id}/delete")
    public String deletePage(@PathVariable long id, 
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        
        User user = findUser(request);
        Deck deckToDelete = findDeck(user, id);
        if(deckToDelete == null) throw new NullDeckException("Deck  not found");

        redirectAttributes.addFlashAttribute("showPopup", true);
        redirectAttributes.addFlashAttribute("deleteDeck", deckToDelete);
        
        return "redirect:/decks";
    }

    @PostMapping("/decks/{id}/delete")
    public String deleteDeck(@PathVariable long id, HttpServletRequest request) {
        
        User user = findUser(request);
        
        List<Deck> decks = user.getDecks();
        if(decks == null) throw new NullDeckException("Deck collection not found");
        
        decks.removeIf(d -> d.getId() == id);
        userRepository.save(user);

        return "redirect:/decks";
    }


    @GetMapping("/decks/{id}/challenge")
    public String getTimeChallenge(Model model, @PathVariable long id, HttpServletRequest request) {

        User user = findUser(request);
        Deck deck = findDeck(user, id);
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
        
        User user = findUser(request);
        Deck deck = findDeck(user, id);

        model.addAttribute("deck", deck);

        return "flashcards";
    }

    @GetMapping("/decks/{id}/quiz")
    public String quizPage(Model model, HttpServletRequest request, @PathVariable long id)
    {   
        User user = findUser(request);
        Deck deck = findDeck(user, id);

        model.addAttribute("deck", deck);
        model.addAttribute("flashcards", deck.flashcardsJson());
        model.addAttribute("numCards", deck.getFlashcards().size());

        return "quiz";
    }
    
    
    // === Helper Methods === //
    private User findUser(HttpServletRequest request) {
        
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) throw new NullUserException("userId not found");
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) throw new NullUserException("user not found");

        return user;
    }

    private Deck findDeck(User user, long deckId) {
        
        Deck deck = user.getDecks().stream()
                        .filter(d -> d.getId() == deckId)
                        .findFirst()
                        .orElse(null);

        if (deck == null) throw new NullDeckException("Deck not found");

        return deck;
    }

    // === Exception Handling Methods === //
    @ExceptionHandler(NullUserException.class)
    public String nullUserHandler() {
        return "redirect:/login";
    }

    @ExceptionHandler(NullDeckException.class)
    public String nullDeckHandler() {
        return "redirect:/decks";
    }
}