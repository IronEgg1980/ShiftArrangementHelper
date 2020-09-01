package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.app.Dialog;
import android.graphics.Color;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.moduls.AbsentRemainDetails;

public class ShowRemainDetailsDialog extends DialogFragment {
    private MyAdapter<AbsentRemainDetails> adapter;
    private DateTimeFormatter formatter2;
    public ShowRemainDetailsDialog(List<AbsentRemainDetails> list){
        formatter2 = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);
        adapter = new MyAdapter<AbsentRemainDetails>(list) {
            @Override
            public void bindData(MyViewHolder myViewHolder, AbsentRemainDetails data) {
                myViewHolder.setText(R.id.textview_item_absentremaindetails_reason,data.getReason());
                myViewHolder.setText(R.id.textview_item_absentremaindetails_date,data.getDate().format(formatter2));
                String text = data.getVarValue() < 0?""+data.getVarValue():"+"+data.getVarValue();
                TextView textView = myViewHolder.getView(R.id.textview_item_absentremaindetails_value);
                textView.setTextColor(data.getVarValue() < 0? Color.RED:getContext().getColor(R.color.colorText));
                textView.setText(text);
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_absentremaindetails;
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_single,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_dialog_selected_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.textview_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        TextView textView = view.findViewById(R.id.textview_dialog_title);
        textView.setText("余假调整明细");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int) (dm.widthPixels * 0.8);
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
