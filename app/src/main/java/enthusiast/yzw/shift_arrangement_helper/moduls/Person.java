package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;
import enthusiast.yzw.shift_arrangement_helper.enums.Gender;
import enthusiast.yzw.shift_arrangement_helper.enums.PersonStatus;

public class Person extends DatabaseEntity {
    private String name = "";
    private int age = 0;
    private Gender gender = Gender.MAN;
    private PersonStatus status = PersonStatus.ONDUTY;
    private double ratio = 0;
    private double absentRemainValue = 0f;
    private String post = "";
    private String professor="";
    private String school="";
    private String phone="";
    private String note="";

    public boolean isSeleted = false;
    public boolean isShowDetails = false;
    public List<Bed> managedBeds = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public PersonStatus getStatus() {
        return status;
    }

    public void setStatus(PersonStatus status) {
        this.status = status;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getAbsentRemainValue() {
        return absentRemainValue;
    }

    public void setAbsentRemainValue(double absentRemainValue) {
        this.absentRemainValue = absentRemainValue;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Person){
            Person p = (Person) obj;
            return this.name.equals(p.getName()) && this.UUID.equals(p.getUUID());
        }
        return false;
    }

    public boolean updateRemainValue(SQLiteDatabase database){
        String condition = "person_uuid = ?";
        String[] args = {this.UUID};
        ContentValues contentValues = new ContentValues();
        contentValues.put(getDbColumnName( "absentRemainValue"), this.absentRemainValue);
        return database.update(this.tableName, contentValues, condition, args) > 0;
    }

    public boolean unRegister(){
        boolean b;
        String condition = "person_uuid = ?";
        String[] args = {this.UUID};

        String personTableName = DbHelper.getDataBaseTableName(Person.class);
        String detailsTableName = DbHelper.getDataBaseTableName(AbsentRemainDetails.class);
        String bedAssignTableName = DbHelper.getDataBaseTableName(BedAssign.class);
        String shiftTableName = DbHelper.getDataBaseTableName(Shift.class);
        String shiftNoteTableName = DbHelper.getDataBaseTableName(ShiftNote.class);
        SQLiteDatabase database = DbHelper.getWriteDB();
        try {
            database.beginTransaction();
            database.delete(personTableName, condition, args);
            database.delete(detailsTableName, condition,args);
            database.delete(bedAssignTableName, condition, args);
            database.delete(shiftNoteTableName, condition, args);
            database.delete(shiftTableName, condition, args);
            database.setTransactionSuccessful();
            b = true;
        }catch (Exception e){
            b = false;
            e.printStackTrace();
        }finally {
            database.endTransaction();
        }
        return b;
    }

    public void fillBedManageList(){
        String sql = "SELECT bed.*,bedassign.*" +
                " FROM bedassign" +
                " INNER JOIN bed ON bedassign.bed_uuid = bed.bed_uuid" +
                " WHERE person_uuid = ?" +
                " ORDER BY bedassign_id";
        String[] args = {this.UUID};
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            do {
                this.managedBeds.add(DbHelper.cursor2Modul(Bed.class, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public boolean saveBedAssign(){
        boolean b = false;
        String condition = "person_uuid = ?";
        String[] args = {this.UUID};
        String tableName = DbHelper.getDataBaseTableName(BedAssign.class);

        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            database.delete(tableName, condition, args);
            for (Bed bed : this.managedBeds) {
                BedAssign bedAssign = new BedAssign();
                bedAssign.setPerson(this);
                bedAssign.setBed(bed);
                database.insert(tableName, null, DbHelper.modul2ContentValues(bedAssign));
            }
            database.setTransactionSuccessful();
            b = true;
        }catch (Exception e) {
            e.printStackTrace();
            b = false;
        }finally {
            database.endTransaction();
        }
        return b;
    }

    public static List<Person> getPersonManagedBedList() {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT person.*,bed.*" +
                " FROM person" +
                " Left JOIN bedassign ON person.person_uuid = bedassign.person_uuid" +
                " Left JOIN bed ON bedassign.bed_uuid = bed.bed_uuid" +
                " ORDER BY person_name,bed_id";
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Person person = DbHelper.cursor2Modul(Person.class, cursor);
                if (person != null) {
                    Bed bed = DbHelper.cursor2Modul(Bed.class, cursor);
                    if (bed != null && bed.getName() != null && !"".equals(bed.getName())) {
                        person.managedBeds.add(bed);
                    }
                    list.add(person);
                    while (cursor.moveToNext()) {
                        Person person1 = DbHelper.cursor2Modul(Person.class, cursor);
                        if (person1 != null && person1.equals(person)) {
                            Bed bed1 = DbHelper.cursor2Modul(Bed.class, cursor);
                            if (bed1 != null && bed1.getName() != null && !"".equals(bed1.getName())) {
                                person.managedBeds.add(bed1);
                            }
                        } else {
                            cursor.moveToPrevious();
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static List<Person> getShiftEditPersonList() {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT person.*,bed.*,bedassign.bedassign_id" +
                " FROM person" +
                " Left JOIN bedassign ON person.person_uuid = bedassign.person_uuid" +
                " Left JOIN bed ON bedassign.bed_uuid = bed.bed_uuid" +
                " WHERE person_status = 0" +
                " ORDER BY person_name,bedassign_id";
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Person person = DbHelper.cursor2Modul(Person.class, cursor);
                if (person != null) {
                    Bed bed = DbHelper.cursor2Modul(Bed.class, cursor);
                    if (bed != null && !TextUtils.isEmpty(bed.getName())) {
                        person.managedBeds.add(bed);
                    }
                    list.add(person);
                    while (cursor.moveToNext()) {
                        Person person1 = DbHelper.cursor2Modul(Person.class, cursor);
                        if (person1 != null && person1.equals(person)) {
                            Bed bed1 = DbHelper.cursor2Modul(Bed.class, cursor);
                            if (bed1 != null && !TextUtils.isEmpty(bed1.getName())) {
                                person.managedBeds.add(bed1);
                            }
                        } else {
                            cursor.moveToPrevious();
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static Person find(SQLiteDatabase database, String uuid){
        Person person = null;
        String selection = "person_uuid = ?";
        String[] args = {uuid};
        Cursor cursor = database.query("person",null,selection,args,null,null,null);
        if (cursor.moveToFirst()) {
            person = DbHelper.cursor2Modul(Person.class, cursor);
        }
        cursor.close();
        return person;
    }
}
