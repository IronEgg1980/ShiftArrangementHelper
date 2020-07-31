package enthusiast.yzw.shift_arrangement_helper.tools;

import java.util.UUID;

public final class OtherTool {
    private OtherTool(){}
    public static String getRandomUUID(){
        String s = UUID.randomUUID().toString();
        return s;
    }
}
