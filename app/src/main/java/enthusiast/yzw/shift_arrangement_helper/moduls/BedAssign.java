package enthusiast.yzw.shift_arrangement_helper.moduls;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;

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

    public static boolean deleAll(long personId) {
        String condition = "person_id = " + personId;
        return DbHelper.getWriteDB().delete("bedassign", condition, null) > 0;
    }
}
