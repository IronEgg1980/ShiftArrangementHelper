package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;

public class ShiftTemplate extends DatabaseEntity {
    private String name;
    private int index;
    private WorkCategory workCategory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public WorkCategory getWorkCategory() {
        return workCategory;
    }

    public void setWorkCategory(WorkCategory workCategory) {
        this.workCategory = workCategory;
    }

    @Override
    public boolean save(SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(getDbColumnName("uuid"), this.UUID);
        contentValues.put(getDbColumnName("name"), this.name);
        contentValues.put(getDbColumnName("index"), this.index);
        contentValues.put("workcategory_uuid", workCategory == null ? "null" : workCategory.getUUID());
        return database.insert(this.tableName, null, contentValues) > 0;
    }

    public static boolean saveAll(List<ShiftOfWeekView> shiftOfWeekViewList,String templateName){
        boolean b;
        SQLiteDatabase sqLiteDatabase = DbHelper.getWriteDB();
        try {
            sqLiteDatabase.beginTransaction();
            int index = 0;
            for(ShiftOfWeekView entity:shiftOfWeekViewList){
                for(WorkCategory workCategory:entity.getWorks()){
                    ShiftTemplate shiftTemplate = new ShiftTemplate();
                    shiftTemplate.setName(templateName);
                    shiftTemplate.setIndex(index++);
                    shiftTemplate.setWorkCategory(workCategory);
                    shiftTemplate.save(sqLiteDatabase);
                }
            }
            sqLiteDatabase.setTransactionSuccessful();
            b = true;
        }catch (Exception e) {
            e.printStackTrace();
            b = false;
        }finally {
            sqLiteDatabase.endTransaction();
        }
        return b;
    }

    public static List<String> getNameList() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT shifttemplate_id,shifttemplate_name FROM shifttemplate" +
                " GROUP BY shifttemplate_name" +
                " ORDER BY shifttemplate_id";
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql,null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndex("shifttemplate_name")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static boolean isExist(String name) {
        String condition = "shifttemplate_name = ?";
        String[] args = {name};
        Cursor cursor = DbHelper.getReadDB().query(DbHelper.getDataBaseTableName(ShiftTemplate.class), null, condition, args, null, null, null);
        boolean b = cursor.moveToFirst();
        cursor.close();
        return b;
    }

    public static boolean deleAll(String name) {
        String condition = "shifttemplate_name = ?";
        String[] args = {name};
        SQLiteDatabase database = DbHelper.getWriteDB();
        return database.delete(DbHelper.getDataBaseTableName(ShiftTemplate.class), condition, args) > 0;
    }

    public static List<ShiftOfWeekView> find(String name) {
        List<ShiftOfWeekView> list = new ArrayList<>();
        String sql = "SELECT shifttemplate.*,workcategory.* FROM shifttemplate" +
                " INNER JOIN workcategory USING(workcategory_uuid)" +
                " WHERE shifttemplate_name = ?" +
                " ORDER BY shifttemplate_index";
        String[] args = {name};
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql,args);
        if (cursor.moveToFirst()) {
            do {
                ShiftTemplate template = DbHelper.cursor2Modul(ShiftTemplate.class, cursor);
                if(template == null)
                    continue;
                ShiftOfWeekView entity;
                if (template.getIndex() % 7 == 0) {
                    entity = new ShiftOfWeekView();
                    list.add(entity);
                } else {
                    entity = list.get(list.size() - 1);
                }
                WorkCategory workCategory = template.getWorkCategory();
                if (workCategory != null && workCategory.isAvailable())
                    entity.setWork(template.getIndex() % 7, template.getWorkCategory());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
