package com.example.Resort.controller;

import com.example.Resort.entity.User;
import com.example.Resort.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String userManagement(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/admin/users/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addUserPage(Model model) {
        model.addAttribute("user", new User());
        return "admin/add-user";
    }

    @GetMapping("/admin/users/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUserPage(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    @PostMapping("/admin/users/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveUser(@ModelAttribute User user) {
        if (user.getId() != null) {
            User existing = userRepository.findById(user.getId()).orElseThrow();
            existing.setFullName(user.getFullName());
            existing.setEmail(user.getEmail());
            existing.setRole(user.getRole() == null || user.getRole().isBlank() ? existing.getRole() : user.getRole());
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            userRepository.save(existing);
        } else {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password is required");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("ROLE_USER");
            }
            userRepository.save(user);
        }
        return "redirect:/admin/users";
    }
}
