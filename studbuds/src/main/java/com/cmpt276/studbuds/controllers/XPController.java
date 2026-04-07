package com.cmpt276.studbuds.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cmpt276.studbuds.exceptions.NullUserException;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.XpLog;
import com.cmpt276.studbuds.models.XpLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

@Controller
public class XPController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private XpLogRepository xpLogRepository;

    // ── API: fetch current user's total XP ────────────────────────────────────
    // Returns JSON: { "totalXp": 1234 }
    // XP.js calls this on page load to seed the bar from the saved state.
    @GetMapping("/xp/total")
    @ResponseBody
    public Map<String, Integer> getTotalXp(HttpServletRequest request) {
        User user = findUser(request);

        List<XpLog> logs = xpLogRepository.findByUser(user);
        int total = logs.stream().mapToInt(XpLog::getXpEarned).sum();

        Map<String, Integer> result = new HashMap<>();
        result.put("totalXp", total);
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(HttpServletRequest request) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) throw new NullUserException("userId not found");

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new NullUserException("user not found");

        return user;
    }

    // Returns 401 JSON so XP.js can handle gracefully instead of redirecting
    @ExceptionHandler(NullUserException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> nullUserHandler() {
        Map<String, String> err = new HashMap<>();
        err.put("error", "not logged in");
        return err;
    }
}