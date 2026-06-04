package com.example.Resort.controller;

import com.example.Resort.entity.MenuItem;
import com.example.Resort.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class MenuController {

    private final MenuItemRepository menuItemRepository;

    @Value("${uploads.path:uploads}")
    private String uploadsPath;

    public MenuController(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping("/admin/menu")
    public String adminMenu(Model model) {
        model.addAttribute("items", menuItemRepository.findAll());
        return "admin/menu";
    }

    @GetMapping("/admin/menu/add")
    public String addMenuItem(Model model) {
        model.addAttribute("menuItem", new MenuItem());
        return "admin/add-menu";
    }

    @GetMapping("/admin/menu/edit/{id}")
    public String editMenuItem(@PathVariable Long id, Model model) {
        MenuItem menuItem = menuItemRepository.findById(id).orElseThrow();
        model.addAttribute("menuItem", menuItem);
        return "admin/add-menu";
    }

    @PostMapping("/admin/menu/save")
    public String saveMenuItem(@ModelAttribute MenuItem menuItem,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        if (menuItem.getStatus() == null || menuItem.getStatus().isBlank()) {
            menuItem.setStatus("Available");
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = StringUtils.cleanPath(imageFile.getOriginalFilename());
            try {
                Path uploadDir = Paths.get(uploadsPath);
                Files.createDirectories(uploadDir);
                Path filePath = uploadDir.resolve(filename);
                imageFile.transferTo(filePath);
                menuItem.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        menuItemRepository.save(menuItem);
        return "redirect:/admin/menu";
    }

    @GetMapping("/admin/menu/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return "redirect:/admin/menu";
    }
}
