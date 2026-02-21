package com.bookshop.controller;

import com.bookshop.dto.SignupDto;
import com.bookshop.security.UserDetailsServiceImp;
import com.bookshop.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private AuthService authService;
    private UserDetailsServiceImp userDetailsService;
    private AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, UserDetailsServiceImp userDetailsService) {
        this.authService = authService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Validated  @ModelAttribute SignupDto signupDto) {
        this.authService.registerUser(signupDto);
        return ResponseEntity.accepted().body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        System.out.println(username + password+"---->");
        return ResponseEntity.ok().body(this.authService.login(username, password));
    }

    @GetMapping("/login-test")
    public ResponseEntity<Authentication> loginTest(Authentication authentication) {
        return ResponseEntity.ok().body(authentication);
    }

}
