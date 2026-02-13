import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HodDAO - Data Access Object for HOD operations
 * Handles Teacher CRUD, Student viewing, and Reports
 */
public class HodDAO {

    // ==================== TEACHER MANAGEMENT ====================

    public boolean addTeacher(Teacher teacher) throws SQLException {
        if (isTeacherExists(teacher.getTeacherId())) {
            return false;
        }
        String sql = "INSERT INTO Teacher (teacher_id, name, email, department, subject) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getTeacherId());
            pstmt.setString(2, teacher.getName());
            pstmt.setString(3, teacher.getEmail());
            pstmt.setString(4, teacher.getDepartment());
            pstmt.setString(5, teacher.getSubject());
            pstmt.executeUpdate();
            return true;
        }
    }

    public List<Teacher> getAllTeachers() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM Teacher ORDER BY teacher_id";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                teachers.add(new Teacher(
                        rs.getString("teacher_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("subject")));
            }
        }
        return teachers;
    }

    public Teacher getTeacherById(String teacherId) throws SQLException {
        String sql = "SELECT * FROM Teacher WHERE teacher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Teacher(
                        rs.getString("teacher_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("subject"));
            }
        }
        return null;
    }

    public boolean updateTeacher(String teacherId, String name, String email, String department, String subject)
            throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE Teacher SET ");
        List<String> updates = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            updates.add("name = ?");
            params.add(name);
        }
        if (email != null && !email.isEmpty()) {
            updates.add("email = ?");
            params.add(email);
        }
        if (department != null && !department.isEmpty()) {
            updates.add("department = ?");
            params.add(department);
        }
        if (subject != null && !subject.isEmpty()) {
            updates.add("subject = ?");
            params.add(subject);
        }

        if (updates.isEmpty())
            return false;

        sql.append(String.join(", ", updates));
        sql.append(" WHERE teacher_id = ?");
        params.add(teacherId);

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteTeacher(String teacherId) throws SQLException {
        String sql = "DELETE FROM Teacher WHERE teacher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean isTeacherExists(String teacherId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Teacher WHERE teacher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        }
        return false;
    }

    public int getTeacherCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Teacher";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        }
        return 0;
    }

    // ==================== STUDENT VIEWING ====================

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student ORDER BY department, roll_number";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getInt("total_classes")));
            }
        }
        return students;
    }

    public int getStudentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Student";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        }
        return 0;
    }

    // ==================== REPORTS ====================

    public List<String[]> getDepartmentReport() throws SQLException {
        List<String[]> report = new ArrayList<>();
        String sql = """
                    SELECT
                        s.department,
                        COUNT(DISTINCT s.roll_number) as student_count,
                        ROUND(AVG(
                            CASE WHEN total.total_days > 0
                            THEN (present.present_days * 100.0 / total.total_days)
                            ELSE 0 END
                        ), 2) as avg_attendance
                    FROM Student s
                    LEFT JOIN (
                        SELECT roll_number, COUNT(*) as total_days
                        FROM Attendance GROUP BY roll_number
                    ) total ON s.roll_number = total.roll_number
                    LEFT JOIN (
                        SELECT roll_number, COUNT(*) as present_days
                        FROM Attendance WHERE status = 'PRESENT' GROUP BY roll_number
                    ) present ON s.roll_number = present.roll_number
                    GROUP BY s.department
                    ORDER BY s.department
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                report.add(new String[] {
                        rs.getString("department"),
                        String.valueOf(rs.getInt("student_count")),
                        String.format("%.2f%%", rs.getDouble("avg_attendance"))
                });
            }
        }
        return report;
    }

    public List<String[]> getLowAttendanceStudents() throws SQLException {
        List<String[]> students = new ArrayList<>();
        String sql = """
                    SELECT
                        s.roll_number, s.name, s.department,
                        CASE WHEN COUNT(a.id) > 0
                            THEN ROUND((SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(a.id)), 2)
                            ELSE 0
                        END as percentage
                    FROM Student s
                    LEFT JOIN Attendance a ON s.roll_number = a.roll_number
                    GROUP BY s.roll_number, s.name, s.department
                    HAVING CASE WHEN COUNT(a.id) > 0
                        THEN ROUND((SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(a.id)), 2)
                        ELSE 0
                    END < 75
                    ORDER BY percentage ASC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new String[] {
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getString("department"),
                        String.format("%.2f%%", rs.getDouble("percentage"))
                });
            }
        }
        return students;
    }

    public String[] getOverallStatistics() throws SQLException {
        int totalStudents = getStudentCount();
        int totalTeachers = getTeacherCount();

        String avgSql = """
                    SELECT
                        COUNT(*) as total_records,
                        ROUND(AVG(CASE WHEN status = 'PRESENT' THEN 100.0 ELSE 0.0 END), 2) as avg_attendance
                    FROM Attendance
                """;

        int totalRecords = 0;
        double avgAttendance = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(avgSql)) {
            if (rs.next()) {
                totalRecords = rs.getInt("total_records");
                avgAttendance = rs.getDouble("avg_attendance");
            }
        }

        return new String[] {
                String.valueOf(totalStudents),
                String.valueOf(totalTeachers),
                String.format("%.2f%%", avgAttendance),
                String.valueOf(totalRecords)
        };
    }
}
