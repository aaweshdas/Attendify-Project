import java.sql.SQLException;
import java.util.Scanner;

/**
 * Attendify - Unified Attendance Management System
 * Single entry point with role-based credential routing
 *
 * Credentials:
 * HOD -> admin / admin
 * Teacher -> admin / aarav
 * Student -> 001 / aarav
 */
public class Main {

    // ANSI CODES
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String CYAN = "\033[36m";
    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String BLUE = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    private static final String WHITE = "\033[97m";

    // CREDENTIALS
    private static final String HOD_USER = "admin";
    private static final String HOD_PASS = "admin";
    private static final String TEACHER_USER = "admin";
    private static final String TEACHER_PASS = "aarav";
    private static final String STUDENT_USER = "001";
    private static final String STUDENT_PASS = "aarav";

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Enable ANSI on Windows 10+
        enableAnsi();

        try {
            DatabaseConnection.initializeDatabase();
            DatabaseConnection.createDefaultHod();
            DatabaseConnection.createSampleData();

            boolean running = true;
            while (running) {
                clearScreen();
                printWelcomeScreen();
                running = loginFlow();
            }

            printGoodbye();

        } catch (SQLException e) {
            System.out.println(RED + "\n  [ERROR] Database error: " + e.getMessage() + RESET);
        } finally {
            DatabaseConnection.closeConnection();
            scanner.close();
        }
    }

    // =========================================================
    // WELCOME SCREEN
    // =========================================================

    private static void printWelcomeScreen() {
        String B = BOLD + CYAN;
        String W = WHITE;
        String R = RESET;

        System.out.println();
        // Top Border (62 chars)
        System.out.println(B + "  +============================================================+" + R);
        System.out.println(B + "  |                                                            |" + R);

        // Title Section
        System.out
                .println(B + "  |" + W + "                         ATTENDIFY                          " + B + "|" + R);
        System.out.println(B + "  |                                                            |" + R);
        System.out.println(
                B + "  |" + DIM + W + "            Student Attendance Management System            " + R + B + "|" + R);
        System.out.println(B + "  |" + DIM + CYAN + "                       v2.0 - Unified                       " + R
                + B + "|" + R);

        System.out.println(B + "  |                                                            |" + R);
        System.out.println(B + "  +============================================================+" + R);

        System.out.println(B + "  |                                                            |" + R);
        System.out
                .println(B + "  |" + W + "                    *  SYSTEM LOGIN  *                       " + B + "|" + R);
        System.out.println(B + "  |                                                            |" + R);
        System.out.println(B + "  +============================================================+" + R);

        System.out.println(B + "  |                                                            |" + R);
        System.out.println(
                B + "  |" + DIM + W + "   Available Portals:                                       " + R + B + "|" + R);
        System.out.println(B + "  |                                                            |" + R);

        // Menu Options (60 chars content + 2 border)
        // " > HOD Portal - Department administration " length = 60
        System.out.println(B + "  |" + MAGENTA + "     > " + W + "HOD Portal         " + DIM
                + "- Department administration       " + R + B + "|" + R);

        // " > Teacher Portal - Student & attendance mgmt " length = 60
        System.out.println(B + "  |" + BLUE + "     > " + W + "Teacher Portal     " + DIM
                + "- Student & attendance mgmt       " + R + B + "|" + R);

        // " > Student Portal - View profile & attendance " length = 60
        System.out.println(B + "  |" + GREEN + "     > " + W + "Student Portal     " + DIM
                + "- View profile & attendance       " + R + B + "|" + R);

        System.out.println(B + "  |                                                            |" + R);
        System.out.println(B + "  +============================================================+" + R);
        System.out.println();
    }

    // =========================================================
    // LOGIN FLOW
    // =========================================================

    private static boolean loginFlow() {
        System.out.print(WHITE + "     Username : " + CYAN);
        String username = scanner.nextLine().trim();

        if (username.equalsIgnoreCase("exit") || username.equalsIgnoreCase("quit")) {
            return false;
        }

        System.out.print(WHITE + "     Password : " + CYAN);
        String password = scanner.nextLine().trim();

        if (password.equalsIgnoreCase("exit") || password.equalsIgnoreCase("quit")) {
            return false;
        }

        System.out.println();

        String role = authenticate(username, password);

        if (role != null) {
            printLoginSuccess(role);
            sleep(1200);
            routeToPortal(role);
        } else {
            printLoginFailed();
            sleep(1500);
        }

        return true;
    }

    private static String authenticate(String username, String password) {
        if (username.equals(HOD_USER) && password.equals(HOD_PASS)) {
            return "HOD";
        } else if (username.equals(TEACHER_USER) && password.equals(TEACHER_PASS)) {
            return "TEACHER";
        } else if (username.equals(STUDENT_USER) && password.equals(STUDENT_PASS)) {
            return "STUDENT";
        }
        return null;
    }

    // =========================================================
    // PORTAL ROUTING
    // =========================================================

    private static void routeToPortal(String role) {
        switch (role) {
            case "HOD" -> {
                HodPortal hodPortal = new HodPortal(scanner);
                hodPortal.run();
            }
            case "TEACHER" -> {
                TeacherPortal teacherPortal = new TeacherPortal(scanner);
                teacherPortal.run();
            }
            case "STUDENT" -> {
                StudentPortal studentPortal = new StudentPortal(scanner);
                studentPortal.run();
            }
        }
    }

    // =========================================================
    // STATUS MESSAGES
    // =========================================================

    private static void printLoginSuccess(String role) {
        String color;
        String portalName;

        switch (role) {
            case "HOD" -> {
                color = MAGENTA;
                portalName = "HOD Portal";
            }
            case "TEACHER" -> {
                color = BLUE;
                portalName = "Teacher Portal";
            }
            default -> {
                color = GREEN;
                portalName = "Student Portal";
            }
        }

        System.out.print(DIM + WHITE + "     Authenticating");
        for (int i = 0; i < 3; i++) {
            sleep(300);
            System.out.print(".");
        }
        System.out.println(RESET);

        System.out.println();
        System.out.println(BOLD + GREEN + "     [OK] LOGIN SUCCESSFUL" + RESET);
        System.out.println();
        System.out.println(BOLD + color + "     +------------------------------------------+" + RESET);
        System.out.println(BOLD + color + "     |  >  Entering " + WHITE + portalName + color
                + "                       |" + RESET);
        System.out.println(BOLD + color + "     |     " + DIM + WHITE + "Welcome, Administrator" + RESET
                + BOLD + color + "                |" + RESET);
        System.out.println(BOLD + color + "     +------------------------------------------+" + RESET);
    }

    private static void printLoginFailed() {
        System.out.print(DIM + WHITE + "     Authenticating");
        for (int i = 0; i < 3; i++) {
            sleep(300);
            System.out.print(".");
        }
        System.out.println(RESET);

        System.out.println();
        System.out.println(BOLD + RED + "     [X] LOGIN FAILED" + RESET);
        System.out.println(DIM + WHITE + "     Invalid username or password. Please try again." + RESET);
        System.out.println(DIM + "     Type 'exit' as username to quit." + RESET);
    }

    // =========================================================
    // GOODBYE SCREEN
    // =========================================================

    private static void printGoodbye() {
        clearScreen();
        System.out.println();
        System.out.println(BOLD + CYAN + "  +============================================================+" + RESET);
        System.out.println(BOLD + CYAN + "  |                                                            |" + RESET);
        System.out.println(BOLD + CYAN + "  |" + WHITE + "              Thank you for using Attendify!                 "
                + CYAN + "|" + RESET);
        System.out.println(BOLD + CYAN + "  |                                                            |" + RESET);
        System.out.println(BOLD + CYAN + "  |" + DIM + WHITE
                + "        Student Attendance Management System                " + RESET
                + BOLD + CYAN + "|" + RESET);
        System.out.println(BOLD + CYAN + "  |" + DIM + CYAN
                + "                  v2.0 - Unified                             " + RESET
                + BOLD + CYAN + "|" + RESET);
        System.out.println(BOLD + CYAN + "  |                                                            |" + RESET);
        System.out.println(BOLD + CYAN + "  |" + WHITE + "                       Goodbye!                              "
                + CYAN + "|" + RESET);
        System.out.println(BOLD + CYAN + "  |                                                            |" + RESET);
        System.out.println(BOLD + CYAN + "  +============================================================+" + RESET);
        System.out.println();
    }

    // =========================================================
    // UTILITIES
    // =========================================================

    private static void enableAnsi() {
        // Enable Virtual Terminal Processing on Windows 10+
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "chcp 65001");
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                pb.start().waitFor();
            }
        } catch (Exception ignored) {
        }
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
