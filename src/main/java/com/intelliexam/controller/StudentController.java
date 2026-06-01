package com.intelliexam.controller;

import com.intelliexam.model.Attempt;
import com.intelliexam.model.AttemptAnswer;
import com.intelliexam.model.Exam;
import com.intelliexam.model.Role;
import com.intelliexam.model.User;
import com.intelliexam.service.AttemptService;
import com.intelliexam.service.ExamService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final ExamService examService;
    private final AttemptService attemptService;

    public StudentController(ExamService examService, AttemptService attemptService) {
        this.examService = examService;
        this.attemptService = attemptService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        List<Exam> exams = examService.getPublishedExams();
        List<Attempt> attempts = attemptService.getStudentAttempts(user);
        Set<Long> takenExamIds = attempts.stream()
                .filter(Attempt::isSubmitted)
                .map(a -> a.getExam().getId())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        long ongoingCount = exams.stream()
                .filter(e -> e.getExamDate() != null && !e.getExamDate().isAfter(today) && !takenExamIds.contains(e.getId()))
                .count();
        long upcomingCount = exams.stream()
                .filter(e -> e.getExamDate() != null && e.getExamDate().isAfter(today))
                .count();
        long resultsAvailable = attempts.stream()
                .filter(Attempt::isSubmitted)
                .filter(a -> a.getExam().isResultsPublished())
                .count();

        model.addAttribute("user", user);
        model.addAttribute("exams", exams);
        model.addAttribute("attempts", attempts);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("resultsAvailable", resultsAvailable);
        model.addAttribute("activePage", "dashboard");
        return "student-dashboard";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("activePage", "profile");
        return "profile";
    }

    @GetMapping("/ongoing")
    public String ongoingTests(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        LocalDate today = LocalDate.now();
        Set<Long> takenExamIds = attemptService.getStudentAttempts(user).stream()
                .filter(Attempt::isSubmitted)
                .map(a -> a.getExam().getId())
                .collect(Collectors.toSet());
        List<Exam> ongoing = examService.getPublishedExams().stream()
                .filter(e -> e.getExamDate() != null && !e.getExamDate().isAfter(today))
                .filter(e -> !takenExamIds.contains(e.getId()))
                .toList();
        model.addAttribute("user", user);
        model.addAttribute("exams", ongoing);
        model.addAttribute("activePage", "ongoing");
        return "ongoing-tests";
    }

    @GetMapping("/upcoming")
    public String upcomingTests(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        LocalDate today = LocalDate.now();
        List<Exam> upcoming = examService.getPublishedExams().stream()
                .filter(e -> e.getExamDate() != null && e.getExamDate().isAfter(today))
                .toList();
        model.addAttribute("user", user);
        model.addAttribute("exams", upcoming);
        model.addAttribute("activePage", "upcoming");
        return "upcoming-tests";
    }

    @GetMapping("/results")
    public String results(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        List<Attempt> attempts = attemptService.getStudentAttempts(user).stream()
                .filter(Attempt::isSubmitted)
                .toList();
        model.addAttribute("user", user);
        model.addAttribute("attempts", attempts);
        model.addAttribute("activePage", "results");
        return "student-results";
    }

    @GetMapping("/result/{attemptId}")
    public String resultDetail(@PathVariable Long attemptId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        Attempt attempt = attemptService.getAttempt(attemptId);
        if (!attempt.getStudent().getId().equals(user.getId())) {
            return "redirect:/student/results";
        }
        if (!attempt.getExam().isResultsPublished()) {
            model.addAttribute("user", user);
            model.addAttribute("attempt", attempt);
            model.addAttribute("activePage", "results");
            return "result-pending";
        }
        List<AttemptAnswer> answers = attemptService.getAttemptAnswers(attempt);
        int total = attempt.getTotalQuestions() == null ? 0 : attempt.getTotalQuestions();
        int score = attempt.getScore() == null ? 0 : attempt.getScore();
        int wrong = Math.max(total - score, 0);
        model.addAttribute("user", user);
        model.addAttribute("attempt", attempt);
        model.addAttribute("answers", answers);
        model.addAttribute("correctCount", score);
        model.addAttribute("wrongCount", wrong);
        model.addAttribute("totalCount", total);
        model.addAttribute("activePage", "results");
        return "student-analysis";
    }

    @GetMapping("/exam/{id}/rules")
    public String rules(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("exam", examService.getExam(id).orElseThrow());
        model.addAttribute("activePage", "ongoing");
        return "exam-rules";
    }

    @PostMapping("/exam/{id}/start")
    public String startExam(@PathVariable Long id,
                            @RequestParam String examPassword,
                            HttpSession session,
                            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.STUDENT) {
            return "redirect:/login";
        }
        Exam exam = examService.getExam(id).orElseThrow();
        if (!exam.getExamPassword().equals(examPassword)) {
            model.addAttribute("error", "Invalid exam password.");
            model.addAttribute("exam", exam);
            model.addAttribute("user", user);
            model.addAttribute("activePage", "ongoing");
            return "exam-rules";
        }
        Attempt attempt;
        try {
            attempt = attemptService.startAttempt(exam, user);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("exam", exam);
            model.addAttribute("user", user);
            model.addAttribute("activePage", "ongoing");
            return "exam-rules";
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

    @PostMapping("/exam/{id}/submit")
    public String submitExam(@PathVariable Long id,
                             @RequestParam Map<String, String> payload,
                             HttpSession session,
                             Model model) {
        Long attemptId = (Long) session.getAttribute("attemptId");
        if (attemptId == null) {
            return "redirect:/student/dashboard";
        }
        Attempt attempt = attemptService.getAttempt(attemptId);
        Map<Long, String> answers = new HashMap<>();
        payload.forEach((k, v) -> {
            if (k.startsWith("q_")) {
                answers.put(Long.parseLong(k.substring(2)), v);
            }
        });
        Attempt submitted = attemptService.submitAttempt(attempt, answers);
        session.removeAttribute("attemptId");
        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("attempt", submitted);
        model.addAttribute("activePage", "results");
        return "exam-result";
    }

    @GetMapping("/attempt/{id}/download")
    public void downloadResult(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Attempt attempt = attemptService.getAttempt(id);
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=result-" + id + ".txt");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("IntelliExam Result");
            writer.println("Student: " + attempt.getStudent().getFullName());
            writer.println("Exam: " + attempt.getExam().getTitle());
            writer.println("Score: " + attempt.getScore() + "/" + attempt.getTotalQuestions());
            writer.println("Submitted At: " + attempt.getSubmittedAt());
        }
    }
}
