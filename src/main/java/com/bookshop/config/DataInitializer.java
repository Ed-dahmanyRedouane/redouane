package com.bookshop.config;

import com.bookshop.entity.Role;
import com.bookshop.entity.UserAccount;
import com.bookshop.repository.RoleRepository;
import com.bookshop.repository.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepo userAccountRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (roleRepo.findByRole("USER").isEmpty()) {
            roleRepo.save(Role.builder().role("USER").build());
        }
        if (roleRepo.findByRole("ADMIN").isEmpty()) {
            roleRepo.save(Role.builder().role("ADMIN").build());
        }

        Role userRole = roleRepo.findByRole("USER").get();
        Role adminRole = roleRepo.findByRole("ADMIN").get();

        if (userAccountRepo.findByUsername("user").isEmpty()) {
            UserAccount user = UserAccount.builder()
                    .username("user")
                    .password(passwordEncoder.encode("1234"))
                    .roles(Set.of(userRole))
                    .build();
            userAccountRepo.save(user);
            System.out.println("Utilisateur 'user' créé avec succès !");
        }
        if (userAccountRepo.findByUsername("admin").isEmpty()) {
            UserAccount admin = UserAccount.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("1234"))
                    .roles(Set.of(adminRole))
                    .build();
            userAccountRepo.save(admin);
            System.out.println("Utilisateur 'admin' créé avec succès !");
        }
        System.out.println("Data initialization completed.");
    }
}
