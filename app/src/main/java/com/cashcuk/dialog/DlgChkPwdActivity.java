package com.cashcuk.dialog;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

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

/**
 * 비밀번화 확인 dialog
 */
public class DlgChkPwdActivity extends Activity implements View.OnTouchListener {
    private EditText etInputPwd;
    private String strPwd;
    private Button btnChkPwdCancel;
    private Button btnChkPwdOk;

    private final int SEND_PWD = 2;

    private final String STR_USER_IDX = "idx";
    private final String STR_PWD = "pwd";
    private final String STR_LOGIN_TYPE = "type";

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(DlgChkPwdActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_PWD_ERR:
                    Toast.makeText(DlgChkPwdActivity.this, getResources().getString(R.string.str_pwd_err), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dlg_chk_pwd);
        CheckLoginService.mActivityList.add(this);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        etInputPwd = (EditText) findViewById(R.id.et_input);

        LinearLayout llChkPwdCancel = (LinearLayout) findViewById(R.id.ll_chk_pwd_cancel);
        LinearLayout llChkPwdOk = (LinearLayout) findViewById(R.id.ll_chk_pwd_ok);
        btnChkPwdCancel = (Button) findViewById(R.id.btn_chk_pwd_cancel);
        btnChkPwdOk = (Button) findViewById(R.id.btn_chk_pwd_ok);
        llChkPwdCancel.setOnTouchListener(this);
        llChkPwdOk.setOnTouchListener(this);
        btnChkPwdCancel.setOnTouchListener(this);
        btnChkPwdOk.setOnTouchListener(this);

//        ((Button) findViewById(R.id.btn_chk_pwd_cancel)).setOnClickListener(this);

        ((TextView) findViewById(R.id.txt_title)).setText(getResources().getString(R.string.str_chk_hint));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_chk_pwd_cancel || v.getId() == R.id.btn_chk_pwd_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnChkPwdCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP)finish();
            return true;
        }else if(v.getId() == R.id.ll_chk_pwd_ok || v.getId() == R.id.btn_chk_pwd_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnChkPwdOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                btnChkPwdOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                ChkInputPwd();
            }
            return true;
        }
        return false;
    }

    /**
     * 비밀번호 확인
     */
    public void ChkInputPwd(){
        strPwd = etInputPwd.getText().toString();

        if(strPwd.trim().equals("")){
            Toast.makeText(DlgChkPwdActivity.this, getResources().getString(R.string.str_input_pwd_err), Toast.LENGTH_SHORT).show();
            return;
        }
        final String url = getResources().getString(R.string.str_new_url)+ getResources().getString(R.string.str_member_pd);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PWD, strPwd);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class DataTask extends AsyncTask<String, Void, String> {
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
                listParams.add(new BasicNameValuePair(STR_PWD, params[SEND_PWD]));

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
            Message msg = new Message();

            if(result.equals("") || result.startsWith("<")){
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
                handler.sendMessage(msg);
                return;
            }

            int mPwdResult = Integer.parseInt(result);
            if(mPwdResult == StaticDataInfo.RESULT_CODE_200 || mPwdResult == StaticDataInfo.RESULT_PWD_ERR) {
                msg.what = mPwdResult;
            }else{
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
            }
            handler.sendMessage(msg);
        }
    }
}
