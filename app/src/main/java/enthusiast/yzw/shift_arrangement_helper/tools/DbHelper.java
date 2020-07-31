package enthusiast.yzw.shift_arrangement_helper.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {
    private final static String dbName = "database.db";
    private final static int version = 1;
    private static DbHelper dbHelper;

    public static void destory() {
        dbHelper = null;
    }

    public static void initial(Context context) {
        dbHelper = new DbHelper(context, dbName, null, version);
    }

    public SQLiteDatabase getWriteableDb() {
        return dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDb() {
        return dbHelper.getReadableDatabase();
    }

    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
