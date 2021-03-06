package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;
import enthusiast.yzw.shift_arrangement_helper.enums.WorkMode;

public class WorkCategory extends DatabaseEntity {
    private String name = "";
    private WorkMode mode = WorkMode.NORMAL;
    private double overTimePay = 0f;
    private boolean isAutoMinus = false;
    private double minusValue = 1.0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkMode getMode() {
        return mode;
    }

    public void setMode(WorkMode mode) {
        this.mode = mode;
    }

    public double getOverTimePay() {
        return overTimePay;
    }

    public void setOverTimePay(double overTimePay) {
        this.overTimePay = overTimePay;
    }

    public boolean isAutoMinus() {
        return isAutoMinus;
    }

    public void setAutoMinus(boolean autoMinus) {
        isAutoMinus = autoMinus;
    }

    public double getMinusValue() {
        return minusValue;
    }

    public void setMinusValue(double minusValue) {
        this.minusValue = minusValue;
    }

    public boolean isAvailable() {
        return isSaved() && !TextUtils.isEmpty(this.name);
    }

    @Override
    public boolean dele(SQLiteDatabase database) {
        String column = getDbColumnName("id");
        String condition = column + " = " + id;
        boolean b;
        try {
            database.beginTransaction();
            database.delete(tableName, condition, null);
            database.delete("shift", condition, null);
            database.delete("shifttemplate", condition, null);
            database.setTransactionSuccessful();
            b = true;
        } catch (Exception e) {
            e.printStackTrace();
            b = false;
        } finally {
            database.endTransaction();
        }
        return b;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof WorkCategory) {
            WorkCategory category = (WorkCategory) obj;
            return this.name.equals(category.getName());
        }
        return false;
    }
}
