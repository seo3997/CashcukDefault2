package com.cashcuk.setting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgFirstPhoneNumActivity;

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
 * 휴대폰 인증 문자 - 휴대폰 번호 수정 시
 */
public class MobileAuthenticationActivity extends Activity implements View.OnClickListener {
    private TextView txtFirstNum;
    private EditText etMiddleNum;
    private EditText etLastNum;
    private EditText etAuthenticationNum; //인증번호
    private LinearLayout llAuthentication; //인증번호 layout (gone/visible)
    private TextView txtRemainingTime; //남은시간
    private CountDownTimer mCountDwon = null;
    private boolean isRenainingTime = false;

    private final int REQUEST_CODE_FIRST_NUM = 999;

    private final int SEND_PHONE_NUM = 1;
    private final int SEND_MAIL = 2;
    private final int SEND_CODE = 3;

    private final String STR_PHONE_NUM = "hp";
    private final String STR_MAIL = "mail";
    private final String STR_CODE = "code";

    private String strPhoneNum;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(MobileAuthenticationActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_AUTHORIZATION_CODE_ERR:
                    intent = new Intent(MobileAuthenticationActivity.this, DlgBtnActivity.class);
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_find_pwd_authorization_code_err));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    intent.putExtra("PHONE_NUM", strPhoneNum);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case StaticDataInfo.RESULT_NO_USER: //가입되지 않은 이메일
                    Toast.makeText(MobileAuthenticationActivity.this, getResources().getString(R.string.str_no_data), Toast.LENGTH_SHORT).show();
                    etAuthenticationNum.setText("");
                    if(llAuthentication.isShown()) llAuthentication.setVisibility(View.GONE);
            }
        }
    };

    Intent intent = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_authentication);
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_mobile_authentication_title));

        intent = getIntent();

        txtFirstNum = (TextView) findViewById(R.id.txt_first_num);
        txtFirstNum.setOnClickListener(this);
        etMiddleNum = (EditText) findViewById(R.id.et_middle_num);
        etLastNum = (EditText) findViewById(R.id.et_last_num);

        etAuthenticationNum = (EditText) findViewById(R.id.et_authentication_num);
        ((Button) findViewById(R.id.btn_call_authentication_num)).setOnClickListener(this); //인증번호 요청 버튼
        ((Button) findViewById(R.id.btn_authentication_ok)).setOnClickListener(this); //확인

        llAuthentication = (LinearLayout) findViewById(R.id.ll_authentication);
        txtRemainingTime = (TextView) findViewById(R.id.txt_remaining_time);
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
        if (viewId == R.id.btn_call_authentication_num) {
            //인증번호 요청
            etAuthenticationNum.setText("");

            if(etMiddleNum.getText().toString().trim().equals("") || etLastNum.getText().toString().trim().equals("")){
                Toast.makeText(MobileAuthenticationActivity.this, getResources().getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show();
            }else {
                llAuthentication.setVisibility(View.VISIBLE);

                getSmsMsg();

                if (mCountDwon != null) mCountDwon.cancel();
                isRenainingTime = true;
                mCountDwon = new CountDownTimer(180000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int days = (int) ((millisUntilFinished / 1000) / 86400);
                        int hours = (int) (((millisUntilFinished / 1000) - (days * 86400)) / 3600);
                        int mins = (int) (((millisUntilFinished / 1000) - ((days * 86400) + (hours * 3600))) / 60);
                        int secs = (int) ((millisUntilFinished / 1000) % 60);

                        String strMins = String.format("%02d", mins);
                        String strSecs = String.format("%02d", secs);
                        txtRemainingTime.setText(strMins + ":" + strSecs);
                    }

                    @Override
                    public void onFinish() {
                        isRenainingTime = false;
                        txtRemainingTime.setText("00:00");
                    }
                }.start();

                ChkInputData(false);
            }
        } else if (viewId == R.id.btn_authentication_ok) {
            //확인
            ChkInputData(true);
        } else if (viewId == R.id.txt_first_num) {
            Intent intent = new Intent(MobileAuthenticationActivity.this, DlgFirstPhoneNumActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_CODE_FIRST_NUM);
        }
    }

    /**
     * 인증번호 문자 읽어와서 d/p
     */
    private BroadcastReceiver broadcastReceiver;
    public void getSmsMsg(){
        IntentFilter f = new IntentFilter();
        f.addAction(getResources().getString(R.string.str_action_sms_msg));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action != null) {
                    if (action.equals(getResources().getString(R.string.str_action_sms_msg))) {
                        etAuthenticationNum.setText(intent.getExtras().getString("SmsMsg"));
                        etAuthenticationNum.requestFocus();
                        etAuthenticationNum.setSelection(etAuthenticationNum.getText().length());
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(f));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FIRST_NUM:
                    txtFirstNum.setText(data.getStringExtra("FirstPhoneNum"));
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCountDwon!=null) mCountDwon.cancel();
        recycleView(findViewById(R.id.ll_bg));
    }

    /**
     * 서버로 전송하는 값
     */
    private boolean isCode = false;
    public void ChkInputData(boolean code){
        isCode = code;
        String strAuthorizationCode = etAuthenticationNum.getText().toString();
        strPhoneNum = txtFirstNum.getText().toString() + "-" + etMiddleNum.getText().toString() + "-" + etLastNum.getText().toString();

        if (etMiddleNum.getText().toString().trim().equals("") || etLastNum.getText().toString().trim().equals("")) {
            Toast.makeText(MobileAuthenticationActivity.this, getResources().getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show();
            return;
        } else if (strAuthorizationCode.trim().equals("") && isCode) {
            Toast.makeText(MobileAuthenticationActivity.this, getResources().getString(R.string.str_empty_authentication_num), Toast.LENGTH_SHORT).show();
            return;
        } else if(!isRenainingTime && !isCode){
            Toast.makeText(MobileAuthenticationActivity.this, getResources().getString(R.string.str_chk_authentication_num), Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        String strEmail = prefs.getString("LogIn_ID","");

        final String url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_member_change_phone);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(SEND_PHONE_NUM, strPhoneNum);
        k_param.put(SEND_MAIL, strEmail);
        k_param.put(SEND_CODE, strAuthorizationCode);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new ChkPhoneTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class ChkPhoneTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);
                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(STR_PHONE_NUM, params[SEND_PHONE_NUM]));
                listParams.add(new BasicNameValuePair(STR_MAIL, params[SEND_MAIL]));
                if(isCode) {
                    listParams.add(new BasicNameValuePair(STR_CODE, params[SEND_CODE]));
                }

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
            msg.what = -1;
            if(result.equals("")){
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
                handler.sendMessage(msg);
                return;
            }

            if(isCode) {
                switch (Integer.parseInt(result)) {
                    case StaticDataInfo.RESULT_CODE_AUTHORIZATION_CODE_ERR:
                        msg.what = StaticDataInfo.RESULT_CODE_AUTHORIZATION_CODE_ERR;
                        break;
                    case StaticDataInfo.RESULT_CODE_200:
                        msg.what = StaticDataInfo.RESULT_CODE_200;
                        break;
                    default:
                        msg.what = StaticDataInfo.RESULT_CODE_ERR;
                        break;
                }
            }

            if(handler!=null && msg!=null && msg.what!=-1) {
                handler.sendMessage(msg);
            }
        }
    }
}
