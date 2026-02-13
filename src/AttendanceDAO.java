import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceDAO - Data Access Object for Attendance operations
 * Handles marking, viewing, and calculating attendance
 */
public class AttendanceDAO {
    private StudentDAO studentDAO = new StudentDAO();

    public boolean markAttendance(String rollNumber, String date, String status) throws SQLException {
        if (!studentDAO.isRollNumberExists(rollNumber)) {
            throw new IllegalArgumentException("Student with roll number '" + rollNumber + "' not found!");
        }
        if (!status.equals("PRESENT") && !status.equals("ABSENT")) {
            throw new IllegalArgumentException("Status must be 'PRESENT' or 'ABSENT'");
        }

        if (isAttendanceMarked(rollNumber, date)) {
            String sql = "UPDATE Attendance SET status = ? WHERE roll_number = ? AND date = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setString(2, rollNumber);
                pstmt.setString(3, date);
                pstmt.executeUpdate();
                return true;
            }
        } else {
            String sql = "INSERT INTO Attendance (roll_number, date, status) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, rollNumber);
                pstmt.setString(2, date);
                pstmt.setString(3, status);
                pstmt.executeUpdate();
                updateStudentTotalClasses(rollNumber);
                return true;
            }
        }
    }

    public boolean isAttendanceMarked(String rollNumber, String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Attendance WHERE roll_number = ? AND date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        }
        return false;
    }

    public List<String[]> getAttendanceByRollNumber(String rollNumber) throws SQLException {
        List<String[]> records = new ArrayList<>();
        String sql = "SELECT date, status FROM Attendance WHERE roll_number = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(new String[] { rs.getString("date"), rs.getString("status") });
            }
        }
        return records;
    }

    public List<String[]> getAttendanceByDate(String date) throws SQLException {
        List<String[]> records = new ArrayList<>();
        String sql = """
                    SELECT s.roll_number, s.name, COALESCE(a.status, 'NOT MARKED') as status
                    FROM Student s
                    LEFT JOIN Attendance a ON s.roll_number = a.roll_number AND a.date = ?
                    ORDER BY s.roll_number
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(new String[] {
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getString("status")
                });
            }
        }
        return records;
    }

    public double calculateAttendancePercentage(String rollNumber) throws SQLException {
        String sql = """
                    SELECT
                        COUNT(*) as total_days,
                        SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_days
                    FROM Attendance
                    WHERE roll_number = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int totalDays = rs.getInt("total_days");
                int presentDays = rs.getInt("present_days");
                if (totalDays == 0)
                    return 0.0;
                return (presentDays * 100.0) / totalDays;
            }
        }
        return 0.0;
    }

    public int[] getAttendanceStats(String rollNumber) throws SQLException {
        String sql = """
                    SELECT
                        COUNT(*) as total_days,
                        SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_days,
                        SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_days
                    FROM Attendance
                    WHERE roll_number = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new int[] {
                        rs.getInt("total_days"),
                        rs.getInt("present_days"),
                        rs.getInt("absent_days")
                };
            }
        }
        return new int[] { 0, 0, 0 };
    }

    public List<String[]> getAttendanceSummary() throws SQLException {
        List<String[]> summary = new ArrayList<>();
        String sql = """
                    SELECT
                        s.roll_number, s.name,
                        COUNT(a.id) as total_days,
                        SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present_days
                    FROM Student s
                    LEFT JOIN Attendance a ON s.roll_number = a.roll_number
                    GROUP BY s.roll_number, s.name
                    ORDER BY s.roll_number
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int totalDays = rs.getInt("total_days");
                int presentDays = rs.getInt("present_days");
                double percentage = totalDays > 0 ? (presentDays * 100.0) / totalDays : 0.0;
                summary.add(new String[] {
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        String.valueOf(totalDays),
                        String.valueOf(presentDays),
                        String.format("%.2f%%", percentage)
                });
            }
        }
        return summary;
    }

    private void updateStudentTotalClasses(String rollNumber) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT date) FROM Attendance WHERE roll_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                studentDAO.updateTotalClasses(rollNumber, rs.getInt(1));
            }
        }
    }
}
