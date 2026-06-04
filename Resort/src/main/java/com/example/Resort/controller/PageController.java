package com.example.Resort.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/write")
    public String writePage(Model model) {
        model.addAttribute("title","Write");
        return "admin/write";
    }
}
