package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseIntArray;
import android.view.View;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyAdapter;
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyFormView;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyPopMenu;
import enthusiast.yzw.shift_arrangement_helper.dialogs.InputTextDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.LoadingDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.dialogs.ShiftTemplateSelect;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.Person;
import enthusiast.yzw.shift_arrangement_helper.moduls.Shift;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftNote;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftOfWeekView;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftTemplate;
import enthusiast.yzw.shift_arrangement_helper.moduls.WorkCategory;
import enthusiast.yzw.shift_arrangement_helper.tools.DateTool;

public class AddOrEditShift extends AppCompatActivity {
    private static final String TAG = "殷宗旺";
    private final int HANDLE_CODE_SAVE_FAIL = -1, HANDLE_CODE_READ = 1, HANDLE_CODE_SAVE = 2, HANDLE_CODE_READ_DONE = 3, HANDLE_CODE_SAVE_EXIT = 4, HANDLE_CODE_SAVE_CONTINUE = 5;
    private Group bottomGroup;
    private AppCompatTextView textviewToolbarTitle;
    private AppCompatImageView imageViewMenu, imageViewSave;
    private MyFormView myFormView;
    private AppCompatTextView textviewInfo;
    private RecyclerView recyclerView;
    private MyToast toast;

    private List<Person> personList;
    private List<WorkCategory> workList;
    private List<ShiftOfWeekView> shiftOfWeekViewList;
    private MyAdapter<Person> personMyAdapter;
    private MyAdapter<WorkCategory> workAdapter;
    private MyFormView.FormAdapter<ShiftOfWeekView> formAdapter;
    private MyFormView.Header header;

    private LocalDate monday;
    private boolean isEditMode = false;
    private Handler handler;
    private LoadingDialog loadingDialog;

    private void initialMyFormViewHeader() {
        String[] headerTitleArray = {"姓名", "", "", "", "", "", "", "", "备注"};
        SparseIntArray widths = new SparseIntArray();
        widths.put(0, 250);
        widths.put(8, 300);
        widths.put(9, 100);
        widths.put(10, 400);
        header = new MyFormView.HeaderBuilder()
                .setHeaderHeight(180)
                .setDefaultWidth(180)
                .setCellTextArray(headerTitleArray)
                .setCellWidthArray(widths).build();
    }

    private void initialView() {
        initialMyFormViewHeader();
        bottomGroup = findViewById(R.id.bottom_group);
        bottomGroup.setVisibility(View.GONE);
        findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        textviewToolbarTitle = findViewById(R.id.textview_toolbar_title);
        textviewToolbarTitle.setText(isEditMode ? "编辑" : "排班");
        imageViewMenu = findViewById(R.id.imageview_toolbar_template);
        imageViewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToolbarMenu();
            }
        });
        findViewById(R.id.imageview_toolbar_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRow();
            }
        });
        findViewById(R.id.imageview_toolbar_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleRow();
            }
        });
        imageViewSave = findViewById(R.id.imageview_toolbar_save);
        imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSaveMenu();
            }
        });
        myFormView = findViewById(R.id.myformview_activity_edit_shift);
        myFormView.setHeader(header);
        myFormView.setRowHeight(120);
        myFormView.setTitleHeight(100);
        myFormView.setTitleString("");
        myFormView.setFirstScale(false);
        myFormView.setOnClick(new MyFormView.OnClick() {
            @Override
            public void onClick(MyFormView.Cell cell) {
                onCellClick(cell);
            }
        });
        textviewInfo = findViewById(R.id.textview_activity_edit_shift_info);
        recyclerView = findViewById(R.id.recyclerview_activity_edit_shift);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        toast = new MyToast(this);
    }

    private void initial() {
        shiftOfWeekViewList = new ArrayList<>();
        shiftOfWeekViewList.add(new ShiftOfWeekView());
        formAdapter = new MyFormView.FormAdapter<ShiftOfWeekView>(shiftOfWeekViewList) {
            @Override
            public void onBindData(MyFormView.Row row, ShiftOfWeekView shift) {
                row.setCellText(0, shift.getPersonName());
                for (int i = 1; i < 8; i++) {
                    row.setCellText(i, shift.getWorkCategoryName(i - 1));
                }
                row.setCellText(8, shift.getNote());
            }
        };
        myFormView.setDataAdapter(formAdapter);
        personList = Person.getShiftEditPersonList();
        workList = DbOperator.findAll(WorkCategory.class);
        personMyAdapter = new MyAdapter<Person>(personList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, Person data) {
                myViewHolder.setImage(R.id.imageview_item_edit_shift_list, R.drawable.ic_person3);
                myViewHolder.setText(R.id.textview_item_edit_shift_list, data.getName());
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSelectPerson(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_edit_shift_list;
            }
        };
        workAdapter = new MyAdapter<WorkCategory>(workList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, WorkCategory data) {
                myViewHolder.setImage(R.id.imageview_item_edit_shift_list, R.drawable.ic_work_categoty);
                myViewHolder.setText(R.id.textview_item_edit_shift_list, data.getName());
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSelectWork(myViewHolder.getAbsoluteAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_edit_shift_list;
            }
        };
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                switch (message.what) {
                    case HANDLE_CODE_READ:
                        loadingDialog = LoadingDialog.newInstance("正在读取...");
                        loadingDialog.show(getSupportFragmentManager(), "show_loading");
                        break;
                    case HANDLE_CODE_READ_DONE: // 读取数据成功
                        loadingDialog.dismiss();
                        formAdapter.notifyDataChanged();
                        personMyAdapter.notifyDataSetChanged();
                        break;
                    case HANDLE_CODE_SAVE:
                        loadingDialog = LoadingDialog.newInstance("正在保存...");
                        loadingDialog.show(getSupportFragmentManager(), "show_loading");
                        break;
                    case HANDLE_CODE_SAVE_EXIT: // 保存数据成功
                        loadingDialog.dismiss();
                        onBackPressed();
                        break;
                    case HANDLE_CODE_SAVE_CONTINUE:
                        loadingDialog.dismiss();
                        nextWeek();
                        break;
                    case HANDLE_CODE_SAVE_FAIL: // 操作失败
                        loadingDialog.dismiss();
                        toast.centerShow("操作失败");
                }
                return true;
            }
        });
    }

    private void readData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(HANDLE_CODE_READ);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                shiftOfWeekViewList.clear();
                shiftOfWeekViewList.addAll(0, ShiftOfWeekView.getAWeekShift(monday));
                ShiftOfWeekView emtpy = new ShiftOfWeekView();
                emtpy.setMonday(monday);
                shiftOfWeekViewList.add(emtpy);
                personList.clear();
                personList.addAll(Person.getShiftEditPersonList());

                for (ShiftOfWeekView entity : shiftOfWeekViewList) {
                    Person person = entity.getPerson();
                    if (person != null) {
                        personList.remove(person);
                    }
                }
                handler.sendEmptyMessage(HANDLE_CODE_READ_DONE);
            }
        }).start();
    }

    private void changeFormInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月\nd日\nEEEE", Locale.CHINA);
        String subTitle = monday.getYear() + " 年度 第 " + DateTool.getWeekOfYear(monday) + " 周";
        myFormView.setSubTitle(subTitle);
        for (int i = 0; i < 7; i++) {
            String text = monday.plusDays(i).format(formatter);
            myFormView.setColumnTitle((i + 1), text);
        }
    }

    private void changeAdapter(boolean isSelectPerson) {
        bottomGroup.setVisibility(View.VISIBLE);
        if (isSelectPerson) {
            textviewInfo.setText("人员");
            recyclerView.setAdapter(personMyAdapter);
        } else {
            textviewInfo.setText("班次");
            recyclerView.setAdapter(workAdapter);
        }
    }

    private void onCellClick(MyFormView.Cell cell) {
        if (myFormView.getCurrentCell() == null)
            bottomGroup.setVisibility(View.GONE);
        else {
            switch (cell.getColumnIndex()) {
                case 0:
                    changeAdapter(true);
                    break;
                case 8:
                    bottomGroup.setVisibility(View.GONE);
                    inputNote(cell.getRowIndex());
                    break;
                default:
                    changeAdapter(false);
                    break;
            }
        }
    }

    private void onSelectPerson(int position) {
        MyFormView.Cell cell = myFormView.getCurrentCell();
        if (cell != null) {
            int rowIndex = cell.getRowIndex();
            ShiftOfWeekView entity = shiftOfWeekViewList.get(rowIndex);
            Person person = entity.getPerson();
            entity.setPerson(personList.remove(position));
            if (person != null) {
                personList.add(position, person);
                personMyAdapter.notifyItemChanged(position);
            } else {
                personMyAdapter.notifyItemRemoved(position);
            }
            formAdapter.notifyDataChanged(rowIndex);
            if (rowIndex == shiftOfWeekViewList.size() - 1) {
                addRow();
            }
            myFormView.setCurrentCell(rowIndex + 1, 0);
        }
    }

    private void onSelectWork(int position) {
        // 需要完善一下
        MyFormView.Cell cell = myFormView.getCurrentCell();
        if (cell != null) {
            int rowIndex = cell.getRowIndex();
            int columnIndex = cell.getColumnIndex();
            WorkCategory workCategory = workList.get(position);
            ShiftOfWeekView entity = shiftOfWeekViewList.get(rowIndex);
            entity.setWork(columnIndex - 1, workCategory);
            int currentRow = rowIndex;
            int currentCol = columnIndex;
            if (columnIndex < 7) {
                currentCol++;
            } else if (rowIndex < shiftOfWeekViewList.size() - 1) {
                currentRow++;
                currentCol = 1;
            }
            formAdapter.notifyDataChanged(rowIndex);
            myFormView.setCurrentCell(currentRow, currentCol);
        }
    }

    private void addRow() {
        ShiftOfWeekView entity = new ShiftOfWeekView();
        entity.setMonday(monday);
        shiftOfWeekViewList.add(entity);
        formAdapter.notifyDataAdd();
    }

    private void deleRow() {
        MyFormView.Cell cell = myFormView.getCurrentCell();
        if (cell != null) {
            int rowIndex = cell.getRowIndex();
            ShiftOfWeekView entity = shiftOfWeekViewList.remove(rowIndex);
            Person person = entity.getPerson();
            if (person != null) {
//                DbOperator.deleAWeekShift(person, monday);
                personList.add(person);
                personMyAdapter.notifyItemInserted(personList.size() - 1);
            }
            formAdapter.notifyDataDelete();
        }
        bottomGroup.setVisibility(View.GONE);
    }

    public void inputNote(final int rowIndex) {
        final ShiftOfWeekView entity = shiftOfWeekViewList.get(rowIndex);
        InputTextDialog dialog = InputTextDialog.newInstance("输入备注", entity.getNote());
        dialog.setListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if (result == DialogResult.CONFIRM) {
                    String s = (String) values[0];
                    entity.setNote(s);
                    formAdapter.notifyDataChanged(rowIndex);
                }
                myFormView.setCurrentCell(null);
            }
        }).show(getSupportFragmentManager(), "input_note");
    }

    private void saveAndExit() {
        if (shiftOfWeekViewList.isEmpty()) {
            toast.centerShow("没有排班数据");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (save()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(HANDLE_CODE_SAVE_EXIT);
                } else
                    handler.sendEmptyMessage(HANDLE_CODE_SAVE_FAIL);
            }
        }).start();
    }

    private void saveAndContinue() {
        if (shiftOfWeekViewList.isEmpty()) {
            toast.centerShow("没有排班数据");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (save()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(HANDLE_CODE_SAVE_CONTINUE);
                } else {
                    handler.sendEmptyMessage(HANDLE_CODE_SAVE_FAIL);
                }
            }
        }).start();
    }

    private boolean save() {
        handler.sendEmptyMessage(HANDLE_CODE_SAVE);
        return ShiftOfWeekView.saveTogether(shiftOfWeekViewList, monday);
    }

    private void showToolbarMenu() {
        String[] items = {"读取上周排班", "调用模板", "存为模板"};
        new MyPopMenu(this, items)
                .setListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            int index = (int) values[0];
                            switch (index) {
                                case 0:
                                    readLastWeekShift();
                                    break;
                                case 1:
                                    showTemplateList();
                                    break;
                                case 2:
                                    saveToTemplate("");
                                    break;
                            }
                        }
                    }
                })
                .showAsDropDown(imageViewMenu);
    }

    private void showSaveMenu() {
        String[] items = {"保存后退出", "保存并继续"};
        new MyPopMenu(this, items)
                .setListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if (result == DialogResult.CONFIRM) {
                            int index = (int) values[0];
                            switch (index) {
                                case 0:
                                    saveAndExit();
                                    break;
                                case 1:
                                    saveAndContinue();
                                    break;
                            }
                        }
                    }
                })
                .showAsDropDown(imageViewSave);
    }

    private void nextWeek() {
        monday = monday.plusDays(7);
        readData();
        changeFormInfo();
    }

    private void afterReadTemplate(){
        ShiftOfWeekView emtpy = new ShiftOfWeekView();
        shiftOfWeekViewList.add(emtpy);

        personList.clear();
        personList.addAll(Person.getShiftEditPersonList());
        for (ShiftOfWeekView shiftOfWeekView : shiftOfWeekViewList) {
            shiftOfWeekView.setMonday(monday);
            shiftOfWeekView.setPerson(null);
        }
    }

    private void readLastWeekShift() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(HANDLE_CODE_READ);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                shiftOfWeekViewList.clear();
                shiftOfWeekViewList.addAll(0, ShiftOfWeekView.getAWeekShift(monday.minusDays(7)));
                afterReadTemplate();
                handler.sendEmptyMessage(HANDLE_CODE_READ_DONE);
            }
        }).start();
    }

    private void readTemplate(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(HANDLE_CODE_READ);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                shiftOfWeekViewList.clear();
                shiftOfWeekViewList.addAll(ShiftTemplate.find(name));
                afterReadTemplate();
                handler.sendEmptyMessage(HANDLE_CODE_READ_DONE);
            }
        }).start();
    }

    private void showTemplateList(){
        ShiftTemplateSelect shiftTemplateSelect = new ShiftTemplateSelect(this);
        shiftTemplateSelect.setDissmissListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if(result == DialogResult.CONFIRM){
                    String name = (String) values[0];
                    readTemplate(name);
                }
            }
        }).show();
    }

    private void saveToTemplate(String name) {
        InputTextDialog dialog = InputTextDialog.newInstance("输入模板名称", name);
        dialog.setListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if (result == DialogResult.CONFIRM) {
                    String name = (String) values[0];
                    if (ShiftTemplate.isExist(name)) {
                        toast.centerShow("已存在模板【" + name + "】，请重新输入");
                        saveToTemplate(name);
                        return;
                    }
                    if (ShiftTemplate.saveAll(shiftOfWeekViewList, name))
                        toast.centerShow("模板已保存");
                    else
                        toast.centerShow("模板保存失败");
                }
            }
        }).show(getSupportFragmentManager(), "input");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shift);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            long l = bundle.getLong("localdate");
            int mode = bundle.getInt("mode");
            monday = DateTool.getMonday(LocalDate.ofEpochDay(l));
            isEditMode = mode == 1;
        } else {
            monday = DateTool.getMonday(LocalDate.now());
        }
        initialView();
        initial();
    }

    @Override
    protected void onStart() {
        super.onStart();
        readData();
        changeFormInfo();
    }
}