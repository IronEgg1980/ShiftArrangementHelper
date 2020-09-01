package enthusiast.yzw.shift_arrangement_helper.db_helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telecom.StatusHints;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.moduls.AbsentRemainDetails;
import enthusiast.yzw.shift_arrangement_helper.moduls.Bed;
import enthusiast.yzw.shift_arrangement_helper.moduls.BedAssign;
import enthusiast.yzw.shift_arrangement_helper.moduls.DatabaseEntity;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;
import enthusiast.yzw.shift_arrangement_helper.moduls.Shift;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftNote;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftOfWeekView;
import enthusiast.yzw.shift_arrangement_helper.moduls.TodayShift;
import enthusiast.yzw.shift_arrangement_helper.moduls.WorkCategory;
import enthusiast.yzw.shift_arrangement_helper.tools.DateTool;

public final class DbOperator {
    private DbOperator() {
    }

    private static String TAG = "殷宗旺";

    private static <T extends DatabaseEntity> Cursor getCursor(Class<T> clazz, String fieldName, String value) {
        String tableName = DbHelper.getDataBaseTableName(clazz);
        String columnName = DbHelper.getDataBaseColumnName(tableName, fieldName);
        String selection = columnName + " = ?";
        String[] args = {value};
        return  DbHelper.getReadDB().query(tableName, null, selection, args, null, null, null);
    }

    public static <T extends DatabaseEntity> List<T> findAll(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        String tableName = DbHelper.getDataBaseTableName(clazz);
        String columnName = DbHelper.getDataBaseColumnName(tableName, "id");
        Cursor cursor = DbHelper.getReadDB().query(tableName, null, null, null, null, null, columnName);
        if (cursor.moveToFirst()) {
            do {
                list.add(DbHelper.cursor2Modul(clazz, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static <T extends DatabaseEntity> List<T> findAll(Class<T> clazz, String selection, String... args) {
        List<T> list = new ArrayList<>();
        String tableName = DbHelper.getDataBaseTableName(clazz);
        String columnName = DbHelper.getDataBaseColumnName(tableName, "id");
        Cursor cursor = DbHelper.getReadDB().query(tableName, null, selection, args, null, null, columnName);
        if (cursor.moveToFirst()) {
            do {
                list.add(DbHelper.cursor2Modul(clazz, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static <T extends DatabaseEntity> T findByUUID(Class<T> clazz, String uuid) {
        Cursor cursor = getCursor(clazz, "UUID", uuid);
        T t = null;
        if (cursor.moveToFirst()) {
            t = DbHelper.cursor2Modul(clazz, cursor);
        }
        cursor.close();
        return t;
    }

    public static <T extends DatabaseEntity> boolean isExist(Class<T> clazz, String fieldName, String value) {
        Cursor cursor = getCursor(clazz, fieldName, value);
        boolean b = cursor.moveToFirst();
        cursor.close();
        return b;
    }

    public static boolean isExist(String table, String selection, String... args) {
        Cursor cursor = DbHelper.getReadDB().query(table, null, selection, args, null, null, null);
        boolean b = cursor.moveToFirst();
        cursor.close();
        return b;
    }

    public static String getOrganizeName() {
        String name = "";
        Cursor cursor = DbHelper.getReadDB().query("organize_name", null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex("name"));
        }
        cursor.close();
        return name;
    }

    public static boolean setOrganizeName(String name) {
        boolean b = false;
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            database.delete("organize_name", null, null);
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name);
            database.insert("organize_name", null, contentValues);
            database.setTransactionSuccessful();
            b = true;
        } finally {
            database.endTransaction();
        }
        return b;
    }

    public static <T extends DatabaseEntity> int saveAll(List<T> list) {
        int i = 0;
        if (list.isEmpty())
            return i;
        SQLiteDatabase sqLiteDatabase = DbHelper.getWriteDB();
        try {
            sqLiteDatabase.beginTransaction();
            for (T t : list) {
                t.save(sqLiteDatabase);
                i++;
            }
            sqLiteDatabase.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
            i = 0;
        }finally {
            sqLiteDatabase.endTransaction();
        }
        return i;
    }

    public static <T extends DatabaseEntity> boolean deleAll(Class<T> clazz, String condition, String... args) {
        String tableName = DbHelper.getDataBaseTableName(clazz);
        return DbHelper.getWriteDB().delete(tableName, condition, args) > 0;
    }
}
