package enthusiast.yzw.shift_arrangement_helper.moduls;

public class BedAssign extends DatabaseEntity {
    private Person person;
    private Bed bed;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }
}
