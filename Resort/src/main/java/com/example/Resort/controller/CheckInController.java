package com.example.Resort.controller;

import com.example.Resort.entity.CheckIn;
import com.example.Resort.repository.CheckInRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@Controller
public class CheckInController {

    private final CheckInRepository checkInRepository;

    public CheckInController(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    @GetMapping("/checkin")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String checkInPage(Model model, Authentication authentication) {
        model.addAttribute("checkins", checkInRepository.findAll());

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isWaiter = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_WAITER".equals(a.getAuthority()));

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isWaiter", isWaiter);

        return "checkin";
    }

    @GetMapping("/checkin/add")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String addCheckInForm(Model model, Authentication authentication) {
        model.addAttribute("checkin", new CheckIn());

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isWaiter = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_WAITER".equals(a.getAuthority()));

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isWaiter", isWaiter);

        return "add-checkin";
    }

    @PostMapping("/checkin/save")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String saveCheckIn(@ModelAttribute CheckIn checkin) {
        checkInRepository.save(checkin);
        return "redirect:/checkin";
    }

    @GetMapping("/checkin/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String deleteCheckIn(@PathVariable Long id) {
        checkInRepository.deleteById(id);
        return "redirect:/checkin";
    }
}