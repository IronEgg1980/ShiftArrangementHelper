package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.ConfirmDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.enums.WorkMode;
import enthusiast.yzw.shift_arrangement_helper.moduls.WorkCategory;

public class WorkCategoryManage extends AppCompatActivity {
    private final int REQUEST_CODE_ADD = 1, REQUEST_CODE_EDIT = 2;
    private RecyclerView recyclerview;

    private MyAdapter<WorkCategory> adapter;
    private List<WorkCategory> dataList;
    private int editPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_category_management);
        initialView();
        initial();
        readData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 666) {
            if (requestCode == REQUEST_CODE_ADD) {
                readData();
                if (dataList.size() > 0) {
                    recyclerview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerview.smoothScrollToPosition(dataList.size() - 1);
                        }
                    }, 50);
                }
            } else if (requestCode == REQUEST_CODE_EDIT) {
                String uuid = dataList.remove(editPosition).getUUID();
                dataList.add(editPosition, DbOperator.findByUUID(WorkCategory.class, uuid));
                adapter.notifyItemChanged(editPosition);
            }
        }
    }

    private void initialView() {
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.imageview_toolbar_menu).setVisibility(View.INVISIBLE);
        AppCompatTextView textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        textviewToolbarTitle.setText("班次设置");
        recyclerview = findViewById(R.id.recyclerview_work_category_management_list);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        MaterialButton buttonAdd = findViewById(R.id.button_work_category_management_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkCategoryManage.this, AddOrEditWorkCategory.class);
                startActivityForResult(intent, REQUEST_CODE_ADD);
            }
        });
    }

    private void initial() {
        dataList = new ArrayList<>();
        adapter = new MyAdapter<WorkCategory>(dataList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final WorkCategory data) {
                myViewHolder.setText(R.id.textview_work_category_name, data.getName());
                myViewHolder.setText(R.id.textview_item_work_category_status, data.getMode().getText());
                TextView textView = myViewHolder.getView(R.id.textview_item_work_overtimepay);
                String extraText = data.isAutoMinus() ? "自动扣假 " + data.getMinusValue() + " 天" : data.getMode() == WorkMode.OVER_TIME ? "加班费（每次）：" + data.getOverTimePay() : "";
                if(extraText.isEmpty()){
                    textView.setVisibility(View.GONE);
                }else {
                    myViewHolder.setText(R.id.textview_item_work_overtimepay, extraText);
                }
                final SwipeMenuLayout menuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuLayout.smoothExpand();
                    }
                });
                myViewHolder.getView(R.id.swipemenu_edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuLayout.smoothClose();
                        edit(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                myViewHolder.getView(R.id.swipemenu_dele).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuLayout.smoothClose();
                        dele(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_work_category_management;
            }
        };
        recyclerview.setAdapter(adapter);
    }

    private void edit(int position) {
        editPosition = position;
        WorkCategory workCategory = dataList.get(position);
        Intent intent = new Intent(WorkCategoryManage.this, AddOrEditWorkCategory.class);
        intent.putExtra("uuid", workCategory.getUUID());
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    private void dele(final int position) {
        final WorkCategory workCategory = dataList.get(position);
        ConfirmDialog.newInstance("删除？", "删除班次【" + workCategory.getName() + "】吗？")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            if (workCategory.dele()) {
                                dataList.remove(position);
                                adapter.notifyItemRemoved(position);
                            } else {
                                Toast.makeText(WorkCategoryManage.this, "删除失败！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show(getSupportFragmentManager(), "confirm_dele");
    }

    private void readData() {
        dataList.clear();
        dataList.addAll(DbOperator.findAll(WorkCategory.class));
        adapter.notifyDataSetChanged();
    }

}