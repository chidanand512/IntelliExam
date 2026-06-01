package com.intelliexam.controller;

import com.intelliexam.model.Attempt;
import com.intelliexam.model.Exam;
import com.intelliexam.model.User;
import com.intelliexam.service.AttemptService;
import com.intelliexam.service.AuthService;
import com.intelliexam.service.ExamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class PublicExamController {

    private final ExamService examService;
    private final AttemptService attemptService;
    private final AuthService authService;

    public PublicExamController(ExamService examService, AttemptService attemptService, AuthService authService) {
        this.examService = examService;
        this.attemptService = attemptService;
        this.authService = authService;
    }

    @GetMapping("/public/exam/{token}")
    public String publicExam(@PathVariable String token, Model model) {
        Exam exam = examService.getExamByPublicToken(token).orElseThrow();
        if (!exam.isPublicExam() || !exam.isPublished()) {
            return "redirect:/login";
        }
        model.addAttribute("exam", exam);
        return "public-exam";
    }

    @PostMapping("/public/exam/{token}/start")
    public String startPublicExam(@PathVariable String token,
                                  @RequestParam String fullName,
                                  @RequestParam String email,
                                  @RequestParam String phone,
                                  @RequestParam String college,
                                  @RequestParam String branch,
                                  @RequestParam String academicYear,
                                  @RequestParam String registerNumber,
                                  @RequestParam String examPassword,
                                  HttpSession session,
                                  Model model) {
        Exam exam = examService.getExamByPublicToken(token).orElseThrow();
        if (!exam.isPublicExam() || !exam.isPublished()) {
            return "redirect:/login";
        }
        if (!exam.getExamPassword().equals(examPassword)) {
            model.addAttribute("error", "Invalid exam password.");
            model.addAttribute("exam", exam);
            return "public-exam";
        }
        User user = authService.createOrUpdatePublicStudent(fullName, email, phone, college, branch, academicYear, registerNumber);
        session.setAttribute("user", user);
        Attempt attempt;
        try {
            attempt = attemptService.startAttempt(exam, user);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("exam", exam);
            return "public-exam";
        }
        session.setAttribute("attemptId", attempt.getId());
        List<?> questions = examService.getQuestions(exam);
        if (exam.isShuffleQuestions()) {
            Collections.shuffle(questions);
        }
        model.addAttribute("user", user);
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        return "exam-take";
    }
}
