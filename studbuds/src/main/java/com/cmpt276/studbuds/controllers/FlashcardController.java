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
import com.cmpt276.studbuds.exceptions.NullFlashcardException;
import com.cmpt276.studbuds.exceptions.NullUserException;
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
    public String getStudyModePage(Model model, 
                                   HttpServletRequest request, 
                                   @PathVariable long id,
                                   RedirectAttributes redirectAttributes) 
    {
        User user = findUser(request);
        Deck deck = getDeck(user, id);
        
        List<FlashCard> flashcards = deck.getFlashcards();
        if(flashcards == null) throw new NullFlashcardException(deck.getId());

        int totalCards = flashcards.size();
        if(totalCards == 0) 
        {   
            String errorMsg = "You cannot study on an empty deck";
            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            return "redirect:/decks/{id}/cards";
        }

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

        User user = findUser(request);
        Deck deck = getDeck(user, id);
        
        FlashCard card = new FlashCard(question, answer, deck);
        deck.getFlashcards().add(card);

        userRepository.save(user);

        return "redirect:/decks/{id}/cards";
    }

    @PostMapping("/decks/{deckId}/cards/{cardId}/delete") 
    public String deleteCard(HttpServletRequest request, 
                             @PathVariable("deckId") long deckId,
                             @PathVariable("cardId") long cardId) 
    {
        User user = findUser(request);
        Deck deck = getDeck(user, deckId);
       
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

     
        User user = findUser(request);
        Deck deck = getDeck(user, deckId);
        FlashCard card = getFlashcard(deck, cardId);
        
        card.setQuestion(question);
        card.setAnswer(answer);
        userRepository.save(user);

        return "redirect:/decks/{deckId}/cards";
    } 
    
    // === Helper Methods === //
    private User findUser(HttpServletRequest request)  
    {
        Integer userId = (Integer) request.getSession()
                                          .getAttribute("userId");
        if(userId == null) throw new NullUserException("userId not found");

        User user = userRepository.findById(userId).orElse(null);
        if(user == null) throw new NullUserException("user not found");

        return user;
    }

    private Deck getDeck(User user, long deckId) 
    {
        Deck deck = user.getDecks().stream()
                .filter(d -> d.getId() == deckId)
                .findFirst()
                .orElse(null);
        if (deck == null) throw new NullDeckException("deck doesn't exist");

        return deck;
    }

    private FlashCard getFlashcard(Deck deck, long cardId)  
    {
        List<FlashCard> flashcards = deck.getFlashcards();
        if(flashcards == null) throw new NullFlashcardException(deck.getId());

        FlashCard card = flashcards.stream()
                                   .filter(f -> f.getId() == cardId)
                                   .findFirst()
                                   .orElse(null);
        if(card == null) throw new NullFlashcardException(deck.getId());

        return card;
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

    @ExceptionHandler(NullFlashcardException.class)
    public String nullCardHandler(NullFlashcardException e) {
        long deckId = e.getDeckId();
        return "redirect:/decks/" + deckId + "/cards";
    }  
}   
