package com.example.Resort.config;

import com.example.Resort.entity.User;
import com.example.Resort.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminDataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminDataLoader(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = "admin@resort.com";
        userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User admin = new User();
            admin.setFullName("Resort Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole("ROLE_ADMIN");
            return userRepository.save(admin);
        });
    }
}
