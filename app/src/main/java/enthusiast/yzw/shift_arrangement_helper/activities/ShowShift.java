package enthusiast.yzw.shift_arrangement_helper.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.MenuPopupWindow;

import android.content.Intent;
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
import enthusiast.yzw.shift_arrangement_helper.custom_views.MyFormView;
import enthusiast.yzw.shift_arrangement_helper.db_helper.DbOperator;
import enthusiast.yzw.shift_arrangement_helper.dialogs.ConfirmDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.DialogDissmissListener;
import enthusiast.yzw.shift_arrangement_helper.dialogs.LoadingDialog;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyPopMenu;
import enthusiast.yzw.shift_arrangement_helper.dialogs.MyToast;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;
import enthusiast.yzw.shift_arrangement_helper.moduls.ShiftOfWeekView;
import enthusiast.yzw.shift_arrangement_helper.tools.DateTool;

public class ShowShift extends AppCompatActivity {
    private final int CODE_BEGIN = 0, CODE_SUCCESS = 1,CODE_FAIL = -1;
    private AppCompatImageView imageviewMenu;
    private List<ShiftOfWeekView> dataList;
    private MyFormView formView;
    private MyFormView.FormAdapter<ShiftOfWeekView> adapter;
    private LocalDate monday;
    private LoadingDialog loadingDialog;
    private Handler handler;
    private MyPopMenu popMenu;

    private MyFormView.Header createHeader() {
        String[] headerTitleArray = {"姓名", "", "", "", "", "", "", "", "备注","余假","管床"};
        SparseIntArray widths = new SparseIntArray();
        widths.put(0,200);
        widths.put(8, 300);
        widths.put(9, 100);
        widths.put(10, 400);
        return new MyFormView.HeaderBuilder()
                .setHeaderHeight(180)
                .setDefaultWidth(180)
                .setCellTextArray(headerTitleArray)
                .setCellWidthArray(widths).build();
    }

    private void creatPopMenu(){
        String[] items = {"调整排班","删除本周排班","保存为模板"};
        popMenu = new MyPopMenu(this,items);
        popMenu.setListener(new DialogDissmissListener() {
            @Override
            public void onDissmiss(DialogResult result, Object... values) {
                if(result == DialogResult.CONFIRM){
                    int index = (int) values[0];
                    switch (index){
                        case 0:
                            edit();
                            break;
                        case 1:
                            del();
                            break;
                        case 2:
                            saveToTemplate();
                            break;
                    }
                }
            }
        });
    }

    private void edit(){
        Intent intent = new Intent(ShowShift.this,AddOrEditShift.class);
        intent.putExtra("localdate",monday.toEpochDay());
        intent.putExtra("mode",1);
        startActivity(intent);
    }

    private void del(){
        if(dataList.isEmpty())
            return;
        ConfirmDialog.newInstance("删除？","确认要删除本周排班？")
                .setDialogDissmissListener(new DialogDissmissListener() {
                    @Override
                    public void onDissmiss(DialogResult result, Object... values) {
                        if(result == DialogResult.CONFIRM){
                            ShiftOfWeekView.deleAll(monday);
                            dataList.clear();
                            adapter.notifyDataChanged();
                            new MyToast(ShowShift.this).centerShow("已删除");
                        }
                    }
                })
                .show(getSupportFragmentManager(),"dele");
    }

    private void saveToTemplate(){

    }

    private void initial(){
        monday = DateTool.getMonday(LocalDate.now());
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                switch (message.what){
                    case CODE_BEGIN:
                        loadingDialog.show(getSupportFragmentManager(),"loading");
                        break;
                    case CODE_SUCCESS:
                        loadingDialog.dismiss();
                        onDataListChanged();
                        break;
                    case CODE_FAIL:
                        loadingDialog.dismiss();
                        new MyToast(ShowShift.this).centerShow("加载数据失败");
                        break;
                }
                return true;
            }
        });
        dataList = new ArrayList<>();
        adapter = new MyFormView.FormAdapter<ShiftOfWeekView>(dataList) {
            @Override
            public void onBindData(MyFormView.Row row, ShiftOfWeekView shiftOfWeekView) {
                row.setCellText(0,shiftOfWeekView.getPersonName());
                for(int i = 0;i<7;i++){
                    row.setCellText(i+1,shiftOfWeekView.getWorkCategoryName(i));
                }
                row.setCellText(8,shiftOfWeekView.getNote());
                row.setCellText(9,shiftOfWeekView.getAbsentRemain()+"");
                row.setCellText(10,shiftOfWeekView.getBedAssignString());
            }
        };
    }

    private void initialView() {
         findViewById(R.id.imageview_toolbar_back).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onBackPressed();
             }
         });
         findViewById(R.id.imageview_last_week).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                lastWeek();
             }
         });
        findViewById(R.id.imageview_next_week).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextWeek();
            }
        });
        imageviewMenu = findViewById(R.id.imageview_toolbar_menu);
        imageviewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });
        formView = findViewById(R.id.activity_show_shift_myformview);
        formView.setHeader(createHeader());
        formView.setRowHeight(120);
        formView.setTitleHeight(250);
        formView.setTitleTextSize(80);
        formView.setTitleString(DbOperator.getOrganizeName() + "排班表");
        formView.setOnClick(new MyFormView.OnClick() {
            @Override
            public void onClick(MyFormView.Cell cell) {
                onCellClicked(cell);
            }
        });
        formView.setDataAdapter(adapter);
        loadingDialog = LoadingDialog.newInstance("正在读取...");
    }

    private void showMenu(){
        if(popMenu == null)
            creatPopMenu();
        popMenu.showAsDropDown(imageviewMenu);
    }

    private void onCellClicked(MyFormView.Cell cell){
        formView.setCurrentCell(null);
    }

    private void thisWeek(){
        monday = DateTool.getMonday(LocalDate.now());
        readData(monday);
    }

    private void lastWeek(){
        monday = monday.minusDays(7);
        readData(monday);
    }

    private void nextWeek(){
        monday = monday.plusDays(7);
        readData(monday);
    }

    private void readData(final LocalDate date){
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(CODE_BEGIN);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dataList.clear();
                dataList.addAll(ShiftOfWeekView.getAWeekShift(date));
                handler.sendEmptyMessage(CODE_SUCCESS);
            }
        }).start();
    }

    private void onDataListChanged(){
        adapter.notifyDataChanged();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月\nd日\nEEEE", Locale.CHINA);
        String subTitle = monday.getYear() + " 年度 第 " + DateTool.getWeekOfYear(monday) + " 周";
        formView.setSubTitle(subTitle);
        for (int i = 0; i < 7; i++) {
            String text = monday.plusDays(i).format(formatter);
            formView.setColumnTitle((i + 1), text);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_shift);

        initial();
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        readData(monday);
    }
}