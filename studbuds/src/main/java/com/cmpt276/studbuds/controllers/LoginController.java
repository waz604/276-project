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

        List<User> userList = userRepo.findByNameAndPassword(name, psw);
        if (userList.isEmpty()) {
            return "/login";
        }

        // Successful login
        User user = userList.get(0);
        request.getSession().setAttribute("session_user", user);
        request.getSession().setAttribute("userId", user.getUid());
        model.addAttribute("user", user);

        // Admins go to the user list; regular users go to their dashboard
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

    // Add user (admin endpoint)
    @PostMapping("/users/add")
    public String addUser(@RequestParam Map<String, String> newuser,
                          HttpServletResponse response, HttpSession session) {
        User requester = (User) session.getAttribute("session_user");
        if (requester == null || requester.getRole() != User.roleType.ADMIN) {
            response.setStatus(403);
            return "redirect:/login";
        }

        String newName = newuser.get("name");
        String newPwd  = newuser.get("password");
        if (newName == null || newName.isBlank() || newPwd == null || newPwd.isBlank()) {
            return "redirect:/view";
        }

        User u = new User(newName, newPwd);
        u.setRole(User.roleType.USER);
        userRepo.save(u);
        response.setStatus(201);
        return "redirect:/view";
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