package com.cashcuk.setting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.common.FindPassword;
import com.cashcuk.common.MobileAuthentication;
import com.cashcuk.common.RequestPhoneCodeNum;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgFirstPhoneNumActivity;
import com.cashcuk.loginout.LoginActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 비밀번호 찾기
 */
public class FindPwdActivity extends Activity implements View.OnClickListener {
    private EditText etEmail;
    private static TextView txtFirstNum;
    private TextView etMiddleNum;
    private TextView etLastNum;

    private final int REQUEST_CODE_FIRST_NUM = 666; //폰 번호 첫번 째 3자리
    private final int REQUEST_CODE_EMAIL_PWD = 888; //비밀번호 발송
    private String mPhoneNum="";
    private String mArrPhoneNum[]= new String[3];

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent i = null;
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(FindPwdActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_USER: //가입되지 않은 이메일
                    Toast.makeText(FindPwdActivity.this, getResources().getString(R.string.str_no_data), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:

                    Log.d("temp","(String)msg.obj["+(String)msg.obj+"]");
                    String sMsg=(String)msg.obj;
                    i = new Intent(FindPwdActivity.this, DlgBtnActivity.class);

                    String sAlertMessage=getResources().getString(R.string.str_init_pwd,sMsg);

                    i.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    i.putExtra("BtnDlgMsg",sAlertMessage);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(i, REQUEST_CODE_EMAIL_PWD);
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd);
        CheckLoginService.mActivityList.add(this);

        mPhoneNum=getIntent().getStringExtra("PhoneNum");

        mArrPhoneNum[0]="";
        mArrPhoneNum[1]="";
        mArrPhoneNum[2]="";

        Log.d("temp","***************mPhoneNum["+mPhoneNum+"]************");

        mPhoneNum="010-7322-4829";



        if(mPhoneNum!=null){
            mArrPhoneNum=mPhoneNum.split("-");
        }


        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(getResources().getString(R.string.str_find_pwd_title));

        ((Button) findViewById(R.id.btn_find_pwd_ok)).setOnClickListener(this);
        etEmail = (EditText) findViewById(R.id.et_email);
        txtFirstNum = (TextView) findViewById(R.id.txt_first_num);
        etMiddleNum = (TextView) findViewById(R.id.et_middle_num);
        etLastNum = (TextView) findViewById(R.id.et_last_num);

        txtFirstNum.setText(mArrPhoneNum[0]);
        etMiddleNum.setText(mArrPhoneNum[1]);
        etLastNum.setText(mArrPhoneNum[2]);

        txtFirstNum.setOnClickListener(this);
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
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_find_pwd_ok) {
            ChkInputData();
        }
    }

    /**
     * 서버로 전송하는 값 (비밀번호 찾기)
     */
    public void ChkInputData(){
        String strEmail = etEmail.getText().toString();
        String strPhoneNum = txtFirstNum.getText().toString()+"-"+etMiddleNum.getText().toString()+"-"+etLastNum.getText().toString();

        if (etMiddleNum.getText().toString().trim().equals("") || etLastNum.getText().toString().trim().equals("")) {
            Toast.makeText(FindPwdActivity.this, getResources().getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show();
            return;
        }

        new FindPassword(FindPwdActivity.this, strPhoneNum, strEmail, handler);
    }

    /**
     * 이메일 확인
     */
    public boolean ChkInputEmail(){
        String strEmail = etEmail.getText().toString();

        if(strEmail.trim().equals("")) {
            Toast.makeText(FindPwdActivity.this, getResources().getString(R.string.str_input_email_err), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if(ChkEmailType(strEmail)){
                return true;
            }
        }
        return false;
    }

    /**
     * e-mail 형식이 맞는지 체크함.
     * @param email
     */
    public boolean ChkEmailType(String email){
        if(checkEmail(email)) {
            return true;
        } else {
            Toast.makeText(FindPwdActivity.this, R.string.str_email_type_err, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 이메일 정규식 체크
     * @param email
     * @return true : 이메일 형식에 맞음, false : 이메일 형식에 맞지 않음
     */
    private boolean checkEmail(String email)
    {
        String mail = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(mail);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FIRST_NUM:
                    txtFirstNum.setText(data.getStringExtra("FirstPhoneNum"));
                    break;
//                case REQUEST_CODE_FIND_EMAIL:
                case REQUEST_CODE_EMAIL_PWD:
//                    finish();
                    CheckLoginService.Close_All();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
            }
        }
    }
}
