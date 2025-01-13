package com.cashcuk;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.cashcuk.ad.detailview.ADDetailViewActivity;
import com.cashcuk.push.PushReceiveActivity;
import com.google.android.gcm.GCMBaseIntentService;

/**
 * Created by Administrator on 2016-04-18.
 */
public class GCMIntentService extends GCMBaseIntentService {
    public final static String SENDER_ID = "503006948060";
    public final static String PROJECT_ID = "cashcuk-1285";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onError(Context arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    private String strADIdx = "";
    private String strPushIdx = "";
    private String strADPoint = "";
    private String strTitle = "";
    private String strImgUrl = "";

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d(TAG, "********************onMessage: ********************" );

        // GCM메세지를 수신받았을 때 발생되는 action인지 체크한다.
        if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {

            SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
            boolean isLogin = prefs.getBoolean("IsLogin", false);

            if(isLogin) {
                ShowMessage(context, intent);
            }
        }
    }

    public void ShowMessage(Context context, Intent intent){

        strADIdx = intent.getStringExtra("AD");
        strPushIdx = intent.getStringExtra("PUSH");
        strADPoint = intent.getStringExtra("POINT");
        strTitle = intent.getStringExtra("TITLE");
        strImgUrl = intent.getStringExtra("MSG");

        Intent notiIntent = null;
        notiIntent = new Intent(context, ADDetailViewActivity.class);

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Activity.NOTIFICATION_SERVICE);
        notiIntent.putExtra("AD_IDX", strADIdx);
        notiIntent.putExtra("PUSH_IDX", strPushIdx);
        notiIntent.putExtra("POINT", strADPoint);
        notiIntent.putExtra("TITLE", strTitle);
        notiIntent.putExtra("MSG", strImgUrl);
        notiIntent.putExtra("AD_KIND", getResources().getString(R.string.str_user_en));
        notiIntent.putExtra("PUSH_MODE", true);
        notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent를 등록 하고, noti를 클릭시에 어떤 클래스를 호출 할 것인지 등록.
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, notiIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification();
        notification.icon = R.drawable.app_icon;
        notification.tickerText = context.getResources().getString(R.string.app_name_ko);
        notification.when = System.currentTimeMillis();

        SharedPreferences prefs = context.getSharedPreferences("SaveSetting", context.MODE_PRIVATE);
        boolean isSound = prefs.getBoolean("setSound", true);
        boolean isVibrate = prefs.getBoolean("setVibrate", true);

        SharedPreferences prefsNoti = context.getSharedPreferences("SaveNotiSound", context.MODE_PRIVATE);
        String strNotiSound = prefsNoti.getString("rmPath", "");

        SharedPreferences prefsDlg = context.getSharedPreferences("SaveNoti", context.MODE_PRIVATE);
        int mNotiDlgStyle = prefsDlg.getInt("svaeRbPosition", 0); //0: 항상받기, 1: 화면켜짐 시, 2:끄지

        AudioManager audioManager =  (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        switch(audioManager.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL:
                if (isVibrate) {
                    notification.defaults |= notification.DEFAULT_VIBRATE;
                }

                if (isSound) {
                    if(strNotiSound!=null && !strNotiSound.equals("")) {
                        notification.sound = Uri.parse(strNotiSound);
                    } else if (strNotiSound.equals("")) {
                        RingtoneManager rm = new RingtoneManager(context);
                        rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                        Cursor cursor = rm.getCursor();

                        cursor.moveToFirst();
                        if (cursor != null) {
                            SharedPreferences.Editor editor = prefsNoti.edit();
                            editor.putInt("svaeSoundPosition", 0);
                            editor.commit();
                            strNotiSound = rm.getRingtoneUri(0).toString();
                        }
                    }
                }
                break;
            case AudioManager.RINGER_MODE_SILENT:
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                if (isVibrate) {
                    notification.defaults |= notification.DEFAULT_VIBRATE;
                }
                break;
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        //notification.setLatestEventInfo(context, context.getResources().getString(R.string.str_push_noti_txt), strTitle, pIntent);

        if(strADIdx!=null && !strADIdx.equals("")){
            notificationManager.notify(0, notification);
        }

        if(strADIdx!=null && !strADIdx.equals("")){
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if(mNotiDlgStyle==0){// || StaticDataInfo.NOTI_POPUP_STYLE==1){
                    WakeLock(context);
            }else if(mNotiDlgStyle==1){
                if(isScreenOn){
                    WakeLock(context);
                }
            }
        }

        }

    private void WakeLock(Context context) {
        Intent i = new Intent(context, PushReceiveActivity.class);
        Bundle b = new Bundle();
        b.putString("AD", strADIdx);
        b.putString("POINT", strADPoint);
        b.putString("TITLE", strTitle);
        b.putString("MSG", strImgUrl);
        b.putString("PUSH", strPushIdx);
        b.putBoolean("PUSH_MODE", true);
        i.putExtras(b);

        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
    }

    @Override
    protected void onRegistered(final Context context, String regID) {
        SharedPreferences prefs = context.getSharedPreferences("SaveRegId", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("setRegId", regID);
        editor.commit();

        // 기다리고 있는 Activity 에 통지하기
        Intent intent = new Intent();
        intent.setAction(IntroActivity.ACTION_GCM_REGISTRATION);
        intent.putExtra("registrationId", regID);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        // TODO Auto-generated method stub

    }
}
