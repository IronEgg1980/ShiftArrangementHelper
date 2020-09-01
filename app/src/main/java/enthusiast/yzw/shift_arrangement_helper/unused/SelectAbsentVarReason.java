package enthusiast.yzw.shift_arrangement_helper.unused;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.enums.AbsentVarMode;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;

public class SelectAbsentVarReason extends PopupWindow {
    private DialogDissmissListener listener;

    public SelectAbsentVarReason setListener(DialogDissmissListener listener) {
        this.listener = listener;
        return this;
    }

    public SelectAbsentVarReason(View parent){
        createView(parent.getContext());
        setWidth(parent.getWidth());
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setTouchable(true);
        setOutsideTouchable(true);
        setFocusable(true);
    }

    private void createView(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.pop_menu_layout,new FrameLayout(context),false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final List<AbsentVarMode> list = Arrays.asList(AbsentVarMode.values());
        MyAdapter<AbsentVarMode> adapter = new MyAdapter<AbsentVarMode>(list) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, AbsentVarMode data) {
                myViewHolder.setText(R.id.textview1,data.getText());
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(listener!=null)
                            listener.onDissmiss(DialogResult.CONFIRM,myViewHolder.getAbsoluteAdapterPosition());
                        dismiss();
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_popmenu;
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        setContentView(view);
    }
}
