package com.example.Resort.controller;

import com.example.Resort.repository.BookingRepository;
import com.example.Resort.repository.CheckInRepository;
import com.example.Resort.repository.RestaurantOrderRepository;
import com.example.Resort.repository.RoomRepository;
import com.example.Resort.repository.ServiceRequestRepository;
import com.example.Resort.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    public DashboardController(
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            RestaurantOrderRepository restaurantOrderRepository,
            ServiceRequestRepository serviceRequestRepository,
            CheckInRepository checkInRepository,
            UserRepository userRepository
    ) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.restaurantOrderRepository = restaurantOrderRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.checkInRepository = checkInRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isWaiter = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_WAITER"));
        boolean isCustomer = !isAdmin && !isWaiter;

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isWaiter", isWaiter);
        model.addAttribute("isCustomer", isCustomer);
        model.addAttribute("totalRooms", roomRepository.count());
        model.addAttribute("availableRooms", roomRepository.countByStatus("Available"));
        model.addAttribute("occupiedRooms", roomRepository.countByStatus("Occupied"));
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("paidRoomBookings", bookingRepository.findAll().stream()
                .filter(b -> "Paid".equalsIgnoreCase(b.getPaymentStatus()))
                .count());
        model.addAttribute("totalOrders", restaurantOrderRepository.count());
        model.addAttribute("paidRestaurantOrders", restaurantOrderRepository.findAll().stream()
                .filter(o -> "Paid".equalsIgnoreCase(o.getStatus()))
                .count());
        model.addAttribute("totalServices", serviceRequestRepository.count());
        model.addAttribute("totalCheckins", checkInRepository.count());
        model.addAttribute("employeeCount", userRepository.findAll().stream()
                .filter(u -> "ROLE_WAITER".equalsIgnoreCase(u.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(u.getRole()))
                .count());
        model.addAttribute("waiterCount", userRepository.findAll().stream()
                .filter(u -> "ROLE_WAITER".equalsIgnoreCase(u.getRole()))
                .count());
        model.addAttribute("customerCount", userRepository.findAll().stream()
                .filter(u -> "ROLE_USER".equalsIgnoreCase(u.getRole()))
                .count());
        double totalRoomRevenue = bookingRepository.findAll().stream()
                .mapToDouble(b -> b.getTotalPrice() == null ? 0.0 : b.getTotalPrice())
                .sum();
        double totalRestaurantRevenue = restaurantOrderRepository.findAll().stream()
                .mapToDouble(o -> o.getTotalPrice() == null ? 0.0 : o.getTotalPrice())
                .sum();
        model.addAttribute("totalRoomRevenue", totalRoomRevenue);
        model.addAttribute("totalRestaurantRevenue", totalRestaurantRevenue);
        model.addAttribute("totalRevenue", totalRoomRevenue + totalRestaurantRevenue);

        return "admin/dashboard";
    }
}