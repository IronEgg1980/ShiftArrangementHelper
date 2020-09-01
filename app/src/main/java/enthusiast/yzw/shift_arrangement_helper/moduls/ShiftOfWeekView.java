package enthusiast.yzw.shift_arrangement_helper.moduls;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.enums.AbsentVarMode;
import enthusiast.yzw.shift_arrangement_helper.tools.DateTool;

public class ShiftOfWeekView {
    private LocalDate monday;
    private Person person;
    private WorkCategory[] works = new WorkCategory[7];
    private String note;

    public Person getPerson() {
        return person;
    }

    public String getPersonName() {
        return person == null ? "" : person.getName();
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public WorkCategory[] getWorks() {
        return this.works;
    }

    public String getWorkCategoryName(int index) {
        return works[index] == null ? "" : works[index].getName();
    }

    public void setWork(int index, WorkCategory work) {
        works[index] = work;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getBedAssignString() {
        if (person == null || person.managedBeds.isEmpty())
            return "";
        StringBuilder s = new StringBuilder();
        if (person.managedBeds.size() > 0) {
            for (Bed bed : person.managedBeds) {
                s.append("、").append(bed.getName());
            }
        }
        return s.substring(1);
    }

    public double getAbsentRemain() {
        return person == null ? 0 : person.getAbsentRemainValue();
    }

    public LocalDate getMonday() {
        return monday;
    }

    public void setMonday(LocalDate monday) {
        this.monday = monday;
    }

    public boolean isAvailable() {
        return person != null && person.getId() > 0 && monday != null;
    }

    public static void deleAll(LocalDate date) {
        SQLiteDatabase database = DbHelper.getWriteDB();
        try {
            database.beginTransaction();
            deleAllNoTraslation(database, date);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    public static void deleAllNoTraslation(SQLiteDatabase database, LocalDate date) {
        LocalDate monday = DateTool.getMonday(date);
        String table1 = DbHelper.getDataBaseTableName(Shift.class);
        String table2 = DbHelper.getDataBaseTableName(ShiftNote.class);
        String selection1 = "shift_date >= ? and shift_date <= ?";
        String[] args1 = {String.valueOf(monday.toEpochDay()), String.valueOf(monday.plusDays(6).toEpochDay())};
        String selection2 = "shiftnote_weekofyear = ?";
        String[] args2 = {String.valueOf(DateTool.getWeekOfYear(monday))};
        String absentSelection = "AbsentRemainDetails_date >= ? and AbsentRemainDetails_date <= ? and AbsentRemainDetails_mode = ?";
        String[] absentArgs = {String.valueOf(monday.toEpochDay()), String.valueOf(monday.plusDays(6).toEpochDay()), String.valueOf(AbsentVarMode.SHIFT_ATUO.ordinal())};
        // 取消余假调整
        List<AbsentRemainDetails> temp = AbsentRemainDetails.findAll(database, absentSelection, absentArgs);
        for (AbsentRemainDetails details : temp) {
            details.deleAutoMinusAbsent(database);
        }
        database.delete(table1, selection1, args1);
        database.delete(table2, selection2, args2);
    }

    public static boolean isExist(LocalDate date) {
        LocalDate monday = DateTool.getMonday(date);
        LocalDate sunday = monday.plusDays(6);
        String table = DbHelper.getDataBaseTableName(Shift.class);
        String column = DbHelper.getDataBaseColumnName(table, "date");
        String selection = column + " >= ? and " + column + " <= ?";
        return DbOperator.isExist(table, selection, String.valueOf(monday.toEpochDay()), String.valueOf(sunday.toEpochDay()));
    }

    public static List<ShiftOfWeekView> getAWeekShift(LocalDate localDate) {
        LocalDate monday = DateTool.getMonday(localDate);
        int weekOfYear = DateTool.getWeekOfYear(monday);
        List<ShiftOfWeekView> list = new ArrayList<>();
        List<Shift> tmp = Shift.find("shift_date >= ? and shift_date <= ?", String.valueOf(monday.toEpochDay()), String.valueOf(monday.plusDays(6).toEpochDay()));
        for (int i = 0; i < tmp.size(); i++) {
            Person p = tmp.get(i).getPerson();
            p.fillBedManageList();
            ShiftNote shiftNote = ShiftNote.find(weekOfYear, p.getUUID());
            ShiftOfWeekView shiftOfWeekView = new ShiftOfWeekView();

            for (int j = 0; j < 7; j++) {
                if (i >= tmp.size())
                    break;
                Shift shift = tmp.get(i);
                LocalDate date = monday.plusDays(j);
                if (shift.getPerson().equals(p) && shift.getDate().isEqual(date)) {
                    shiftOfWeekView.setWork(j, shift.getWork());
                    i++;
                }
            }
            shiftOfWeekView.setMonday(monday);
            shiftOfWeekView.setPerson(p);
            shiftOfWeekView.setNote(shiftNote == null ? "" : shiftNote.getNote());
            i--;
            list.add(shiftOfWeekView);
        }
        return list;
    }

    public static boolean saveTogether(@NonNull List<ShiftOfWeekView> list, @NonNull LocalDate date) {
        boolean result = false;
        LocalDate monday = DateTool.getMonday(date);
        SQLiteDatabase database = DbHelper.getWriteDB();
        try {
            database.beginTransaction();
            deleAllNoTraslation(database,date);
            for (ShiftOfWeekView entity : list) {
                Person person = entity.getPerson();
                if (person == null)
                    continue;
                String s = entity.getNote();
                if (!TextUtils.isEmpty(s)) {
                    ShiftNote note = new ShiftNote();
                    note.setWeekOfYear(DateTool.getWeekOfYear(monday));
                    note.setPerson(person);
                    note.setNote(s);
                    note.save(database);
                }
                for (int i = 0; i < 7; i++) {
                    WorkCategory work = entity.getWorks()[i];
                    if (work != null) {
                        LocalDate date1 = monday.plusDays(i);
                        Shift shift = new Shift();
                        shift.setDate(date1);
                        shift.setPerson(person);
                        shift.setWork(work);
                        shift.save(database);
                        if (work.isAutoMinus()) {
                            AbsentRemainDetails absentRemainDetails = new AbsentRemainDetails();
                            absentRemainDetails.setPerson(person);
                            absentRemainDetails.setDate(date1);
                            absentRemainDetails.setRecordTime(LocalDateTime.now());
                            absentRemainDetails.setReason("排班自动扣除");
                            absentRemainDetails.setMode(AbsentVarMode.SHIFT_ATUO);
                            absentRemainDetails.setVarValue(-1 * work.getMinusValue());
                            absentRemainDetails.saveAutoMinusAbsent(database);
                        }
                    }
                }
            }
            database.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            database.endTransaction();
        }
        return result;
    }
}
