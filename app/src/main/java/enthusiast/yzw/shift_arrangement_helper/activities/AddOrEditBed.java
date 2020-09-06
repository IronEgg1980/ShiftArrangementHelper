package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.Group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.moduls.Bed;

public class AddOrEditBed extends AppCompatActivity {
    private int resultCode = -1;
    private AppCompatTextView textviewToolbarTitle;
    private EditText edittextBedNamePre;
    private AppCompatImageView imageviewToolbarMenu;
    private EditText edittextActivityAddEditBedName;
    private MaterialButton buttonActivityAddEditBed1;
    private EditText edittextActivityAddEditBedNameStart;
    private EditText edittextActivityAddEditBedNameEnd;
    private AppCompatCheckBox checkboxActivityAddEditBed2;
    private MaterialButton buttonActivityAddEditBed2;
    private Group batchAdditionGroup;

    private Bed bed = null;
    private String bedName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_bed);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String uuid = bundle.getString("bed_uuid");
            bed = DbOperator.findByUUID(Bed.class, uuid);
        }
        intialView();
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        super.onBackPressed();
    }

    private void intialView() {
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        edittextBedNamePre = findViewById(R.id.edittext_activity_add_edit_bed_pre_name);
        edittextBedNamePre.setEnabled(false);
        String title = bed == null ? "新增床位" : "编辑床位";
        textviewToolbarTitle.setText(title);
        findViewById(R.id.imageview_toolbar_menu).setVisibility(View.INVISIBLE);
        edittextActivityAddEditBedName = findViewById(R.id.edittext_activity_add_edit_bed_name);
        bedName = bed == null ? "" : bed.getName();
        edittextActivityAddEditBedName.setText(bedName);
        buttonActivityAddEditBed1 = findViewById(R.id.button_activity_add_edit_bed_1);
        buttonActivityAddEditBed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(saveOne())
                    onBackPressed();
            }
        });
        findViewById(R.id.button_activity_add_edit_bed_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(saveOne())
                    reset();
            }
        });
        edittextActivityAddEditBedNameStart = findViewById(R.id.edittext_activity_add_edit_bed_name_start);
        edittextActivityAddEditBedNameEnd = findViewById(R.id.edittext_activity_add_edit_bed_name_end);
        checkboxActivityAddEditBed2 = findViewById(R.id.checkbox_activity_add_edit_bed_2);
        checkboxActivityAddEditBed2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edittextBedNamePre.setEnabled(b);
                if (b) {
                    edittextBedNamePre.requestFocus();
                    edittextBedNamePre.selectAll();
                }
            }
        });
        buttonActivityAddEditBed2 = findViewById(R.id.button_activity_add_edit_bed_2);
        buttonActivityAddEditBed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAll();
            }
        });
        batchAdditionGroup = findViewById(R.id.batch_addition_group);
        batchAdditionGroup.setVisibility(bed == null ? View.VISIBLE : View.INVISIBLE);
    }

    private void reset(){
        edittextActivityAddEditBedName.setText("");
        bed = null;
        bedName = "";
        textviewToolbarTitle.setText("新增床位");
    }

    private boolean saveOne() {
        if (TextUtils.isEmpty(edittextActivityAddEditBedName.getText())) {
            edittextActivityAddEditBedName.setError("请输入床号！");
            return false;
        }
        String text = edittextActivityAddEditBedName.getText().toString();
        if (!text.equals(bedName)) {
            //去重
            if (DbOperator.isExist(Bed.class, "name", text)) {
                edittextActivityAddEditBedName.setError("床号重复！");
                return false;
            }
        }
        if (bed == null) {
            bed = new Bed();
        }
        bed.setName(text);
        if (bed.saveOrUpdate()) {
            new MyToast(this).centerShow("已保存");
            resultCode = 666;
            return true;
        } else {
            new MyToast(this).centerShow("保存失败！");
            return false;
        }
    }

    private void saveAll() {
        if (TextUtils.isEmpty(edittextActivityAddEditBedNameStart.getText())) {
            edittextActivityAddEditBedNameStart.setError("床号不能为空！");
            return;
        }
        if (TextUtils.isEmpty(edittextActivityAddEditBedNameEnd.getText())) {
            edittextActivityAddEditBedNameEnd.setError("床号不能为空！");
            return;
        }
        if(checkboxActivityAddEditBed2.isChecked() && TextUtils.isEmpty(edittextBedNamePre.getText())){
            edittextBedNamePre.setError("加床床号前缀不能为空！");
            return;
        }
        int start, end;
        start = Integer.parseInt(edittextActivityAddEditBedNameStart.getText().toString());
        end = Integer.parseInt(edittextActivityAddEditBedNameEnd.getText().toString());
        if (end < start) {
            edittextActivityAddEditBedNameEnd.setError("小于开始床号，请重新输入...");
            return;
        }
        String pre = edittextBedNamePre.getText().toString();
        List<Bed> list = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String text = checkboxActivityAddEditBed2.isChecked() ? pre + i : "" + i;
            if (DbOperator.isExist(Bed.class, "name", text)) {
                continue;
            }
            Bed bed = new Bed();
            bed.setName(text);
            list.add(bed);
        }
        if(DbOperator.saveAll(list) > 0){
            new MyToast(this).centerShow("已保存（自动跳过重复床号）");
            resultCode = 666;
            onBackPressed();
        }else{
            new MyToast(this).centerShow("保存失败！");
        }
    }
}