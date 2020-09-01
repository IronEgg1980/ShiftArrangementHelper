package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;

public class PersonSelectDialog extends DialogFragment {
    private MyAdapter<Person> adapter;
    private List<Person> personList;
    private List<Person> selectedList;
    private DialogResult result = DialogResult.CANCEL;
    private DialogDissmissListener dialogDissmissListener;
    private TextView infoTextView;

    public PersonSelectDialog setDialogDissmissListener(DialogDissmissListener dialogDissmissListener) {
        this.dialogDissmissListener = dialogDissmissListener;
        return this;
    }

    public PersonSelectDialog(@NonNull List<Person> selected){
        this.selectedList = selected;
        personList = DbOperator.findAll(Person.class);
        for(Person person:personList){
            person.isSeleted = selectedList.contains(person);
        }
        adapter = new MyAdapter<Person>(personList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final Person data) {
                myViewHolder.setText(R.id.textview_item_person_select,data.getName());
                myViewHolder.getView(R.id.flag_item_person_select).setVisibility(data.isSeleted? View.VISIBLE:View.INVISIBLE);
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        data.isSeleted = !data.isSeleted;
                        notifyItemChanged(myViewHolder.getAbsoluteAdapterPosition());
                        showInfo();
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_person_select;
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
        for(Person person:personList){
            if(person.isSeleted)
                selectedList.add(person);
        }
    }

    private void showInfo(){
        StringBuilder stringBuilder = new StringBuilder("管床人员：");
        for(Person p : personList){
            if(p.isSeleted)
                stringBuilder.append(p.getName()).append("、");
        }
        String s =stringBuilder.length() > 5? stringBuilder.substring(0,stringBuilder.length() - 1):"无管床人员";
        infoTextView.setText(s);
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
        View view = inflater.inflate(R.layout.dialog_select_person,container,false);
        TextView textView = view.findViewById(R.id.textview_dialog_title);
        textView.setText("选择人员");
        infoTextView = view.findViewById(R.id.textview_dialog_info);
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
                for(Person person:personList){
                    person.isSeleted = true;
                }
                adapter.notifyDataSetChanged();
                showInfo();
            }
        });
        view.findViewById(R.id.textview_select_reverse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Person person:personList){
                    person.isSeleted = !person.isSeleted;
                }
                adapter.notifyDataSetChanged();
                showInfo();
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
        showInfo();
    }
}
