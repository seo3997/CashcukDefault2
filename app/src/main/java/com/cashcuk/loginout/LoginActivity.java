package com.cashcuk.loginout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainActivity;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.membership.TermsAgreeActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 로그인 Activity
 */
public class LoginActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {
    private EditText etEmail;
    private EditText etPwd;

    private LinearLayout llProgress;

    /**
     * 결과 값 받는 handler
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StaticDataInfo.RESULT_NO_USER:
                case StaticDataInfo.RESULT_NO_DATA:
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.str_find_email_err), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_PWD_ERR:
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.str_pwd_err), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (llProgress != null && llProgress.isShown()) {
                        llProgress.setVisibility(View.GONE);
                    }
                }
            }, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CheckLoginService.mActivityList.add(this);

        ((Button) findViewById(R.id.btn_membership)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_login)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_find_id_pwd)).setOnClickListener(this);

        etEmail = (EditText) findViewById(R.id.et_email);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        etEmail.setOnFocusChangeListener(this);
        etPwd.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.btn_membership) {
            etEmail.setText("");
            etPwd.setText("");
            intent = new Intent(LoginActivity.this, TermsAgreeActivity.class);
        } else if (viewId == R.id.btn_find_id_pwd) {
            etEmail.setText("");
            etPwd.setText("");
            intent = new Intent(LoginActivity.this, FindEmailPwdActivity.class);
        } else if (viewId == R.id.btn_login) {
            ChkLoginCondition();
        }

        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(etEmail.getText().toString().trim().equals("")){
            etEmail.setHint(getResources().getString(R.string.str_email_en));
        }
        if(etPwd.getText().toString().trim().equals("")){
            etPwd.setHint(getResources().getString(R.string.str_pwd_en));
        }

        etEmail.requestFocus();
    }

    /**
     * 로그인 가능 상태 체크
     */
    public void ChkLoginCondition(){
        String strEmail = etEmail.getText().toString();
        String strPwd = etPwd.getText().toString();
        if(strEmail.trim().equals("")){
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.str_input_id_err), Toast.LENGTH_SHORT).show();
        }else if (ChkEmailType(strEmail)) {
            if(strPwd.trim().equals("")){
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.str_input_pwd_err), Toast.LENGTH_SHORT).show();
            }else{
                if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

                String mThisAppVersion = getAppVersion(this);
                new LoginInfo(LoginActivity.this, strEmail, strPwd, mThisAppVersion, handler);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
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

    /**
     * e-mail 형식이 맞는지 체크함.
     * @param email
     */
    public boolean ChkEmailType(String email){

        if(checkEmail(email)) {
            return true;
        } else {
            Toast.makeText(LoginActivity.this, R.string.str_email_type_err, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getId() == R.id.et_email && hasFocus){
            etEmail.setHint("");
            if(etPwd.getText().toString().trim().equals("")){
                etPwd.setHint(getResources().getString(R.string.str_pwd_en));
            }
        }else if(v.getId() == R.id.et_pwd && hasFocus){
            if(etEmail.getText().toString().trim().equals("")){
                etEmail.setHint(getResources().getString(R.string.str_email_en));
            }
            etPwd.setHint("");
        }
    }


    /**
     * @return {@code PackageManager}의 애플리케이션의 버전 코드.
     */
    private static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // 일어나지 않아야 합니다
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
