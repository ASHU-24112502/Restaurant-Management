package com.example.Resort;

import com.example.Resort.entity.User;
import com.example.Resort.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ResortApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResortApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataSeeder(UserRepository userRepository,
                                        BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            seedAccount(userRepository, passwordEncoder,
                    "admin@resort.com", "admin123", "Resort Admin", "ROLE_ADMIN");
            seedAccount(userRepository, passwordEncoder,
                    "waiter@resort.com", "waiter123", "Resort Waiter", "ROLE_WAITER");
            seedAccount(userRepository, passwordEncoder,
                    "guest@resort.com", "guest123", "Resort Guest", "ROLE_USER");
        };
    }

    private void seedAccount(UserRepository userRepository,
                             BCryptPasswordEncoder passwordEncoder,
                             String email,
                             String rawPassword,
                             String fullName,
                             String role) {
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            existing.setFullName(fullName);
            existing.setRole(role);
            existing.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(existing);
        }, () -> {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            userRepository.save(user);
        });
    }
}
