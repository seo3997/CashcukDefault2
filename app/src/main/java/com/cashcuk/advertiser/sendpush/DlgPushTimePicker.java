package com.cashcuk.advertiser.sendpush;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;

/**
 * push 발송 요청 시간
 */
public class DlgPushTimePicker extends Activity implements View.OnClickListener, View.OnTouchListener {
    private int mHour = 1;
    private EditText etHour;
    private String strHour;
//    private ToggleButton tbTime;

    private Button btnDateOk;
    private Button btnDateCancel;

    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dlg_push_timepicker);
        CheckLoginService.mActivityList.add(this);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

//        tbTime = (ToggleButton) this.findViewById(R.id.tb_time);

        intent = new Intent(this.getIntent());
        mHour = intent.getIntExtra("Hour", 1);
//        if(intent!=null) {
//            String[] strGetHour = (intent.getStringExtra("Hour")).split(" ");
//            if(strGetHour[0].equals(getResources().getString(R.string.str_push_time_pm))){
//                tbTime.setChecked(true);
//            }else{
//                tbTime.setChecked(false);
//            }
//
//            mHour = Integer.parseInt(strGetHour[1]);
//        }
        etHour = (EditText) findViewById(R.id.et_hour);

//        strHour =
        strHour = String.format("%02d", mHour);
//        strHour = String.valueOf(mHour);
//        if (strHour.length() < 2) {
//            strHour = "0" + strHour;
//        }
        etHour.setText(strHour);
//        tbTime.setOnClickListener(this);

        ((ImageButton) findViewById(R.id.ib_add_hour)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ib_minus_hour)).setOnClickListener(this);

        LinearLayout llDateOk = (LinearLayout) findViewById(R.id.ll_date_ok);
        btnDateOk = (Button) findViewById(R.id.btn_date_ok);
        llDateOk.setOnTouchListener(this);
        btnDateOk.setOnTouchListener(this);

        LinearLayout llDateCancel = (LinearLayout) findViewById(R.id.ll_date_cancel);
        btnDateCancel = (Button) findViewById(R.id.btn_date_cancel);
        llDateCancel.setOnTouchListener(this);
        btnDateCancel.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.ib_add_hour) {
            ChangeHour(true);
        } else if (viewId == R.id.ib_minus_hour) {
            ChangeHour(false);
        }
    }

    private void ChangeHour(boolean hour_mode) {
        etHour.requestFocus();
        etHour.setSelection(etHour.getText().length());

        if(hour_mode){
            mHour++;

            if(mHour > 23){
                mHour = 0;
//                tbTime.setChecked(!tbTime.isChecked());
//                if(tbTime.isChecked()){
//                    tbTime.setText(getResources().getString(R.string.str_push_time_pm));
//                }else{
//                    tbTime.setText(getResources().getString(R.string.str_push_time_am));
//                }
            }
        }else{
            mHour--;

            if(mHour < 0){
                mHour = 23;
//                tbTime.setChecked(!tbTime.isChecked());
//                if(tbTime.isChecked()){
//                    tbTime.setText(getResources().getString(R.string.str_push_time_pm));
//                }else{
//                    tbTime.setText(getResources().getString(R.string.str_push_time_am));
//                }
            }
        }

//        strHour = String.valueOf(mHour);
//        if (strHour.length() < 2) {
//            strHour = "0" + strHour;
//        }
        strHour = String.format("%02d", mHour);
//        strHour = String.valueOf(mHour);
        etHour.setText(strHour);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_date_ok || v.getId() == R.id.btn_date_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnDateOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                intent.putExtra("Hour", Integer.parseInt(etHour.getText().toString()));
                setResult(Activity.RESULT_OK, intent);

                finish();
            }
            return true;
        }else if(v.getId() == R.id.ll_date_cancel || v.getId() == R.id.btn_date_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnDateCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) finish();
            return true;
        }

        return false;
    }
}
