package com.cmpt276.studbuds.controllers;

import java.time.LocalDate;
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

import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.XpLog;
import com.cmpt276.studbuds.models.XpLogRepository;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

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
        Mockito.when(xpLogRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
    }

    @Test
    void getProfile_withSession_returnsProfileView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/profile").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("profile"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("username"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("totalXp"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("currentStreak"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("longestStreak"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("mostActiveDay"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("heatmap"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("heatmapDates"));
    }

    @Test
    void getProfile_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/profile"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getProfile_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/profile").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getProfile_withXpLogs_returnsCorrectStats() throws Exception {
        List<XpLog> logs = new ArrayList<>();
        logs.add(new XpLog(testUser, LocalDate.now(), 150));
        logs.add(new XpLog(testUser, LocalDate.now().minusDays(1), 50));

        Mockito.when(xpLogRepository.findByUser(testUser)).thenReturn(logs);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("totalXp", 200))
                .andExpect(MockMvcResultMatchers.model().attribute("currentStreak", 2));
    }

    //post

    @Test
    void editUsername_withSession_redirectsToProfile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/profile/edit")
                        .session(session)
                        .param("username", "newName"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/profile"));

        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
    }

    @Test
    void editUsername_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/profile/edit")
                        .param("username", "newName"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void editUsername_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/profile/edit")
                        .session(session)
                        .param("username", "newName"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }
    
    

    

    @Test
    void getTutorial_withSession_returnsTutorialView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/tutorial").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("tutorial"));
    }

    @Test
    void getTutorial_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/tutorial"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void getTutorial_userNotFound_redirectsToLogin() throws Exception {
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/tutorial").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }
}
