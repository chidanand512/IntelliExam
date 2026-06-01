package com.intelliexam.repository;

import com.intelliexam.model.Attempt;
import com.intelliexam.model.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {
    List<AttemptAnswer> findByAttempt(Attempt attempt);
}
