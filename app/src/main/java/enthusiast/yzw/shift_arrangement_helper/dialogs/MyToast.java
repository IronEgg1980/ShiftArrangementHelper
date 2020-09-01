package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import enthusiast.yzw.shift_arrangement_helper.R;

public class MyToast {
    private TextView textView;
    private Toast toast;

    public MyToast(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.toast_view,null);
        textView = view.findViewById(R.id.textview_toast);
        toast = new Toast(context);
        toast.setView(view);
    }

    public void show(String message,int gravity,int duration){
        textView.setText(message);
        toast.setGravity(gravity,0,0);
        toast.setDuration(duration);
        toast.show();
    }

    public void defaultShow(String message){
        show(message, Gravity.BOTTOM,Toast.LENGTH_SHORT);
    }

    public void centerShow(String message){
        show(message,Gravity.CENTER,Toast.LENGTH_SHORT);
    }
}
