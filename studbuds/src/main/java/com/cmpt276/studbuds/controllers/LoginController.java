package com.cmpt276.studbuds.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginController {
    
    @GetMapping("/studbuds/login")
    public String showLoginPage() {
        return "/studbuds/login";
    }
    

    @PostMapping("/studbuds/login")
    public String login(@RequestParam String uname, @RequestParam String psw) {
        return "/studbuds/loggedIn";
    }
}
