package enthusiast.yzw.shift_arrangement_helper.enums;

public enum Gender {
    MAN("男"),
    WOMAN("女");
    private String text;
    public String getText(){
        return this.text;
    }
    Gender(String text){
        this.text = text;
    }
}
