package com.cashcuk.character.dlg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.SoftKeyboardLinear;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.ad.charactercall.send.SendDB;
import com.cashcuk.ad.charactercall.send.SendDBopenHelper;
import com.cashcuk.character.CharacterInfo;
import com.cashcuk.common.ImageLoader;

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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 캐릭터 설정 popup
 */
public class DlgCharaterSet extends Activity implements View.OnTouchListener {
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    private RelativeLayout rlIvCharProgress;
    private ProgressBar pbivChar;
    private LinearLayout llProgress;
    private Button btn1; //확인
    private Button btn2; //취소

    private EditText etCharSendMsg;

    private CharacterInfo mCharInfo;
    private Intent intentData=null;

    private String strCharIdx="";
    private String strSendMsgMode = "";
    private final String STR_REGI_SEND_MSG_NEW = "New";
    private final String STR_REGI_SEND_MSG_MODIFY = "Modify";

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(DlgCharaterSet.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    Toast.makeText(DlgCharaterSet.this, getResources().getString(R.string.str_set_character_ok), Toast.LENGTH_SHORT).show();
                    intentData.putExtra("SEND_MSG", etCharSendMsg.getText().toString());
                    setResult(RESULT_OK, intentData);
                    finish();
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dlg_character_set);
        CheckLoginService.mActivityList.add(this);

        intentData = getIntent();

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        final ImageView ivChar = (ImageView) findViewById(R.id.iv_char);
        etCharSendMsg = (EditText) findViewById(R.id.et_char_send_msg);

        pbivChar = (ProgressBar) findViewById(R.id.pb_iv_char);
        rlIvCharProgress = (RelativeLayout) findViewById(R.id.rl_iv_char);
        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        LinearLayout ll1 = (LinearLayout) findViewById(R.id.ll1);
        btn1 = (Button) findViewById(R.id.btn1);
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.ll2);
        btn2 = (Button) findViewById(R.id.btn2);
        ll1.setOnTouchListener(this);
        ll2.setOnTouchListener(this);
        btn1.setOnTouchListener(this);
        btn2.setOnTouchListener(this);

        Intent intent = getIntent();
        if(intent!=null){
            strSendMsgMode = intent.getStringExtra("SendMsgMode");
            if(strSendMsgMode.equals(STR_REGI_SEND_MSG_NEW)){
                strCharIdx = intent.getStringExtra("CharIdx");
                ((LinearLayout) findViewById(R.id.ll_btn_divider)).setVisibility(View.GONE);
                ll2.setVisibility(View.GONE);
                rlIvCharProgress.setVisibility(View.GONE);
            }else {
                mCharInfo = (CharacterInfo) intent.getSerializableExtra("CharInfo");
                strCharIdx =  mCharInfo.getStrIdx();
                String strImgUrl = mCharInfo.getStrImgUrl().replace("\\", "//");
                if(pbivChar!=null && !pbivChar.isShown()) pbivChar.setVisibility(View.VISIBLE);

                ImageLoader.loadImage(this, strImgUrl, ivChar, pbivChar);

                etCharSendMsg.setText(mCharInfo.getStrTxt());
                etCharSendMsg.setSelection(etCharSendMsg.getText().toString().length());
            }

        }

        InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
        SoftKeyboardLinear rootView = (SoftKeyboardLinear) findViewById(R.id.keyboad_view);
        rootView.addSoftKeyboardLsner(new SoftKeyboardLinear.SoftKeyboardLsner() {
            @Override
            public void onSoftKeyboardShow() {
                rlIvCharProgress.setVisibility(View.GONE);
            }

            @Override
            public void onSoftKeyboardHide() {
                rlIvCharProgress.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pbivChar!=null && pbivChar.isShown()) pbivChar.setVisibility(View.GONE);
    }

    private WebViewClient mWebviewClient = new WebViewClient(){
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll1 || v.getId() == R.id.btn1){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                requestCharSet();
            }
            return true;
        }else if(v.getId() == R.id.ll2 || v.getId() == R.id.btn2){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btn2.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) finish();
            return true;
        }

        return false;
    }

    private final int SEND_CHAR_IDX = 2;
    private final int SEND_CHAR_TXT = 3;
    private final String STR_CHAR_IDX = "char_idx";
    private final String STR_CHAR_TXT = "char_text";

    public void requestCharSet(){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_character_set);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_CHAR_IDX, strCharIdx);
        k_param.put(SEND_CHAR_TXT, etCharSendMsg.getText().toString());

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
                HttpParams httpParams = new BasicHttpParams(); //접속을 하기 위한 기존 환경설정
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); //웹 통신 프로토콜 버전 설정
                HttpClient client = new DefaultHttpClient(httpParams); //접속 기능 객체
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_CHAR_IDX, params[SEND_CHAR_IDX]));
                listParams.add(new BasicNameValuePair(STR_CHAR_TXT, params[SEND_CHAR_TXT]));

                //접속 제한시간 설정
                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                //응답 제한시간 설정
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
                updateDBSendMsg();
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    private void updateDBSendMsg(){
        SendDBopenHelper mOpenHelper = new SendDBopenHelper(this);
//        String strDataIdKind = "";
        String strDataIdIdx = "";
        try {
            mOpenHelper.open();

            TelephonyManager telManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            String phoneNum = telManager.getLine1Number();
            SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
            String strMyEmail = prefs.getString("LogIn_ID", "");

            Cursor cursorIdx = mOpenHelper.Search(SendDB.CHAR_IMG_IDX + " = '"+strCharIdx+"'");

            if (cursorIdx.moveToFirst()) {
                strDataIdIdx = cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CreateDB._ID));
                mOpenHelper.update(strDataIdIdx, cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_KIND)), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_IMG_PATH)), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_IMG_IDX)), etCharSendMsg.getText().toString(), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_TITLE_TXT)));
            } else {
                if(mCharInfo!=null) {
                    mOpenHelper.insert(getResources().getString(R.string.str_rep_img), strMyEmail, phoneNum, mCharInfo.getStrImgUrl(), strCharIdx, etCharSendMsg.getText().toString(), "얀녕하세요. 캐시쿡입니다.");
                }
            }
            cursorIdx.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(mOpenHelper!=null) mOpenHelper.close();
        }

        handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
    }
}
