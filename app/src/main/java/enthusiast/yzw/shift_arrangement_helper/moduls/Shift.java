package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;
import enthusiast.yzw.shift_arrangement_helper.enums.AbsentVarMode;
import enthusiast.yzw.shift_arrangement_helper.tools.DateTool;

public class Shift extends DatabaseEntity {
    // 到这里
    private LocalDate date;
    private Person person;
    private WorkCategory work;

    public int getWeekOfYear() {
        return date == null ? -1 : DateTool.getWeekOfYear(date);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public WorkCategory getWork() {
        return work;
    }

    public void setWork(WorkCategory work) {
        this.work = work;
    }

    public boolean isAvailable() {
        return date != null &&
                person != null && person.getId() > 0 &&
                work != null && work.getId() > 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Shift) {
            Shift shift = (Shift) obj;
            return this.date == shift.getDate() && this.person == shift.getPerson() && this.work == shift.getWork();
        }
        return false;
    }

    public static List<Shift> find(String selection, String... args) {
        String sql = "SELECT shift.*,person.*,workcategory.*" +
                " FROM shift" +
                " INNER JOIN person ON shift.person_uuid = person.person_uuid" +
                " INNER JOIN workcategory ON shift.workcategory_uuid = workcategory.workcategory_uuid";
        if (selection != null) {
            sql = sql + " WHERE " + selection;
        }
        sql = sql + " ORDER BY shift_id";
        List<Shift> tmp = new ArrayList<>();
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            do {
                tmp.add(DbHelper.cursor2Modul(Shift.class, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tmp;
    }
}
