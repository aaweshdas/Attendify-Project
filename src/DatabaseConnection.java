import java.sql.*;

/**
 * DatabaseConnection - Unified MySQL connection for Attendify
 * Connects to MySQL, creates the 'attendify' database and all tables
 */
public class DatabaseConnection {
    // MySQL connection parameters
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "attendify";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345";

    private static final String BASE_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static Connection connection = null;

    /**
     * Get database connection (creates if not exists)
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found! Ensure mysql-connector-j is in lib/", e);
            }
        }
        return connection;
    }

    /**
     * Initialize database and all tables
     */
    public static void initializeDatabase() throws SQLException {
        // First: create the database itself
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }

        try (Connection baseConn = DriverManager.getConnection(BASE_URL, DB_USER, DB_PASS);
                Statement stmt = baseConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        }

        // Now connect to the attendify database and create tables
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        String createStudentTable = """
                    CREATE TABLE IF NOT EXISTS Student (
                        roll_number VARCHAR(50) PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100),
                        department VARCHAR(50),
                        total_classes INT DEFAULT 0
                    )
                """;

        String createAttendanceTable = """
                    CREATE TABLE IF NOT EXISTS Attendance (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        roll_number VARCHAR(50) NOT NULL,
                        date VARCHAR(20) NOT NULL,
                        status VARCHAR(10) NOT NULL,
                        FOREIGN KEY (roll_number) REFERENCES Student(roll_number),
                        UNIQUE KEY unique_attendance (roll_number, date)
                    )
                """;

        String createTeacherTable = """
                    CREATE TABLE IF NOT EXISTS Teacher (
                        teacher_id VARCHAR(50) PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100),
                        department VARCHAR(50),
                        subject VARCHAR(100)
                    )
                """;

        String createHodTable = """
                    CREATE TABLE IF NOT EXISTS HOD (
                        hod_id VARCHAR(50) PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        department VARCHAR(50),
                        password VARCHAR(100) DEFAULT '1234'
                    )
                """;

        stmt.execute(createStudentTable);
        stmt.execute(createAttendanceTable);
        stmt.execute(createTeacherTable);
        stmt.execute(createHodTable);
        stmt.close();
    }

    /**
     * Create default HOD if none exists
     */
    public static void createDefaultHod() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM HOD";
        String insertSql = "INSERT IGNORE INTO HOD (hod_id, name, department, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "HOD001");
                    pstmt.setString(2, "Dr. Admin");
                    pstmt.setString(3, "Computer Science");
                    pstmt.setString(4, "1234");
                    pstmt.executeUpdate();
                }
            }
        }
    }

    /**
     * Create sample data if tables are empty
     */
    public static void createSampleData() throws SQLException {
        Connection conn = getConnection();

        // --- Sample Teachers ---
        String insertTeacher = "INSERT IGNORE INTO Teacher (teacher_id, name, email, department, subject) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertTeacher)) {
            String[][] teachers = {
                    { "T001", "Prof. Aarav Sharma", "aarav.sharma@attendify.edu", "Computer Science",
                            "Data Structures" },
                    { "T002", "Prof. Sneha Patel", "sneha.patel@attendify.edu", "Computer Science",
                            "Operating Systems" },
                    { "T003", "Prof. Rajesh Kumar", "rajesh.kumar@attendify.edu", "Electronics", "Digital Circuits" },
                    { "T004", "Prof. Priya Menon", "priya.menon@attendify.edu", "Mechanical", "Thermodynamics" },
                    { "T005", "Prof. Vikram Singh", "vikram.singh@attendify.edu", "Computer Science",
                            "Database Systems" }
            };
            for (String[] t : teachers) {
                ps.setString(1, t[0]);
                ps.setString(2, t[1]);
                ps.setString(3, t[2]);
                ps.setString(4, t[3]);
                ps.setString(5, t[4]);
                ps.executeUpdate();
            }
        }

        // --- Sample Students ---
        String insertStudent = "INSERT IGNORE INTO Student (roll_number, name, email, department, total_classes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertStudent)) {
            Object[][] students = {
                    { "001", "Aarav Mehta", "aarav.mehta@student.edu", "Computer Science", 30 },
                    { "002", "Diya Sharma", "diya.sharma@student.edu", "Computer Science", 30 },
                    { "003", "Rohan Gupta", "rohan.gupta@student.edu", "Computer Science", 30 },
                    { "004", "Ananya Iyer", "ananya.iyer@student.edu", "Electronics", 28 },
                    { "005", "Kabir Patel", "kabir.patel@student.edu", "Electronics", 28 },
                    { "006", "Ishita Reddy", "ishita.reddy@student.edu", "Mechanical", 25 },
                    { "007", "Arjun Nair", "arjun.nair@student.edu", "Computer Science", 30 },
                    { "008", "Meera Joshi", "meera.joshi@student.edu", "Computer Science", 30 },
                    { "009", "Siddharth Das", "siddharth.das@student.edu", "Electronics", 28 },
                    { "010", "Kavya Pillai", "kavya.pillai@student.edu", "Mechanical", 25 }
            };
            for (Object[] s : students) {
                ps.setString(1, (String) s[0]);
                ps.setString(2, (String) s[1]);
                ps.setString(3, (String) s[2]);
                ps.setString(4, (String) s[3]);
                ps.setInt(5, (int) s[4]);
                ps.executeUpdate();
            }
        }

        // --- Sample Attendance Records ---
        String insertAttendance = "INSERT IGNORE INTO Attendance (roll_number, date, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertAttendance)) {
            // Generate attendance for the last 10 days
            String[] dates = {
                    "2026-02-03", "2026-02-04", "2026-02-05", "2026-02-06", "2026-02-07",
                    "2026-02-08", "2026-02-09", "2026-02-10", "2026-02-11", "2026-02-12"
            };
            // Attendance patterns per student (P=Present, A=Absent)
            String[][] patterns = {
                    { "001", "P", "P", "P", "P", "A", "P", "P", "P", "P", "P" }, // 90%
                    { "002", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P" }, // 100%
                    { "003", "P", "A", "P", "A", "P", "P", "A", "P", "P", "A" }, // 60%
                    { "004", "P", "P", "P", "P", "P", "P", "A", "P", "P", "P" }, // 90%
                    { "005", "A", "P", "P", "A", "P", "A", "P", "P", "A", "P" }, // 60%
                    { "006", "P", "P", "P", "P", "P", "P", "P", "P", "P", "A" }, // 90%
                    { "007", "P", "P", "A", "P", "P", "P", "P", "A", "P", "P" }, // 80%
                    { "008", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P" }, // 100%
                    { "009", "A", "A", "P", "P", "A", "P", "P", "A", "P", "P" }, // 60%
                    { "010", "P", "P", "P", "A", "P", "P", "P", "P", "A", "P" } // 80%
            };
            for (String[] pat : patterns) {
                String roll = pat[0];
                for (int i = 0; i < dates.length; i++) {
                    ps.setString(1, roll);
                    ps.setString(2, dates[i]);
                    ps.setString(3, pat[i + 1]);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
