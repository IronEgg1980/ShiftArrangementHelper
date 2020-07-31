package enthusiast.yzw.shift_arrangement_helper.activities;

import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.custom_views.RecyclerViewToGalleryHelper;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;
import enthusiast.yzw.shift_arrangement_helper.moduls.TodayShift;
import enthusiast.yzw.shift_arrangement_helper.moduls.WorkCategory;

public class MainActivity extends AppCompatActivity {
    private AppCompatTextView textviewGroupName;
    private RecyclerView recyclerviewTodayShift;
    private MaterialButton button1;
    private MaterialButton button2;
    private MaterialButton button3;
    private MaterialButton button4;
    private MaterialButton button5;
    private MaterialButton button6;
    private MaterialButton button7;

    private SnapHelper snapHelper;
    private MyAdapter<TodayShift> adapter;
    private List<TodayShift> todayShifts;

    private void getTestData(){
        for(int i = 1;i<5;i++){
            TodayShift todayShift = new TodayShift();
            WorkCategory workCategory = new WorkCategory();
            workCategory.setName("班次"+i);
            todayShift.workCategory = workCategory;
            for(int j = 1;j<4;j++){
                Person person = new Person();
                person.setName("人员"+j);
                todayShift.people.add(person);
            }
            todayShifts.add(todayShift);
        }
    }

    private void initialView(){
        textviewGroupName = findViewById(R.id.textview_group_name);
        recyclerviewTodayShift = findViewById(R.id.recyclerview_today_shift);
        button1 = findViewById(R.id.button_1);
        button2 = findViewById(R.id.button_2);
        button3 = findViewById(R.id.button_3);
        button4 = findViewById(R.id.button_4);
        button5 = findViewById(R.id.button_5);
        button6 = findViewById(R.id.button_6);
        button7 = findViewById(R.id.button_7);
    }

    private void initial(){
        todayShifts = new ArrayList<>();
        getTestData();
        snapHelper = new LinearSnapHelper();
        adapter = new MyAdapter<TodayShift>(todayShifts) {
            @Override
            public void bindData(MyViewHolder myViewHolder, TodayShift data) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Person p : data.people){
                    stringBuilder.append("、").append(p.getName());
                }
                myViewHolder.setText(R.id.textview_item_today_shift_person,stringBuilder.substring(1));
                myViewHolder.setText(R.id.textview_item_today_shift_work,data.workCategory.getName());
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_today_shift;
            }
        };
        recyclerviewTodayShift.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerviewTodayShift.setLayoutManager(manager);
        snapHelper.attachToRecyclerView(recyclerviewTodayShift);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialView();
        initial();
    }
}