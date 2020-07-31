package enthusiast.yzw.shift_arrangement_helper.moduls;

import androidx.annotation.Nullable;

public class Person {
    public enum GENDER{
        MAN("男"),
        WOMAN("女");
        private String text;
        public String getText(){
            return this.text;
        }
        private GENDER(String text){
            this.text = text;
        }
    }

    private long id;
    private String UUID;
    private String name;
    private int age;
    private GENDER gender;
    private String post;
    private String professor;
    private String school;
    private String phone;
    private String note;

    public long getId() {
        return id;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public GENDER getGender() {
        return gender;
    }

    public void setGender(GENDER gender) {
        this.gender = gender;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Person){
            Person p = (Person) obj;
            return this.name.equals(p.getName()) && this.UUID.equals(p.getUUID());
        }
        return false;
    }
}
