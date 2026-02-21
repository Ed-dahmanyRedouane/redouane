package com.bookshop.config;

import com.bookshop.entity.*;
import com.bookshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds the database with initial users, categories, and books on startup.
 * Only runs if the respective tables are empty (idempotent).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCategoriesAndBooks();
        log.info("Data initialization complete.");
    }

    private void seedUsers() {
        if (userRepository.count() > 0)
            return;

        userRepository.save(User.builder()
                .email("admin@bookshop.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .build());

        userRepository.save(User.builder()
                .email("user@bookshop.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .role(User.Role.USER)
                .build());

        log.info("Users seeded: admin@bookshop.com / admin123, user@bookshop.com / user123");
    }

    private void seedCategoriesAndBooks() {
        if (categoryRepository.count() > 0)
            return;

        Category fiction = categoryRepository.save(
                Category.builder().name("Fiction").build());
        Category tech = categoryRepository.save(
                Category.builder().name("Technology").build());

        bookRepository.save(Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .price(new BigDecimal("34.99"))
                .stock(50)
                .description("A handbook of agile software craftsmanship")
                .category(tech)
                .build());

        bookRepository.save(Book.builder()
                .title("The Pragmatic Programmer")
                .author("Andy Hunt, Dave Thomas")
                .price(new BigDecimal("42.50"))
                .stock(30)
                .description("Your journey to mastery")
                .category(tech)
                .build());

        bookRepository.save(Book.builder()
                .title("Dune")
                .author("Frank Herbert")
                .price(new BigDecimal("18.99"))
                .stock(100)
                .description("Epic science fiction novel")
                .category(fiction)
                .build());

        log.info("Categories and books seeded.");
    }
}
