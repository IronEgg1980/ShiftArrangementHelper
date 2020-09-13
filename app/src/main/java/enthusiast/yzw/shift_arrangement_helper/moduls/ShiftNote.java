package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;

public class ShiftNote extends DatabaseEntity {
    private int weekOfYear;
    private Person person;
    private String note = "";

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isAvailable() {
        return person != null && person.getId() > 0 && !TextUtils.isEmpty(note);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ShiftNote) {
            ShiftNote shiftNote = (ShiftNote) obj;
            return this.weekOfYear == shiftNote.getWeekOfYear()
                    && this.note.equals(shiftNote.getNote())
                    && this.person == shiftNote.getPerson();
        }
        return false;
    }

    public static ShiftNote find(int weekOfYear, long personId) {
        ShiftNote shiftNote = null;
        String sql = "SELECT shiftnote.*,person.*" +
                " FROM shiftnote" +
                " INNER JOIN person USING(person_id)" +
                " WHERE shiftnote_weekofyear = " + weekOfYear + " and shiftnote.person_uuid = " + personId;
        Cursor cursor = DbHelper.getReadDB().rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            shiftNote = DbHelper.cursor2Modul(ShiftNote.class, cursor);
        }
        cursor.close();
        return shiftNote;
    }
}
