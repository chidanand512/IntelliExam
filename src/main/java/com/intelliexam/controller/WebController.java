package com.intelliexam.controller;

import com.intelliexam.model.Role;
import com.intelliexam.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String landing(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user.getRole() == Role.EXAMINER
                    ? "redirect:/examiner/dashboard"
                    : "redirect:/student/dashboard";
        }
        return "landing";
    }

    @GetMapping("/about")
    public String about(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user.getRole() == Role.EXAMINER
                    ? "redirect:/examiner/dashboard"
                    : "redirect:/student/dashboard";
        }
        return "about";
    }

    @GetMapping("/contact")
    public String contact(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user.getRole() == Role.EXAMINER
                    ? "redirect:/examiner/dashboard"
                    : "redirect:/student/dashboard";
        }
        return "contact";
    }
}
