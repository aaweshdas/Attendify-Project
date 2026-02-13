import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDAO - Data Access Object for Student operations
 * Handles all JDBC operations: INSERT, UPDATE, DELETE, SEARCH
 */
public class StudentDAO {

    public boolean addStudent(Student student) throws SQLException {
        if (isRollNumberExists(student.getRollNumber())) {
            return false;
        }

        String sql = "INSERT INTO Student (roll_number, name, email, department, total_classes) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getRollNumber());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getEmail());
            pstmt.setString(4, student.getDepartment());
            pstmt.setInt(5, student.getTotalClasses());
            pstmt.executeUpdate();
            return true;
        }
    }

    public boolean updateStudent(String rollNumber, String name, String email, String department) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE Student SET ");
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

        if (updates.isEmpty())
            return false;

        sql.append(String.join(", ", updates));
        sql.append(" WHERE roll_number = ?");
        params.add(rollNumber);

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            return pstmt.executeUpdate() > 0;
        }
    }

    public Student searchByRollNumber(String rollNumber) throws SQLException {
        String sql = "SELECT * FROM Student WHERE roll_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Student(
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getInt("total_classes"));
            }
        }
        return null;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student ORDER BY roll_number";

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

    public boolean deleteStudent(String rollNumber) throws SQLException {
        String deleteAttendance = "DELETE FROM Attendance WHERE roll_number = ?";
        String deleteStudent = "DELETE FROM Student WHERE roll_number = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAttendance)) {
                pstmt.setString(1, rollNumber);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(deleteStudent)) {
                pstmt.setString(1, rollNumber);
                return pstmt.executeUpdate() > 0;
            }
        }
    }

    public boolean isRollNumberExists(String rollNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Student WHERE roll_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        }
        return false;
    }

    public void updateTotalClasses(String rollNumber, int totalClasses) throws SQLException {
        String sql = "UPDATE Student SET total_classes = ? WHERE roll_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, totalClasses);
            pstmt.setString(2, rollNumber);
            pstmt.executeUpdate();
        }
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
}
