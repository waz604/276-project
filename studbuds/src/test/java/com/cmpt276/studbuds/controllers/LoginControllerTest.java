package com.cmpt276.studbuds.controllers;

import java.util.List;

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

@WebMvcTest(LoginController.class)
public class LoginControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    private User user;
    private User adminUser;
    private MockHttpSession session;
   
    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "admin123");
        adminUser.setRole(User.roleType.ADMIN);
        adminUser.setUid(1);

        user = new User("User", "password123");
        user.setUid(2);
        user.setRole(User.roleType.USER);

    }

    // Success Tests
    @Test
    void login_withValidRegularUser() throws Exception {
        Mockito.when(userRepository.findByNameAndPassword("User", "password123"))
               .thenReturn(List.of(user));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("uname", "User")
                .param("psw", "password123"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/protected"));

        Mockito.verify(userRepository, Mockito.times(1))
               .findByNameAndPassword("User", "password123");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    
    @Test
    void login_withValidAdminUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("uname", "admin")
                .param("psw", "admin123"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/view"));

        Mockito.verify(userRepository, Mockito.never())
            .findByNameAndPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void viewAllUsers_Success() throws Exception {
        session = new MockHttpSession();
        session.setAttribute("session_user", adminUser);

        Mockito.when(userRepository.findById(1))
               .thenReturn(java.util.Optional.of(adminUser));

        mockMvc.perform(MockMvcRequestBuilders
               .get("/view")
               .session(session))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.model().attributeExists("us"));

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    void createLogin_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/create")
                .param("name", "User")
                .param("password", "password123"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));

        Mockito.verify(userRepository, Mockito.times(1))
               .save(Mockito.any(User.class));
    }

    @Test
    // feature is for admin users only
    void deleteUser_Success() throws Exception {
        session = new MockHttpSession();
        session.setAttribute("session_user", adminUser);

        Mockito.when(userRepository.findById(1))
               .thenReturn(java.util.Optional.of(adminUser));

        mockMvc.perform(MockMvcRequestBuilders
               .post("/delete/2")
               .session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/view"));
        
        Mockito.verify(userRepository, Mockito.times(1))
               .deleteById(2);
    } 

    @Test
    void logout_invalidatesSession() throws Exception {
        session = new MockHttpSession();
        session.setAttribute("session_user", user);

        mockMvc.perform(MockMvcRequestBuilders.get("/logout").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home"));
    }

    // Failure Tests
    @Test
    void login_withInvalidRegularUser() throws Exception {
        Mockito.when(userRepository.findByNameAndPassword("User", "wrongpassword"))
            .thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("uname", "User")
                .param("psw", "wrongpassword"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("loginError"));

        Mockito.verify(userRepository, Mockito.times(1))
            .findByNameAndPassword("User", "wrongpassword");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void viewAllUsers_InvalidAdminUser() throws Exception {
        session = new MockHttpSession();
        session.setAttribute("session_user", user);
        mockMvc.perform(MockMvcRequestBuilders
               .get("/view")
               .session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/protected"));
    }

    @Test
    void createLogin_blankName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/create")
                .param("name", "")
                .param("password", "password123"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("add"));

        Mockito.verify(userRepository, Mockito.never())
               .save(Mockito.any(User.class));
    }

    @Test
    void createLogin_blankPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/create")
                .param("name", "User")
                .param("password", ""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("add"));

        Mockito.verify(userRepository, Mockito.never())
               .save(Mockito.any(User.class));
    }

    @Test
    void deleteUser_invalidAdminUser() throws Exception {
        session = new MockHttpSession();
        session.setAttribute("session_user", user);

        
        Mockito.when(userRepository.findById(2))
               .thenReturn(java.util.Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders
               .post("/delete/1")
               .session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
        
        Mockito.verify(userRepository, Mockito.never())
               .deleteById(1);
    }
}

