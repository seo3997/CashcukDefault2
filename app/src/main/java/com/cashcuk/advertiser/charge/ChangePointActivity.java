package com.cashcuk.advertiser.charge;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.GetChargePoint;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.GetChargeAmount;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgChkPwdActivity;

/**
 * 포인트로 전환
 */
public class ChangePointActivity extends Activity {
    private LinearLayout llProgress;
    private TextView txtMyPoint; //나의 포인트
    private EditText etChangeMoney; //전홤금액 입력
    private TextView txtChangeMoney; //충전금
    private String strMyChangeMoney;

    private final int REQUEST_CHK_PWD = 999;
    private final int REQUEST_CHANGE_POINT = 888;
    private final int REQUEST_CHANGE_POINT_COMPLETE = 777;

    private final String STR_MODE_MY_POINT = "MyPoint"; //마이포인트
    private final String STR_MODE_CHANGE_AMOUNT = "ChangeAmount"; //충전금
    private final String STR_MODE_CHANGE = "Change"; //포인트로 전환
    private String strMode = STR_MODE_MY_POINT;

    private String strMyPoint = "";
    private String strChangeAmount = "";

    private final String STR_CHANGE_CHARGE = "point_chgamnt"; //전환금액
    private final int SEND_CHARGE = 2;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(ChangePointActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(strMode.equals(STR_MODE_MY_POINT)) {
                        strMyPoint = (String) msg.obj;
                        strMode = STR_MODE_CHANGE_AMOUNT;

                        txtMyPoint.setText(StaticDataInfo.makeStringComma(strMyPoint));
                        new GetChargeAmount(ChangePointActivity.this, handler);
                        return;
                    }else if(strMode.equals(STR_MODE_CHANGE_AMOUNT)){
                        strChangeAmount = (String) msg.obj;
                        strMode = "";

                        txtChangeMoney.setText(StaticDataInfo.makeStringComma(strChangeAmount));
                    }else if(strMode.equals(STR_MODE_CHANGE)){
                        Intent intent = new Intent(ChangePointActivity.this, DlgBtnActivity.class);
                        intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                        intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_change_money_complete), StaticDataInfo.makeStringComma(etChangeMoney.getText().toString())));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, REQUEST_CHANGE_POINT_COMPLETE);
                    }
                    break;
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_chagne_point_activity);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_change_point));
        ((View) findViewById(R.id.layout_my_point)).findViewById(R.id.ll_my_point_shadow).setVisibility(View.GONE);
        txtMyPoint = (TextView) ((View) findViewById(R.id.layout_my_point)).findViewById(R.id.txt_title_my_point);

        txtChangeMoney = (TextView) findViewById(R.id.txt_my_charge_money);
        etChangeMoney = (EditText) findViewById(R.id.et_change_money);
        etChangeMoney.addTextChangedListener(mTextWatcher);

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        ((Button) findViewById(R.id.btn_change)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeMoneyChk();
            }
        });

        MainTitleBar mainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mainTitleBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
                strMode = STR_MODE_MY_POINT;
                new GetChargePoint(ChangePointActivity.this, handler);
            }
        });

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        if(strMode.equals(STR_MODE_MY_POINT)) new GetChargePoint(ChangePointActivity.this, handler);

        WebView wbInfo = (WebView) findViewById(R.id.wv_info_msg);
        wbInfo.getSettings().setJavaScriptEnabled(true);
        wbInfo.setBackgroundColor(0);
        wbInfo.loadUrl(getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_link_chgpoint_info));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.fl_bg));
    }

    private void recycleView(View view) {
        if(view != null) {
            Drawable bg = view.getBackground();
            if(bg != null) {
                bg.setCallback(null);
                ((BitmapDrawable)bg).getBitmap().recycle();
                view.setBackgroundDrawable(null);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 전환 금액 체크
     */
    public void ChangeMoneyChk(){
        if(txtChangeMoney.getText().toString().contains(",")){
            strMyChangeMoney = txtChangeMoney.getText().toString().replace(",", "");
        }else{
            strMyChangeMoney = txtChangeMoney.getText().toString();
        }

        String strInputChangeMoney = etChangeMoney.getText().toString();
        long mInputChangeMoney = 0;

        if(strInputChangeMoney.trim().equals("") || strInputChangeMoney.equals("0")){
            Toast.makeText(ChangePointActivity.this, getResources().getString(R.string.str_change_money_input_err), Toast.LENGTH_SHORT).show();
            return;
        }else{
            if(strInputChangeMoney.contains(",")) {
                mInputChangeMoney =Long.parseLong(strInputChangeMoney.replace(",", ""));
            }else{
                mInputChangeMoney = Long.parseLong(strInputChangeMoney);
            }

            if(mInputChangeMoney>Long.parseLong(strMyChangeMoney)){
                Intent intent = new Intent(ChangePointActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_change_money_input_err1));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }else {
                Intent intent = new Intent(ChangePointActivity.this, DlgChkPwdActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_CHK_PWD);
            }
        }
    }

    private String strCommaResult="";
    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(strCommaResult)){     // StackOverflow를 막기위해,
                strCommaResult = StaticDataInfo.makeStringComma(s.toString().replaceAll(",", ""));   // 에딧텍스트의 값을 변환하여, result에 저장.
                etChangeMoney.setText(strCommaResult);    // 결과 텍스트 셋팅.
                etChangeMoney.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CHK_PWD){
                strMode = STR_MODE_CHANGE;
                new GetChargePoint(ChangePointActivity.this, etChangeMoney.getText().toString().replaceAll(",", ""), handler);
            }else if(requestCode == REQUEST_CHANGE_POINT) {
//                Intent intent = new Intent(ChangePoint.this, DlgBtnActivity.class);
//                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
//                intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_input_refund_money_chk_complete), StaticDataInfo.makeStringComma(etInputRefundMoney.getText().toString())));
//                startActivityForResult(intent, REQUEST_CHANGE_POINT_COMPLETE);
            }else if(requestCode==REQUEST_CHANGE_POINT_COMPLETE){
                finish();
            }
        }
    }
}
