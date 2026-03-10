package com.cmpt276.studbuds.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.User;
import java.util.Map;
import java.util.List;


@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/view")
    public String getAllUsers(Model model) {

        List<User> users = userRepo.findAll();
        model.addAttribute("us",users);
        return "/viewUsers";
    }
    
    @PostMapping("/users/add")
    public String addUser(@RequestParam Map<String, String> newuser, HttpServletResponse response) {
        String newName = newuser.get("name");
        String newPwd = newuser.get("password");
        userRepo.save(new User(newName,newPwd));
        response.setStatus(201);
        return "/login";
    }

    // redirect root to login
    @GetMapping("/")
    public RedirectView process() {
        return new RedirectView("/login");
    }
    
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) return "/login";

        else {
            model.addAttribute("user",user);
            return "/protected";
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam Map<String,String> formData, Model model, HttpServletRequest request, HttpSession session) {
        // processing login
        String name = formData.get("uname");
        String psw = formData.get("psw");
        List<User> userList = userRepo.findByNameAndPassword(name,psw);

        if (userList.isEmpty()) {
            // unsuccessful login
            return "/login";
        } else {
            // successful login
            User user = userList.get(0);
            request.getSession().setAttribute("session_user", user);
            request.getSession().setAttribute("userId", user.getUid());
            model.addAttribute("user", user);
            return "/protected";
        }
    }

    @GetMapping("/logout")
    public String destroySession(HttpServletRequest request) {
        request.getSession().invalidate();
        return "/login";
    }
}
