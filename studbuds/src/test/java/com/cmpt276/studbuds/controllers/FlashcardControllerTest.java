package com.cmpt276.studbuds.controllers;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cmpt276.studbuds.models.Deck;
import com.cmpt276.studbuds.models.FlashCard;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.XpLogRepository;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FlashcardController.class)
public class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private XpLogRepository xpLogRepository;

    private User testUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "password", "test@gmail.com");
        testUser.setUid(1);
        Deck testDeck = new Deck();
        testDeck.setId(1);
        testDeck.setName("testDeck");
        testUser.getDecks().add(testDeck);
        FlashCard testFlashcard = new FlashCard("tq", "ts", testDeck);
        testFlashcard.setId(1);
        testUser.getDecks().get(0).getFlashcards().add(testFlashcard);
        

        session = new MockHttpSession();
        session.setAttribute("userId", 1);

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        Mockito.when(xpLogRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
    }

    // Normal Operations Test
    @Test
    void getStudyModePage_withSession() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(testUser.getDecks().get(0).getFlashcards());
        int totalFlashcards = testUser.getDecks().get(0).getFlashcards().size();

        mockMvc.perform(MockMvcRequestBuilders.get("/decks/1/study")
               .session(session))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.view().name("study"))
               .andExpect(MockMvcResultMatchers.model().attributeExists("deck"))
               .andExpect(MockMvcResultMatchers.model().attributeExists("cards"))
               .andExpect(MockMvcResultMatchers.model().attributeExists("totalCards"))
               .andExpect(MockMvcResultMatchers.model().attribute("deck", testUser.getDecks().get(0)))
               .andExpect(MockMvcResultMatchers.model().attribute("cards", json))
               .andExpect(MockMvcResultMatchers.model().attribute("totalCards", totalFlashcards));
    }

    @Test
    void addCard_withSession() throws Exception {
        int before = testUser.getDecks().get(0).getFlashcards().size();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/decks/1/cards")
                .session(session)
                .param("question", "test question")
                .param("answer", "test answer"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"));

        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);

        int after = testUser.getDecks().get(0).getFlashcards().size();
        assertEquals(before + 1, after);
    }

    @Test
    void deleteCard_withSession() throws Exception {
        int before = testUser.getDecks().get(0).getFlashcards().size();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/decks/1/cards/1/delete")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"));

        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);

        int after = testUser.getDecks().get(0).getFlashcards().size();
        assertEquals(before - 1, after);
    }

    @Test
    void editCard_withSession() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
               .post("/decks/1/cards/1/edit")
               .session(session)
               .param("question", "Updated question")
               .param("answer", "Updated answer"))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"));
            
        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);

        // assertion for flashcard content being updated
        FlashCard card = testUser.getDecks().get(0).getFlashcards().get(0);
        assertEquals("Updated question", card.getQuestion());
        assertEquals("Updated answer", card.getAnswer());

    }

    // Missing Parameter Tests
    @Test
    void addCard_BlankQuestion() throws Exception {

        int before = testUser.getDecks().get(0).getFlashcards().size();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/decks/1/cards")
                .session(session)
                .param("question", "")
                .param("answer", "answer"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"));

        Mockito.verify(userRepository, Mockito.never()).save(testUser);
        int after = testUser.getDecks().get(0).getFlashcards().size();
        assertEquals(before, after);
    }

    @Test
    void addCard_BlankAnswer() throws Exception {

        int before = testUser.getDecks().get(0).getFlashcards().size();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/decks/1/cards")
                .session(session)
                .param("question", "question")
                .param("answer", ""))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"));

        Mockito.verify(userRepository, Mockito.never()).save(testUser);
        int after = testUser.getDecks().get(0).getFlashcards().size();
        assertEquals(before, after);
    }

    @Test
    void editCard_BlankQuestion() throws Exception {

        String before = testUser.getDecks()
                                .get(0)
                                .getFlashcards()
                                .get(0)
                                .getQuestion();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/decks/1/cards/1/edit")
                .session(session)
                .param("question", "")
                .param("answer", "answer"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"));

        Mockito.verify(userRepository, Mockito.never()).save(testUser);
        FlashCard card = testUser.getDecks().get(0).getFlashcards().get(0);
        assertEquals(before, card.getQuestion());
    }

    @Test
    void editCard_BlankAnswer() throws Exception {

        String before = testUser.getDecks()
                                .get(0)
                                .getFlashcards()
                                .get(0)
                                .getAnswer();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/decks/1/cards/1/edit")
                .session(session)
                .param("question", "question")
                .param("answer", ""))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"));

        Mockito.verify(userRepository, Mockito.never()).save(testUser);
        FlashCard card = testUser.getDecks().get(0).getFlashcards().get(0);
        assertEquals(before, card.getAnswer());

    }

    // Empty Deck Test
    @Test 
    void studyMode_emptyDeck() throws Exception {
        testUser.getDecks().get(0).setFlashcards(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders
               .get("/decks/1/study")
               .session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/decks/1/cards"))
               .andExpect(MockMvcResultMatchers.flash().attribute("errorMsg", "You cannot study on an empty deck"));
    }

    // Missing Session/Valid Account Tests
    @Test
    void getStudyMode_withoutSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/decks/1/study"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void addCard_withoutSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/decks/1/cards")
                .param("question", "test question")
                .param("answer", "test answer"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
        Mockito.verify(userRepository, Mockito.never()).save(testUser);
    }

    @Test
    void deleteCard_withoutSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/decks/1/cards/1/delete"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
        Mockito.verify(userRepository, Mockito.never()).save(testUser);
    }

    @Test
    void editCard_withoutSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/decks/1/cards/1/edit")
                .param("question", "test question")
                .param("answer", "test answer"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
        Mockito.verify(userRepository, Mockito.never()).save(testUser);
    }
}