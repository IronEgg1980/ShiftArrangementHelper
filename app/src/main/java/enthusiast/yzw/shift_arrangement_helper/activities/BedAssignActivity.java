package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.BedSelectDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.Bed;
import enthusiast.yzw.shift_arrangement_helper.moduls.BedAssign;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;

public class BedAssignActivity extends AppCompatActivity {
    private static final String TAG = "殷宗旺";
    private AppCompatTextView textviewToolbarTitle;
    private RecyclerView recyclerView;
    private MyAdapter<Person> adapter;
    private List<Person> dataList;
    private MyToast toast;

    private void initialView() {
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        textviewToolbarTitle.setText("管床分配");
        recyclerView = findViewById(R.id.recyclerview_bed_management_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        toast = new MyToast(this);
    }

    private void initial() {
        dataList = Person.getPersonManagedBedList();
        adapter = new MyAdapter<Person>(dataList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, Person data) {
                myViewHolder.setText(R.id.textview_item_bedassign_person, data.getName());
                StringBuilder s = new StringBuilder();
                if (data.managedBeds.size() > 0) {
                    for (Bed bed : data.managedBeds) {
                        s.append("、").append(bed.getName());
                    }
                }
                myViewHolder.setText(R.id.textview_item_bedassign_bed, s.length() == 0 ? "未管床" : s.substring(1));
                final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothExpand();
                    }
                });
                myViewHolder.getView(R.id.swipmenu_assign_bed).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothClose();
                        assignBed(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                View view = myViewHolder.getView(R.id.swipmenu_clear_bed);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothClose();
                        clearBedAssign(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                view.setVisibility(data.managedBeds.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_bedassign;
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private void assignBed(final int position) {
        final Person person = dataList.get(position);
        BedSelectDialog bedSelectDialog = new BedSelectDialog(person.managedBeds);
        bedSelectDialog.setDialogDissmissListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if (result == DialogResult.CONFIRM) {
                    if (person.saveBedAssign()) {
                        adapter.notifyItemChanged(position);
                    } else {
                        toast.centerShow("保存失败");
                    }
                }
            }
        });
        bedSelectDialog.show(getSupportFragmentManager(), "select_bed");
    }

    private void clearBedAssign(int position) {
        Person person = dataList.get(position);
        if (DbOperator.deleAll(BedAssign.class, "person_uuid = ?", person.getUUID())) {
            person.managedBeds.clear();
            adapter.notifyItemChanged(position);
        } else {
            toast.centerShow("操作失败");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bedassign);
        initialView();
        initial();
    }
}