-- ============================================================
-- College Placement Management System - MySQL schema
-- Run from the project root with:
--   mysql -u root -p < database/placement_management.sql
-- WARNING: the first two lines drop and recreate the database.
-- ============================================================

DROP DATABASE IF EXISTS placement_management;
CREATE DATABASE placement_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE placement_management;

-- ------------------------------------------------------------
-- 1. users: shared login table for students and officers
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone         VARCHAR(15)  NOT NULL,
    role          ENUM('STUDENT', 'PLACEMENT_OFFICER') NOT NULL
);

-- ------------------------------------------------------------
-- 2. students: one row per student, same ID as users.user_id
-- ------------------------------------------------------------
CREATE TABLE students (
    student_id         INT PRIMARY KEY,
    department         VARCHAR(60)  NOT NULL DEFAULT '',
    course             VARCHAR(60)  NOT NULL DEFAULT '',
    graduation_year    INT          NOT NULL DEFAULT 0,
    cgpa               DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    backlogs           INT          NOT NULL DEFAULT 0,
    tenth_percentage   DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    twelfth_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    address            VARCHAR(255) NOT NULL DEFAULT '',
    skills             TEXT,
    placement_status   ENUM('NOT_PLACED', 'PLACED') NOT NULL DEFAULT 'NOT_PLACED',
    CONSTRAINT fk_student_user FOREIGN KEY (student_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_student_cgpa CHECK (cgpa BETWEEN 0 AND 10),
    CONSTRAINT chk_student_backlogs CHECK (backlogs >= 0),
    CONSTRAINT chk_student_10th CHECK (tenth_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_student_12th CHECK (twelfth_percentage BETWEEN 0 AND 100)
);

-- ------------------------------------------------------------
-- 3. placement_officers: one row per officer, same ID as users.user_id
-- ------------------------------------------------------------
CREATE TABLE placement_officers (
    officer_id  INT PRIMARY KEY,
    employee_id VARCHAR(30) NOT NULL UNIQUE,
    designation VARCHAR(80) NOT NULL,
    CONSTRAINT fk_officer_user FOREIGN KEY (officer_id)
        REFERENCES users (user_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 4. companies
-- ------------------------------------------------------------
CREATE TABLE companies (
    company_id   INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(120) NOT NULL UNIQUE,
    industry     VARCHAR(80)  NOT NULL,
    location     VARCHAR(120) NOT NULL,
    website      VARCHAR(200),
    hr_name      VARCHAR(100) NOT NULL,
    hr_email     VARCHAR(120) NOT NULL,
    hr_phone     VARCHAR(15)  NOT NULL
);

-- ------------------------------------------------------------
-- 5. jobs: RESTRICT means a company with jobs cannot be deleted
-- ------------------------------------------------------------
CREATE TABLE jobs (
    job_id               INT AUTO_INCREMENT PRIMARY KEY,
    company_id           INT NOT NULL,
    title                VARCHAR(120) NOT NULL,
    description          TEXT,
    job_type             ENUM('FULL_TIME', 'INTERNSHIP', 'INTERNSHIP_TO_FULL_TIME') NOT NULL,
    location             VARCHAR(120) NOT NULL,
    ctc                  DECIMAL(6,2) NOT NULL,
    minimum_cgpa         DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    maximum_backlogs     INT          NOT NULL DEFAULT 0,
    eligible_branches    VARCHAR(255) NOT NULL,
    graduation_year      INT          NOT NULL,
    application_deadline DATE         NOT NULL,
    minimum_tenth        DECIMAL(5,2) NOT NULL DEFAULT 0.00,   -- 0 = no requirement
    minimum_twelfth      DECIMAL(5,2) NOT NULL DEFAULT 0.00,   -- 0 = no requirement
    CONSTRAINT fk_job_company FOREIGN KEY (company_id)
        REFERENCES companies (company_id) ON DELETE RESTRICT,
    CONSTRAINT chk_job_ctc CHECK (ctc >= 0),
    CONSTRAINT chk_job_tenth CHECK (minimum_tenth BETWEEN 0 AND 100),
    CONSTRAINT chk_job_twelfth CHECK (minimum_twelfth BETWEEN 0 AND 100)
);

-- ------------------------------------------------------------
-- 6. resumes: exactly one resume per student (UNIQUE student_id)
-- ------------------------------------------------------------
CREATE TABLE resumes (
    resume_id            INT AUTO_INCREMENT PRIMARY KEY,
    student_id           INT  NOT NULL UNIQUE,
    education            TEXT,
    skills               TEXT,
    projects             TEXT,
    certifications       TEXT,
    achievements         TEXT,
    internships          TEXT,
    programming_languages TEXT,
    linkedin             VARCHAR(200),
    github               VARCHAR(200),
    portfolio            VARCHAR(200),
    CONSTRAINT fk_resume_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 7. placement_drives: a company visit on campus
-- ------------------------------------------------------------
CREATE TABLE placement_drives (
    drive_id              INT AUTO_INCREMENT PRIMARY KEY,
    company_id            INT NOT NULL,
    drive_date            DATE NOT NULL,
    registration_deadline DATE NOT NULL,
    venue                 VARCHAR(120) NOT NULL,
    status                ENUM('UPCOMING', 'OPEN', 'CLOSED', 'COMPLETED', 'CANCELLED')
                          NOT NULL DEFAULT 'UPCOMING',
    CONSTRAINT fk_drive_company FOREIGN KEY (company_id)
        REFERENCES companies (company_id) ON DELETE RESTRICT
);

-- ------------------------------------------------------------
-- 8. drive_jobs: many-to-many link between drives and jobs
-- ------------------------------------------------------------
CREATE TABLE drive_jobs (
    drive_id INT NOT NULL,
    job_id   INT NOT NULL,
    PRIMARY KEY (drive_id, job_id),
    CONSTRAINT fk_drive_jobs_drive FOREIGN KEY (drive_id)
        REFERENCES placement_drives (drive_id) ON DELETE CASCADE,
    CONSTRAINT fk_drive_jobs_job FOREIGN KEY (job_id)
        REFERENCES jobs (job_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 9. applications: UNIQUE (student_id, job_id) blocks duplicate applications
-- ------------------------------------------------------------
CREATE TABLE applications (
    application_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id       INT  NOT NULL,
    job_id           INT  NOT NULL,
    application_date DATE NOT NULL,
    status ENUM('APPLIED', 'SHORTLISTED', 'APTITUDE_TEST', 'TECHNICAL_INTERVIEW',
                'HR_INTERVIEW', 'SELECTED', 'REJECTED', 'WITHDRAWN')
           NOT NULL DEFAULT 'APPLIED',
    CONSTRAINT uq_student_job UNIQUE (student_id, job_id),
    CONSTRAINT fk_application_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_application_job FOREIGN KEY (job_id)
        REFERENCES jobs (job_id) ON DELETE RESTRICT
);

-- ------------------------------------------------------------
-- 10. interviews: one row per round; a round can be scheduled only once per application
-- ------------------------------------------------------------
CREATE TABLE interviews (
    interview_id   INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT  NOT NULL,
    round          ENUM('APTITUDE', 'TECHNICAL', 'HR', 'MANAGERIAL') NOT NULL,
    interview_date DATE NOT NULL,
    interview_time TIME NOT NULL,
    mode           VARCHAR(40)  NOT NULL,
    venue          VARCHAR(120) NOT NULL,
    interviewer    VARCHAR(100) NOT NULL,
    result         ENUM('PENDING', 'PASSED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    remarks        TEXT,
    CONSTRAINT uq_application_round UNIQUE (application_id, round),
    CONSTRAINT fk_interview_application FOREIGN KEY (application_id)
        REFERENCES applications (application_id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 11. placements: one placement per student, one per application
-- ------------------------------------------------------------
CREATE TABLE placements (
    placement_id   INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL UNIQUE,
    student_id     INT NOT NULL UNIQUE,
    company_id     INT NOT NULL,
    job_id         INT NOT NULL,
    ctc            DECIMAL(6,2) NOT NULL,
    placement_date DATE NOT NULL,
    offer_status   ENUM('OFFERED', 'ACCEPTED', 'DECLINED') NOT NULL DEFAULT 'OFFERED',
    joining_date   DATE NULL,
    CONSTRAINT fk_placement_application FOREIGN KEY (application_id)
        REFERENCES applications (application_id) ON DELETE RESTRICT,
    CONSTRAINT fk_placement_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_placement_company FOREIGN KEY (company_id)
        REFERENCES companies (company_id) ON DELETE RESTRICT,
    CONSTRAINT fk_placement_job FOREIGN KEY (job_id)
        REFERENCES jobs (job_id) ON DELETE RESTRICT
);