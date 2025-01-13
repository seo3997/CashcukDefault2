package com.cashcuk;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Login service
 */
public class CheckLoginService extends Service {
    public static boolean start_service = false;
    public static ArrayList<Activity> mActivityList = new ArrayList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();

        start_service = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 전체 엑티비티 종료
    public static void Close_All() {

        for (int i = 0; i < mActivityList.size(); i++) {
            try {
                mActivityList.get(i).finish();
            } catch (Exception e) {;}
            //List가 Static 이므로, Class명.변수명.get으로 접근
        }
    }

    public static void CloseActivity(){
        for (int i = 0; i < mActivityList.size(); i++) {
            if (!mActivityList.get(i).toString().matches(".*" + "MainActivity" + ".*")) {
                mActivityList.get(i).finish();
            }
        }
    }
}
