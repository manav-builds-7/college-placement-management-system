-- ============================================================
-- Demo dataset for College Placement Management System
-- Assumes: fresh schema, app started once (officer = user 1),
-- Asha = user 2, Dev = user 3, profiles entered as in the guide.
-- Dates are set around October 2026. Adjust them if you run this later.
-- ============================================================

USE placement_management;

-- ---------- 1. companies ----------
INSERT INTO companies (company_id, company_name, industry, location, website, hr_name, hr_email, hr_phone) VALUES
(1, 'Infosys',  'IT Services',     'Bengaluru', 'https://www.infosys.com',  'Priya Nair',  'hr@infosys.com',       '9000000001'),
(2, 'TCS',      'IT Services',     'Mumbai',    'https://www.tcs.com',     'Rahul Mehta', 'hr@tcs.com',           '9000000002'),
(3, 'Flipkart', 'E-commerce',      'Bengaluru', 'https://www.flipkart.com', 'Anita Rao',   'careers@flipkart.com', '9000000003'),
(4, 'Zomato',   'Food Technology', 'Gurugram',  'https://www.zomato.com',  'Karan Shah',  'talent@zomato.com',    '9000000004');

-- ---------- 2. jobs ----------
INSERT INTO jobs (job_id, company_id, title, description, job_type, location, ctc,
                  minimum_cgpa, maximum_backlogs, eligible_branches, graduation_year,
                  application_deadline, minimum_tenth, minimum_twelfth) VALUES
(1, 1, 'Backend Developer Intern', 'Build and test REST APIs for internal tools',
   'INTERNSHIP', 'Remote', 6.00, 7.50, 0, 'CSE, IT', 2027, '2026-12-31', 75.00, 0.00),
(2, 2, 'Systems Engineer', 'Software development and maintenance for client projects',
   'FULL_TIME', 'Pune', 7.00, 6.50, 1, 'CSE, IT, ECE', 2027, '2026-12-20', 60.00, 60.00),
(3, 3, 'SDE Intern', 'Work on product features for a live e-commerce platform',
   'INTERNSHIP_TO_FULL_TIME', 'Bengaluru', 12.00, 8.00, 0, 'CSE', 2027, '2026-11-30', 80.00, 80.00),
(4, 4, 'Frontend Developer', 'Build user-facing web features',
   'FULL_TIME', 'Gurugram', 9.50, 7.00, 0, 'CSE, IT', 2027, '2026-12-15', 0.00, 0.00),
(5, 1, 'QA Intern', 'Manual and automated testing of web applications',
   'INTERNSHIP', 'Mysuru', 4.50, 6.00, 2, 'CSE, IT, ECE, EEE', 2027, '2026-10-30', 0.00, 0.00);

-- ---------- 3. placement_drives ----------
INSERT INTO placement_drives (drive_id, company_id, drive_date, registration_deadline, venue, status) VALUES
(1, 1, '2026-10-25', '2026-10-18', 'Main Auditorium', 'OPEN'),
(2, 3, '2026-11-05', '2026-10-28', 'Seminar Hall 2',  'UPCOMING');

-- ---------- 4. drive_jobs ----------
INSERT INTO drive_jobs (drive_id, job_id) VALUES
(1, 1),
(1, 5),
(2, 3);

-- ---------- 5. resumes ----------
INSERT INTO resumes (student_id, education, skills, projects, certifications, achievements,
                     internships, programming_languages, linkedin, github, portfolio) VALUES
(2,
 'B.Tech CSE, Demo Institute of Technology, 2027, CGPA 8.2',
 'Java, SQL, Git, REST APIs',
 'Smart Recipe Generator (React, Java); College Placement Management System (Java, MySQL, JDBC)',
 'Oracle Java Certification (Associate)',
 'Winner, college coding contest 2025',
 'Summer intern, local web agency (2025)',
 'Java, Python, JavaScript',
 'https://www.linkedin.com/in/asha-verma-demo',
 'https://github.com/asha-demo',
 'https://asha-demo.dev'),
(3,
 'B.Tech CSE, Demo Institute of Technology, 2027, CGPA 7.8',
 'Python, React, SQL',
 'Campus Event Portal (React, Node.js); Expense Tracker (Python, Flask)',
 'AWS Cloud Practitioner (2025)',
 'Second place, inter-college hackathon',
 'Frontend intern, startup (2025, 3 months)',
 'Python, JavaScript, Java',
 'https://www.linkedin.com/in/dev-rao-demo',
 'https://github.com/dev-demo',
 NULL);

-- ---------- 6. applications ----------
INSERT INTO applications (application_id, student_id, job_id, application_date, status) VALUES
(1, 2, 1, '2026-10-01', 'SELECTED'),
(2, 3, 1, '2026-10-01', 'HR_INTERVIEW'),
(3, 3, 5, '2026-10-06', 'APPLIED'),
(4, 2, 4, '2026-10-02', 'SHORTLISTED'),
(5, 3, 4, '2026-10-02', 'REJECTED');

-- ---------- 7. interviews ----------
INSERT INTO interviews (interview_id, application_id, round, interview_date, interview_time,
                        mode, venue, interviewer, result, remarks) VALUES
(1, 1, 'TECHNICAL', '2026-10-03', '10:00:00', 'On-campus', 'Room 204',    'Dr. Suresh Kumar', 'PASSED',  'Strong DSA and SQL'),
(2, 1, 'HR',        '2026-10-04', '11:00:00', 'Online',    'Video call',  'Priya Nair',      'PASSED',  'Good communication'),
(3, 2, 'APTITUDE',  '2026-10-02', '09:30:00', 'On-campus', 'Hall A',      'Placement Cell',  'PASSED',  ''),
(4, 2, 'TECHNICAL', '2026-10-04', '10:00:00', 'On-campus', 'Room 204',    'Dr. Suresh Kumar', 'PASSED',  'Good problem solving'),
(5, 2, 'HR',        '2026-10-09', '11:00:00', 'Online',    'Video call',  'Priya Nair',      'PENDING', '');

-- ---------- 8. placements ----------
INSERT INTO placements (placement_id, application_id, student_id, company_id, job_id,
                        ctc, placement_date, offer_status, joining_date) VALUES
(1, 1, 2, 1, 1, 6.00, '2026-10-05', 'ACCEPTED', '2026-11-01');

-- Keep the student record consistent with the placement (the app does this during selection)
UPDATE students SET placement_status = 'PLACED' WHERE student_id = 2;