package com.cmpt276.studbuds.controllers;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.XpLog;
import com.cmpt276.studbuds.models.XpLogRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
public class ProfileControllerXpTest {

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

        session = new MockHttpSession();
        session.setAttribute("userId", 1);

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
    }

    @Test
    void awardTimeChallengeXp_successfulChallenge_savesXp() throws Exception {
        mockMvc.perform(post("/xp/time-challenge")
                .session(session)
                .param("totalCards", "10")
                .param("score", "10")
                .param("timeRemaining", "20"))
                .andExpect(status().isOk());

        Mockito.verify(xpLogRepository, Mockito.times(1))
                .save(Mockito.argThat(new ArgumentMatcher<XpLog>() {
                    @Override
                    public boolean matches(XpLog log) {
                        return log != null && log.getXpEarned() == 125;
                    }
                }));
    }

    @Test
    void awardTimeChallengeXp_fastCompletion_savesBonusXp() throws Exception {
        mockMvc.perform(post("/xp/time-challenge")
                .session(session)
                .param("totalCards", "10")
                .param("score", "10")
                .param("timeRemaining", "50"))
                .andExpect(status().isOk());

        Mockito.verify(xpLogRepository, Mockito.times(1))
                .save(Mockito.argThat(new ArgumentMatcher<XpLog>() {
                    @Override
                    public boolean matches(XpLog log) {
                        return log != null && log.getXpEarned() == 150;
                    }
                }));
    }

    @Test
    void awardTimeChallengeXp_incompleteChallenge_doesNotSaveXp() throws Exception {
        mockMvc.perform(post("/xp/time-challenge")
                .session(session)
                .param("totalCards", "10")
                .param("score", "8")
                .param("timeRemaining", "20"))
                .andExpect(status().isOk());

        Mockito.verify(xpLogRepository, Mockito.never())
                .save(Mockito.any(XpLog.class));
    }

    @Test
    void awardTimeChallengeXp_timeRanOut_doesNotSaveXp() throws Exception {
        mockMvc.perform(post("/xp/time-challenge")
                .session(session)
                .param("totalCards", "10")
                .param("score", "10")
                .param("timeRemaining", "0"))
                .andExpect(status().isOk());

        Mockito.verify(xpLogRepository, Mockito.never())
                .save(Mockito.any(XpLog.class));
    }

    @Test
    void awardTimeChallengeXp_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/xp/time-challenge")
                .param("totalCards", "10")
                .param("score", "10")
                .param("timeRemaining", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void awardTimeChallengeXp_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/xp/time-challenge")
                .session(session)
                .param("totalCards", "10")
                .param("score", "10")
                .param("timeRemaining", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}