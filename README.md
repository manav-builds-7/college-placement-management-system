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

### Prerequisites

- Java 17 or newer
- MySQL 8.x, running locally
- MySQL Connector/J 8.x

### Configure and initialize

Open PowerShell in the project root:

```powershell
cd E:\Programs\Java\Project
```

Edit `db.properties` and set `db.user` and `db.password` to your MySQL credentials.
Make sure `db.url` points to the database `placement_management`.

If the JDBC driver is not already in `lib/`, download it:

```powershell
New-Item -ItemType Directory -Force lib | Out-Null
Invoke-WebRequest `
  -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" `
  -OutFile "lib\mysql-connector-j-8.0.33.jar"
```

Import the schema from PowerShell using `cmd /c` (PowerShell does not support
the `<` input-redirection operator). MySQL will prompt for the root password:

```powershell
cmd /c "mysql -u root -p < database\placement_management.sql"
```

**Warning:** `database\placement_management.sql` drops and recreates the
`placement_management` database. Importing it again erases existing data.

### Compile and run

Run these commands from the project root in PowerShell:

```powershell
javac -cp "lib\mysql-connector-j-8.0.33.jar" -d out `
  (Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object FullName)
java -cp "out;lib\mysql-connector-j-8.0.33.jar" Main
```

The application starts in the console. Choose **Exit** in the main menu to
close it.

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