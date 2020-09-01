package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftTemplate;

public class ShiftTemplateSelect extends PopupWindow {
    private AppCompatActivity mActivity;
    private MyAdapter<String> adapter;
    private List<String> mList;
    private DialogDissmissListener dissmissListener;

    public ShiftTemplateSelect(AppCompatActivity activity){
        this.mActivity = activity;
        initial();
        initialView();
        setTouchable(true);
        setOutsideTouchable(true);
        setFocusable(true);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                changeAlpha(1f);
            }
        });
    }

    public ShiftTemplateSelect setDissmissListener(DialogDissmissListener dissmissListener) {
        this.dissmissListener = dissmissListener;
        return this;
    }

    private void dele(final int position){
        final String name = mList.get(position);
        ConfirmDialog confirmDialog = ConfirmDialog.newInstance("删除？","是否删除排班模板【"+name+"】？");
        confirmDialog.setDialogDissmissListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if(result == DialogResult.CONFIRM){
                    if(ShiftTemplate.deleAll(name)){
                        mList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }else{
                        new MyToast(mActivity).centerShow("删除失败");
                    }
                }
            }
        }).show(mActivity.getSupportFragmentManager(),"confirm");
    }

    private void initial(){
        mList = ShiftTemplate.getNameList();
        adapter = new MyAdapter<String>(mList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final String data) {
                myViewHolder.setText(R.id.textview_item_name,data);
                myViewHolder.getView(R.id.imageview_item_dele).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dele(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(dissmissListener!=null)
                            dissmissListener.onDissmiss(DialogResult.CONFIRM,data);
                        dismiss();
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_shifttemplate_select;
            }
        };
    }

    private void initialView(){
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_select_single,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_dialog_selected_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new DividerItemDecoration(mActivity,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.textview_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        TextView textView = view.findViewById(R.id.textview_dialog_title);
        textView.setText("排班模板列表");
        setContentView(view);
    }

    private void changeAlpha(float value){
        if (mActivity == null)
            return;
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = value;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }

    public void show(){
        View view = mActivity.getWindow().getDecorView();
        int width = (int) (view.getWidth() * 0.8);
        int height = (int) (view.getHeight() * 0.5);
        setWidth(width);
        setHeight(height);
        showAtLocation(view, Gravity.CENTER,0,0);
        changeAlpha(0.5f);
    }
}
