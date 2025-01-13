package com.cashcuk.ad.charactercall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.cashcuk.ad.charactercall.receive.ReceiveCallService;
import com.cashcuk.ad.charactercall.send.SendCallService;

/**
 * 전화 수신
 */
public class CallStateBR extends BroadcastReceiver {
    private static String mLastState;
//    private static boolean isOffHook = false; //통화 연결 시 true
//    private static boolean isSend = false;
//    private static boolean isReceive = false;

    private Context mContext;
//    private String strPhoneNum="";
    private static String strOutNum = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        SharedPreferences prefs = mContext.getSharedPreferences("SaveLoginInfo", mContext.MODE_PRIVATE);
        boolean isLogin = prefs.getBoolean("IsLogin", false);

        if (isLogin) {
            /**
             * 2번 호출되는 문제 해결
             */
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state != null && state.equals(mLastState)) {
                return;
            } else {
                mLastState = state;
            }

            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String myPhoneNum = telManager.getLine1Number();

            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                strOutNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            }

            if (state != null) {
                boolean isSendCall = true;
                boolean isReceiveCall = true;
                Intent serviceIntent = null;

                if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    isSendCall = true;
                    isReceiveCall = true;
                } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    closeWindow();

                    serviceIntent = new Intent(mContext, ReceiveCallService.class);
                    if (serviceIntent != null) {
                        mContext.stopService(serviceIntent);
                    }

                    if (isReceiveCall) {
                        isReceiveCall = false;
                        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        serviceIntent.putExtra("IncomingNum", number);
                        mContext.startService(serviceIntent);

                    }
                    //통화 벨 울릴 시 구현 ...
                } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    closeWindow();
                    serviceIntent = new Intent(mContext, SendCallService.class);
                    if (serviceIntent != null) {
                        mContext.stopService(serviceIntent);
                    }

                    if (isSendCall) {
                        isSendCall = false;
                        serviceIntent.putExtra("ReceiveNum", strOutNum);
                        mContext.startService(serviceIntent);

                    }
                    //통화 중 상태일 때 구현 ...
                }
            }
        }
    }

    public void closeWindow() {
        if (SendCallService.rootSendNewView != null && SendCallService.windowSendNewManager != null) {
            SendCallService.windowSendNewManager.removeView(SendCallService.rootSendNewView);
            SendCallService.rootSendNewView = null;
            SendCallService.windowSendNewManager = null;
        }

        if (ReceiveCallService.rootView != null && ReceiveCallService.windowManager != null){
            ReceiveCallService.windowManager.removeView(ReceiveCallService.rootView);
            ReceiveCallService.rootView = null;
            ReceiveCallService.windowManager = null;
        }
    }
}
