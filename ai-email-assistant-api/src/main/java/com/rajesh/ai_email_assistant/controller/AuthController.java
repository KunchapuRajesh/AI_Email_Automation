package com.rajesh.ai_email_assistant.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal OAuth2User user) {

        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("authenticated", false);
            return ResponseEntity.status(401).body(response);
        }

        response.put("authenticated", true);
        response.put("name", user.getAttribute("name"));
        response.put("email", user.getAttribute("email"));
        response.put("picture", user.getAttribute("picture"));

        return ResponseEntity.ok(response);
    }
}
