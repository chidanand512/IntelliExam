IntelliExam – Smart Online Examination Platform

📌 Overview

IntelliExam is a comprehensive web-based Online Examination Management System developed using Spring Boot, Thymeleaf, MySQL, JPA/Hibernate, HTML, CSS, and JavaScript.

The platform enables educational institutions, trainers, and organizations to conduct secure online examinations with features such as negative marking, fullscreen examination mode, public exam sharing, result publishing, performance analytics, and Excel report generation.

The system follows the MVC (Model-View-Controller) architecture and implements a layered design consisting of Controllers, Services, Repositories, and Entities to ensure scalability, maintainability, and clean code practices.

---

🚀 Key Features

👨‍🏫 Examiner Module

- Secure Examiner Registration & Login
- Create and Manage Exams
- Configure Exam Duration
- Enable/Disable Negative Marking
- Configure Negative Mark Value
- Enable Strict Fullscreen Mode
- Publish/Unpublish Exams
- Generate Public Exam Links
- Add Questions Manually
- Bulk Question Upload Support
- View Student Attempts
- Publish Results
- Download Results as Excel Reports
- View Exam Analytics Dashboard

---

👨‍🎓 Student Module

- Secure Student Registration & Login
- View Upcoming Exams
- View Ongoing Exams
- Attempt Available Exams
- Fullscreen Examination Experience
- Auto Evaluation
- View Published Results
- Track Exam History

---

🧠 Smart Examination Features

Negative Marking

Supports configurable negative marking.

---

Fullscreen Exam Mode

Students can be required to stay in fullscreen mode during examinations to reduce malpractice and distractions.

---

Public Exam Access

Examiners can generate unique public exam links using secure tokens, allowing students to access exams without searching manually.

---

Automatic Evaluation

The system automatically evaluates submitted answers and calculates scores instantly after submission.

---

Result Publishing Control

Results remain hidden until the examiner explicitly publishes them.

This allows examiners to review outcomes before making them visible to students.

---

Analytics Dashboard

Provides valuable insights including:

- Total Participants
- Highest Score
- Lowest Score
- Average Score
- Student Performance Statistics
- Exam Participation Metrics

---

Excel Report Generation

Results can be exported to Excel using Apache POI for further analysis and record keeping.

---

🏗️ System Architecture

The project follows a layered MVC architecture.

Presentation Layer
│
├── Thymeleaf Views
├── HTML
├── CSS
└── JavaScript

Controller Layer
│
├── AuthController
├── ExamController
├── StudentController
└── AnalyticsController

Service Layer
│
├── AuthService
├── ExamService
├── AttemptService
└── AnalyticsService

Repository Layer
│
├── UserRepository
├── ExamRepository
├── QuestionRepository
└── AttemptRepository

Database Layer
│
└── MySQL

---

🛠️ Technology Stack

Backend

- Java 17+
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate

Frontend

- HTML5
- CSS3
- JavaScript
- Thymeleaf

Database

- MySQL

Security

- BCrypt Password Encoder

Reporting

- Apache POI

Build Tool

- Maven

---

📂 Project Structure

src
│
├── controller
│   ├── AuthController
│   ├── ExamController
│   ├── StudentController
│   └── AnalyticsController
│
├── service
│   ├── AuthService
│   ├── ExamService
│   ├── AttemptService
│   └── AnalyticsService
│
├── repository
│   ├── UserRepository
│   ├── ExamRepository
│   ├── QuestionRepository
│   └── AttemptRepository
│
├── model
│   ├── User
│   ├── Exam
│   ├── Question
│   └── Attempt
│
├── templates
│
├── static
│
└── resources

---

🔄 Workflow

Step 1 – Registration & Login

Users register as either:

- Examiner
- Student

Passwords are encrypted using BCrypt before storage.

---

Step 2 – Exam Creation

The examiner creates an exam by specifying:

- Exam Name
- Description
- Duration
- Exam Date
- Negative Marking Configuration
- Fullscreen Mode Settings

---

Step 3 – Question Management

Questions are added with:

- Question Statement
- Four Options
- Correct Answer

Questions are associated with a specific exam.

---

Step 4 – Exam Publishing

The examiner publishes the exam.

A secure public token can be generated for public access.

---

Step 5 – Student Examination

Students start the exam and answer questions within the configured duration.

---

Step 6 – Submission & Evaluation

The system:

- Evaluates answers
- Calculates score
- Applies negative marking
- Stores attempt details

---

Step 7 – Result Publishing

Results become visible only after examiner approval.

---

Step 8 – Analytics & Reports

The examiner can:

- View statistics
- Analyze performance
- Export reports

---

🔐 Security Features

- BCrypt Password Encryption
- Session-Based Authentication
- Exam Attempt Validation
- Duplicate Attempt Prevention
- Secure Public Exam Tokens
- Role-Based User Management

---

📈 Future Enhancements

- Spring Security Integration
- AI-Based Proctoring
- Webcam Monitoring
- Face Recognition Verification
- Email Notifications
- SMS Alerts
- Cloud Deployment
- Mobile Application

---

🎯 Learning Outcomes

This project demonstrates practical implementation of:

- Java Enterprise Application Development
- Spring Boot Framework
- MVC Architecture
- Object Relational Mapping (ORM)
- Database Design
- Authentication & Authorization
- Secure Coding Practices
- Report Generation
- Full Stack Development

---

👨‍💻 Author

Chidanand Gowda

Passionate Full Stack Java Developer focused on building scalable, secure, and user-friendly web applications using Java, Spring Boot, MySQL, and modern web technologies.

---

⭐ Support

If you found this project useful, consider giving it a Star on GitHub and sharing your feedback.
