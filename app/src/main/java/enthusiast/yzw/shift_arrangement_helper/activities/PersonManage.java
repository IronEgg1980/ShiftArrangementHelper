package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.tabs.TabLayout;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.ConfirmDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.enums.Gender;
import enthusiast.yzw.shift_arrangement_helper.enums.PersonStatus;
import enthusiast.yzw.shift_arrangement_helper.moduls.Bed;
import enthusiast.yzw.shift_arrangement_helper.moduls.BedAssign;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;

public class PersonManage extends AppCompatActivity {
    private static final String TAG = "殷宗旺";
    private final int REQUEST_CODE_ADD = 1, REQUEST_CODE_EDIT = 2;
    private TabLayout tabLayout;
    private RecyclerView recyclerview;
    private List<Person> ondutyPeople, leavePeople, deletedPeople;
    private MyAdapter<Person> onDutyAdapter, leaveAdapter, deletedAdapter;
    private int editPosition = 0;
    private MyToast toast;

    private static class PersonAdapter extends MyAdapter<Person> {

        public PersonAdapter(List<Person> list) {
            super(list);
        }

        @Override
        public void bindData(MyViewHolder myViewHolder, final Person data) {
            myViewHolder.setText(R.id.textview_item_person_name, data.getName());
            myViewHolder.setText(R.id.textview_item_person_gender, data.getGender().getText());
            myViewHolder.setText(R.id.textview_item_person_age, data.getAge() + " 岁");
            myViewHolder.setText(R.id.textview_item_person_phone, data.getPhone());
            myViewHolder.setText(R.id.textview_item_person_post, data.getPost());
            myViewHolder.setText(R.id.textview_item_person_professor, data.getProfessor());
            myViewHolder.setText(R.id.textview_item_person_ratio, "系数： " + data.getRatio());
            myViewHolder.setText(R.id.textview_item_person_school, data.getSchool());
            myViewHolder.setText(R.id.textview_item_person_note, data.getNote());
            final Group group = myViewHolder.getView(R.id.bottomGroup);
            AppCompatCheckBox compatCheckBox = myViewHolder.getView(R.id.checkbox_item_person_management_showdetails);
            compatCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    data.isShowDetails = b;
                    group.setVisibility(b ? View.VISIBLE : View.GONE);
                }
            });
            compatCheckBox.setChecked(data.isShowDetails);
        }

        @Override
        public int getLayoutId(int position) {
            return 0;
        }
    }

    private class OnDutyAdapter extends PersonAdapter {

        public OnDutyAdapter(List<Person> list) {
            super(list);
        }

        @Override
        public void bindData(final MyViewHolder myViewHolder, final Person data) {
            super.bindData(myViewHolder, data);
            final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
            myViewHolder.getView(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothExpand();
                }
            });
            myViewHolder.getView(R.id.swipemenu_edit_person).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothClose();
                    editPerson(myViewHolder.getAbsoluteAdapterPosition());
                }
            });
            myViewHolder.getView(R.id.swipemenu_person_leave).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothClose();
                    leavePerson(myViewHolder.getAbsoluteAdapterPosition());
                }
            });
        }

        @Override
        public int getLayoutId(int position) {
            return R.layout.item_person_management;
        }
    }

    private class LeaveAdapter extends PersonAdapter {

        public LeaveAdapter(List<Person> list) {
            super(list);
        }

        @Override
        public void bindData(final MyViewHolder myViewHolder, Person data) {
            super.bindData(myViewHolder, data);
            final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
            myViewHolder.getView(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothExpand();
                }
            });
            myViewHolder.getView(R.id.swipemenu_person_join).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothClose();
                    personJoin(myViewHolder.getAbsoluteAdapterPosition());
                }
            });
            myViewHolder.getView(R.id.swipemenu_person_deleted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothClose();
                    delePerson(myViewHolder.getAbsoluteAdapterPosition());
                }
            });
        }

        @Override
        public int getLayoutId(int position) {
            return R.layout.item_person_management_leave;
        }
    }

    private class DeletedAdapter extends PersonAdapter {

        public DeletedAdapter(List<Person> list) {
            super(list);
        }

        @Override
        public void bindData(final MyViewHolder myViewHolder, Person data) {
            super.bindData(myViewHolder, data);
            final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
            myViewHolder.getView(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothExpand();
                }
            });
            myViewHolder.getView(R.id.swipemenu_person_join).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothClose();
                    personCallback(myViewHolder.getAbsoluteAdapterPosition());
                }
            });
            myViewHolder.getView(R.id.swipemenu_person_remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swipeMenuLayout.smoothClose();
                    removePerson(myViewHolder.getAbsoluteAdapterPosition());
                }
            });
        }

        @Override
        public int getLayoutId(int position) {
            return R.layout.item_person_management_deleted;
        }
    }

    private void initial() {
        ondutyPeople = DbOperator.findAll(Person.class, "person_status = ?", String.valueOf(PersonStatus.ONDUTY.ordinal()));
        leavePeople = DbOperator.findAll(Person.class, "person_status = ?", String.valueOf(PersonStatus.LEAVED.ordinal()));
        deletedPeople = DbOperator.findAll(Person.class, "person_status = ?", String.valueOf(PersonStatus.DELETED.ordinal()));
        onDutyAdapter = new OnDutyAdapter(ondutyPeople);
        leaveAdapter = new LeaveAdapter(leavePeople);
        deletedAdapter = new DeletedAdapter(deletedPeople);
        recyclerview.setAdapter(onDutyAdapter);
    }

    private void initialView() {
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        AppCompatTextView textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        textviewToolbarTitle.setText("成员管理");
        tabLayout = findViewById(R.id.tabLayout_activity_person_management);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        recyclerview.setAdapter(onDutyAdapter);
                        break;
                    case 1:
                        recyclerview.setAdapter(leaveAdapter);
                        break;
                    case 2:
                        recyclerview.setAdapter(deletedAdapter);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        recyclerview = findViewById(R.id.recyclerview_person_management_list);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.button_person_management_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPerson();
            }
        });
        toast = new MyToast(this);
    }

    private void addPerson() {
        startActivityForResult(new Intent(PersonManage.this, AddOrEditPerson.class), REQUEST_CODE_ADD);
    }

    private void editPerson(int position) {
        editPosition = position;
        Person person = ondutyPeople.get(position);
        Intent intent = new Intent(PersonManage.this, AddOrEditPerson.class);
        intent.putExtra("id", person.getId());
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    private void leavePerson(final int position) {
        final Person person = ondutyPeople.get(position);
        ConfirmDialog.newInstance("出科？", "是否将【" + person.getName() + "】的状态设置为【出科】？")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            person.setStatus(PersonStatus.LEAVED);
                            if (person.update()) {
                                person.isShowDetails = false;
                                leavePeople.add(ondutyPeople.remove(position));
                                onDutyAdapter.notifyItemRemoved(position);
                            } else {
                                person.setStatus(PersonStatus.ONDUTY);
                                toast.centerShow("操作失败！");
                            }

                        }
                    }
                })
                .show(getSupportFragmentManager(), "confirm");
    }

    private void personJoin(int position) {
        final Person person = leavePeople.get(position);
        person.setStatus(PersonStatus.ONDUTY);
        if (person.update()) {
            person.isShowDetails = false;
            ondutyPeople.add(leavePeople.remove(position));
            leaveAdapter.notifyItemRemoved(position);
        } else {
            person.setStatus(PersonStatus.LEAVED);
            toast.centerShow("操作失败！");
        }
    }

    private void personCallback(int position) {
        final Person person = deletedPeople.get(position);
        person.setStatus(PersonStatus.ONDUTY);
        if (person.update()) {
            person.isShowDetails = false;
            ondutyPeople.add(deletedPeople.remove(position));
            deletedAdapter.notifyItemRemoved(position);
        } else {
            person.setStatus(PersonStatus.DELETED);
            toast.centerShow("操作失败！");
        }
    }

    private void removePerson(final int position) {
        final Person person = deletedPeople.get(position);
        ConfirmDialog.newInstance("注销", "是否注销【" + person.getName() + "】的所有信息？注意：注销后不能恢复数据，请谨慎操作。")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            if(person.unRegister()){
                                deletedPeople.remove(position);
                                deletedAdapter.notifyItemRemoved(position);
                            }else{
                                toast.centerShow("操作失败");
                            }
                        }
                    }
                })
                .show(getSupportFragmentManager(), "removeConfirm");
    }

    private void delePerson(final int position) {
        final Person person = leavePeople.get(position);
        ConfirmDialog.newInstance("删除？", "是否从成员列表中删除【" + person.getName() + "】？")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            person.setStatus(PersonStatus.DELETED);
                            if (person.update()) {
                                person.isShowDetails = false;
                                deletedPeople.add(leavePeople.remove(position));
                                leaveAdapter.notifyItemRemoved(position);
                            } else {
                                person.setStatus(PersonStatus.LEAVED);
                                toast.centerShow("操作失败！");
                            }
                        }
                    }
                })
                .show(getSupportFragmentManager(), "confirm");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 666) {
            if (requestCode == REQUEST_CODE_ADD) {
                ondutyPeople.clear();
                ondutyPeople.addAll(DbOperator.findAll(Person.class, "person_status = ?", String.valueOf(PersonStatus.ONDUTY.ordinal())));
                TabLayout.Tab tab = tabLayout.getTabAt(0);
                tabLayout.selectTab(tab);
                onDutyAdapter.notifyDataSetChanged();
                recyclerview.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!ondutyPeople.isEmpty())
                            recyclerview.smoothScrollToPosition(ondutyPeople.size() - 1);
                    }
                }, 50);
            } else if (requestCode == REQUEST_CODE_EDIT) {
                long id = ondutyPeople.remove(editPosition).getId();
                ondutyPeople.add(editPosition, DbOperator.findByID(Person.class, id));
                onDutyAdapter.notifyItemChanged(editPosition);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_management);
        initialView();
        initial();
    }
}