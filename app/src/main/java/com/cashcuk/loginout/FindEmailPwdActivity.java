package com.cashcuk.loginout;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.TitleBar;
import com.cashcuk.membership.MobileAuthenticationWebActivity;

/**
 * ID/Pwd 찾기
 */
public class FindEmailPwdActivity extends FragmentActivity implements View.OnClickListener {
    //이메일 찾기[[
    private TextView txtFindEmail;
    private LinearLayout llFindEmailUnder;
    //이메일 찾기]]

    //비밀번호 찾기[[
    private TextView txtFindPwd;
    private LinearLayout llFindPwdUnder;
    //비밀번호 찾기]]

    private TitleBar mTitle;
    private TextView txtInfo;

    private final boolean BOOL_FIND_MODE_EMAIL = true;
    private final boolean BOOL_FIND_MODE_PWD = false;

    private final int REQUEST_CODE_FIRST_NUM = 999;
    private final int REQUEST_CODE_FIND_EMAIL = 777;
    private final int REQUEST_CODE_EMAIL_PWD = 888; //비밀번호 이메일 발송

    private final int REQUEST_PHONE_NUM = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_find_id_pwd);
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        MainTitleBar mMainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_refresh)).setVisibility(View.GONE);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_home)).setVisibility(View.GONE);
        mTitle = (TitleBar)findViewById(R.id.title_bar);

        //이메일 찾기[[
        mTitle.setTitle(getResources().getString(R.string.str_find_email_title));
        txtFindEmail = (TextView) findViewById(R.id.txt_find_email);
        txtFindEmail.setOnClickListener(this);

        llFindEmailUnder = (LinearLayout) findViewById(R.id.ll_find_email_under);
        //이메일 찾기]]

        //비밀번호 찾기[[
        txtFindPwd = (TextView) findViewById(R.id.txt_find_pwd);
        txtFindPwd.setOnClickListener(this);

        llFindPwdUnder = (LinearLayout) findViewById(R.id.ll_find_pwd_under);
        //비밀번호 찾기]]

        txtInfo = (TextView) findViewById(R.id.txt_info);
        txtInfo.setText(getResources().getString(R.string.str_find_email_info));

        switchFragment(BOOL_FIND_MODE_EMAIL,"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.ll_bg));
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

    public void switchFragment(boolean findMode,String pPHoneNum) {
        Fragment fr;

        if (findMode) {
            fr = new FrFindEmail();
        } else {
            Bundle args =new Bundle();
            args.putString("PhoneNum",pPHoneNum);
            fr = new FrFindPwd();
            fr.setArguments(args);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.ll_find_fr, fr);
        fragmentTransaction.commit();
    }

    public void setFindPwdInit(String phoneNum){
        mTitle.setTitle(getResources().getString(R.string.str_find_pwd_title));
        txtInfo.setText(getResources().getString(R.string.str_find_pwd_info));
        llFindEmailUnder.setVisibility(View.GONE);
        llFindPwdUnder.setVisibility(View.VISIBLE);
        txtFindEmail.setBackgroundColor(getResources().getColor(R.color.color_white));
        txtFindPwd.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
        switchFragment(BOOL_FIND_MODE_PWD,phoneNum);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.txt_find_email) {
            mTitle.setTitle(getResources().getString(R.string.str_find_email_title));
            txtInfo.setText(getResources().getString(R.string.str_find_email_info));
            llFindEmailUnder.setVisibility(View.VISIBLE);
            llFindPwdUnder.setVisibility(View.GONE);
            txtFindEmail.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
            txtFindPwd.setBackgroundColor(getResources().getColor(R.color.color_white));
            switchFragment(BOOL_FIND_MODE_EMAIL,"");
        } else if (viewId == R.id.txt_find_pwd) {
            //휴대폰 본인인증
            Intent intent = new Intent(this, MobileAuthenticationWebActivity.class);
            intent.putExtra("ReturnCd",3);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_PHONE_NUM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FIRST_NUM:
                    FrFindEmail.getActivityResult(data.getStringExtra("FirstPhoneNum"));
                    break;
                case REQUEST_PHONE_NUM:
                    String phoneNum=data.getStringExtra("PHONE_NUM");
                    //Log.d("temp","data.getStringExtra(\"PHONE_NUM\")"+data.getStringExtra("PHONE_NUM")+"]");
                    setFindPwdInit(phoneNum);
                    break;
                case REQUEST_CODE_FIND_EMAIL:
                case REQUEST_CODE_EMAIL_PWD:
                    finish();
                    break;
            }
        }
    }
}
