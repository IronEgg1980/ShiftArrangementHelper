package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.ConfirmDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyPopMenu;
import enthusiast.yzw.shift_arrangement_helper.dialogs.PersonSelectDialog;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.Bed;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;

public class BedManage extends AppCompatActivity {
    private final int REQUEST_CODE_ADD = 1, REQUEST_CODE_EDIT = 2;
    private int editPosition = 0;
    private RecyclerView recyclerView;
    private ImageView toolbarMenu;
    private MyPopMenu popMenu;
    private MyAdapter<Bed> adapter;
    private List<Bed> bedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bed_management);
        initialView();
        initial();
        readDb();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 666) {
            if (requestCode == REQUEST_CODE_ADD) {
                readDb();
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getItemCount() > 0) {
                            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    }
                }, 50);
            } else if (requestCode == REQUEST_CODE_EDIT) {
                long id = bedList.remove(editPosition).getId();
                bedList.add(editPosition, DbOperator.findByID(Bed.class, id));
                adapter.notifyItemChanged(editPosition);
            }
        }
    }

    private void initialView() {
        recyclerView = findViewById(R.id.recyclerview_bed_management_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        findViewById(R.id.button_bed_management_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(BedManage.this, AddOrEditBed.class), REQUEST_CODE_ADD);
            }
        });
        toolbarMenu = findViewById(R.id.imageview_toolbar_menu);
        toolbarMenu.setVisibility(View.VISIBLE);
        toolbarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopMenu();
            }
        });
        TextView title = findViewById(R.id.textview_toolbar_title);
        title.setText("床位管理");
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initial() {
        bedList = new ArrayList<>();
        adapter = new MyAdapter<Bed>(bedList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final Bed data) {
                String s = "无管理人员";
                if (!data.bedManagerList.isEmpty()) {
                    StringBuilder stringBuilder = new StringBuilder("管床：");
                    for (Person p : data.bedManagerList) {
                        stringBuilder.append(p.getName()).append("、");
                    }
                    s = stringBuilder.substring(0, stringBuilder.length() - 1);
                }
                myViewHolder.setText(R.id.item_bed_management_bedassign, s);
                myViewHolder.setText(R.id.item_bed_management_name, data.getName());
                final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.menu_bed_manage_showassign).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothClose();
                        showAssignPerson(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                myViewHolder.getView(R.id.menu_bed_manage_edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothClose();
                        edit(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                myViewHolder.getView(R.id.menu_bed_manage_dele).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothClose();
                        dele(data, myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
                myViewHolder.getView(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        swipeMenuLayout.smoothExpand();
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_bed_management;
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                this.parentWidth = parent.getMeasuredWidth();
                View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (parentWidth - 60) / 3;
                view.setLayoutParams(layoutParams);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private void showPopMenu() {
        String[] items = {"全部删除"};
        if (popMenu == null) {
            popMenu = new MyPopMenu(this, items);
            popMenu.setListener(new DialogDissmissListener() {
                @Override
                public void onDissmiss(DialogResult result, Object... values) {
                    if (result == DialogResult.CONFIRM) {
                        deleAll();
                    }
                }
            });
        }
        popMenu.showAsDropDown(toolbarMenu);
    }

    private void deleAll() {
        ConfirmDialog.newInstance("全部删除？", "是否删除全部床位？")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            Bed.deleAll();
                            bedList.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }
                }).show(getSupportFragmentManager(), "confirm");
    }

    private void showAssignPerson(final int position) {
        final Bed bed = bedList.get(position);
        PersonSelectDialog dialog = new PersonSelectDialog(bed.bedManagerList);
        dialog.setDialogDissmissListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if (result == DialogResult.CONFIRM) {
                    bed.saveBedAssign();
                    adapter.notifyItemChanged(position);
                }
            }
        }).show(getSupportFragmentManager(), "select_person");
    }

    private void edit(int position) {
        editPosition = position;
        Bed bed = bedList.get(position);
        Intent intent = new Intent(BedManage.this, AddOrEditBed.class);
        intent.putExtra("bed_id", bed.getId());
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    private void dele(final Bed bed, final int position) {
        ConfirmDialog.newInstance("删除？", "是否删除床位【" + bed.getName() + " 床】？")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            if (bed.dele()) {
                                bedList.remove(position);
                                adapter.notifyItemRemoved(position);
                            } else {
                                Toast.makeText(BedManage.this, "删除失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show(getSupportFragmentManager(), "confirm");
    }

    private void readDb() {
        bedList.clear();
        bedList.addAll(Bed.findAll());
        adapter.notifyDataSetChanged();
    }
}