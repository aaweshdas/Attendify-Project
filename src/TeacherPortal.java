import java.sql.*;
import java.util.List;
import java.util.Scanner;

/**
 * TeacherPortal - Teacher Dashboard
 * Student management (CRUD) and attendance operations
 */
public class TeacherPortal {

    // ANSI Color Codes
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[36m";
    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String WHITE = "\033[97m";
    private static final String BG_BLUE = "\033[44m";
    private static final String DIM = "\033[2m";

    private Scanner scanner;
    private StudentDAO studentDAO;
    private AttendanceDAO attendanceDAO;

    public TeacherPortal(Scanner scanner) {
        this.scanner = scanner;
        this.studentDAO = new StudentDAO();
        this.attendanceDAO = new AttendanceDAO();
    }

    public void run() {
        loadSampleDataIfNeeded();

        boolean running = true;
        while (running) {
            clearScreen();
            printTeacherHeader();
            printMenu();
            int choice = getIntInput(CYAN + "  Enter your choice: " + RESET);

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAllStudents();
                case 3 -> searchStudent();
                case 4 -> updateStudent();
                case 5 -> deleteStudent();
                case 6 -> markAttendance();
                case 7 -> viewAttendance();
                case 8 -> viewAttendanceSummary();
                case 0 -> {
                    running = false;
                    System.out.println(GREEN + "\n  Logging out from Teacher Portal..." + RESET);
                    sleep(800);
                }
                default -> {
                    System.out.println(RED + "\n  Invalid choice! Please try again." + RESET);
                    sleep(1000);
                }
            }
        }
    }

    private void printTeacherHeader() {
        System.out.println(BOLD + BLUE + "  +============================================================+" + RESET);
        System.out.println(BOLD + BLUE + "  |                                                            |" + RESET);
        System.out.println(BOLD + BLUE + "  |" + WHITE
                + "                 TEACHER PORTAL - DASHBOARD                 " + RESET + BOLD + BLUE + "|" + RESET);
        System.out.println(BOLD + BLUE + "  |                                                            |" + RESET);
        System.out.println(BOLD + BLUE + "  +============================================================+" + RESET);
        System.out.println();
    }

    private void printMenu() {
        System.out.println(DIM + "  -------------  Student Management  -------------" + RESET);
        System.out.println(CYAN + "  [1]" + WHITE + "  Add Student" + RESET);
        System.out.println(CYAN + "  [2]" + WHITE + "  View All Students" + RESET);
        System.out.println(CYAN + "  [3]" + WHITE + "  Search Student" + RESET);
        System.out.println(CYAN + "  [4]" + WHITE + "  Update Student" + RESET);
        System.out.println(CYAN + "  [5]" + WHITE + "  Delete Student" + RESET);
        System.out.println(DIM + "  -------------  Attendance  ---------------------" + RESET);
        System.out.println(CYAN + "  [6]" + WHITE + "  Mark Attendance" + RESET);
        System.out.println(CYAN + "  [7]" + WHITE + "  View Attendance" + RESET);
        System.out.println(CYAN + "  [8]" + WHITE + "  View Attendance Summary" + RESET);
        System.out.println(DIM + "  ------------------------------------------------" + RESET);
        System.out.println(RED + "  [0]" + WHITE + "  Logout" + RESET);
        System.out.println();
    }

    // ==================== STUDENT MANAGEMENT ====================

    private void addStudent() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Add New Student ═══" + RESET);
        System.out.print(CYAN + "  Roll Number : " + RESET);
        String rollNumber = scanner.nextLine().trim();
        System.out.print(CYAN + "  Name        : " + RESET);
        String name = scanner.nextLine().trim();
        System.out.print(CYAN + "  Email       : " + RESET);
        String email = scanner.nextLine().trim();
        System.out.print(CYAN + "  Department  : " + RESET);
        String department = scanner.nextLine().trim();

        if (rollNumber.isEmpty() || name.isEmpty()) {
            System.out.println(RED + "\n  Error: Roll number and name are required!" + RESET);
            pressEnter();
            return;
        }

        try {
            Student student = new Student(rollNumber, name, email, department);
            if (studentDAO.addStudent(student)) {
                System.out.println(GREEN + "\n  [OK] Student added successfully!" + RESET);
            } else {
                System.out.println(RED + "\n  [X] Roll number already exists!" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewAllStudents() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ All Students ═══" + RESET);
        try {
            List<Student> students = studentDAO.getAllStudents();
            if (students.isEmpty()) {
                System.out.println(YELLOW + "\n  No students found." + RESET);
            } else {
                System.out.println(
                        DIM + "  ┌──────────────┬──────────────────────┬───────────────────────────┬──────────────┐"
                                + RESET);
                System.out.printf(BOLD + "  │ %-12s │ %-20s │ %-25s │ %-12s │%n" + RESET,
                        "Roll Number", "Name", "Email", "Department");
                System.out.println(
                        DIM + "  ├──────────────┼──────────────────────┼───────────────────────────┼──────────────┤"
                                + RESET);
                for (Student s : students) {
                    System.out.println("  " + s.toString());
                }
                System.out.println(
                        DIM + "  └──────────────┴──────────────────────┴───────────────────────────┴──────────────┘"
                                + RESET);
                System.out.println(CYAN + "\n  Total: " + students.size() + " students" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void searchStudent() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Search Student ═══" + RESET);
        System.out.print(CYAN + "  Enter Roll Number: " + RESET);
        String rollNumber = scanner.nextLine().trim();

        try {
            Student student = studentDAO.searchByRollNumber(rollNumber);
            if (student != null) {
                System.out.println(GREEN + "\n  Student Found:" + RESET);
                System.out.println(WHITE + "  Roll Number : " + CYAN + student.getRollNumber() + RESET);
                System.out.println(WHITE + "  Name        : " + CYAN + student.getName() + RESET);
                System.out.println(WHITE + "  Email       : " + CYAN + student.getEmail() + RESET);
                System.out.println(WHITE + "  Department  : " + CYAN + student.getDepartment() + RESET);
                System.out.println(WHITE + "  Total Class : " + CYAN + student.getTotalClasses() + RESET);
            } else {
                System.out.println(RED + "\n  Student not found!" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void updateStudent() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Update Student ═══" + RESET);
        System.out.print(CYAN + "  Enter Roll Number: " + RESET);
        String rollNumber = scanner.nextLine().trim();

        try {
            Student student = studentDAO.searchByRollNumber(rollNumber);
            if (student == null) {
                System.out.println(RED + "\n  Student not found!" + RESET);
                pressEnter();
                return;
            }

            System.out.println(GREEN + "\n  Current Details:" + RESET);
            System.out.println(WHITE + "  Name       : " + student.getName() + RESET);
            System.out.println(WHITE + "  Email      : " + student.getEmail() + RESET);
            System.out.println(WHITE + "  Department : " + student.getDepartment() + RESET);
            System.out.println(YELLOW + "\n  (Press Enter to keep current value)" + RESET);

            System.out.print(CYAN + "  New Name       : " + RESET);
            String name = scanner.nextLine().trim();
            System.out.print(CYAN + "  New Email      : " + RESET);
            String email = scanner.nextLine().trim();
            System.out.print(CYAN + "  New Department : " + RESET);
            String department = scanner.nextLine().trim();

            if (studentDAO.updateStudent(rollNumber,
                    name.isEmpty() ? null : name,
                    email.isEmpty() ? null : email,
                    department.isEmpty() ? null : department)) {
                System.out.println(GREEN + "\n  [OK] Student updated successfully!" + RESET);
            } else {
                System.out.println(YELLOW + "\n  No changes made." + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void deleteStudent() {
        clearScreen();
        System.out.println(BOLD + RED + "\n  ═══ Delete Student ═══" + RESET);
        System.out.print(CYAN + "  Enter Roll Number: " + RESET);
        String rollNumber = scanner.nextLine().trim();

        try {
            Student student = studentDAO.searchByRollNumber(rollNumber);
            if (student == null) {
                System.out.println(RED + "\n  Student not found!" + RESET);
                pressEnter();
                return;
            }

            System.out.println(YELLOW + "\n  Student: " + student.getName() + " (" + rollNumber + ")" + RESET);
            System.out.print(RED + "  Are you sure? (yes/no): " + RESET);
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("yes") || confirm.equals("y")) {
                if (studentDAO.deleteStudent(rollNumber)) {
                    System.out.println(GREEN + "\n  [OK] Student deleted successfully!" + RESET);
                } else {
                    System.out.println(RED + "\n  [X] Failed to delete student!" + RESET);
                }
            } else {
                System.out.println(YELLOW + "\n  Deletion cancelled." + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    // ==================== ATTENDANCE ====================

    private void markAttendance() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Mark Attendance ═══" + RESET);
        System.out.print(CYAN + "  Enter Date (YYYY-MM-DD): " + RESET);
        String date = scanner.nextLine().trim();

        if (date.isEmpty()) {
            System.out.println(RED + "\n  Date is required!" + RESET);
            pressEnter();
            return;
        }

        try {
            List<Student> students = studentDAO.getAllStudents();
            if (students.isEmpty()) {
                System.out.println(YELLOW + "\n  No students registered yet." + RESET);
                pressEnter();
                return;
            }

            System.out.println(YELLOW + "  Mark P (Present) or A (Absent) for each student:\n" + RESET);

            for (Student s : students) {
                System.out.print(WHITE + "  " + s.getRollNumber() + " - " + s.getName() + " [P/A]: " + RESET);
                String input = scanner.nextLine().trim().toUpperCase();
                String status = input.equals("A") ? "ABSENT" : "PRESENT";
                attendanceDAO.markAttendance(s.getRollNumber(), date, status);
            }

            System.out.println(GREEN + "\n  [OK] Attendance marked for " + students.size() + " students!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + "\n  Error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewAttendance() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ View Attendance ═══" + RESET);
        System.out.println(CYAN + "  [1]" + WHITE + " By Roll Number" + RESET);
        System.out.println(CYAN + "  [2]" + WHITE + " By Date" + RESET);
        int choice = getIntInput(CYAN + "  Choice: " + RESET);

        try {
            if (choice == 1) {
                System.out.print(CYAN + "  Enter Roll Number: " + RESET);
                String roll = scanner.nextLine().trim();
                List<String[]> records = attendanceDAO.getAttendanceByRollNumber(roll);
                if (records.isEmpty()) {
                    System.out.println(YELLOW + "\n  No attendance records found." + RESET);
                } else {
                    System.out.println(DIM + "\n  ┌────────────┬──────────┐" + RESET);
                    System.out.printf(BOLD + "  │ %-10s │ %-8s │%n" + RESET, "Date", "Status");
                    System.out.println(DIM + "  ├────────────┼──────────┤" + RESET);
                    for (String[] r : records) {
                        String statusColor = r[1].equals("PRESENT") ? GREEN : RED;
                        System.out.printf("  │ %-10s │ %s%-8s%s │%n", r[0], statusColor, r[1], RESET);
                    }
                    System.out.println(DIM + "  └────────────┴──────────┘" + RESET);
                }
            } else if (choice == 2) {
                System.out.print(CYAN + "  Enter Date (YYYY-MM-DD): " + RESET);
                String date = scanner.nextLine().trim();
                List<String[]> records = attendanceDAO.getAttendanceByDate(date);
                if (records.isEmpty()) {
                    System.out.println(YELLOW + "\n  No records for this date." + RESET);
                } else {
                    System.out.println(DIM + "\n  ┌──────────────┬──────────────────────┬────────────┐" + RESET);
                    System.out.printf(BOLD + "  │ %-12s │ %-20s │ %-10s │%n" + RESET, "Roll Number", "Name", "Status");
                    System.out.println(DIM + "  ├──────────────┼──────────────────────┼────────────┤" + RESET);
                    for (String[] r : records) {
                        String statusColor = r[2].equals("PRESENT") ? GREEN : r[2].equals("ABSENT") ? RED : YELLOW;
                        System.out.printf("  │ %-12s │ %-20s │ %s%-10s%s │%n", r[0], r[1], statusColor, r[2], RESET);
                    }
                    System.out.println(DIM + "  └──────────────┴──────────────────────┴────────────┘" + RESET);
                }
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewAttendanceSummary() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Attendance Summary ═══" + RESET);
        try {
            List<String[]> summary = attendanceDAO.getAttendanceSummary();
            if (summary.isEmpty()) {
                System.out.println(YELLOW + "\n  No attendance data available." + RESET);
            } else {
                System.out.println(
                        DIM + "  ┌──────────────┬──────────────────────┬──────────┬─────────┬────────────┐" + RESET);
                System.out.printf(BOLD + "  │ %-12s │ %-20s │ %-8s │ %-7s │ %-10s │%n" + RESET,
                        "Roll Number", "Name", "Total", "Present", "Percentage");
                System.out.println(
                        DIM + "  ├──────────────┼──────────────────────┼──────────┼─────────┼────────────┤" + RESET);
                for (String[] s : summary) {
                    String color = Double.parseDouble(s[4].replace("%", "")) >= 75 ? GREEN : RED;
                    System.out.printf("  │ %-12s │ %-20s │ %-8s │ %-7s │ %s%-10s%s │%n",
                            s[0], s[1], s[2], s[3], color, s[4], RESET);
                }
                System.out.println(
                        DIM + "  └──────────────┴──────────────────────┴──────────┴─────────┴────────────┘" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    // ==================== SAMPLE DATA ====================

    private void loadSampleDataIfNeeded() {
        try {
            if (studentDAO.getStudentCount() == 0) {
                String[][] sampleStudents = {
                        { "CS001", "Rahul Sharma", "rahul@email.com", "CS" },
                        { "CS002", "Priya Patel", "priya@email.com", "CS" },
                        { "CS003", "Amit Kumar", "amit@email.com", "CS" },
                        { "EC001", "Neha Gupta", "neha@email.com", "ECE" },
                        { "EC002", "Rohan Singh", "rohan@email.com", "ECE" },
                        { "ME001", "Sneha Reddy", "sneha@email.com", "ME" },
                        { "ME002", "Vikas Jain", "vikas@email.com", "ME" },
                        { "IT001", "Ananya Das", "ananya@email.com", "IT" },
                        { "IT002", "Karthik Nair", "karthik@email.com", "IT" },
                        { "EE001", "Deepa Menon", "deepa@email.com", "EE" }
                };

                for (String[] s : sampleStudents) {
                    studentDAO.addStudent(new Student(s[0], s[1], s[2], s[3]));
                }

                String[] dates = { "2025-01-06", "2025-01-07", "2025-01-08", "2025-01-09", "2025-01-10" };
                String[][] attendance = {
                        { "PRESENT", "PRESENT", "ABSENT", "PRESENT", "PRESENT" },
                        { "PRESENT", "ABSENT", "PRESENT", "PRESENT", "PRESENT" },
                        { "ABSENT", "PRESENT", "PRESENT", "ABSENT", "PRESENT" },
                        { "PRESENT", "PRESENT", "PRESENT", "PRESENT", "ABSENT" },
                        { "PRESENT", "PRESENT", "PRESENT", "PRESENT", "PRESENT" },
                        { "ABSENT", "ABSENT", "PRESENT", "PRESENT", "PRESENT" },
                        { "PRESENT", "PRESENT", "ABSENT", "ABSENT", "PRESENT" },
                        { "PRESENT", "PRESENT", "PRESENT", "PRESENT", "PRESENT" },
                        { "ABSENT", "PRESENT", "PRESENT", "PRESENT", "ABSENT" },
                        { "PRESENT", "ABSENT", "PRESENT", "PRESENT", "PRESENT" }
                };

                for (int i = 0; i < sampleStudents.length; i++) {
                    for (int j = 0; j < dates.length; j++) {
                        attendanceDAO.markAttendance(sampleStudents[i][0], dates[j], attendance[i][j]);
                    }
                }
            }
        } catch (Exception e) {
            // Silently skip sample data on error
        }
    }

    // ==================== UTILITIES ====================

    private int getIntInput(String prompt) {
        System.out.print(prompt);
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            return value;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void pressEnter() {
        System.out.print(DIM + "\n  Press Enter to continue..." + RESET);
        scanner.nextLine();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
