package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.ConfirmDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.enums.AbsentVarMode;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.AbsentRemainDetails;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;

public class EditAbsentRemain extends AppCompatActivity {
    private AppCompatTextView textviewAdjustAbsentremainPerson;
    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private EditText editTextValue,editTextReason;
    private RecyclerView recyclerView;
    private Person person = null;
    private AbsentVarMode mode = null;
    private List<AbsentRemainDetails> dataList;
    private MyAdapter<AbsentRemainDetails> adapter;
    private MyToast toast;
    private DateTimeFormatter formatter,formatter2;
    private int resultCode = -1;

    private void initialView() {
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        AppCompatTextView textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        textviewToolbarTitle.setText("余假调整");
        textviewAdjustAbsentremainPerson = findViewById(R.id.textview_adjust_absentremain_person);
        editTextValue = findViewById(R.id.edittext_adjust_absentremain_value);
        editTextReason = findViewById(R.id.edittext_adjust_absentremain_reason);
        radioGroup = findViewById(R.id.radiogoup);
        radioButton1 = findViewById(R.id.radio1);
        MaterialButton buttonAdjustAbsentremainConfirm = findViewById(R.id.button_adjust_absentremain_confirm);
        buttonAdjustAbsentremainConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        recyclerView = findViewById(R.id.recyclerview_adjust_absentremain);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        toast = new MyToast(this);
    }

    private void initial() {
        if (dataList == null)
            dataList = new ArrayList<>();
        formatter = DateTimeFormatter.ofPattern("记录时间：yyyy年M月d日 HH:mm:ss",Locale.CHINA);
        formatter2 = DateTimeFormatter.ofPattern("yyyy年\nM月d日", Locale.CHINA);
        adapter = new MyAdapter<AbsentRemainDetails>(dataList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, AbsentRemainDetails data) {
                myViewHolder.setText(R.id.textview_item_absentremaindetails_date,data.getDate().format(formatter2));
                myViewHolder.setText(R.id.textview_item_absentremaindetails_time, data.getRecordTime().format(formatter));
                myViewHolder.setText(R.id.textview_item_absentremaindetails_reason, data.getReason());
                String text = data.getVarValue() < 0 ? "" + data.getVarValue() : "+" + data.getVarValue();
                TextView textView = myViewHolder.getView(R.id.textview_item_absentremaindetails_value);
                textView.setTextColor(data.getVarValue() < 0 ? Color.RED : getColor(R.color.colorText));
                textView.setText(text);
                final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.swipemenu_dele).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothClose();
                        dele(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_absentremaindetails_withmenu;
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private double getVarValue() {
        double value = Double.parseDouble(editTextValue.getText().toString());
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio1:
                mode = AbsentVarMode.ADJUST;
                break;
            case R.id.radio2:
                mode = AbsentVarMode.ADJUST;
                value *= -1;
                break;
            case R.id.radio3:
                mode = AbsentVarMode.INPUT;
                value -= person.getAbsentRemainValue();
                break;
        }
        return value;
    }

    private String getReason() {
        String reason = "";
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio1:
                reason = "补假";
                break;
            case R.id.radio2:
                reason = "扣除";
                break;
            case R.id.radio3:
                reason = "校正";
                break;
        }
        if(!TextUtils.isEmpty(editTextReason.getText()))
            reason = editTextReason.getText().toString();
        return reason;
    }

    private void showInfo() {
        String personText = person == null ? "未选择人员" : person.getName() + "    余假： " + person.getAbsentRemainValue() + " 天";
        textviewAdjustAbsentremainPerson.setText(personText);
        editTextValue.setText("");
        editTextReason.setText("");
        radioButton1.setChecked(true);
        adapter.notifyDataSetChanged();
        if (!dataList.isEmpty()) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    private void dele(final int position) {
        ConfirmDialog.newInstance("撤销？", "是否撤销此次余额调整记录？")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            AbsentRemainDetails details = dataList.get(position);
                            if (details.dele()) {
                                AbsentRemainDetails details1 = dataList.remove(position);
                                person = details1.getPerson();
                                adapter.notifyItemRemoved(position);
                                String personText = person.getName() + "    余假： " + person.getAbsentRemainValue() + " 天";
                                textviewAdjustAbsentremainPerson.setText(personText);
                                resultCode = 666;
                            } else {
                                toast.centerShow("撤销操作失败");
                            }
                        }
                    }
                })
                .show(getSupportFragmentManager(), "dele");
    }

    private void save() {
        if (TextUtils.isEmpty(editTextValue.getText())) {
            editTextValue.setError("天数不能为空");
            return;
        }
        double varValue = getVarValue();
        String reason = getReason();
        AbsentRemainDetails details = new AbsentRemainDetails();
        details.setRecordTime(LocalDateTime.now());
        details.setPerson(person);
        details.setMode(mode);
        details.setVarValue(varValue);
        details.setReason(reason);
        details.setDate(LocalDate.now());
        if (details.save()) {
            dataList.add(0, details);
            mode = null;
            showInfo();
            resultCode = 666;
        } else {
            toast.centerShow("数据保存失败");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_absent_remain);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            long id = bundle.getLong("person_id");
            person = DbOperator.findByID(Person.class, id);
            dataList = AbsentRemainDetails.findAll(id);
            Collections.reverse(dataList);
        }
        initialView();
        initial();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInfo();
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        super.onBackPressed();
    }
}