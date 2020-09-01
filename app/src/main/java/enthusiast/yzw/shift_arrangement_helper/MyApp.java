package enthusiast.yzw.shift_arrangement_helper;

import android.app.Application;

import enthusiast.yzw.shift_arrangement_helper.db_helper.DbHelper;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DbHelper.initial(this);
    }
}
