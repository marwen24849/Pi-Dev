package esprit.tn.pidevrh.teams_departements;

public  class Team {
    private final int id;
    private final String name;
    private final int members;
    private String department;

    public Team(int id, String name, int members, String department) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.department = department;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getMembers() { return members; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}