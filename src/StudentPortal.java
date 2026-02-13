import java.sql.*;
import java.util.List;
import java.util.Scanner;

/**
 * StudentPortal - Student Dashboard
 * Read-only access to profile and attendance data
 */
public class StudentPortal {

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
    private static final String BG_GREEN = "\033[42m";
    private static final String DIM = "\033[2m";

    private Scanner scanner;
    private StudentDAO studentDAO;
    private AttendanceDAO attendanceDAO;

    public StudentPortal(Scanner scanner) {
        this.scanner = scanner;
        this.studentDAO = new StudentDAO();
        this.attendanceDAO = new AttendanceDAO();
    }

    public void run() {
        // Student portal needs roll number identification
        boolean running = true;
        while (running) {
            clearScreen();
            printStudentLoginHeader();
            System.out.print(CYAN + "  Enter your Roll Number: " + RESET);
            String rollNumber = scanner.nextLine().trim();

            if (rollNumber.equalsIgnoreCase("back") || rollNumber.equalsIgnoreCase("logout")) {
                running = false;
                System.out.println(GREEN + "\n  Logging out from Student Portal..." + RESET);
                sleep(800);
                continue;
            }

            try {
                Student student = studentDAO.searchByRollNumber(rollNumber);
                if (student != null) {
                    System.out.println(GREEN + "\n  ✓ Welcome, " + student.getName() + "!" + RESET);
                    sleep(1000);
                    runStudentDashboard(student);
                } else {
                    System.out.println(RED + "\n  ✗ Roll number not found! Please try again." + RESET);
                    System.out.println(DIM + "  Type 'back' to return to main login." + RESET);
                    sleep(1500);
                }
            } catch (SQLException e) {
                System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
                sleep(1500);
            }
        }
    }

    private void runStudentDashboard(Student student) {
        boolean running = true;
        while (running) {
            clearScreen();
            printDashboardHeader(student);
            printMenu();
            int choice = getIntInput(CYAN + "  Enter your choice: " + RESET);

            switch (choice) {
                case 1 -> viewProfile(student);
                case 2 -> viewAttendanceRecords(student);
                case 3 -> viewAttendancePercentage(student);
                case 0 -> {
                    running = false;
                    System.out.println(GREEN + "\n  Logging out..." + RESET);
                    sleep(800);
                }
                default -> {
                    System.out.println(RED + "\n  Invalid choice!" + RESET);
                    sleep(1000);
                }
            }
        }
    }

    private void printStudentLoginHeader() {
        System.out.println(BOLD + GREEN + "  +============================================================+" + RESET);
        System.out.println(BOLD + GREEN + "  |" + WHITE + BG_GREEN
                + "              STUDENT PORTAL - IDENTIFICATION               " + RESET + BOLD + GREEN + "|" + RESET);
        System.out.println(BOLD + GREEN + "  +============================================================+" + RESET);
        System.out.println();
        System.out.println(DIM + "  Enter your roll number to access your dashboard." + RESET);
        System.out.println(DIM + "  Type 'back' to return to the main login screen." + RESET);
        System.out.println();
    }

    private void printDashboardHeader(Student student) {
        System.out.println(BOLD + GREEN + "  +============================================================+" + RESET);
        System.out.println(BOLD + GREEN + "  |" + WHITE + BG_GREEN
                + "                 STUDENT PORTAL - DASHBOARD                 " + RESET + BOLD + GREEN + "|" + RESET);
        System.out.println(BOLD + GREEN + "  +============================================================+" + RESET);
        System.out.println(CYAN + "  Logged in as: " + WHITE + student.getName() + DIM + " (" + student.getRollNumber()
                + ")" + RESET);
        System.out.println();
    }

    private void printMenu() {
        System.out.println(DIM + "  ─────────────  Options  ────────────────────────" + RESET);
        System.out.println(CYAN + "  [1]" + WHITE + "  View Profile" + RESET);
        System.out.println(CYAN + "  [2]" + WHITE + "  View Attendance Records" + RESET);
        System.out.println(CYAN + "  [3]" + WHITE + "  View Attendance Percentage" + RESET);
        System.out.println(DIM + "  ────────────────────────────────────────────────" + RESET);
        System.out.println(RED + "  [0]" + WHITE + "  Logout" + RESET);
        System.out.println();
    }

    private void viewProfile(Student student) {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Your Profile ═══" + RESET);
        System.out.println();
        System.out.println(DIM + "  +-------------------------------------------+" + RESET);
        System.out.println(WHITE + "  |  Roll Number  : " + CYAN + String.format("%-24s", student.getRollNumber())
                + WHITE + "|" + RESET);
        System.out.println(WHITE + "  |  Name         : " + CYAN + String.format("%-24s", student.getName()) + WHITE
                + "|" + RESET);
        System.out.println(WHITE + "  |  Email        : " + CYAN + String.format("%-24s", student.getEmail()) + WHITE
                + "|" + RESET);
        System.out.println(WHITE + "  |  Department   : " + CYAN + String.format("%-24s", student.getDepartment())
                + WHITE + "|" + RESET);
        System.out.println(WHITE + "  |  Total Classes: " + CYAN + String.format("%-24d", student.getTotalClasses())
                + WHITE + "|" + RESET);
        System.out.println(DIM + "  +-------------------------------------------+" + RESET);
        pressEnter();
    }

    private void viewAttendanceRecords(Student student) {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Attendance Records ═══" + RESET);
        try {
            List<String[]> records = attendanceDAO.getAttendanceByRollNumber(student.getRollNumber());
            if (records.isEmpty()) {
                System.out.println(YELLOW + "\n  No attendance records found." + RESET);
            } else {
                System.out.println(DIM + "\n  +------+------------+----------+" + RESET);
                System.out.printf(BOLD + "  | %-4s | %-10s | %-8s |%n" + RESET, "#", "Date", "Status");
                System.out.println(DIM + "  +------+------------+----------+" + RESET);
                int idx = 1;
                for (String[] r : records) {
                    String statusColor = r[1].equals("PRESENT") ? GREEN : RED;
                    System.out.printf("  | %-4d | %-10s | %s%-8s%s |%n", idx++, r[0], statusColor, r[1], RESET);
                }
                System.out.println(DIM + "  +------+------------+----------+" + RESET);
                System.out.println(CYAN + "\n  Total Records: " + records.size() + RESET);
            }
        } catch (SQLException e) {
            System.out.println(RED + "\n  Database error: " + e.getMessage() + RESET);
        }
        pressEnter();
    }

    private void viewAttendancePercentage(Student student) {
        clearScreen();
        System.out.println(BOLD + GREEN + "\n  ═══ Attendance Percentage ═══" + RESET);
        try {
            double percentage = attendanceDAO.calculateAttendancePercentage(student.getRollNumber());
            int[] stats = attendanceDAO.getAttendanceStats(student.getRollNumber());

            System.out.println();
            System.out.println(DIM + "  +--------------------------------------------+" + RESET);
            System.out.println(
                    WHITE + "  |  Total Days   : " + CYAN + String.format("%-25d", stats[0]) + WHITE + "|" + RESET);
            System.out.println(
                    WHITE + "  |  Present      : " + GREEN + String.format("%-25d", stats[1]) + WHITE + "|" + RESET);
            System.out.println(
                    WHITE + "  |  Absent       : " + RED + String.format("%-25d", stats[2]) + WHITE + "|" + RESET);
            System.out.println(DIM + "  +--------------------------------------------+" + RESET);

            // Progress bar
            int barWidth = 25;
            int filled = (int) (percentage / 100 * barWidth);
            // Replaced Unicode block characters with ASCII
            String bar = "#".repeat(Math.max(0, filled)) + "-".repeat(Math.max(0, barWidth - filled));
            String color = percentage >= 75 ? GREEN : percentage >= 50 ? YELLOW : RED;
            System.out.printf(WHITE + "  |  Percentage   : %s%s %.1f%%%s     |%n" + RESET, color, bar, percentage,
                    RESET);

            // Status
            String status = percentage >= 75 ? GREEN + "SAFE"
                    : percentage >= 50 ? YELLOW + "WARNING" : RED + "CRITICAL";
            System.out.printf(WHITE + "  |  Status       : %s%-25s" + WHITE + "|%n" + RESET, status, RESET);
            System.out.println(DIM + "  +--------------------------------------------+" + RESET);

            if (percentage < 75) {
                System.out.println(RED + "\n  ⚠ Your attendance is below 75%! Please improve." + RESET);
            }
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
