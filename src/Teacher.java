/**
 * Teacher - Model class representing a teacher entity
 */
public class Teacher {
    private String teacherId;
    private String name;
    private String email;
    private String department;
    private String subject;

    public Teacher(String teacherId, String name, String email, String department, String subject) {
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.subject = subject;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getSubject() {
        return subject;
    }
}
