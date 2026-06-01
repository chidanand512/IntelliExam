package com.intelliexam.service;

import com.intelliexam.model.Attempt;
import com.intelliexam.model.AttemptAnswer;
import com.intelliexam.model.Exam;
import com.intelliexam.model.Question;
import com.intelliexam.model.User;
import com.intelliexam.repository.AttemptAnswerRepository;
import com.intelliexam.repository.AttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final ExamService examService;

    public AttemptService(AttemptRepository attemptRepository,
                          AttemptAnswerRepository attemptAnswerRepository,
                          ExamService examService) {
        this.attemptRepository = attemptRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.examService = examService;
    }

    public Attempt startAttempt(Exam exam, User student) {
        return attemptRepository.findByExamAndStudent(exam, student)
                .map(attempt -> {
                    if (attempt.isSubmitted()) {
                        throw new IllegalStateException("You have already attempted this exam.");
                    }
                    return attempt;
                })
                .orElseGet(() -> {
                    Attempt attempt = new Attempt();
                    attempt.setExam(exam);
                    attempt.setStudent(student);
                    attempt.setStartedAt(LocalDateTime.now());
                    attempt.setSubmitted(false);
                    return attemptRepository.save(attempt);
                });
    }

    public Attempt submitAttempt(Attempt attempt, Map<Long, String> submittedAnswers) {
        if (attempt.isSubmitted()) {
            return attempt;
        }
        List<Question> questions = examService.getQuestions(attempt.getExam());
        int score = 0;
        for (Question q : questions) {
            String answer = submittedAnswers.get(q.getId());
            boolean correct = answer != null && answer.equalsIgnoreCase(q.getCorrectOption());
            if (correct) {
                score++;
            } else if (answer != null && attempt.getExam().isNegativeMarking()) {
                int penalty = attempt.getExam().getNegativeMarksPerWrong() == null ? 1 : attempt.getExam().getNegativeMarksPerWrong();
                score -= penalty;
            }
            AttemptAnswer aa = new AttemptAnswer();
            aa.setAttempt(attempt);
            aa.setQuestion(q);
            aa.setSelectedOption(answer);
            aa.setCorrect(correct);
            attemptAnswerRepository.save(aa);
        }
        attempt.setScore(score);
        attempt.setTotalQuestions(questions.size());
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setSubmitted(true);
        return attemptRepository.save(attempt);
    }

    public List<Attempt> getExamAttempts(Exam exam) {
        return attemptRepository.findByExam(exam);
    }

    public List<Attempt> getStudentAttempts(User student) {
        return attemptRepository.findByStudent(student);
    }

    public List<Attempt> getStudentAttemptsForPublishedResults(User student) {
        return attemptRepository.findByStudent(student).stream()
                .filter(a -> a.getExam().isResultsPublished())
                .toList();
    }

    public Attempt getAttempt(Long attemptId) {
        return attemptRepository.findById(attemptId).orElseThrow();
    }

    public List<AttemptAnswer> getAttemptAnswers(Attempt attempt) {
        return attemptAnswerRepository.findByAttempt(attempt);
    }
}
