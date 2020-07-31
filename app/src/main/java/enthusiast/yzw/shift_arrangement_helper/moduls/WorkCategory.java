package enthusiast.yzw.shift_arrangement_helper.moduls;

import androidx.annotation.Nullable;

public class WorkCategory {
    public enum MODE {
        NORMAL("正常"),
        OVER_TIME("加班"),
        ABSENT("缺勤");

        private MODE(String text){
            this.text = text;
        }

        private String text;

        public String getText() {
            return text;
        }
    }

    private long id;
    private String name;
    private String UUID;
    private MODE mode;
    private float overTimePay;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public MODE getMode() {
        return mode;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public float getOverTimePay() {
        return overTimePay;
    }

    public void setOverTimePay(float overTimePay) {
        this.overTimePay = overTimePay;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof WorkCategory){
            WorkCategory category = (WorkCategory) obj;
            return this.name.equals(category.getName())&&this.UUID.equals(category.getUUID());
        }
        return false;
    }
}
