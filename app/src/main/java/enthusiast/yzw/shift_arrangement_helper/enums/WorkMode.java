package enthusiast.yzw.shift_arrangement_helper.enums;

public enum WorkMode {
    NORMAL("常规"),
    OVER_TIME("加班"),
    ABSENT("缺勤");

    private WorkMode(String text){
        this.text = text;
    }

    private String text;

    public String getText() {
        return text;
    }
}
