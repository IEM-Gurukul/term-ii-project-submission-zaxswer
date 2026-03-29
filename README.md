[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/pG3gvzt-)
# PCCCS495 – Term II Project

## Project Title
Course Management System
---

## Problem Statement (max 150 words)
Educational institutions often struggle with fragmented course management workflows where student enrollment, weekly learning resources, and progress tracking are handled manually or through hardcoded systems. Such approaches make it difficult to add new students or teachers, update course content, and maintain consistency across student and teacher portals. In particular, hardcoded user and course data increases maintenance effort, causes data mismatch, and prevents real-time updates when records change. A centralized, file-driven solution is needed to manage users, courses, and weekly YouTube learning links dynamically. The system should allow students to log in, select topics, and track weekly progress, while enabling teachers to add or remove weekly resources without code changes. This problem calls for a lightweight, scalable course registration and learning platform that improves data flexibility, usability, and academic coordination.
---

## Target User
Academic Institutions, Coordinators, Students and Faculty members.
---

## Core Features

- Role-based login for Student and Teacher portals.
- Course registration with capacity and prerequisite validation.
- Topic selection, weekly video flow, and quiz-based learning progress.

---

## OOP Concepts Used

- Abstraction:
- Inheritance:
- Polymorphism:
- Exception Handling:
- Collections / Threads:

---

## Proposed Architecture Description

The project follows a layered package architecture:
- `model`: domain entities (`Student`, `Course`, `User`).
- `service`: business rules and orchestration (`CourseRegistrationSystem`, `AuthenticationManager`).
- `ui`: Swing-based views for login, student, and teacher interactions.
- `learning`: progress/topic persistence helpers.
- `quiz`: week-wise quiz generation and quiz-question model.

Data flow starts from `Main`, authenticates users via CSV-backed authentication, and routes users to role-specific portals. UI actions call service methods; services validate rules and update model state, while progress/topic selections are persisted for continuity.

---

## How to Run

1. Compile:
`javac -d bin src/com/course/registration/Main.java src/com/course/registration/model/*.java src/com/course/registration/service/*.java src/com/course/registration/ui/*.java src/com/learning/*.java src/com/course/quiz/*.java`

2. Run:
`java -cp bin com.course.registration.Main`

3. Data file:
Ensure users are available in `src/data/users.csv`.

---

## Git Discipline Notes
Minimum 10 meaningful commits required.
