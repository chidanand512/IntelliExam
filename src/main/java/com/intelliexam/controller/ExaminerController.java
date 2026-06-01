package com.intelliexam.controller;

import com.intelliexam.model.Attempt;
import com.intelliexam.model.AttemptAnswer;
import com.intelliexam.model.Exam;
import com.intelliexam.model.Question;
import com.intelliexam.model.Role;
import com.intelliexam.model.User;
import com.intelliexam.service.AttemptService;
import com.intelliexam.service.ExamService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/examiner")
public class ExaminerController {

    private final ExamService examService;
    private final AttemptService attemptService;

    public ExaminerController(ExamService examService, AttemptService attemptService) {
        this.examService = examService;
        this.attemptService = attemptService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EXAMINER) {
            return "redirect:/login";
        }
        List<Exam> exams = examService.getExaminerExams(user);
        long publishedCount = exams.stream().filter(Exam::isPublished).count();
        long draftCount = exams.size() - publishedCount;
        model.addAttribute("user", user);
        model.addAttribute("exams", exams);
        model.addAttribute("publishedCount", publishedCount);
        model.addAttribute("draftCount", draftCount);
        model.addAttribute("activePage", "dashboard");
        return "examiner-dashboard";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EXAMINER) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("activePage", "profile");
        return "profile";
    }

    @GetMapping("/exam/new")
    public String newExam(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EXAMINER) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("exam", new Exam());
        model.addAttribute("activePage", "dashboard");
        return "exam-form";
    }

    @PostMapping("/exam/save")
    public String saveExam(@ModelAttribute Exam exam, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EXAMINER) {
            return "redirect:/login";
        }
        exam.setExaminer(user);
        examService.saveExam(exam);
        return "redirect:/examiner/dashboard";
    }

    @GetMapping("/exam/{id}/questions")
    public String manageQuestions(@PathVariable Long id,
                                  @RequestParam(defaultValue = "false") boolean published,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EXAMINER) {
            return "redirect:/login";
        }
        Exam exam = examService.getExam(id).orElseThrow();
        List<Question> questions = examService.getQuestions(exam);
        model.addAttribute("user", user);
        model.addAttribute("exam", exam);
        model.addAttribute("question", new Question());
        model.addAttribute("questions", questions);
        model.addAttribute("limitReached", examService.isQuestionLimitReached(exam));
        model.addAttribute("attempts", attemptService.getExamAttempts(exam));
        if (published) {
            model.addAttribute("publishedExam", true);
            model.addAttribute("publishedTitle", exam.getTitle());
            model.addAttribute("publishedPassword", exam.getExamPassword());
            model.addAttribute("publishedPublic", exam.isPublicExam());
            model.addAttribute("publishedLink", exam.isPublicExam() && exam.getPublicToken() != null
                    ? "/public/exam/" + exam.getPublicToken()
                    : "/student/exam/" + exam.getId() + "/rules");
        }
        model.addAttribute("activePage", "dashboard");
        return "exam-questions";
    }

    @PostMapping("/exam/{id}/questions")
    public String addQuestion(@PathVariable Long id,
                              @RequestParam("questionText") List<String> questionTexts,
                              @RequestParam("optionA") List<String> optionAs,
                              @RequestParam("optionB") List<String> optionBs,
                              @RequestParam("optionC") List<String> optionCs,
                              @RequestParam("optionD") List<String> optionDs,
                              @RequestParam("correctOption") List<String> correctOptions,
                              RedirectAttributes redirect) {
        Exam exam = examService.getExam(id).orElseThrow();
        try {
            List<Question> questions = new ArrayList<>();
            int totalSubmitted = questionTexts.size();
            for (int i = 0; i < totalSubmitted; i++) {
                Question question = new Question();
                question.setQuestionText(questionTexts.get(i));
                question.setOptionA(optionAs.get(i));
                question.setOptionB(optionBs.get(i));
                question.setOptionC(optionCs.get(i));
                question.setOptionD(optionDs.get(i));
                question.setCorrectOption(correctOptions.get(i));
                questions.add(question);
            }
            int saved = examService.addQuestions(exam, questions).size();
            redirect.addFlashAttribute("success", saved + " question(s) added successfully.");
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/examiner/exam/" + id + "/questions";
    }

    @PostMapping("/exam/{id}/publish")
    public String publishExam(@PathVariable Long id) {
        Exam exam = examService.getExam(id).orElseThrow();
        exam.setPublished(true);
        examService.saveExam(exam);
        return "redirect:/examiner/exam/" + id + "/questions?published=true";
    }

    @PostMapping("/exam/{id}/publish-results")
    public String publishResults(@PathVariable Long id) {
        Exam exam = examService.getExam(id).orElseThrow();
        exam.setResultsPublished(!exam.isResultsPublished());
        examService.saveExam(exam);
        return "redirect:/examiner/exam/" + id + "/analytics";
    }

    @GetMapping("/exam/{id}/analytics")
    public String analytics(@PathVariable Long id,
                            @RequestParam(defaultValue = "") String search,
                            @RequestParam(defaultValue = "") String college,
                            @RequestParam(defaultValue = "") String branch,
                            @RequestParam(defaultValue = "") String academicYear,
                            @RequestParam(defaultValue = "") String studentType,
                            @RequestParam(defaultValue = "scoreDesc") String sort,
                            HttpSession session,
                            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EXAMINER) {
            return "redirect:/login";
        }
        Exam exam = examService.getExam(id).orElseThrow();
        List<Attempt> attempts = filterAndSortAttempts(exam, search, college, branch, academicYear, studentType, sort);

        int totalQuestions = exam.getTotalQuestions() == null ? 0 : exam.getTotalQuestions();
        int submitted = attempts.size();
        double avg = attempts.stream()
                .mapToInt(a -> a.getScore() == null ? 0 : a.getScore())
                .average().orElse(0);
        int top = attempts.stream()
                .mapToInt(a -> a.getScore() == null ? 0 : a.getScore())
                .max().orElse(0);
        int low = attempts.stream()
                .mapToInt(a -> a.getScore() == null ? 0 : a.getScore())
                .min().orElse(0);

        List<String> labels = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();
        for (Attempt a : attempts) {
            labels.add(a.getStudent().getFullName());
            scores.add(a.getScore() == null ? 0 : a.getScore());
        }

        model.addAttribute("user", user);
        model.addAttribute("exam", exam);
        model.addAttribute("attempts", attempts);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("submitted", submitted);
        model.addAttribute("avgScore", Math.round(avg * 10.0) / 10.0);
        model.addAttribute("topScore", top);
        model.addAttribute("lowScore", low);
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartScores", scores);
        model.addAttribute("search", search);
        model.addAttribute("college", college);
        model.addAttribute("branch", branch);
        model.addAttribute("academicYear", academicYear);
        model.addAttribute("studentType", studentType);
        model.addAttribute("sort", sort);
        model.addAttribute("activePage", "dashboard");
        return "examiner-analytics";
    }

    @GetMapping("/exam/{examId}/attempt/{attemptId}")
    public String studentDetail(@PathVariable Long examId,
                                @PathVariable Long attemptId,
                                HttpSession session,
                                Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EXAMINER) {
            return "redirect:/login";
        }
        Attempt attempt = attemptService.getAttempt(attemptId);
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
        model.addAttribute("activePage", "dashboard");
        return "student-analysis";
    }

    @GetMapping("/exam/{id}/report.csv")
    public void downloadReport(@PathVariable Long id,
                               @RequestParam(defaultValue = "") String search,
                               @RequestParam(defaultValue = "") String college,
                               @RequestParam(defaultValue = "") String branch,
                               @RequestParam(defaultValue = "") String academicYear,
                               @RequestParam(defaultValue = "") String studentType,
                               @RequestParam(defaultValue = "scoreDesc") String sort,
                               HttpServletResponse response) throws IOException {
        Exam exam = examService.getExam(id).orElseThrow();
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=exam-" + id + "-report.csv");
        List<Attempt> attempts = filterAndSortAttempts(exam, search, college, branch, academicYear, studentType, sort);
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Student,Email,College,Branch,Year,Register Number,Student Type,Phone,Score,Total,Percentage,SubmittedAt");
            for (Attempt attempt : attempts) {
                int score = attempt.getScore() == null ? 0 : attempt.getScore();
                int total = attempt.getTotalQuestions() == null ? 0 : attempt.getTotalQuestions();
                double pct = total == 0 ? 0 : (score * 100.0 / total);
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%d,%d,%.2f,%s%n",
                        attempt.getStudent().getFullName(),
                        attempt.getStudent().getEmail(),
                        attempt.getStudent().getCollege(),
                        attempt.getStudent().getBranch(),
                        attempt.getStudent().getAcademicYear(),
                        attempt.getStudent().getRegisterNumber(),
                        attempt.getStudent().isPublicStudent() ? "Public" : "Private",
                        attempt.getStudent().getPhone(),
                        score, total, pct,
                        attempt.getSubmittedAt());
            }
        }
    }

    @GetMapping("/exam/{id}/report.xlsx")
    public void downloadReportExcel(@PathVariable Long id,
                                    @RequestParam(defaultValue = "") String search,
                                    @RequestParam(defaultValue = "") String college,
                                    @RequestParam(defaultValue = "") String branch,
                                    @RequestParam(defaultValue = "") String academicYear,
                                    @RequestParam(defaultValue = "") String studentType,
                                    @RequestParam(defaultValue = "scoreDesc") String sort,
                                    HttpServletResponse response) throws IOException {
        Exam exam = examService.getExam(id).orElseThrow();
        List<Attempt> attempts = filterAndSortAttempts(exam, search, college, branch, academicYear, studentType, sort);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=exam-" + id + "-report.xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Final Report");

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("IntelliExam Final Report - " + exam.getTitle());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 11));

            Row metaRow = sheet.createRow(1);
            metaRow.createCell(0).setCellValue("Exam Date: " + exam.getExamDate() + " " + exam.getExamTime()
                    + "  |  Duration: " + exam.getDurationMinutes() + " min  |  Total Questions: " + exam.getTotalQuestions());

            String[] headers = {"#", "Student", "Email", "College", "Branch", "Year", "Register Number", "Student Type", "Phone", "Score", "Total", "Percentage"};
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int sl = 1;
            for (Attempt attempt : attempts) {
                Row row = sheet.createRow(rowIdx++);
                int score = attempt.getScore() == null ? 0 : attempt.getScore();
                int total = attempt.getTotalQuestions() == null ? 0 : attempt.getTotalQuestions();
                double pct = total == 0 ? 0 : (score * 100.0 / total);
                row.createCell(0).setCellValue(sl++);
                row.createCell(1).setCellValue(attempt.getStudent().getFullName());
                row.createCell(2).setCellValue(attempt.getStudent().getEmail());
                row.createCell(3).setCellValue(attempt.getStudent().getCollege() == null ? "" : attempt.getStudent().getCollege());
                row.createCell(4).setCellValue(attempt.getStudent().getBranch() == null ? "" : attempt.getStudent().getBranch());
                row.createCell(5).setCellValue(attempt.getStudent().getAcademicYear() == null ? "" : attempt.getStudent().getAcademicYear());
                row.createCell(6).setCellValue(attempt.getStudent().getRegisterNumber() == null ? "" : attempt.getStudent().getRegisterNumber());
                row.createCell(7).setCellValue(attempt.getStudent().isPublicStudent() ? "Public" : "Private");
                row.createCell(8).setCellValue(attempt.getStudent().getPhone() == null ? "" : attempt.getStudent().getPhone());
                row.createCell(9).setCellValue(score);
                row.createCell(10).setCellValue(total);
                row.createCell(11).setCellValue(String.format("%.2f%%", pct));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(response.getOutputStream());
        }
    }

    private List<Attempt> filterAndSortAttempts(Exam exam, String search, String college, String branch, String academicYear, String studentType, String sort) {
        Comparator<Attempt> comparator = switch (sort) {
            case "scoreAsc" -> Comparator.comparing(a -> a.getScore() == null ? 0 : a.getScore());
            case "nameAsc" -> Comparator.comparing(a -> safe(a.getStudent().getFullName()).toLowerCase());
            case "nameDesc" -> Comparator.comparing((Attempt a) -> safe(a.getStudent().getFullName()).toLowerCase()).reversed();
            case "dateAsc" -> Comparator.comparing(Attempt::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "dateDesc" -> Comparator.comparing(Attempt::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
            default -> Comparator.comparing((Attempt a) -> a.getScore() == null ? 0 : a.getScore()).reversed();
        };
        return attemptService.getExamAttempts(exam).stream()
                .filter(Attempt::isSubmitted)
                .filter(a -> matchesStudent(a.getStudent(), search, college, branch, academicYear, studentType))
                .sorted(comparator)
                .toList();
    }

    private boolean matchesStudent(User student, String search, String college, String branch, String academicYear, String studentType) {
        String needle = safe(search).toLowerCase();
        boolean matchesSearch = needle.isBlank()
                || safe(student.getFullName()).toLowerCase().contains(needle)
                || safe(student.getEmail()).toLowerCase().contains(needle)
                || safe(student.getRegisterNumber()).toLowerCase().contains(needle);
        return matchesSearch
                && matchesValue(student.getCollege(), college)
                && matchesValue(student.getBranch(), branch)
                && matchesValue(student.getAcademicYear(), academicYear)
                && matchesStudentType(student, studentType);
    }

    private boolean matchesStudentType(User student, String studentType) {
        return safe(studentType).isBlank()
                || ("public".equalsIgnoreCase(studentType) && student.isPublicStudent())
                || ("private".equalsIgnoreCase(studentType) && !student.isPublicStudent());
    }

    private boolean matchesValue(String actual, String expected) {
        return safe(expected).isBlank() || safe(actual).toLowerCase().contains(safe(expected).toLowerCase());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
