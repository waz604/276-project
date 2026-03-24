package com.cmpt276.studbuds;

import java.util.ArrayList;
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

import com.cmpt276.studbuds.controllers.DeckController;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;

@WebMvcTest(DeckController.class)
public class DeckControllerTimeChallengeTest {

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

    @Test
    void getTimeChallenge_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/decks/1/challenge"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getTimeChallenge_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/decks/1/challenge").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getTimeChallenge_deckNotFound_redirectsToDecks() throws Exception {
        testUser.setDecks(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders.get("/decks/999/challenge").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks"));
    }
}