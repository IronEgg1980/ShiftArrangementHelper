package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.Setup;

public class AutoCompleteDialog extends PopupWindow {
    private MyAdapter<Setup> adapter;
    private RecyclerView recyclerView;
    private List<Setup> mList;
    private View parent;
    private DialogDissmissListener onSeletedListener;

    public AutoCompleteDialog(View parent,List<Setup> list){
        this.mList = list;
        this.parent = parent;
        createView();
        setTouchable(true);
    }

    public void notifyDataSetChanged(){
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }

    public void setOnSeletedListener(DialogDissmissListener onSeletedListener){
        this.onSeletedListener = onSeletedListener;
    }

    private void onSelected(int position){
        Setup setup = mList.get(position);
        if(onSeletedListener!=null)
            onSeletedListener.onDissmiss(DialogResult.CONFIRM,setup.getValue());
    }

    private void dele(int position){
        Setup setup = mList.get(position);
        setup.dele();
        mList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void createView(){
        adapter = new MyAdapter<Setup>(mList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, Setup data) {
                myViewHolder.setText(R.id.textview,data.getValue());
                myViewHolder.getView(R.id.imageview).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dele(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSelected(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_autocomplete;
            }
        };

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pop_autocomplete_layout,null);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(parent.getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(parent.getContext(),DividerItemDecoration.VERTICAL));
        view.findViewById(R.id.textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setContentView(view);
    }

    public void show(){
        setWidth(800);
        setHeight(600);
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        int yOffset = -600 + parent.getHeight() / 3;
        showAtLocation(parent, Gravity.NO_GRAVITY,location[0],location[1]+yOffset);
    }
}
