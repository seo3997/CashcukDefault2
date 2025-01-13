package com.cashcuk.ad.charactercall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cashcuk.ad.charactercall.receive.ReceiveCallService;
import com.cashcuk.ad.charactercall.send.SendCallService;

/**
 * Created by Administrator on 2017-02-13.
 */
public class ScreenStateBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            SendCallService sendService = new SendCallService();
            sendService.removePopup();

            ReceiveCallService receiveService = new ReceiveCallService();
            receiveService.removePopup();
        }
    }
}
