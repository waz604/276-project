package com.cmpt276.studbuds.controllers;

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

import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.XpLogRepository;

@WebMvcTest(ProfileController.class)
public class ProfileControllerLeaderboardTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private XpLogRepository xpLogRepository;

    private User user;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        user = new User("User", "password123", "user@gmail.com");
        user.setUid(1);

        session = new MockHttpSession();
        session.setAttribute("userId", 1);

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Mockito.when(xpLogRepository.findByUser(user)).thenReturn(new ArrayList<>());
    }

    @Test
    void testDisplayLeaderboard_Success() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/leaderboard").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("leaderboard"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("xp"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("level"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("us"));
    }

    @Test
    void getLeaderboard_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/leaderboard"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getLeaderboard_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/leaderboard").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }
}