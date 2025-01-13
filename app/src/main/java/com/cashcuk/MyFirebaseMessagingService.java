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
import android.util.ArrayMap;
import android.util.Log;

import com.cashcuk.ad.detailview.ADDetailViewActivity;
import com.cashcuk.push.PushReceiveActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private String strADIdx = "";
    private String strPushIdx = "";
    private String strADPoint = "";
    private String strTitle = "";
    private String strImgUrl = "";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("temp", "***************onMessageReceived********************");
        Log.d("temp", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("temp", "Message data payload: " + remoteMessage.getData());

            SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
            boolean isLogin = prefs.getBoolean("IsLogin", false);
            if(isLogin) {
                ShowMessage(this, remoteMessage);
            }

            /*
            if (true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
            */
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("temp", "Message Notification Body: " + remoteMessage.getNotification().getBody());
       }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]
    public void ShowMessage(Context context, RemoteMessage remoteMessage){

        Map<String,String> aMsg = remoteMessage.getData();
        strADIdx = aMsg.get("AD");
        strPushIdx = aMsg.get("PUSH");
        strADPoint = aMsg.get("POINT");
        strTitle = aMsg.get("TITLE");
        strImgUrl = aMsg.get("MSG");

        Log.d("temp", "strADIdx[" + strADIdx+"]");
        Log.d("temp", "strPushIdx[" + strPushIdx+"]");
        Log.d("temp", "strADPoint[" + strADPoint+"]");
        Log.d("temp", "strTitle[" + strTitle+"]");
        Log.d("temp", "strImgUrl[" + strImgUrl+"]");


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


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d("temp", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d("temp", "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
    }
}