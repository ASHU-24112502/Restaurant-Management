package com.example.Resort.controller;

import com.example.Resort.entity.ServiceRequest;
import com.example.Resort.repository.ServiceRequestRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ServiceController {

    private final ServiceRequestRepository repository;

    public ServiceController(
            ServiceRequestRepository repository
    ) {
        this.repository = repository;
    }

    @GetMapping("/services")
    public String services(Model model) {

        model.addAttribute(
                "services",
                repository.findAll()
        );

        return "services/services";
    }

    @GetMapping("/services/request")
    public String requestForm(
            @RequestParam(required = false) String serviceType,
            Model model
    ) {
        ServiceRequest serviceRequest = new ServiceRequest();
        if (serviceType != null && !serviceType.isBlank()) {
            serviceRequest.setServiceType(serviceType);
        }

        model.addAttribute("serviceRequest", serviceRequest);

        return "services/request-form";
    }

    @PostMapping("/services/save")
    public String saveService(
            @ModelAttribute ServiceRequest request
    ) {
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            request.setStatus("Pending");
        }
        repository.save(request);

        return "redirect:/services";
    }

    @GetMapping("/services/delete/{id}")
    public String deleteService(
            @PathVariable Long id
    ) {

        repository.deleteById(id);

        return "redirect:/services";
    }
}