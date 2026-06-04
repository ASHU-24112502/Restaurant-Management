package com.example.Resort.controller;

import com.example.Resort.entity.Booking;
import com.example.Resort.entity.Room;
import com.example.Resort.repository.BookingRepository;
import com.example.Resort.repository.RoomRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingController(BookingRepository bookingRepository,
                             RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/bookings")
    public String bookings(Model model, org.springframework.security.core.Authentication authentication) {
        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ADMIN")));
        model.addAttribute("isWaiter", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("WAITER")));
        model.addAttribute("isCustomer", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("USER")));
        return "bookings/bookings";
    }

    @GetMapping("/bookings/add")
    public String bookingForm(Model model, org.springframework.security.core.Authentication authentication) {
        model.addAttribute("booking", new Booking());
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ADMIN")));
        model.addAttribute("isWaiter", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("WAITER")));
        model.addAttribute("isCustomer", authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("USER")));
        return "bookings/add-booking";
    }

    @PostMapping("/bookings/save")
    public String saveBooking(@ModelAttribute Booking booking, org.springframework.security.core.Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ADMIN"));
        boolean isWaiter = authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("WAITER"));
        if (!isAdmin && !isWaiter) {
            booking.setBookingStatus("Pending");
        } else {
            if (booking.getBookingStatus() == null || booking.getBookingStatus().isBlank()) {
                booking.setBookingStatus("Pending");
            }
        }
        booking.setPaymentStatus("Pending");
        Room chosenRoom = roomRepository.findByRoomNumber(booking.getRoomNumber()).orElse(null);
        if (chosenRoom != null && chosenRoom.getPrice() != null) {
            booking.setTotalPrice(chosenRoom.getPrice());
        }
        bookingRepository.save(booking);
        return "redirect:/bookings";
    }

    @GetMapping("/bookings/delete/{id}")
    public String deleteBooking(@PathVariable Long id) {
        bookingRepository.deleteById(id);
        return "redirect:/bookings";
    }
}