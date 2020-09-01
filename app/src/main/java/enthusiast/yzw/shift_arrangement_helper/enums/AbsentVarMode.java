package enthusiast.yzw.shift_arrangement_helper.enums;

public enum AbsentVarMode {
    INPUT("输入"),
    ADJUST("调整"),
    SHIFT_ATUO("排班")
    ;

    public String getText() {
        return text;
    }

    private String text;
    private AbsentVarMode(String reason){
        this.text = reason;
    }
}
