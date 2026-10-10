# College Placement Management System

A console-based Java OOP project for managing the college placement lifecycle:
student registration, companies, jobs, placement drives, eligibility checks,
applications, interviews, selection, placement records, statistics, and reports.

## Technology

- Java 17
- MySQL 8.x with JDBC (MySQL Connector/J 8.x)
- No frameworks, no GUI

## Architecture

Main -> ui (menus) -> service (business rules) -> dao (SQL) -> JDBC -> MySQL

Supporting packages: model, interfaces, enums, exception, util, file.

## Setup

1. Install MySQL and start the server.
2. Run the schema from the project root:
   `mysql -u root -p < database/placement_management.sql`
3. Edit `db.properties` with your MySQL username and password.
4. Put `mysql-connector-j-8.x.jar` in the `lib/` folder.
5. Compile and run (see the commands in the project guide).

## Default accounts

On first run, the program creates a placement officer account:

- Email: `admin@college.edu`
- Password: `admin123`

Change this password in a real deployment. The current version has no
password-change screen for officers.

Students register themselves from the login screen.

## Eligibility

A student is eligible for a job when all of these hold:

- CGPA meets the job's minimum (if one is set)
- 10th and 12th percentages meet the job's minimums (if set)
- Backlogs do not exceed the job's maximum
- Branch is in the job's eligible branches
- Graduation year matches the job's batch

A value of 0 on the job means "no requirement."

## Known limitations

- Password input is visible as it is typed (Scanner echoes input).
- Declined placement offers are not supported, because each student holds one placement record.
- Other open applications are not withdrawn automatically when a student is placed.
- Selection and placement are not one database transaction. A failed step is
  rolled back by deleting the placement record.
- Interview results do not change the application status automatically.

## Folder structure

- `src/` source code, grouped by package
- `database/` MySQL schema
- `reports/` generated TXT reports
- `lib/` JDBC driver jar
- `db.properties` database settings