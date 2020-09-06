package enthusiast.yzw.shift_arrangement_helper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.InputTextDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;
import enthusiast.yzw.shift_arrangement_helper.moduls.TodayShift;
import enthusiast.yzw.shift_arrangement_helper.moduls.WorkCategory;
import enthusiast.yzw.shift_arrangement_helper.tools.DateTool;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "殷宗旺";
    private AppCompatTextView textviewGroupName,textviewToday;
    private RecyclerView recyclerviewTodayShift;
    private MaterialButton button1;
    private MaterialButton button2;
    private MaterialButton button3;
    private MaterialButton button4;
    private MaterialButton button5;
    private MaterialButton button6;
    private MaterialButton button7;

    private MyAdapter<TodayShift> adapter;
    private List<TodayShift> todayShifts;
    private String organizeName = "";
    private DateTimeFormatter formatter;

    private void startActivity(Class<?> clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
    }

    private void startEditShiftActivity(){
        LocalDate date = DateTool.getUnassignedShiftDate(LocalDate.now());
//        LocalDate date = LocalDate.now();
        Intent intent = new Intent(MainActivity.this,AddOrEditShift.class);
        intent.putExtra("localdate", date.toEpochDay());
        intent.putExtra("mode",0);
        startActivity(intent);
    }

    private void setOrganizeName(){
        InputTextDialog.newInstance("输入名称","").setListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if(result == DialogResult.CONFIRM){
                    String s = (String) values[0];
                    if(DbOperator.setOrganizeName(s)){
                        organizeName = s;
                        textviewGroupName.setText(s);
                    }else{
                        new MyToast(MainActivity.this).centerShow("操作失败");
                    }
                }
            }
        }).show(getSupportFragmentManager(),"input");
    }

    private void initialView() {
        textviewToday = findViewById(R.id.textview_main_today);
        textviewGroupName = findViewById(R.id.textview_group_name);
        textviewGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOrganizeName();
            }
        });
        recyclerviewTodayShift = findViewById(R.id.recyclerview_today_shift);
        button1 = findViewById(R.id.button_1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ShowShift.class);
            }
        });
        button2 = findViewById(R.id.button_2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEditShiftActivity();
            }
        });
        button3 = findViewById(R.id.button_3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(PersonManage.class);
            }
        });
        button4 = findViewById(R.id.button_4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(WorkCategoryManage.class);
            }
        });
        button5 = findViewById(R.id.button_5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(BedManage.class);
            }
        });
        button6 = findViewById(R.id.button_6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(BedAssignActivity.class);
            }
        });
        button7 = findViewById(R.id.button_7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(AbsentRemain.class);
            }
        });
    }

    private void initial() {
        formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE");
        textviewToday.setText(LocalDateTime.now().format(formatter));
        organizeName = DbOperator.getOrganizeName();
        if(TextUtils.isEmpty(organizeName))
            organizeName = "点击这里设置科室或团体的名称";
        textviewGroupName.setText(organizeName);
        todayShifts = new ArrayList<>();
        adapter = new MyAdapter<TodayShift>(todayShifts) {
            @Override
            public void bindData(MyViewHolder myViewHolder, TodayShift data) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Person p : data.people) {
                    stringBuilder.append("、").append(p.getName());
                }
                myViewHolder.setText(R.id.textview_item_today_shift_person, stringBuilder.substring(1));
                myViewHolder.setText(R.id.textview_item_today_shift_work, data.workCategory.getName());
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_today_shift;
            }
        };
        recyclerviewTodayShift.setAdapter(adapter);
        recyclerviewTodayShift.setLayoutManager(new LinearLayoutManager(this));
        recyclerviewTodayShift.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void readData(){
        todayShifts.clear();
        todayShifts.addAll(TodayShift.find(LocalDate.now()));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialView();
        initial();
    }

    @Override
    protected void onStart() {
        super.onStart();
        readData();
    }
}