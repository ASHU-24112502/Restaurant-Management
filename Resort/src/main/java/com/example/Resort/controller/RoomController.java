package com.example.Resort.controller;

import com.example.Resort.entity.Room;
import com.example.Resort.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/rooms";
    }

    @GetMapping("/rooms")
    public String viewRooms(Model model) {
        model.addAttribute("rooms", roomRepository.findAll());
        return "rooms";
    }

    @GetMapping("/rooms/add")
    public String addRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "add-room";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute Room room) {
        roomRepository.save(room);
        return "redirect:/rooms";
    }

    @GetMapping("/rooms/edit/{id}")
    public String editRoom(@PathVariable Long id, Model model) {
        Room room = roomRepository.findById(id).orElseThrow();
        model.addAttribute("room", room);
        return "add-room";
    }

    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomRepository.deleteById(id);
        return "redirect:/rooms";
    }
}