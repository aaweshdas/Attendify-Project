/**
 * HOD - Model class representing Head of Department
 */
public class HOD {
    private String hodId;
    private String name;
    private String department;

    public HOD(String hodId, String name, String department) {
        this.hodId = hodId;
        this.name = name;
        this.department = department;
    }

    public String getHodId() {
        return hodId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }
}
