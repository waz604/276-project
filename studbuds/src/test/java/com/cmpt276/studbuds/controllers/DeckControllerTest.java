package com.cmpt276.studbuds.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cmpt276.studbuds.models.Deck;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;

@WebMvcTest(DeckController.class)
public class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "password");
        testUser.setUid(1);
        testUser.setDecks(new ArrayList<>());

        session = new MockHttpSession();
        session.setAttribute("userId", 1);

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
    }

    // for get

    @Test
    void getAllDecks_withSession_returnsDecksView() throws Exception {  
         
        testUser.setDecks(new ArrayList<>(List.of(new Deck("Math", testUser), new Deck("Science", testUser)))); 

        mockMvc.perform(MockMvcRequestBuilders.get("/decks").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())  
                .andExpect(MockMvcResultMatchers.view().name("decks"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("decks"));

    }

    @Test
    void getAllDecks_withEmptyDecks_returnsDecksView() throws Exception {  
        mockMvc.perform(MockMvcRequestBuilders.get("/decks").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("decks"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("decks"));
    }

    @Test
    void getAllDecks_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/decks"))  
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getAllDecks_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());


        mockMvc.perform(MockMvcRequestBuilders.get("/decks").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()) 
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    // for post
    @Test
    void addDeck_withSession_redirectsToDecks() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders.post("/decks/add")
                        .session(session)
                        .param("deckName", "History"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks"));

        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
    }

    @Test
    void addDeck_withoutSession_redirectsToLogin() throws Exception {  


        mockMvc.perform(MockMvcRequestBuilders.post("/decks/add")
                        .param("deckName", "History"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any()); 

    }

    @Test
    void addDeck_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/decks/add")
                        .session(session)
                        .param("deckName", "History"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    //for delete 
    @Test
    void deleteDeck_withSession_redirectsToDecks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/decks/999/delete").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks"));  
 
        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
    }

    @Test
    void deleteDeck_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/decks/1/delete"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
 
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void deleteDeck_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/decks/1/delete").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    

    @Test
    void getDeck_deckNotFound_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/decks/999/cards").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getDeck_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/decks/1/cards"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

  
    @Test
    void getDeck_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/decks/1/cards").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }
}
