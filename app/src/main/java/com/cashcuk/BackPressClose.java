package com.cashcuk;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.cashcuk.dialog.DlgSelImg;

/**
 * Created by Administrator on 2016-04-19.
 */
public class BackPressClose {
    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;
    private Context mContext;

    public BackPressClose(Activity context){
        this.activity = context;
        mContext = this.activity.getApplicationContext();
    }

    public void onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            showToast();
            return;
        }
        if(System.currentTimeMillis() <= backKeyPressedTime+2000){
            activity.finish();
            toast.cancel();

            DlgSelImg mSelDlg = new DlgSelImg(activity);
            mSelDlg.DeleteDir(mSelDlg.STR_DIR);
            CheckLoginService.Close_All();
        }
    }

    public void showToast(){
        toast = Toast.makeText(activity, R.string.str_app_finish_msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
