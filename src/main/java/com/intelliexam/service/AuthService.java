package com.intelliexam.service;

import com.intelliexam.model.Role;
import com.intelliexam.model.User;
import com.intelliexam.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> encoder.matches(rawPassword, user.getPassword()));
    }

    public User register(String fullName, String email, String password, String phone, String college, Role role,
                         String branch, String academicYear, String registerNumber) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setPhone(phone);
        user.setCollege(college);
        user.setRole(role);
        user.setPublicStudent(false);
        if (role == Role.STUDENT) {
            user.setBranch(branch);
            user.setAcademicYear(academicYear);
            user.setRegisterNumber(registerNumber);
        }
        return userRepository.save(user);
    }

    public User createOrUpdatePublicStudent(String fullName, String email, String phone, String college,
                                            String branch, String academicYear, String registerNumber) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        if (user.getId() == null) {
            user.setEmail(email);
            user.setPassword(encoder.encode("public-" + System.nanoTime()));
            user.setRole(Role.STUDENT);
        }
        user.setPublicStudent(true);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setCollege(college);
        user.setBranch(branch);
        user.setAcademicYear(academicYear);
        user.setRegisterNumber(registerNumber);
        return userRepository.save(user);
    }

    public User updateProfile(Long id, String fullName, String phone, String college,
                              String branch, String academicYear, String registerNumber) {
        User user = userRepository.findById(id).orElseThrow();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setCollege(college);
        if (user.getRole() == Role.STUDENT) {
            user.setBranch(branch);
            user.setAcademicYear(academicYear);
            user.setRegisterNumber(registerNumber);
        }
        return userRepository.save(user);
    }
}
