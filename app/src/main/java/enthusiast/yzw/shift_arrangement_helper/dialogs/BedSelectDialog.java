package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.Bed;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;

public class BedSelectDialog extends DialogFragment {
    private MyAdapter<Bed> adapter;
    private List<Bed> beds;
    private List<Bed> selectedList;
    private DialogResult result = DialogResult.CANCEL;
    private DialogDissmissListener dialogDissmissListener;

    public BedSelectDialog setDialogDissmissListener(DialogDissmissListener dialogDissmissListener) {
        this.dialogDissmissListener = dialogDissmissListener;
        return this;
    }

    public BedSelectDialog(@NonNull List<Bed> selected){
        this.selectedList = selected;
        beds = DbOperator.findAll(Bed.class);
        for(Bed bed:beds){
            bed.isSeleted = selectedList.contains(bed);
        }
        adapter = new MyAdapter<Bed>(beds) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final Bed data) {
                myViewHolder.setText(R.id.textview_item_bed_select,data.getName());
                myViewHolder.getView(R.id.flag_item_bed_select).setVisibility(data.isSeleted? View.VISIBLE:View.INVISIBLE);
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        data.isSeleted = !data.isSeleted;
                        notifyItemChanged(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_bed_select;
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                this.parentWidth = parent.getMeasuredWidth();
                View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (parentWidth - 60) / 4;
                view.setLayoutParams(layoutParams);
                return new MyViewHolder(view);
            }
        };
    }

    private void save(){
        selectedList.clear();
        for(Bed bed:beds){
            if(bed.isSeleted)
               selectedList.add(bed);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dialogDissmissListener != null) {
            dialogDissmissListener.onDissmiss(result);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_multi,container,false);
        TextView textView = view.findViewById(R.id.textview_dialog_title);
        textView.setText("选择床位");
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerview_dialog_selected_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),4));
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.textview_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result = DialogResult.CANCEL;
                dismiss();
            }
        });
        view.findViewById(R.id.textview_dialog_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
                result = DialogResult.CONFIRM;
                dismiss();
            }
        });
        view.findViewById(R.id.textview_select_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Bed bed:beds){
                    bed.isSeleted = true;
                }
                adapter.notifyDataSetChanged();
            }
        });
        view.findViewById(R.id.textview_select_reverse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Bed bed:beds){
                    bed.isSeleted = !bed.isSeleted;
                }
                adapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int) (dm.widthPixels * 0.9);
        int height =(int) (dm.heightPixels * 0.8);
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            if(window!=null){
                window.setLayout(width,height);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }
}
