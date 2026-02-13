import java.sql.*;
import java.util.List;
import java.util.Scanner;

/**
 * HodPortal - Head of Department Dashboard
 * Teacher management, student viewing, and department reports
 */
public class HodPortal {

    // ANSI Color Codes
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[36m";
    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    private static final String WHITE = "\033[97m";
    private static final String BG_MAGENTA = "\033[45m";
    private static final String DIM = "\033[2m";

    private Scanner scanner;
    private HodDAO hodDAO;

    public HodPortal(Scanner scanner) {
        this.scanner = scanner;
        this.hodDAO = new HodDAO();
    }

    public void run() {
        boolean running = true;
        while (running) {
            clearScreen();
            printHodHeader();
            printMenu();
            int choice = getIntInput(CYAN + "  Enter your choice: " + RESET);

            switch (choice) {
                case 1 -> teacherManagement();
                case 2 -> viewAllStudents();
                case 3 -> viewDepartmentReport();
                case 4 -> viewLowAttendance();
                case 5 -> viewOverallStatistics();
                case 0 -> {
                    running = false;
                    System.out.println(GREEN + "\n  Logging out from HOD Portal..." + RESET);
                    sleep(800);
                }
                default -> {
                    System.out.println(RED + "\n  Invalid choice!" + RESET);
                    sleep(1000);
                }
            }
        }
    }

    private void printHodHeader() {
        System.out.println(BOLD + MAGENTA + "  +============================================================+" + RESET);
        System.out.println(BOLD + MAGENTA + "  |" + WHITE + BG_MAGENTA
                + "                   HOD PORTAL - DASHBOARD                   " + RESET + BOLD + MAGENTA + "|"
                + RESET);
        System.out.println(BOLD + MAGENTA + "  +============================================================+" + RESET);
        System.out.println();
    }

    private void printMenu() {
        System.out.println(DIM + "  ─────────────  Management  ─────────────────────" + RESET);
        System.out.println(CYAN + "  [1]" + WHITE + "  Teacher Management" + RESET);
        System.out.println(CYAN + "  [2]" + WHITE + "  View All Students" + RESET);
        System.out.println(DIM + "  ─────────────  Reports  ────────────────────────" + RESET);
        System.out.println(CYAN + "  [3]" + WHITE + "  Department Attendance Report" + RESET);
        System.out.println(CYAN + "  [4]" + WHITE + "  Low Attendance Alerts" + RESET);
        System.out.println(CYAN + "  [5]" + WHITE + "  Overall Statistics" + RESET);
        System.out.println(DIM + "  ────────────────────────────────────────────────" + RESET);
        System.out.println(RED + "  [0]" + WHITE + "  Logout" + RESET);
        System.out.println();
    }

    // ==================== TEACHER MANAGEMENT ====================

    private void teacherManagement() {
        boolean running = true;
        while (running) {
            clearScreen();
            System.out.println(BOLD + MAGENTA + "\n  ═══ Teacher Management ═══" + RESET);
            System.out.println();
            System.out.println(CYAN + "  [1]" + WHITE + "  Add Teacher" + RESET);
            System.out.println(CYAN + "  [2]" + WHITE + "  View All Teachers" + RESET);
            System.out.println(CYAN + "  [3]" + WHITE + "  Search Teacher" + RESET);
            System.out.println(CYAN + "  [4]" + WHITE + "  Update Teacher" + RESET);
            System.out.println(CYAN + "  [5]" + WHITE + "  Delete Teacher" + RESET);
            System.out.println(RED + "  [0]" + WHITE + "  Back to Dashboard" + RESET);
            System.out.println();

            int choice = getIntInput(CYAN + "  Choice: " + RESET);

            switch (choice) {
                case 1 -> addTeacher();
                case 2 -> viewAllTeachers();
                case 3 -> searchTeacher();
                case 4 -> updateTeacher();
                case 5 -> deleteTeacher();
                case 0 -> running = false;
                default -> {
                    System.out.println(RED + "\n  Invalid choice!" + RESET);
                    sleep(1000);
                }
            }
        }
    }

    private void addTeacher() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Add New Teacher ═══" + RESET);
        System.out.print(CYAN + "  Teacher ID  : " + RESET);
        String teacherId = scanner.nextLine().trim();
        System.out.print(CYAN + "  Name        : " + RESET);
        String name = scanner.nextLine().trim();
        System.out.print(CYAN + "  Email       : " + RESET);
        String email = scanner.nextLine().trim();
        System.out.print(CYAN + "  Department  : " + RESET);
        String department = scanner.nextLine().trim();
        System.out.print(CYAN + "  Subject     : " + RESET);
        String subject = scanner.nextLine().trim();

        if (teacherId.isEmpty() || name.isEmpty()) {
            System.out.println(RED + "\n  Error: Teacher ID and name are required!" + RESET);
            pressEnter();
            return;
        }

        try {
            Teacher teacher = new Teacher(teacherId, name, email, department, subject);
            if (hodDAO.addTeacher(teacher)) {
                System.out.println(GREEN + "\n  ✓ Teacher added successfully!" + RESET);
            } else {
                System.out.println(RED + "\n  ✗ Teacher ID already exists!" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewAllTeachers() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ All Teachers ═══" + RESET);
        try {
            List<Teacher> teachers = hodDAO.getAllTeachers();
            if (teachers.isEmpty()) {
                System.out.println(YELLOW + "\n  No teachers registered yet." + RESET);
            } else {
                System.out.println(DIM
                        + "  +------------+----------------------+---------------------------+------------+--------------+"
                        + RESET);
                System.out.printf(BOLD + "  | %-10s | %-20s | %-25s | %-10s | %-12s |%n" + RESET,
                        "Teacher ID", "Name", "Email", "Dept", "Subject");
                System.out.println(DIM
                        + "  +------------+----------------------+---------------------------+------------+--------------+"
                        + RESET);
                for (Teacher t : teachers) {
                    System.out.printf("  | %-10s | %-20s | %-25s | %-10s | %-12s |%n",
                            t.getTeacherId(), t.getName(), t.getEmail(), t.getDepartment(), t.getSubject());
                }
                System.out.println(DIM
                        + "  +------------+----------------------+---------------------------+------------+--------------+"
                        + RESET);
                System.out.println(CYAN + "\n  Total: " + teachers.size() + " teachers" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void searchTeacher() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Search Teacher ═══" + RESET);
        System.out.print(CYAN + "  Enter Teacher ID: " + RESET);
        String teacherId = scanner.nextLine().trim();

        try {
            Teacher t = hodDAO.getTeacherById(teacherId);
            if (t != null) {
                System.out.println(GREEN + "\n  Teacher Found:" + RESET);
                System.out.println(WHITE + "  Teacher ID  : " + CYAN + t.getTeacherId() + RESET);
                System.out.println(WHITE + "  Name        : " + CYAN + t.getName() + RESET);
                System.out.println(WHITE + "  Email       : " + CYAN + t.getEmail() + RESET);
                System.out.println(WHITE + "  Department  : " + CYAN + t.getDepartment() + RESET);
                System.out.println(WHITE + "  Subject     : " + CYAN + t.getSubject() + RESET);
            } else {
                System.out.println(RED + "\n  Teacher not found!" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void updateTeacher() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Update Teacher ═══" + RESET);
        System.out.print(CYAN + "  Enter Teacher ID: " + RESET);
        String teacherId = scanner.nextLine().trim();

        try {
            Teacher t = hodDAO.getTeacherById(teacherId);
            if (t == null) {
                System.out.println(RED + "\n  Teacher not found!" + RESET);
                pressEnter();
                return;
            }

            System.out.println(GREEN + "\n  Current Details:" + RESET);
            System.out.println(WHITE + "  Name       : " + t.getName() + RESET);
            System.out.println(WHITE + "  Email      : " + t.getEmail() + RESET);
            System.out.println(WHITE + "  Department : " + t.getDepartment() + RESET);
            System.out.println(WHITE + "  Subject    : " + t.getSubject() + RESET);
            System.out.println(YELLOW + "\n  (Press Enter to keep current value)" + RESET);

            System.out.print(CYAN + "  New Name       : " + RESET);
            String name = scanner.nextLine().trim();
            System.out.print(CYAN + "  New Email      : " + RESET);
            String email = scanner.nextLine().trim();
            System.out.print(CYAN + "  New Department : " + RESET);
            String department = scanner.nextLine().trim();
            System.out.print(CYAN + "  New Subject    : " + RESET);
            String subject = scanner.nextLine().trim();

            if (hodDAO.updateTeacher(teacherId,
                    name.isEmpty() ? null : name,
                    email.isEmpty() ? null : email,
                    department.isEmpty() ? null : department,
                    subject.isEmpty() ? null : subject)) {
                System.out.println(GREEN + "\n  ✓ Teacher updated successfully!" + RESET);
            } else {
                System.out.println(YELLOW + "\n  No changes made." + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void deleteTeacher() {
        clearScreen();
        System.out.println(BOLD + RED + "\n  ═══ Delete Teacher ═══" + RESET);
        System.out.print(CYAN + "  Enter Teacher ID: " + RESET);
        String teacherId = scanner.nextLine().trim();

        try {
            Teacher t = hodDAO.getTeacherById(teacherId);
            if (t == null) {
                System.out.println(RED + "\n  Teacher not found!" + RESET);
                pressEnter();
                return;
            }

            System.out.println(YELLOW + "\n  Teacher: " + t.getName() + " (" + teacherId + ")" + RESET);
            System.out.print(RED + "  Are you sure? (yes/no): " + RESET);
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("yes") || confirm.equals("y")) {
                if (hodDAO.deleteTeacher(teacherId)) {
                    System.out.println(GREEN + "\n  ✓ Teacher deleted successfully!" + RESET);
                } else {
                    System.out.println(RED + "\n  ✗ Failed to delete teacher!" + RESET);
                }
            } else {
                System.out.println(YELLOW + "\n  Deletion cancelled." + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    // ==================== STUDENTS & REPORTS ====================

    private void viewAllStudents() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ All Students (Read-Only) ═══" + RESET);
        try {
            List<Student> students = hodDAO.getAllStudents();
            if (students.isEmpty()) {
                System.out.println(YELLOW + "\n  No students registered yet." + RESET);
            } else {
                System.out.println(
                        DIM + "  +--------------+----------------------+---------------------------+--------------+"
                                + RESET);
                System.out.printf(BOLD + "  | %-12s | %-20s | %-25s | %-12s |%n" + RESET,
                        "Roll Number", "Name", "Email", "Department");
                System.out.println(
                        DIM + "  +--------------+----------------------+---------------------------+--------------+"
                                + RESET);
                for (Student s : students) {
                    System.out.println("  " + s.toString().replace("│", "|")); // Ensure toString uses ASCII if relied
                                                                               // upon, or replace on fly
                }
                System.out.println(
                        DIM + "  +--------------+----------------------+---------------------------+--------------+"
                                + RESET);
                System.out.println(CYAN + "\n  Total: " + students.size() + " students" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewDepartmentReport() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Department Attendance Report ═══" + RESET);
        try {
            List<String[]> report = hodDAO.getDepartmentReport();
            if (report.isEmpty()) {
                System.out.println(YELLOW + "\n  No data available." + RESET);
            } else {
                System.out.println(DIM + "  +--------------+--------------+-------------------+" + RESET);
                System.out.printf(BOLD + "  | %-12s | %-12s | %-17s |%n" + RESET,
                        "Department", "Students", "Avg Attendance");
                System.out.println(DIM + "  +--------------+--------------+-------------------+" + RESET);
                for (String[] r : report) {
                    double pct = Double.parseDouble(r[2].replace("%", ""));
                    String color = pct >= 75 ? GREEN : pct >= 50 ? YELLOW : RED;
                    System.out.printf("  | %-12s | %-12s | %s%-17s%s |%n",
                            r[0], r[1], color, r[2], RESET);
                }
                System.out.println(DIM + "  +--------------+--------------+-------------------+" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewLowAttendance() {
        clearScreen();
        System.out.println(BOLD + RED + "\n  ═══ Low Attendance Alerts (<75%) ═══" + RESET);
        try {
            List<String[]> students = hodDAO.getLowAttendanceStudents();
            if (students.isEmpty()) {
                System.out.println(GREEN + "\n  ✓ All students have attendance above 75%!" + RESET);
            } else {
                System.out.println(
                        DIM + "  +--------------+----------------------+--------------+------------+" + RESET);
                System.out.printf(BOLD + "  | %-12s | %-20s | %-12s | %-10s |%n" + RESET,
                        "Roll Number", "Name", "Department", "Attendance");
                System.out
                        .println(DIM + "  +--------------+----------------------+--------------+------------+" + RESET);
                for (String[] s : students) {
                    System.out.printf("  | %-12s | %-20s | %-12s | %s%-10s%s |%n",
                            s[0], s[1], s[2], RED, s[3], RESET);
                }
                System.out
                        .println(DIM + "  +--------------+----------------------+--------------+------------+" + RESET);
                System.out.println(YELLOW + "\n  ⚠ " + students.size() + " student(s) need attention!" + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewOverallStatistics() {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Overall System Statistics ═══" + RESET);
        try {
            String[] stats = hodDAO.getOverallStatistics();
            System.out.println();
            System.out.println(DIM + "  +--------------------------------------------+" + RESET);
            System.out.println(WHITE + "  |  Total Students       : " + CYAN + String.format("%-18s", stats[0]) + WHITE
                    + "|" + RESET);
            System.out.println(WHITE + "  |  Total Teachers       : " + CYAN + String.format("%-18s", stats[1]) + WHITE
                    + "|" + RESET);
            System.out.println(WHITE + "  |  Average Attendance   : " + GREEN + String.format("%-18s", stats[2]) + WHITE
                    + "|" + RESET);
            System.out.println(WHITE + "  |  Total Records        : " + CYAN + String.format("%-18s", stats[3]) + WHITE
                    + "|" + RESET);
            System.out.println(DIM + "  +--------------------------------------------+" + RESET);
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    // ==================== UTILITIES ====================

    private int getIntInput(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
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
