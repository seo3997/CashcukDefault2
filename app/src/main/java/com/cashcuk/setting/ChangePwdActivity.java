package com.cashcuk.setting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.loginout.LoginActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 비밀번호 변경
 */
public class ChangePwdActivity extends Activity {
    private EditText etNowPwd;
    private EditText etNewPwd;
    private EditText etChkNewPwd;
    private final int SEND_NEW_PWD = 2;
    private final String STR_NEW_PWD = "newpwd";

    private final int REQUEST_LOGIN_OK = 999;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(ChangePwdActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    Intent intent = new Intent(ChangePwdActivity.this, DlgBtnActivity.class);
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_change_pwd_succese));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, REQUEST_LOGIN_OK);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_change_pwd);
        CheckLoginService.mActivityList.add(this);

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_set_change_pwd));
        ((Button)findViewById(R.id.btn_change_pwd_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChkPwd();
            }
        });

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        etNowPwd = (EditText) findViewById(R.id.et_now_pwd);
        etNewPwd = (EditText) findViewById(R.id.et_new_pwd);
        etChkNewPwd = (EditText) findViewById(R.id.et_chk_new_pwd);
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


    public void ChkPwd() {
        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        String strPrefsPwd = prefs.getString("LogIn_PWD", "");

        String strNowPwd = etNowPwd.getText().toString();
        String strNewPwd = etNewPwd.getText().toString();
        String strChkNewPwd = etChkNewPwd.getText().toString();

        Intent intent = null;
        if (strNowPwd.trim().equals("")) {
//            Toast.makeText(ChangePwdActivity.this, getResources().getString(R.string.str_input_now_pwd_err), Toast.LENGTH_SHORT).show();
            intent = new Intent(ChangePwdActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_input_now_pwd_err));
        } else if (strNewPwd.trim().equals("") || strChkNewPwd.trim().equals("")) {
//            Toast.makeText(ChangePwdActivity.this, getResources().getString(R.string.str_input_new_pwd_err), Toast.LENGTH_SHORT).show();
            intent = new Intent(ChangePwdActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_input_new_pwd_err));
        } else if (!strNowPwd.equals(strPrefsPwd)) {
            intent = new Intent(ChangePwdActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_now_pwd_err));
        } else if (!strNewPwd.equals(strChkNewPwd)) {
            intent = new Intent(ChangePwdActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_chk_new_pwd_err));
        } else if (!checkPattern(strNewPwd)) {
            intent = new Intent(ChangePwdActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_new_pwd_type_err));
        } else if (strNowPwd.equals(strNewPwd)) {
            intent = new Intent(ChangePwdActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_now_new_pwd_chk));
        }else{
            String strPrefsIdx = prefs.getString("Idx", "");
            String strPrefsType = prefs.getString("LogIn_Type", "U");

            final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_member_pd);
            SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
            final String token = pref.getString(getResources().getString(R.string.str_token), "");

            HashMap<Integer, String> k_param = new HashMap<Integer, String>();
            k_param.put(StaticDataInfo.SEND_URL, url);
            k_param.put(StaticDataInfo.SEND_TOKEN, token);
            k_param.put(SEND_NEW_PWD, strChkNewPwd);

            String[] strTask = new String[k_param.size()];
            for (int i = 0; i < strTask.length; i++) {
                strTask[i] = k_param.get(i);
            }

            new ChangePwdTask().execute(strTask);
        }

        if(intent!=null){
            intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**
     * 비밀번호 정규식 체크
     * @param strPattern : 값
     * @return true: 형식에 맞음, false: 형식에 맞지 않음
     */
    private boolean checkPattern(String strPattern)
    {
        Pattern p = null;

        p = Pattern.compile("([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~])|([!,@,#,$,%,^,&,*,?,_,~].*[a-zA-Z0-9])");
        Matcher m = p.matcher(strPattern);

            if (m.find()) {
                return true;
            }

        return false;
    }

    private class ChangePwdTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);
                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_NEW_PWD, params[SEND_NEW_PWD]));

                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(listParams, HTTP.UTF_8);
                post.setEntity(ent);
                HttpResponse responsePOST = client.execute(post);
                HttpEntity resEntity = responsePOST.getEntity();
                if (resEntity != null) {
                    retMsg = EntityUtils.toString(resEntity);
                }
            } catch (Exception e) {
                retMsg = e.toString();
            }
            return retMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_LOGIN_OK){
                CheckLoginService.Close_All();
                Intent intent = new Intent(ChangePwdActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }
}
