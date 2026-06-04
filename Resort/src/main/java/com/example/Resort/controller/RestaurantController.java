package com.example.Resort.controller;

import com.example.Resort.entity.MenuItem;
import com.example.Resort.entity.RestaurantOrder;
import com.example.Resort.repository.MenuItemRepository;
import com.example.Resort.repository.RestaurantOrderRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RestaurantController {

    private final RestaurantOrderRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantController(RestaurantOrderRepository restaurantRepository,
                                MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping("/restaurant")
    public String restaurantRedirect() {
        return "redirect:/restaurant/order";
    }

    @GetMapping("/admin/restaurant")
    public String restaurantPage(Model model) {
        model.addAttribute("orders", restaurantRepository.findAll());
        return "admin/restaurant";
    }

    @GetMapping("/restaurant/order")
    public String orderPage(Authentication authentication, Model model) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isWaiter = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_WAITER"));
        boolean isCustomer = authentication != null && !isAdmin && !isWaiter;

        model.addAttribute("menuItems", menuItemRepository.findAll());
        model.addAttribute("restaurantOrder", new RestaurantOrder());
        model.addAttribute("orders", restaurantRepository.findAll());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isWaiter", isWaiter);
        model.addAttribute("isCustomer", isCustomer);
        return "restaurant/order-form";
    }

    @PostMapping("/restaurant/save")
    public String saveOrder(@ModelAttribute RestaurantOrder order,
                            @RequestParam(required = false) Long menuItemId) {
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("Pending");
        }
        if (menuItemId != null) {
            MenuItem item = menuItemRepository.findById(menuItemId).orElse(null);
            if (item != null) {
                order.setFoodItem(item.getItemName());
                if (order.getQuantity() == null) {
                    order.setQuantity(1);
                }
                order.setTotalPrice(item.getPrice() * order.getQuantity());
            }
        }
        restaurantRepository.save(order);
        return "redirect:/restaurant/order";
    }

    @GetMapping("/admin/restaurant/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        restaurantRepository.deleteById(id);
        return "redirect:/admin/restaurant";
    }

    @GetMapping("/restaurant/delete/{id}")
    public String deleteRestaurantOrder(@PathVariable Long id) {
        restaurantRepository.deleteById(id);
        return "redirect:/restaurant";
    }
}