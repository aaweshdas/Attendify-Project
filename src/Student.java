/**
 * Student - Model class representing a student entity
 */
public class Student {
    private String rollNumber;
    private String name;
    private String email;
    private String department;
    private int totalClasses;

    // Constructor
    public Student(String rollNumber, String name, String email, String department) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.email = email;
        this.department = department;
        this.totalClasses = 0;
    }

    // Full constructor (for loading from database)
    public Student(String rollNumber, String name, String email, String department, int totalClasses) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.email = email;
        this.department = department;
        this.totalClasses = totalClasses;
    }

    // Getters
    public String getRollNumber() { return rollNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public int getTotalClasses() { return totalClasses; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartment(String department) { this.department = department; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }

    @Override
    public String toString() {
        return String.format("| %-12s | %-20s | %-25s | %-12s |",
                rollNumber, name, email, department);
    }
}
