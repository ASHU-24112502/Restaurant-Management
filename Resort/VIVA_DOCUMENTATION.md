# Resort Application Viva Documentation

## 1. Main application flow

- `ResortApplication.java` is the Spring Boot entry point.
- It seeds three demo user accounts using a `CommandLineRunner`:
  - `admin@resort.com / admin123` → `ROLE_ADMIN`
  - `waiter@resort.com / waiter123` → `ROLE_WAITER`
  - `guest@resort.com / guest123` → `ROLE_USER`

## 2. Security and authentication

- `SecurityConfig.java` defines the app security rules.
- Public endpoints:
  - `/`
  - `/register`
  - `/login`
  - `/css/**`, `/js/**`, `/images/**`, `/uploads/**`
- All other routes require login.
- Login uses a custom login page at `/login` and redirects to `/dashboard` after successful sign-in.
- Logout redirects to `/login?logout`.

## 3. Login and registration controllers

- `AuthController.java`
  - `@GetMapping("/register")` shows the registration page.
  - `@PostMapping("/register")` saves a new user with role `ROLE_USER`.
  - `@GetMapping("/login")` shows the login page.

## 4. Dashboard controller: what is displayed for viva

- `DashboardController.java` handles `@GetMapping("/dashboard")`.
- It loads six repositories:
  - `RoomRepository`
  - `BookingRepository`
  - `RestaurantOrderRepository`
  - `ServiceRequestRepository`
  - `CheckInRepository`
  - `UserRepository`
- It computes role flags:
  - `isAdmin`
  - `isWaiter`
  - `isCustomer`
- It supplies the dashboard view with values such as:
  - `totalRooms`
  - `availableRooms`
  - `occupiedRooms`
  - `totalBookings`
  - `paidRoomBookings`
  - `totalOrders`
  - `paidRestaurantOrders`
  - `totalServices`
  - `totalCheckins`
  - `employeeCount`
  - `waiterCount`
  - `customerCount`
  - `totalRoomRevenue`
  - `totalRestaurantRevenue`
  - `totalRevenue`

## 5. Repository layer that feeds dashboard values

- `RoomRepository.java`
  - `countByStatus(String status)` for available/occupied room counts.
- `BookingRepository.java`, `RestaurantOrderRepository.java`, `ServiceRequestRepository.java`, and `CheckInRepository.java`
  - All extend `JpaRepository` and are used for counting rows and computing revenue.
- `UserRepository.java`
  - `findByEmail(String email)` supports login
  - Used by dashboard logic to count customers, waiters, and employees.

## 6. Dashboard view template

- `src/main/resources/templates/admin/dashboard.html`
- This template renders the dashboard UI using Thymeleaf expressions.
- It shows:
  - Sidebar navigation
  - Dashboard title and welcome messages per role
  - Analytics cards for admin statistics
  - Role-specific UI sections based on `isAdmin`, `isWaiter`, and `isCustomer`
- Example Thymeleaf bindings:
  - `th:text="${totalRooms}"`
  - `th:text="${availableRooms}"`
  - `th:text="${totalRevenue}"`
  - `th:if="${isAdmin}"`

## 7. Demo path for viva

1. Run the application.
2. Open `/login` in the browser.
3. Use one of the seeded accounts:
   - admin, waiter, or guest.
4. After login, you are redirected to `/dashboard`.
5. Explain the flow:
   - the controller fetches data from repositories
   - security controls access
   - Thymeleaf renders values in the HTML template
   - admin sees full analytics, waiter/customer see restricted views

## 8. Why this matters for viva

- The code shows a complete Spring Boot MVC flow:
  - security configuration
  - controller-to-view passing
  - data-backed dashboard rendering
- It demonstrates role-based access and user experience differences.
- The dashboard is the main “what we need to show” feature during the presentation.

## 9. Files to reference during viva

- `src/main/java/com/example/Resort/ResortApplication.java`
- `src/main/java/com/example/Resort/config/SecurityConfig.java`
- `src/main/java/com/example/Resort/controller/AuthController.java`
- `src/main/java/com/example/Resort/controller/DashboardController.java`
- `src/main/resources/templates/admin/dashboard.html`
- `src/main/java/com/example/Resort/repository/RoomRepository.java`
- `src/main/java/com/example/Resort/repository/UserRepository.java`
