package com.intelliexam.service;

import com.intelliexam.model.Exam;
import com.intelliexam.model.Question;
import com.intelliexam.model.User;
import com.intelliexam.repository.ExamRepository;
import com.intelliexam.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public ExamService(ExamRepository examRepository, QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    public Exam saveExam(Exam exam) {
        if (exam.isPublicExam() && !hasText(exam.getPublicToken())) {
            exam.setPublicToken(UUID.randomUUID().toString().replace("-", ""));
        }
        if (!exam.isPublicExam()) {
            exam.setPublicToken(null);
        }
        return examRepository.save(exam);
    }

    public List<Exam> getExaminerExams(User examiner) {
        return examRepository.findByExaminer(examiner);
    }

    public List<Exam> getPublishedExams() {
        return examRepository.findByPublishedTrue();
    }

    public Optional<Exam> getExam(Long id) {
        return examRepository.findById(id);
    }

    public Optional<Exam> getExamByPublicToken(String token) {
        return examRepository.findByPublicToken(token);
    }

    public Question addQuestion(Exam exam, Question question) {
        int existing = questionRepository.findByExam(exam).size();
        int limit = exam.getTotalQuestions() == null ? 0 : exam.getTotalQuestions();
        if (limit > 0 && existing >= limit) {
            throw new IllegalStateException("Question limit reached. This exam allows only " + limit + " questions.");
        }
        question.setExam(exam);
        return questionRepository.save(question);
    }

    @Transactional
    public List<Question> addQuestions(Exam exam, List<Question> questions) {
        List<Question> validQuestions = questions.stream()
                .filter(this::hasQuestionContent)
                .toList();
        if (validQuestions.isEmpty()) {
            throw new IllegalStateException("Add at least one complete question.");
        }

        int existing = questionRepository.findByExam(exam).size();
        int limit = exam.getTotalQuestions() == null ? 0 : exam.getTotalQuestions();
        if (limit > 0 && existing + validQuestions.size() > limit) {
            int remaining = Math.max(limit - existing, 0);
            throw new IllegalStateException("You can add only " + remaining + " more question(s) to this exam.");
        }

        List<Question> saved = new ArrayList<>();
        for (Question question : validQuestions) {
            question.setExam(exam);
            saved.add(questionRepository.save(question));
        }
        return saved;
    }

    private boolean hasQuestionContent(Question question) {
        return hasText(question.getQuestionText())
                && hasText(question.getOptionA())
                && hasText(question.getOptionB())
                && hasText(question.getOptionC())
                && hasText(question.getOptionD())
                && hasText(question.getCorrectOption());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public List<Question> getQuestions(Exam exam) {
        return questionRepository.findByExam(exam);
    }

    public boolean isQuestionLimitReached(Exam exam) {
        int existing = questionRepository.findByExam(exam).size();
        int limit = exam.getTotalQuestions() == null ? 0 : exam.getTotalQuestions();
        return limit > 0 && existing >= limit;
    }
}
