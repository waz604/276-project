package com.cmpt276.studbuds.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    return new RedirectView("login");
    }

    // Show login page, or redirect already-logged-in users to the right place
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) return "login";
        model.addAttribute("user", user);
        return redirectForRole(user);
    }

    // Handle login form submission
    @PostMapping("/login")
    public String login(@RequestParam Map<String,String> formData, Model model,
                        HttpServletRequest request, HttpSession session) {
        String name = formData.get("uname");
        String psw  = formData.get("psw");
        String googleId = formData.get("google_id");

        boolean isGoogleUser = (googleId != null && !googleId.isBlank());


        // Guard against blank values reaching the DB
        if (name == null || name.isBlank()) {
            return "login";
        }

        // If the user is NOT a google user, block them if they are missing a password
        if (!isGoogleUser && (psw == null || psw.isBlank())) {
            return "login";
        }

    // Google user login
    if (isGoogleUser) {
        return userRepo.findByGoogleID(googleId)
            .map(existingUser -> {
                session.setAttribute("session_user", existingUser);
                return "redirect:/protected";
            })
            .orElseGet(() -> {
                // Create new Google User
                User newUser = new User();
                newUser.setName(name);
                newUser.setGoogleId(googleId);
                newUser.setPassword(null);
                userRepo.save(newUser);
                
                session.setAttribute("session_user", newUser);
                return "redirect:/protected";
            });
    }

        // Check hardcoded admin account first, never touches the DB
        if (name.equals(ADMIN_USERNAME) && psw.equals(ADMIN_PASSWORD)) {
            User adminUser = new User(ADMIN_USERNAME, ADMIN_PASSWORD, null);
            adminUser.setRole(User.roleType.ADMIN);
            request.getSession().setAttribute("session_user", adminUser);
            model.addAttribute("user", adminUser);
            return "redirect:/view";
        }

        // Regular user login via DB
        List<User> userList = userRepo.findByNameAndPassword(name, psw);
        if (userList.isEmpty()) {
            model.addAttribute("loginError", "Invalid username or password.");
            return "login";
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
        return "redirect:/profile";
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
        return "viewUsers";
    }

    @GetMapping("/add")
    public String showAddUserPage() {
        return "add";
    }

    // Add user
    @PostMapping("/create")
    public String addUser(@RequestParam Map<String, String> newuser, HttpServletResponse response) {
        String newName = newuser.get("name");
        String newPwd = newuser.get("password");

        // Avoid empty user from entering the database
        if (newName == null || newName.isBlank()) {
            return "add";
        }

        User addedUser = new User();
        addedUser.setName(newName);
        userRepo.save(addedUser);
        response.setStatus(201);

        return "redirect:/login";
    }

    // Logout
    @GetMapping("/logout")
    public String destroySession(HttpServletRequest request) {
        request.getSession().invalidate();
        return "home";
    }

    // Helper: pick destination based on role
    private String redirectForRole(User user) {
        if (user.getRole() == User.roleType.ADMIN) {
            return "redirect:/view";
        }
        return "redirect:/protected";
    }

    // Delete user (ADMIN ONLY)
    @PostMapping("/delete/{id}")
    public String deleteStaffRating(@PathVariable int id, HttpSession session) {
        User currentUser = (User) session.getAttribute("session_user");
    
        // Check if logged in AND if ADMIN
        if (currentUser == null || currentUser.getRole() != User.roleType.ADMIN) {
            return "redirect:/login";
        }

        userRepo.deleteById(id);
        return "redirect:/view";
    }
}