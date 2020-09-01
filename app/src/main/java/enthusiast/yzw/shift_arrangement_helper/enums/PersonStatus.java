package enthusiast.yzw.shift_arrangement_helper.enums;

public enum  PersonStatus {
    ONDUTY("在岗"),
    LEAVED("出科"),
    DELETED("已删除"),
    ONABSENT("请假中");
    private PersonStatus(String text){
        this.text=text;
    }
    private String text ;

    public String getText() {
        return text;
    }
}
