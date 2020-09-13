package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.rengwuxian.materialedittext.MaterialEditText;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.enums.WorkMode;
import enthusiast.yzw.shift_arrangement_helper.moduls.WorkCategory;

public class AddOrEditWorkCategory extends AppCompatActivity {
    private int resultCode = -1;
    private AppCompatTextView textviewToolbarTitle;
    private EditText edittextActivityAddEditWorkName;
    private RadioGroup radiogoup;
    private AppCompatRadioButton radiobuttonActivityAddEditWorkNormal;
    private AppCompatRadioButton radiobuttonActivityAddEditWorkOvertime;
    private AppCompatRadioButton radiobuttonActivityAddEditWorkAbsent;
    private EditText edittextActivityAddEditWorkOvertimepay, editTextMinusValue;
    private CheckBox checkBoxAutoMinus;

    private WorkCategory workCategory = null;
    private String mName;

    private void initialView() {
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.imageview_toolbar_menu).setVisibility(View.INVISIBLE);
        textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        edittextActivityAddEditWorkName = findViewById(R.id.edittext_activity_add_edit_work_name);
        edittextActivityAddEditWorkOvertimepay = findViewById(R.id.edittext_activity_add_edit_work_overtimepay);
        radiogoup = findViewById(R.id.radiogoup);
        radiobuttonActivityAddEditWorkNormal = findViewById(R.id.radiobutton_activity_add_edit_work_normal);
        radiobuttonActivityAddEditWorkOvertime = findViewById(R.id.radiobutton_activity_add_edit_work_overtime);
        radiobuttonActivityAddEditWorkOvertime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edittextActivityAddEditWorkOvertimepay.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
                if (b) {
                    edittextActivityAddEditWorkOvertimepay.requestFocus();
                    edittextActivityAddEditWorkOvertimepay.selectAll();
                }
            }
        });
        radiobuttonActivityAddEditWorkAbsent = findViewById(R.id.radiobutton_activity_add_edit_work_absent);
        radiobuttonActivityAddEditWorkAbsent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkBoxAutoMinus.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
                editTextMinusValue.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
                editTextMinusValue.setEnabled(checkBoxAutoMinus.isChecked());
                if (b) {
                    editTextMinusValue.requestFocus();
                    editTextMinusValue.selectAll();
                }
            }
        });
        editTextMinusValue = findViewById(R.id.edittext_activity_add_edit_work_minusValue);
        checkBoxAutoMinus = findViewById(R.id.checkbox_activity_add_edit_work_autominus);
        checkBoxAutoMinus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editTextMinusValue.setEnabled(b);
                editTextMinusValue.requestFocus();
                editTextMinusValue.selectAll();
            }
        });
        findViewById(R.id.button_activity_add_edit_work_save1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (save())
                    onBackPressed();
            }
        });
        findViewById(R.id.button_activity_add_edit_work_save2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (save()) {
                    workCategory = null;
                    reset();
                }
            }
        });
    }

    private void initial() {
        mName = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            long id = bundle.getLong("id");
            workCategory = DbOperator.findByID(WorkCategory.class, id);
        }
    }

    private void reset() {
        mName = workCategory == null ? "" : workCategory.getName();
        textviewToolbarTitle.setText(workCategory == null ? "新增班次" : "编辑班次");
        edittextActivityAddEditWorkName.setText(workCategory == null ? "" : workCategory.getName());
        radiobuttonActivityAddEditWorkNormal.setChecked(workCategory == null || workCategory.getMode() == WorkMode.NORMAL);
        radiobuttonActivityAddEditWorkOvertime.setChecked(workCategory != null && workCategory.getMode() == WorkMode.OVER_TIME);
        radiobuttonActivityAddEditWorkAbsent.setChecked(workCategory != null && workCategory.getMode() == WorkMode.ABSENT);
        edittextActivityAddEditWorkOvertimepay.setVisibility(workCategory != null && workCategory.getMode() == WorkMode.OVER_TIME ? View.VISIBLE : View.INVISIBLE);
        edittextActivityAddEditWorkOvertimepay.setText(workCategory == null ? "" : workCategory.getOverTimePay() + "");
        checkBoxAutoMinus.setVisibility(workCategory != null && workCategory.getMode() == WorkMode.ABSENT ? View.VISIBLE : View.INVISIBLE);
        checkBoxAutoMinus.setChecked(workCategory != null && workCategory.isAutoMinus());
        editTextMinusValue.setVisibility(workCategory != null && workCategory.getMode() == WorkMode.ABSENT ? View.VISIBLE : View.INVISIBLE);
        editTextMinusValue.setEnabled(checkBoxAutoMinus.isChecked());
        editTextMinusValue.setText(workCategory != null && workCategory.isAutoMinus() ? workCategory.getMinusValue() + "" : "1.0");
        edittextActivityAddEditWorkName.postDelayed(new Runnable() {
            @Override
            public void run() {
                edittextActivityAddEditWorkName.requestFocus();
                edittextActivityAddEditWorkName.selectAll();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
            }
        },100);
    }

    private boolean save() {
        if (TextUtils.isEmpty(edittextActivityAddEditWorkName.getText())) {
            edittextActivityAddEditWorkName.setError("名称不能为空！");
            return false;
        }
        String text = edittextActivityAddEditWorkName.getText().toString();
        WorkMode workMode = WorkMode.NORMAL;
        switch (radiogoup.getCheckedRadioButtonId()) {
            case R.id.radiobutton_activity_add_edit_work_overtime:
                workMode = WorkMode.OVER_TIME;
                break;
            case R.id.radiobutton_activity_add_edit_work_absent:
                workMode = WorkMode.ABSENT;
                break;
        }
        float overTimePay = 0;
        if (workMode == WorkMode.OVER_TIME && !TextUtils.isEmpty(edittextActivityAddEditWorkOvertimepay.getText())) {
            overTimePay = Float.parseFloat(edittextActivityAddEditWorkOvertimepay.getText().toString());
        }
        if (!mName.equals(text)) {
            if (DbOperator.isExist(WorkCategory.class, "name", text)) {
                edittextActivityAddEditWorkName.setError("名称重复，请改名！");
                edittextActivityAddEditWorkName.requestFocus();
                edittextActivityAddEditWorkName.selectAll();
                return false;
            }
        }
        if (workCategory == null)
            workCategory = new WorkCategory();
        workCategory.setName(text);
        workCategory.setMode(workMode);
        workCategory.setOverTimePay(overTimePay);
        workCategory.setAutoMinus(checkBoxAutoMinus.isChecked());
        double minusValue = 0;
        if (checkBoxAutoMinus.isChecked()) {
            minusValue = TextUtils.isEmpty(editTextMinusValue.getText()) ? 1.0 : Double.parseDouble(editTextMinusValue.getText().toString());
        }
        workCategory.setMinusValue(minusValue);
        if (workCategory.saveOrUpdate()) {
            new MyToast(this).centerShow("已保存");
            resultCode = 666;
            return true;
        } else {
            new MyToast(this).centerShow("保存失败！");
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_work_category);
        initial();
        initialView();
        reset();
    }
}