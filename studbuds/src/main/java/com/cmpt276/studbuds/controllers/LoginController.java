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

    // Hardcoded admin credentials shared between testers
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    private UserRepository userRepo;

    // Redirect root to login
    @GetMapping("/")
    public RedirectView process() {
        return new RedirectView("/login");
    }

    // Show login page, or redirect already-logged-in users to the right place
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) return "/login";
        model.addAttribute("user", user);
        return redirectForRole(user);
    }

    // Handle login form submission
    @PostMapping("/login")
    public String login(@RequestParam Map<String,String> formData, Model model,
                        HttpServletRequest request, HttpSession session) {
        String name = formData.get("uname");
        String psw  = formData.get("psw");

        // Guard against blank values reaching the DB
        if (name == null || name.isBlank() || psw == null || psw.isBlank()) {
            return "/login";
        }

        // Check hardcoded admin account first, never touches the DB
        if (name.equals(ADMIN_USERNAME) && psw.equals(ADMIN_PASSWORD)) {
            User adminUser = new User(ADMIN_USERNAME, ADMIN_PASSWORD);
            adminUser.setRole(User.roleType.ADMIN);
            request.getSession().setAttribute("session_user", adminUser);
            model.addAttribute("user", adminUser);
            return "redirect:/view";
        }

        // Regular user login via DB
        List<User> userList = userRepo.findByNameAndPassword(name, psw);
        if (userList.isEmpty()) {
            return "/login";
        }

        User user = userList.get(0);
        request.getSession().setAttribute("session_user", user);
        request.getSession().setAttribute("userId", user.getUid());
        model.addAttribute("user", user);
        return redirectForRole(user);
    }

    // Dashboard (regular users)
    @GetMapping("/protected")
    public String dashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "/protected";
    }

    // View all users (admin only)
    @GetMapping("/view")
    public String getAllUsers(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) return "redirect:/login";
        if (user.getRole() == null || user.getRole() != User.roleType.ADMIN) {
            return "redirect:/protected";
        }
        List<User> users = userRepo.findAll();
        model.addAttribute("us", users);
        return "/viewUsers";
    }

    // Add user - blocked, no dynamic admin creation
    @PostMapping("/users/add")
    public String addUser(HttpServletResponse response) {
        response.setStatus(403);
        return "redirect:/login";
    }

    // Logout
    @GetMapping("/logout")
    public String destroySession(HttpServletRequest request) {
        request.getSession().invalidate();
        return "/login";
    }

    // Helper: pick destination based on role
    private String redirectForRole(User user) {
        if (user.getRole() == User.roleType.ADMIN) {
            return "redirect:/view";
        }
        return "redirect:/protected";
    }
}