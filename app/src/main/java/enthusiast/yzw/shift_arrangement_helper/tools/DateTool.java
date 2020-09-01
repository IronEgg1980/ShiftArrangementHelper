package enthusiast.yzw.shift_arrangement_helper.tools;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftOfWeekView;

public class DateTool {
    public static int getWeekOfYear(LocalDate localDate){
        return localDate.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public static LocalDate getMonday(LocalDate localDate){
        return localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getUnassignedShiftDate(LocalDate localDate){
        if(ShiftOfWeekView.isExist(localDate))
            return getUnassignedShiftDate(localDate.plusDays(7));
        return localDate;
    }
}
