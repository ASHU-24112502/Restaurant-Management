package com.example.Resort.controller;

import com.example.Resort.entity.Room;
import com.example.Resort.repository.RoomRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@Controller
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }


        @GetMapping("/rooms")
        @PreAuthorize("hasRole('ADMIN')")
        public String rooms(Model model, Authentication authentication) {

        model.addAttribute(
            "rooms",
            roomRepository.findAll()
        );

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        model.addAttribute("isAdmin", isAdmin);

        return "admin/rooms";
        }

    @GetMapping("/rooms/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addRoom(Model model) {

        model.addAttribute(
                "room",
                new Room()
        );

        return "admin/add-room";
    }

    @PostMapping("/rooms/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveRoom(
            @ModelAttribute Room room
    ) {

        roomRepository.save(room);

        return "redirect:/rooms";
    }

        @GetMapping("/rooms/edit/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public String editRoom(
            @PathVariable Long id,
            Model model
        ) {

        Room room = roomRepository
            .findById(id)
            .orElseThrow();

        model.addAttribute(
            "room",
            room
        );

        return "admin/add-room";
        }

    @GetMapping("/rooms/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRoom(
            @PathVariable Long id
    ) {

        roomRepository.deleteById(id);

        return "redirect:/rooms";
    }

    

    

  

  
}