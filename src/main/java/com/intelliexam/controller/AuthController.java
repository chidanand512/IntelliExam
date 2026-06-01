package com.intelliexam.controller;

import com.intelliexam.model.Role;
import com.intelliexam.model.User;
import com.intelliexam.repository.UserRepository;
import com.intelliexam.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String phone,
                           @RequestParam String college,
                           @RequestParam Role role,
                           @RequestParam(required = false) String branch,
                           @RequestParam(required = false) String academicYear,
                           @RequestParam(required = false) String registerNumber,
                           Model model) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists.");
            return "register";
        }
        if (role == Role.STUDENT && (isBlank(branch) || isBlank(academicYear) || isBlank(registerNumber))) {
            model.addAttribute("error", "Branch, year, and register number are required for students.");
            return "register";
        }
        authService.register(fullName, email, password, phone, college, role, branch, academicYear, registerNumber);
        model.addAttribute("success", "Registration successful. Please login.");
        return "login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        return authService.login(email, password)
                .map(user -> {
                    session.setAttribute("user", user);
                    return user.getRole() == Role.EXAMINER ? "redirect:/examiner/dashboard" : "redirect:/student/dashboard";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid credentials.");
                    return "login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam Long userId,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String college,
                                @RequestParam(required = false) String branch,
                                @RequestParam(required = false) String academicYear,
                                @RequestParam(required = false) String registerNumber,
                                HttpSession session) {
        User updated = authService.updateProfile(userId, fullName, phone, college, branch, academicYear, registerNumber);
        session.setAttribute("user", updated);
        return updated.getRole() == Role.EXAMINER ? "redirect:/examiner/dashboard" : "redirect:/student/dashboard";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
