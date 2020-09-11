package enthusiast.yzw.shift_arrangement_helper.db_helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.moduls.AbsentRemainDetails;
import enthusiast.yzw.shift_arrangement_helper.moduls.Bed;
import enthusiast.yzw.shift_arrangement_helper.moduls.BedAssign;
import enthusiast.yzw.shift_arrangement_helper.moduls.DatabaseEntity;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;
import enthusiast.yzw.shift_arrangement_helper.moduls.Setup;
import enthusiast.yzw.shift_arrangement_helper.moduls.Shift;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftNote;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftTemplate;
import enthusiast.yzw.shift_arrangement_helper.moduls.WorkCategory;

public class DbHelper extends SQLiteOpenHelper {
    private static final String dbName = "database.db"; // 数据库名称
    private static final int version = 1; // 数据库版本号
    private static final String TAG = "殷宗旺";
    private static DbHelper dbHelper = null;

    // 数据库表
    private final Class<?>[] classes =
            {
                    AbsentRemainDetails.class,
                    Bed.class,
                    BedAssign.class,
                    Person.class,
                    Shift.class,
                    ShiftNote.class,
                    WorkCategory.class,
                    ShiftTemplate.class,
                    Setup.class
            };

    public static void onDestory() {
        dbHelper.close();
        dbHelper = null;
    }

    public static SQLiteDatabase getWriteDB() {
        return dbHelper.getWritableDatabase();
    }

    public static SQLiteDatabase getReadDB() {
        return dbHelper.getReadableDatabase();
    }

    public static void initial(Context context) {
        if (dbHelper == null)
            dbHelper = new DbHelper(context, dbName, null, version);
    }

    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<?> clazz : classes) {
            Log.d(TAG, "onCreate: " + clazz.getSimpleName());
            db.execSQL(getCreateTableSql(clazz));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
            case 2:
            case 3:
                /*...*/
        }
    }


    private String getCreateTableSql(Class<?> clazz) {
        String tableName = getDataBaseTableName(clazz);
        StringBuilder builder = new StringBuilder("CREATE TABLE ").append(tableName).append("(");
        List<Field> list = new ArrayList<>();
        getDataBaseFields(clazz, list);
        for (Field field : list) {
            field.setAccessible(true);
            String columnName = getDataBaseColumnName(tableName, field.getName());
            if (field.getName().equalsIgnoreCase("id")) {
                builder.append(columnName).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
                continue;
            }
            Class<?> type = field.getType();
            if (type == Long.class || type == Long.TYPE
                    || type == Integer.class || type == Integer.TYPE
                    || type == Boolean.class || type == Boolean.TYPE
                    || type == LocalDate.class || type == LocalDateTime.class) {
                builder.append(columnName).append(" INTEGER,");
            } else if (type == String.class) {
                builder.append(columnName).append(" TEXT,");
            } else if (type == Float.class || type == Float.TYPE || type == Double.class || type == Double.TYPE) {
                builder.append(columnName).append(" REAL,");
            } else if (type.isEnum()) {
                builder.append(columnName).append(" INTEGER,");
            } else {
                String typeName = type.getTypeName();
                int index = typeName.lastIndexOf(".");
                String _tableName = typeName.substring(index + 1);
                String _columnName = getDataBaseColumnName(_tableName, "UUID");
                builder.append(_columnName).append(" TEXT,");
            }
        }
        return builder.substring(0, builder.length() - 1) + ")";
    }

    public static <T> String getDataBaseTableName(Class<T> clazz) {
        String name = clazz.getName();
        int index = name.lastIndexOf(".") + 1;
        return name.substring(index).toLowerCase();
    }

    public static String getDataBaseColumnName(String tableName, String fieldName) {
        return tableName.toLowerCase() + "_" + fieldName.toLowerCase();
    }

    public static void getDataBaseFields(Class<?> clazz, @NonNull List<Field> resultList) {
        if (clazz == null)
            return;
        Field[] fields1 = clazz.getDeclaredFields();
        for (Field field : fields1) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isPublic(modifiers)
                    || Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
                continue;
            field.setAccessible(true);
            resultList.add(field);
        }
        getDataBaseFields(clazz.getSuperclass(), resultList);
    }

    public static <T> T cursor2Modul(Class<T> clazz, Cursor cursor) {
        try {
            String tableName = getDataBaseTableName(clazz);
            T t = clazz.newInstance();
            List<Field> list = new ArrayList<>();
            getDataBaseFields(clazz, list);
            for (Field field : list) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                String columnName = getDataBaseColumnName(tableName, field.getName());

                int index = cursor.getColumnIndex(columnName);
                Object value = null;

                if (type == Long.class || type == Long.TYPE) {
                    value = cursor.getLong(index);
                } else if (type == Integer.class || type == Integer.TYPE) {
                    value = cursor.getInt(index);
                } else if (type == Boolean.class || type == Boolean.TYPE) {
                    value = cursor.getInt(index) == 1;
                } else if (type == Float.class || type == Float.TYPE) {
                    BigDecimal bigDecimal = new BigDecimal(Float.toString(cursor.getFloat(index)));
                    value = bigDecimal.floatValue();
                } else if (type == Double.class || type == Double.TYPE) {
                    BigDecimal bigDecimal = new BigDecimal(Double.toString(cursor.getDouble(index)));
                    value = bigDecimal.doubleValue();
                } else if (type == String.class) {
                    value = cursor.getString(index);
                } else if (type == LocalDate.class) {
                    value = LocalDate.ofEpochDay(cursor.getLong(index));
                } else if (type == LocalDateTime.class) {
                    value = LocalDateTime.ofEpochSecond(cursor.getLong(index), 0, ZoneOffset.ofHours(8));
                } else if (type.isEnum()) {
                    int i = cursor.getInt(index);
                    Object[] values = type.getEnumConstants();
                    if (values != null)
                        value = values[i];
                } else {
                    value = cursor2Modul(type, cursor);
                }
                field.set(t, value);
            }
            return t;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> ContentValues modul2ContentValues(Object t) {
        ContentValues contentValues = new ContentValues();
        try {
            List<Field> list = new ArrayList<>();
            getDataBaseFields(t.getClass(), list);
            String tableName = getDataBaseTableName(t.getClass());
            for (Field field : list) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                String columnName = getDataBaseColumnName(tableName, field.getName());
                if (field.getName().equalsIgnoreCase("id"))
                    continue;
                if (type == Long.class || type == Long.TYPE) {
                    contentValues.put(columnName, field.getLong(t));
                } else if (type == Integer.class || type == Integer.TYPE) {
                    contentValues.put(columnName, field.getInt(t));
                } else if (type == Boolean.class || type == Boolean.TYPE) {
                    int i = field.getBoolean(t) ? 1 : 0;
                    contentValues.put(columnName, i);
                } else if (type == Float.class || type == Float.TYPE) {
                    BigDecimal bigDecimal = new BigDecimal(Float.toString(field.getFloat(t)));
                    contentValues.put(columnName, bigDecimal.floatValue());
                } else if (type == Double.class || type == Double.TYPE) {
                    BigDecimal bigDecimal = new BigDecimal(Double.toString(field.getDouble(t)));
                    contentValues.put(columnName, bigDecimal.doubleValue());
                } else if (type == String.class) {
                    Object o = field.get(t);
                    String s = null == o ? "" : o.toString();
                    contentValues.put(columnName, s);
                } else if (type == LocalDate.class) {
                    LocalDate date = (LocalDate) field.get(t);
                    long localDate = date == null ? -1 : date.toEpochDay();
                    contentValues.put(columnName, localDate);
                } else if (type == LocalDateTime.class) {
                    LocalDateTime localDateTime = (LocalDateTime) field.get(t);
                    long localDate = localDateTime == null ? -1 : localDateTime.toEpochSecond(ZoneOffset.ofHours(8));
                    contentValues.put(columnName, localDate);
                } else if (type.isEnum()) {
                    Enum<?> e = (Enum<?>) field.get(t);
                    int value = e == null ? -1 : e.ordinal();
                    contentValues.put(columnName, value);
                } else {
                    DatabaseEntity entity = (DatabaseEntity) field.get(t);
                    String value = entity == null ? "" : entity.getUUID();
                    String _columnName = getDataBaseColumnName(getDataBaseTableName(type), "UUID");
                    contentValues.put(_columnName, value);
                }
            }
            return contentValues;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
