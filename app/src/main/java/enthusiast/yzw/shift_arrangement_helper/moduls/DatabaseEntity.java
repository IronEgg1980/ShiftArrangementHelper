package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;

public abstract class DatabaseEntity {
    public String tableName = DbHelper.getDataBaseTableName(this.getClass());
    protected long id;
//    protected String UUID = OtherTool.getRandomUUID();

    public long getId() {
        return id;
    }

//    public String getUUID() {
//        return UUID;
//    }

    public boolean save() {
        return save(DbHelper.getWriteDB());
    }

    public boolean update() {
        return update(DbHelper.getWriteDB());
    }

    public boolean dele() {
        return dele(DbHelper.getWriteDB());
    }

    public boolean save(SQLiteDatabase database) {
        ContentValues contentValues = DbHelper.modul2ContentValues(this);
        id = database.insert(tableName, null, contentValues);
        return id > 0;
    }

    public boolean update(SQLiteDatabase database) {
        ContentValues contentValues = DbHelper.modul2ContentValues(this);
        String selection = getDbColumnName("id") + " = " + id;
        return database.update(tableName, contentValues, selection, null) > 0;
    }

    public boolean dele(SQLiteDatabase database) {
        String selection = getDbColumnName("id") + " = "+id;
        return database.delete(tableName, selection, null) > 0;
    }

    public boolean saveOrUpdate() {
        if (isSaved())
            return update();
        return save();
    }

    public boolean isSaved() {
        return this.id > 0;
    }

    public String getDbColumnName(String fieldName) {
        return DbHelper.getDataBaseColumnName(tableName, fieldName);
    }
}
