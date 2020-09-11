package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;

public class Setup extends DatabaseEntity {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean dele(SQLiteDatabase database) {
        String selection = "setup_key = ? and setup_value = ?";
        String[] args = {this.key,this.value};
        return database.delete(tableName,selection,args) > 0;
    }

    public boolean isExist() {
        String[] args = {this.key, this.value};
        Cursor cursor = DbHelper.getReadDB().rawQuery("SELECT * FROM setup WHERE setup_key = ? AND setup_value = ?",args);
        boolean b = cursor.moveToFirst();
        cursor.close();
        return b;
    }

    public static List<Setup> find(SQLiteDatabase database, String key) {
        List<Setup> list = new ArrayList<>();
        String selection = "setup_key = ?";
        String[] args = {key};
        Cursor cursor = database.query("setup", null, selection, args, null, null, "setup_value");
        if (cursor.moveToFirst()) {
            do {
                list.add(DbHelper.cursor2Modul(Setup.class, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static List<Setup> lookup(String key,String valueWords){
        List<Setup> list = new ArrayList<>();
        String sql = "SELECT * FROM setup WHERE setup_key = ? AND setup_value LIKE '%"+valueWords+"%'";
        String[] args = {key};
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql,args);
        if (cursor.moveToFirst()) {
            do {
                list.add(DbHelper.cursor2Modul(Setup.class, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static List<Setup> find(String key) {
        return find(DbHelper.getReadDB(), key);
    }

    public static Setup findOneOrFirst(SQLiteDatabase database, String key) {
        Setup setup = null;
        String selection = "setup_key = ?";
        String[] args = {key};
        Cursor cursor = database.query("setup", null, selection, args, null, null, "setup_value");
        if (cursor.moveToFirst()) {
            setup = DbHelper.cursor2Modul(Setup.class, cursor);
        }
        cursor.close();
        return setup;
    }

    public static Setup findOneOrFirst(String key) {
        return findOneOrFirst(DbHelper.getReadDB(), key);
    }
}
