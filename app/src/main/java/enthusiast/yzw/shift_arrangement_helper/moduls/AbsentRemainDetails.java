package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;
import enthusiast.yzw.shift_arrangement_helper.enums.AbsentVarMode;

public class AbsentRemainDetails extends DatabaseEntity {
    private LocalDateTime recordTime;
    private LocalDate date;
    private Person person;
    private double varValue;
    private AbsentVarMode mode;
    private String reason;

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public double getVarValue() {
        return varValue;
    }

    public void setVarValue(double varValue) {
        this.varValue = varValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AbsentVarMode getMode() {
        return mode;
    }

    public void setMode(AbsentVarMode mode) {
        this.mode = mode;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof AbsentRemainDetails){
            AbsentRemainDetails details = (AbsentRemainDetails) obj;
            return this.recordTime == details.getRecordTime()
                    && this.person.equals(details.getPerson());
        }
        return false;
    }

    @Override
    public boolean save(SQLiteDatabase database) {
        boolean b = false;
        try {
            database.beginTransaction();
            person.setAbsentRemainValue(person.getAbsentRemainValue() + varValue);
            person.updateRemainValue(database);
            ContentValues contentValues = DbHelper.modul2ContentValues(this);
            database.insert(tableName,null,contentValues);
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
    public boolean dele(SQLiteDatabase database) {
        boolean b = false;
        try {
            database.beginTransaction();
            person.setAbsentRemainValue(person.getAbsentRemainValue() - this.varValue);
            person.updateRemainValue(database);
            String condition = "absentremaindetails_uuid = ?";
            String[] args = {this.UUID};
            database.delete(tableName,condition,args);
            database.setTransactionSuccessful();
            b = true;
        }catch (Exception e) {
            e.printStackTrace();
            b = false;
            Log.e("殷宗旺", e.getLocalizedMessage(),e);
        } finally {
            database.endTransaction();
        }
        return b;
    }

    public void saveAutoMinusAbsent(SQLiteDatabase database){
        person = Person.find(database,person.getUUID());
        person.setAbsentRemainValue(person.getAbsentRemainValue() + varValue);
        person.updateRemainValue(database);
        ContentValues contentValues = DbHelper.modul2ContentValues(this);
        database.insert(tableName,null,contentValues);
    }

    public void deleAutoMinusAbsent(SQLiteDatabase database){
        person = Person.find(database,person.getUUID());
        person.setAbsentRemainValue(person.getAbsentRemainValue() - this.varValue);
        person.updateRemainValue(database);
        String condition = "absentremaindetails_uuid = ?";
        String[] args = {this.UUID};
        database.delete(tableName,condition,args);
    }

    public static List<AbsentRemainDetails> findAll(SQLiteDatabase sqLiteDatabase,String selection,String[] args){
        List<AbsentRemainDetails> list = new ArrayList<>();
        String sql = "SELECT absentremaindetails.*,person.* FROM absentremaindetails" +
                " INNER JOIN person USING(person_uuid)" ;
        if(!TextUtils.isEmpty(selection))
            sql = sql + " WHERE "+selection;
        sql = sql + " ORDER BY absentremaindetails_date";
        Cursor cursor = sqLiteDatabase.rawQuery(sql,args);
        if(cursor.moveToFirst()){
            do {
                list.add(DbHelper.cursor2Modul(AbsentRemainDetails.class, cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static List<AbsentRemainDetails> findAll(){
        return findAll(DbHelper.getReadDB(),null,null);
    }

    public static List<AbsentRemainDetails> findAll(String person_uuid){
        String[] args = {person_uuid};
        return findAll(DbHelper.getReadDB(),"person_uuid = ?",args);
    }
}
