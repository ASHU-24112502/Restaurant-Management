package com.example.Resort.controller;

import com.example.Resort.entity.Booking;
import com.example.Resort.entity.RestaurantOrder;
import com.example.Resort.repository.BookingRepository;
import com.example.Resort.repository.RestaurantOrderRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class BillingController {

    private final BookingRepository bookingRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;

    public BillingController(BookingRepository bookingRepository,
                             RestaurantOrderRepository restaurantOrderRepository) {
        this.bookingRepository = bookingRepository;
        this.restaurantOrderRepository = restaurantOrderRepository;
    }

    @GetMapping("/admin/billing/rooms")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String roomBilling(Model model, Authentication authentication) {
        List<Booking> bookings = bookingRepository.findAll();
        double totalRevenue = bookings.stream()
                .mapToDouble(b -> b.getTotalPrice() == null ? 0.0 : b.getTotalPrice())
                .sum();
        long paidCount = bookings.stream()
                .filter(b -> "Paid".equalsIgnoreCase(b.getPaymentStatus()))
                .count();
        long pendingCount = bookings.stream()
                .filter(b -> !"Paid".equalsIgnoreCase(b.getPaymentStatus()))
                .count();

        model.addAttribute("bookings", bookings);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("isAdmin", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ADMIN")));
        model.addAttribute("isWaiter", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("WAITER")));
        model.addAttribute("isCustomer", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("USER")));
        return "billing/rooms";
    }

    @GetMapping("/admin/billing/restaurant")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String restaurantBilling(Model model, Authentication authentication) {
        List<RestaurantOrder> orders = restaurantOrderRepository.findAll();
        double totalRevenue = orders.stream()
                .mapToDouble(o -> o.getTotalPrice() == null ? 0.0 : o.getTotalPrice())
                .sum();
        long paidCount = orders.stream()
                .filter(o -> "Paid".equalsIgnoreCase(o.getStatus()))
                .count();
        long pendingCount = orders.stream()
                .filter(o -> !"Paid".equalsIgnoreCase(o.getStatus()))
                .count();

        model.addAttribute("orders", orders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("isAdmin", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ADMIN")));
        model.addAttribute("isWaiter", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("WAITER")));
        model.addAttribute("isCustomer", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("USER")));
        return "billing/restaurant";
    }

    @GetMapping("/admin/billing/rooms/mark-paid/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String markRoomPaid(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setPaymentStatus("Paid");
        bookingRepository.save(booking);
        return "redirect:/admin/billing/rooms";
    }

    @GetMapping("/admin/billing/restaurant/mark-paid/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public String markRestaurantPaid(@PathVariable Long id) {
        RestaurantOrder order = restaurantOrderRepository.findById(id).orElseThrow();
        order.setStatus("Paid");
        restaurantOrderRepository.save(order);
        return "redirect:/admin/billing/restaurant";
    }
}
