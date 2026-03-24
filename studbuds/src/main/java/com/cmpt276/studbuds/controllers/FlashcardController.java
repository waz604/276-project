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
import tools.jackson.databind.ObjectMapper;


@Controller
public class FlashcardController {
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/decks/{id}/study")
    public String getStudyModePage(Model model, HttpServletRequest request, @PathVariable long id) 
    {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/login";

        Deck deck = user.getDecks().stream()
                .filter(d -> d.getId() == id)
                .findFirst()
                .orElse(null);
        if (deck == null) return "redirect:/login";

        List<FlashCard> flashcards = deck.getFlashcards();
        if(flashcards == null) return "redirect:/decks/{id}/cards";

        int totalCards = flashcards.size();

        if(totalCards == 0) return "redirect:/decks/{id}/cards";

        // Serialize flashcard collection as JSON
        ObjectMapper mapper = new ObjectMapper();
        String cardsJson = mapper.writeValueAsString(flashcards);

        model.addAttribute("deck", deck);
        model.addAttribute("cards", cardsJson);
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

        FlashCard card = new FlashCard(question, answer, deck);
        List<FlashCard> flashcards = deck.getFlashcards();
        flashcards.add(card);

        userRepository.save(user);

        return "redirect:/decks/{id}/cards";
    }

    @PostMapping("/decks/{deckId}/cards/{cardId}/delete") 
    public String deleteCard(HttpServletRequest request, 
                             @PathVariable("deckId") long deckId,
                             @PathVariable("cardId") long cardId) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
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

        Integer userId = (Integer) request.getSession().getAttribute("userId");
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

        return "redirect:/decks/{deckId}/cards";
    } 
    

}   
