package enthusiast.yzw.shift_arrangement_helper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.AutoCompleteDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.enums.Gender;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;
import enthusiast.yzw.shift_arrangement_helper.moduls.Setup;

public class AddOrEditPerson extends AppCompatActivity {
    private TextView titleTextView;
    private EditText etName;
    private AppCompatRadioButton rbMan, rbWoman;
    private EditText etAge;
    private EditText etProfessor;
    private EditText etPost;
    private EditText etRatio;
    private EditText etPhone;
    private EditText etSchool;
    private EditText etNote;
    private MyToast toast;
    private int resultCode = -1;

    private Person person;
    private String mName = "";

    private List<Setup> postList, proList;
    private AutoCompleteDialog postDialog, proDialog;

    private void initialView() {
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        titleTextView = findViewById(R.id.textview_toolbar_title);
        etName = findViewById(R.id.edittext_activity_add_edit_person_name);
        rbMan = findViewById(R.id.radiobutton_man);
        rbWoman = findViewById(R.id.radiobutton_woman);
        etAge = findViewById(R.id.edittext_activity_add_edit_person_age);
        etProfessor = findViewById(R.id.edittext_activity_add_edit_person_professor);
        etPost = findViewById(R.id.edittext_activity_add_edit_person_post);
        etRatio = findViewById(R.id.edittext_activity_add_edit_person_ratio);
        etPhone = findViewById(R.id.edittext_activity_add_edit_person_phone);
        etSchool = findViewById(R.id.edittext_activity_add_edit_person_school);
        etNote = findViewById(R.id.edittext_activity_add_edit_person_note);

        findViewById(R.id.button_activity_add_edit_person_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndExit();
            }
        });
        findViewById(R.id.button_activity_add_edit_person_save2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndContinue();
            }
        });
        toast = new MyToast(this);
        postDialog = new AutoCompleteDialog(etPost, postList);
        postDialog.setOnSeletedListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                String text = (String) values[0];
                etPost.setText(text);
            }
        });
        proDialog = new AutoCompleteDialog(etProfessor, proList);
        proDialog.setOnSeletedListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                String text = (String) values[0];
                etProfessor.setText(text);
            }
        });
        etPost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    postDialog.show();
                else
                    postDialog.dismiss();
            }
        });
        etPost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                postList.clear();
                if(TextUtils.isEmpty(editable)){
                    postList.addAll(Setup.find("post"));
                }else{
                    postList.addAll(Setup.lookup("post",editable.toString()));
                }
                postDialog.notifyDataSetChanged();
            }
        });
        etProfessor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    proDialog.show();
                else
                    proDialog.dismiss();
            }
        });
        etProfessor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                proList.clear();
                if(TextUtils.isEmpty(editable)){
                    proList.addAll(Setup.find("professor"));
                }else{
                    proList.addAll(Setup.lookup("professor",editable.toString()));
                }
                proDialog.notifyDataSetChanged();
            }
        });
    }

    private void initial() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            person = DbOperator.findByUUID(Person.class, bundle.getString("uuid"));
        }
        postList = Setup.find("post");
        proList = Setup.find("professor");
    }

    private String getEditTextString(EditText editText) {
        return TextUtils.isEmpty(editText.getText()) ? "" : editText.getText().toString();
    }

    private boolean save() {
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("姓名不能为空！");
            return false;
        }
        String name = getEditTextString(etName);
        int age = TextUtils.isEmpty(etAge.getText()) ? 1 : Integer.parseInt(etAge.getText().toString());
        double ratio = TextUtils.isEmpty(etRatio.getText()) ? 0 : new BigDecimal(etRatio.getText().toString()).doubleValue();
        Gender gender = rbMan.isChecked() ? Gender.MAN : Gender.WOMAN;
        String professor = getEditTextString(etProfessor);
        String post = getEditTextString(etPost);
        String phone = getEditTextString(etPhone);
        String school = getEditTextString(etSchool);
        String note = getEditTextString(etNote);

        if (!name.equals(mName) && DbOperator.isExist(Person.class, "name", name)) {
            etName.setError("姓名重复！");
            return false;
        }

        if (!TextUtils.isEmpty(post)) {
            Setup postSetup = new Setup();
            postSetup.setKey("post");
            postSetup.setValue(post);
            if (!postSetup.isExist()) {
                postSetup.save();
                postList.add(postSetup);
                postDialog.notifyDataSetChanged();
            }
        }
        if (!TextUtils.isEmpty(professor)) {
            Setup proSetup = new Setup();
            proSetup.setKey("professor");
            proSetup.setValue(professor);
            if (!proSetup.isExist()) {
                proSetup.save();
                proList.add(proSetup);
                proDialog.notifyDataSetChanged();
            }
        }

        if (person == null)
            person = new Person();
        person.setName(name);
        person.setAge(age);
        person.setRatio(ratio);
        person.setGender(gender);
        person.setProfessor(professor);
        person.setPost(post);
        person.setPhone(phone);
        person.setSchool(school);
        person.setNote(note);
        return person.saveOrUpdate();
    }

    private void saveAndExit() {
        if (save()) {
            toast.centerShow("【" + person.getName() + "】已保存");
            resultCode = 666;
            onBackPressed();
        }
    }

    private void saveAndContinue() {
        if (save()) {
            toast.centerShow("【" + person.getName() + "】已保存，请继续添加");
            person = null;
            reset();
            resultCode = 666;
        }
    }

    private void reset() {
        mName = person == null ? "" : person.getName();
        titleTextView.setText(person == null ? "新增人员" : "编辑人员信息");
        etName.setText(mName);
        rbMan.setChecked(person == null || person.getGender() == Gender.MAN);
        rbWoman.setChecked(person != null && person.getGender() == Gender.WOMAN);
        etAge.setText(person == null ? "" : String.valueOf(person.getAge()));
        etProfessor.setText(person == null ? "" : person.getProfessor());
        etPost.setText(person == null ? "" : person.getPost());
        etRatio.setText(String.valueOf(person == null ? 1.0 : person.getRatio()));
        etPhone.setText(person == null ? "" : person.getPhone());
        etSchool.setText(person == null ? "" : person.getSchool());
        etNote.setText(person == null ? "" : person.getNote());
        etName.requestFocus();
        etName.selectAll();
        etName.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_person);
        initial();
        initialView();
        reset();
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        super.onBackPressed();
    }
}