package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.dialogs.ShowRemainDetailsDialog;
import enthusiast.yzw.shift_arrangement_helper.moduls.AbsentRemainDetails;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;

public class AbsentRemain extends AppCompatActivity {
    private AppCompatTextView textviewToolbarTitle;
    private RecyclerView recyclerView;
    private MyAdapter<Person> adapter;
    private List<Person> dataList;
    private int editPosition = -1;

    private void initialView(){
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        textviewToolbarTitle.setText("余假管理");
        recyclerView = findViewById(R.id.recyclerview_absent_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private void initial(){
        dataList = DbOperator.findAll(Person.class);
        adapter = new MyAdapter<Person>(dataList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, Person data) {
                myViewHolder.setText(R.id.textview_item_absent_person,data.getName());
                myViewHolder.setText(R.id.textview_item_absent_value,String.valueOf(data.getAbsentRemainValue()));
                myViewHolder.getView(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDetails(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.swipmenu_absent_adjust).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothClose();
                        adjustAbsentRemain(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_absent_remain;
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private void showDetails(int position){
        List<AbsentRemainDetails> list = AbsentRemainDetails.findAll(dataList.get(position).getId());
        if(!list.isEmpty()){
            ShowRemainDetailsDialog detailsDialog = new ShowRemainDetailsDialog(list);
            detailsDialog.show(getSupportFragmentManager(),"details");
        }else{
            new MyToast(this).centerShow("没有【"+dataList.get(position).getName()+"】的余假调整明细");
        }
    }

    private void adjustAbsentRemain(int position){
        editPosition = position;
        Intent intent = new Intent(AbsentRemain.this,EditAbsentRemain.class);
        intent.putExtra("person_id",dataList.get(position).getId());
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == 666){
            long id = dataList.remove(editPosition).getId();
            dataList.add(editPosition,DbOperator.findByID(Person.class,id));
            adapter.notifyItemChanged(editPosition);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absent_remain);
        initialView();
        initial();
    }
}