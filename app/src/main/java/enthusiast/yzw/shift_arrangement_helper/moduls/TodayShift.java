package enthusiast.yzw.shift_arrangement_helper.moduls;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;

public class TodayShift {
    public WorkCategory workCategory;
    public List<Person> people = new ArrayList<>();

    public static List<TodayShift> find(LocalDate localDate){
        List<TodayShift> list = new ArrayList<>();
        List<Shift> shifts = Shift.find("shift_date = ?", String.valueOf(localDate.toEpochDay()));
        if (!shifts.isEmpty()) {
            Collections.sort(shifts, new Comparator<Shift>() {
                @Override
                public int compare(Shift shift, Shift t1) {
                    return shift.getWork().getName().compareTo(t1.getWork().getName());
                }
            });

            Shift shift = shifts.get(0);
            TodayShift todayShift = new TodayShift();
            todayShift.workCategory = shift.getWork();
            todayShift.people.add(shift.getPerson());
            list.add(todayShift);
            for (int i = 1; i < shifts.size(); i++) {
                TodayShift todayShift0 = list.get(list.size() - 1);
                Shift shift1 = shifts.get(i);
                if (todayShift0.workCategory.equals(shift1.getWork())) {
                    todayShift0.people.add(shift1.getPerson());
                } else {
                    TodayShift todayShift1 = new TodayShift();
                    todayShift1.workCategory = shift1.getWork();
                    todayShift1.people.add(shift1.getPerson());
                    list.add(todayShift1);
                }
            }
        }
        return list;
    }
}
