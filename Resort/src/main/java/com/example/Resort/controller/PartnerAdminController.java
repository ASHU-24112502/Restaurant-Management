package com.example.Resort.controller;

import com.example.Resort.entity.Partner;
import com.example.Resort.repository.PartnerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
public class PartnerAdminController {

    private final PartnerRepository partnerRepository;

    public PartnerAdminController(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @GetMapping("/admin/partners")
    @PreAuthorize("hasRole('ADMIN')")
    public String partnerList(Model model) {
        model.addAttribute("partners", partnerRepository.findAll());
        model.addAttribute("partner", new Partner());
        return "admin/partners";
    }

    @PostMapping("/admin/partners/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String savePartner(@ModelAttribute Partner partner) {
        if (partner.getApiKey() == null || partner.getApiKey().isBlank()) {
            partner.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        }
        partnerRepository.save(partner);
        return "redirect:/admin/partners";
    }

    @GetMapping("/admin/partners/generate-key/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String regenerateApiKey(@PathVariable Long id) {
        Partner partner = partnerRepository.findById(id).orElseThrow();
        partner.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        partnerRepository.save(partner);
        return "redirect:/admin/partners";
    }
}
