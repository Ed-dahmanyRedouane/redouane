package com.bookshop.service;

import com.bookshop.config.SecurityConfig;
import com.bookshop.dto.SignupDto;
import com.bookshop.entity.Role;
import com.bookshop.entity.UserAccount;
import com.bookshop.repository.RoleRepository;
import com.bookshop.repository.UserAccountRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Value("${jwt.secret}")
    private String JWT_SECRET;
    private UserAccountRepo userAccountRepo;


    private JwtEncoder jwtEncoder;

    private  RoleRepository roleRepo; // <-- 1. Ajouter le repository des rôles

    private SecurityConfig securityConfig;
    private AuthenticationManager authenticationManager;

    public AuthService(UserAccountRepo userAccountRepo, JwtEncoder jwtEncoder, SecurityConfig securityConfig, AuthenticationManager authenticationManager, RoleRepository roleRepo) {
        this.userAccountRepo = userAccountRepo;
        this.jwtEncoder = jwtEncoder;
        this.securityConfig = securityConfig;
        this.authenticationManager = authenticationManager;
        this.roleRepo = roleRepo; // <-- 2. Initialiser le repository des rôles
    }

    public void registerUser(SignupDto signupDto) {
        Role defaultRole = roleRepo.findByRole("USER")
                .orElseThrow(() -> new RuntimeException("Erreur : Le rôle par défaut (USER) est introuvable."));
        UserAccount userAccount = UserAccount.builder()
                .username(signupDto.getUsername())
                .password(securityConfig.passwordEncoder().encode(signupDto.getPassword()))
                .email(signupDto.getEmail())
                .roles(Set.of(defaultRole))
                .build();
        this.userAccountRepo.save(userAccount);
    }

    public Map<String, String> login(String username, String password) {
        return generateToken(this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)));
    }


    public Map<String, String> generateToken(Authentication authentication) {
        Instant now = Instant.now();
        long expiryMinutes = 60;
        UserAccount userDetails = (UserAccount) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        String scope = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                 .issuedAt(now)
                .expiresAt(now.plus(expiryMinutes, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("scope", scope)
//                .claim("email",userEmail)
                .build();
        String jwt = this.jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claimsSet
        )).getTokenValue();
        return Map.of("access_token", jwt);
    }


}
