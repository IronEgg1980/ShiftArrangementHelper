package enthusiast.yzw.shift_arrangement_helper.dialogs;

import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;

public interface DialogDissmissListener {
    void onDissmiss(DialogResult result,Object...values);
}
