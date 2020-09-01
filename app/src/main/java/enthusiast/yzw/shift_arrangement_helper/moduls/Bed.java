package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;

public class Bed extends DatabaseEntity {
    private String name = "";

    public List<Person> bedManagerList = new ArrayList<>();
    public boolean isSeleted = false;
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Bed) {
            Bed bed = (Bed) obj;
            return this.name.equals(bed.getName()) && this.UUID.equals(bed.getUUID());
        }
        return false;
    }

    public boolean saveBedAssign(){
        boolean b = false;
        String condition = "bed_uuid = ?";
        String[] args = {this.UUID};
        String tableName = DbHelper.getDataBaseTableName(BedAssign.class);

        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            database.delete(tableName, condition, args);
            for (Person person : this.bedManagerList) {
                BedAssign bedAssign = new BedAssign();
                bedAssign.setPerson(person);
                bedAssign.setBed(this);
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

    @Override
    public boolean dele() {
        boolean b;
        String condition = "bed_uuid = ?";
        String[] args = {this.UUID};
        String tableName1 = DbHelper.getDataBaseTableName(BedAssign.class);
        String tableName2 = DbHelper.getDataBaseTableName(Bed.class);
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            database.delete(tableName1, condition, args);
            database.delete(tableName2, condition, args);
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

    public void fillPersonList(){
        String sql = "SELECT person.*,bedassign.bed_uuid FROM bedassign" +
                " INNER JOIN person ON person.person_uuid = bedassign.person_uuid" +
                " WHERE bedassign.bed_uuid = ?";
        String[] args = {this.UUID};
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql,args);
        if(cursor.moveToFirst()){
            do {
                bedManagerList.add(DbHelper.cursor2Modul(Person.class,cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    public static List<Bed> findAll(){
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT person.*,bed.*" +
                " FROM bed" +
                " Left JOIN bedassign ON bed.bed_uuid = bedassign.bed_uuid" +
                " Left JOIN person ON bedassign.person_uuid = person.person_uuid" +
                " ORDER BY bed_id";
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Bed bed = DbHelper.cursor2Modul(Bed.class, cursor);
                if (bed != null) {
                    Person person = DbHelper.cursor2Modul(Person.class, cursor);
                    if (person != null && !TextUtils.isEmpty(person.getName())) {
                        bed.bedManagerList.add(person);
                    }
                    list.add(bed);
                    while (cursor.moveToNext()) {
                        Bed bed1 = DbHelper.cursor2Modul(Bed.class, cursor);
                        if (bed1 != null && bed1.equals(bed)) {
                            Person person1 = DbHelper.cursor2Modul(Person.class, cursor);
                            if (person1 != null && !TextUtils.isEmpty(person1.getName())) {
                                bed.bedManagerList.add(person1);
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
}
