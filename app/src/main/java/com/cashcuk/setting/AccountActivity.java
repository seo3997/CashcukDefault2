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
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.AdvertiserRegistrationActivity;
import com.cashcuk.advertiser.main.AdvertiserNewMainActivity;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.membership.MobileAuthenticationWebActivity;

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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 계정
 */
public class AccountActivity extends Activity implements View.OnTouchListener {
    private ImageButton ibSetAccount; //계정설정
    private ImageButton ibChangePwd; //비밀번호 변경
    private ImageButton ibFindPwd; //비밀번호 찾기
    private ImageButton ibOutMembership; //회원탈퇴

    //광고주 설정
    private TextView txtAdvertiser;
    private ImageButton ibChangeAdvertiser;
    //바로광고
    private LinearLayout llMakeAD;
    private ImageButton ibMakeAD;

    private int mDlgRequest = -1;

    private final String STR_ADVERTISER_STATUS = "biz_status"; //광고주 등록 상태 값
    private final String STR_REJECT_CUZ = "biz_rejbcoz"; //광고주 승인거부 사유

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;

    private LinearLayout llProgress;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                        Toast.makeText(AccountActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_ADVERTISER: //광고주
                    llMakeAD.setVisibility(View.VISIBLE);
                    txtAdvertiser.setText(getResources().getString(R.string.str_set_change_advertiser));
                    mDlgRequest = StaticDataInfo.RESULT_CODE_ADVERTISER;
                    break;
                case StaticDataInfo.RESULT_CODE_NO_ADVERTISER: //비광고주
                    llMakeAD.setVisibility(View.GONE);
                    txtAdvertiser.setText(getResources().getString(R.string.str_registration_advertiser));
                    mDlgRequest = StaticDataInfo.RESULT_CODE_NO_ADVERTISER;
                    break;
                case StaticDataInfo.RESULT_CODE_WAIT_ADVERTISER: //승인대기
                    llMakeAD.setVisibility(View.GONE);
                    txtAdvertiser.setText(getResources().getString(R.string.str_registration_advertiser));
                    mDlgRequest = StaticDataInfo.RESULT_CODE_WAIT_ADVERTISER;
                    break;
                case StaticDataInfo.RESULT_CODE_REJECT_ADVERTISER: //승인거부
                    llMakeAD.setVisibility(View.GONE);
                    txtAdvertiser.setText(getResources().getString(R.string.str_registration_advertiser));
                        mDlgRequest = StaticDataInfo.RESULT_CODE_REJECT_ADVERTISER;
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
        setContentView(R.layout.activity_setting_account);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ImageButton ibRefesh = (ImageButton)((MainTitleBar) findViewById(R.id.main_title_bar)).findViewById(R.id.ib_refresh);
        ibRefesh.setVisibility(View.VISIBLE);
        ibRefesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advertiserRegiChk();
            }
        });

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_setting_account));
        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        Intent intent = getIntent();
        ((TextView) findViewById(R.id.txt_email_id)).setText(intent.getStringExtra("Account"));

        //계정설정
        ((RelativeLayout) findViewById(R.id.rl_set_account)).setOnTouchListener(this);
        ibSetAccount = (ImageButton) findViewById(R.id.ib_set_account);
        ibSetAccount.setOnTouchListener(this);

        //비밀번호 변경
        ((RelativeLayout) findViewById(R.id.rl_change_pwd)).setOnTouchListener(this);
        ibChangePwd = (ImageButton) findViewById(R.id.ib_change_pwd);
        ibChangePwd.setOnTouchListener(this);

        //비밀번호 찾기
        ((RelativeLayout) findViewById(R.id.rl_find_pwd)).setOnTouchListener(this);
        ibFindPwd = (ImageButton) findViewById(R.id.ib_find_pwd);
        ibFindPwd.setOnTouchListener(this);

        //회원탈퇴
        ((RelativeLayout) findViewById(R.id.rl_out_membership)).setOnTouchListener(this);
        ((ImageButton) findViewById(R.id.ib_out_membership)).setOnTouchListener(this);
        ibOutMembership = (ImageButton) findViewById(R.id.ib_out_membership);
        ibOutMembership.setOnTouchListener(this);

        //광고주 등록/설정
        ((RelativeLayout) findViewById(R.id.rl_change_advertiser)).setOnTouchListener(this);
        txtAdvertiser = (TextView) findViewById(R.id.txt_advertiser);
        ibChangeAdvertiser = (ImageButton) findViewById(R.id.ib_change_advertiser);
        ibChangeAdvertiser.setOnTouchListener(this);

        //바로광고
        llMakeAD = (LinearLayout) findViewById(R.id.ll_make_ad);
        ((RelativeLayout) findViewById(R.id.rl_make_ad)).setOnTouchListener(this);
        ibMakeAD = (ImageButton) findViewById(R.id.ib_make_ad);
        ibMakeAD.setOnTouchListener(this);
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
    protected void onResume() {
        super.onResume();

        advertiserRegiChk();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 광고주 등록 여부
     */
    public void advertiserRegiChk(){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_member_bizstatus);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);

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

            if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                resultStatusAdvertiser(result);
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    private String strAdvertiserStatus;
    private String strRejectMsg;
    /**
     * 광고주 등록 여부 상태
     */
    public void resultStatusAdvertiser(String result){
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(result));

            int eventType = parser.getEventType();
            int k_data_num = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(STR_ADVERTISER_STATUS)) {
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_REJECT_CUZ)) {
                            k_data_num = PARSER_NUM_1;
                        }else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    strAdvertiserStatus = parser.getText();
                                    break;
                                case PARSER_NUM_1:
                                    strRejectMsg = parser.getText();
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
            handler.sendEmptyMessage(Integer.valueOf(strAdvertiserStatus));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Intent intent = null;
        if(v.getId() == R.id.rl_make_ad || v.getId() == R.id.ib_make_ad){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibMakeAD.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibMakeAD.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));
                intent = new Intent(AccountActivity.this, AdvertiserNewMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        }else if(v.getId() == R.id.rl_change_advertiser || v.getId() == R.id.ib_change_advertiser){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibChangeAdvertiser.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibChangeAdvertiser.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));
                advertiserMode(mDlgRequest);
            }
            return true;
        }else if(v.getId() == R.id.rl_set_account || v.getId() == R.id.ib_set_account){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibSetAccount.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibSetAccount.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));
                intent = new Intent(AccountActivity.this, AccountSetActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        }else if(v.getId() == R.id.rl_change_pwd || v.getId() == R.id.ib_change_pwd){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibChangePwd.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibChangePwd.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));
                intent = new Intent(AccountActivity.this, ChangePwdActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        }else if(v.getId() == R.id.rl_find_pwd || v.getId() == R.id.ib_find_pwd){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibFindPwd.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibFindPwd.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));

                /*  테스트용
                intent = new Intent(AccountActivity.this, FindPwdActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                */

                //패스워드 개발후 아래것로 적용 휴대폰 본인인증적용
                intent = new Intent(this, MobileAuthenticationWebActivity.class);
                intent.putExtra("ReturnCd",2);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
            return true;
        }else if(v.getId() == R.id.rl_out_membership || v.getId() == R.id.ib_out_membership){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibOutMembership.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibOutMembership.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));
                intent = new Intent(AccountActivity.this, UnregisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        }
        return false;
    }

    /**
     * 광고주 모드에 따른 동작
     * @param mode
     */
    public void advertiserMode(int mode){
        Intent intent = new Intent(AccountActivity.this, DlgBtnActivity.class);

        switch(mode){
            case StaticDataInfo.RESULT_CODE_ADVERTISER: //광고주
                intent = new Intent(AccountActivity.this, AdvertiserRegistrationActivity.class);
                intent.putExtra("PageMode", "E");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            case StaticDataInfo.RESULT_CODE_NO_ADVERTISER: //비광고주
                intent.putExtra("DlgMode", "Two");
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_no_advertiser));
                break;
            case StaticDataInfo.RESULT_CODE_WAIT_ADVERTISER: //승인대기
                intent.putExtra("BtnDlgOneText", getResources().getString(R.string.str_close));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_wait_advertiser));
                break;
            case StaticDataInfo.RESULT_CODE_REJECT_ADVERTISER: //승인거부
                if(!strRejectMsg.trim().equals("")) {
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_reject_cus_title));
                    intent.putExtra("BtnDlgCancelText", getResources().getString(R.string.str_cancel));
                    intent.putExtra("BtnDlgOneText", getResources().getString(R.string.str_redemand_advertiser));
                    intent.putExtra("DlgMode", "Two");
                    intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_reject_advertiser), strRejectMsg));
                }else{
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
                }
                break;
        }

        if(intent != null && mDlgRequest!=-1){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, mDlgRequest);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Intent intent = null;
            switch (requestCode){
                case StaticDataInfo.RESULT_CODE_NO_ADVERTISER: //비광고주
                    intent = new Intent(AccountActivity.this, AdvertiserRegistrationActivity.class);
                    intent.putExtra("PageMode", "A");
                    break;
                case StaticDataInfo.RESULT_CODE_REJECT_ADVERTISER: //승인거부
                    intent = new Intent(AccountActivity.this, AdvertiserRegistrationActivity.class);
                    intent.putExtra("PageMode", "R");
                    break;
            }

            if(intent!=null){
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }
}
