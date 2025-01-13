package com.cashcuk.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.common.CommCode;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.loginout.LoginActivity;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;

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
 * 회원탈퇴
 */
public class UnregisterActivity extends Activity {
    private LinearLayout llCause;
    private LayoutInflater inflater;

    private RadioButton rbEtc; //기타 버튼
    private EditText etEtc; //기타 사유 edit
    private LinearLayout llEtc;
    private int mLayoutCnt; //서버에서 받은 탈퇴사유 개수

    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;

    private ArrayList<TxtListDataInfo> arrData1; //1차 분류 data
    private int mClickId; //클릭 된 사유
    private String strCauseIdx; //사유 idx
    private final int SEND_CAUSE = 2; //사유 (기타 선택 시 0값 전달)
    private final int SEND_CAUSE_ETC_TXT = 3; //기타 선택 시 사유 전달
    private final String STR_CAUSE = "ulCd";
    private final String STR_CAUSE_ETC= "ulCd_etc";

    private final int REQUEST_CODE = 999;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(UnregisterActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if (msg.arg1 == PARSER_NUM_1 && ((ArrayList<TxtListDataInfo>) msg.obj).size()>0) {
                        arrData1 = new ArrayList<TxtListDataInfo>();
                        arrData1.addAll((ArrayList<TxtListDataInfo>) msg.obj);

                        new CommCode(UnregisterActivity.this, StaticDataInfo.COMMON_CODE_TYPE_JD, PARSER_NUM_2, arrData1.get(0).getStrIdx(), handler);
                    }else if (msg.arg1 == PARSER_NUM_2 && ((ArrayList<TxtListDataInfo>) msg.obj).size()>0) {
                        arrData1 = new ArrayList<TxtListDataInfo>();
                        arrData1.addAll((ArrayList<TxtListDataInfo>) msg.obj);

                        DisplayUnregisterList();
                    }else if(msg.arg1 == PARSER_NUM_3){
                        Toast.makeText(UnregisterActivity.this, getResources().getString(R.string.str_unregister), Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("LogIn_ID", "");
                        editor.putString("LogIn_PWD", "");
                        editor.commit();

                        SharedPreferences prefToken = getSharedPreferences("TokenInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editorToken = prefToken.edit();
                        editorToken.putString("token", "");
                        editorToken.commit();
                        CheckLoginService.Close_All();

                        Intent intent = new Intent(UnregisterActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unregister);
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_set_out_membership));
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        llCause = (LinearLayout) findViewById(R.id.ll_cause);
        rbEtc = (RadioButton) findViewById(R.id.rb_etc);
        etEtc = (EditText) findViewById(R.id.et_etc);
        etEtc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                RadioButton rbCause;

                if(hasFocus){
                    rbEtc.setChecked(true);

                    for(int i=0; i<mLayoutCnt; i++){
                        llCause = (LinearLayout) findViewById(i);
                        rbCause = (RadioButton) llCause.findViewById(R.id.rb_cause);
                        if(rbCause.isChecked()) rbCause.setChecked(false);
                    }
                    //키보드 보이게 하는 부분
                    imm.showSoftInput(etEtc, InputMethodManager.SHOW_FORCED);

                    strCauseIdx = "0";
                } else {
                    imm.hideSoftInputFromWindow(etEtc.getWindowToken(), 0);
                }
            }
        });
//        etEtc.setOnClickListener(mClick);
        llEtc = (LinearLayout) findViewById(R.id.ll_etc);
        llEtc.setOnClickListener(mClick);

        ((Button)findViewById(R.id.btn_unregister)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Unregister();
                Intent intent = new Intent(UnregisterActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_dlg_chk_unregister));
                intent.putExtra("DlgMode", "Two");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        new CommCode(UnregisterActivity.this, StaticDataInfo.COMMON_CODE_TYPE_JD, PARSER_NUM_1, "", handler);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_CODE){
                Unregister();
            }
        }
    }

    /**
     * 탈퇴사유 d/p
     */
    public void DisplayUnregisterList(){
        mLayoutCnt = arrData1.size();
        for(int i=0; i<arrData1.size(); i++) {
            LinearLayout llADSendInfoView = (LinearLayout) inflater.inflate(R.layout.unregister_item, null);
            llADSendInfoView.setId(i);
            llADSendInfoView.setTag((int) i);
            llADSendInfoView.setOnClickListener(mClick);

            RadioButton rbCause = (RadioButton) llADSendInfoView.findViewById(R.id.rb_cause);
//            rbCause.setId(i);
            if(i==0){
                strCauseIdx = arrData1.get(i).getStrIdx();
                rbCause.setChecked(true);
            }
            TextView txtCause = (TextView) llADSendInfoView.findViewById(R.id.txt_cause);
            txtCause.setText(arrData1.get(i).getStrMsg());

            llCause.addView(llADSendInfoView);
        }
    }

    public View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton rbCause;

            if(v.getId()==R.id.ll_etc){
                rbEtc.setChecked(true);
                etEtc.requestFocus();

                for(int i=0; i<mLayoutCnt; i++){
                    llCause = (LinearLayout) findViewById(i);
                    rbCause = (RadioButton) llCause.findViewById(R.id.rb_cause);
                    if(rbCause.isChecked()) rbCause.setChecked(false);
                }
            } else {
                etEtc.clearFocus();
                rbEtc.setChecked(false);

                mClickId = (int) v.getTag();
                for (int i = 0; i < mLayoutCnt; i++) {
                    llCause = (LinearLayout) findViewById(i);
                    rbCause = (RadioButton) llCause.findViewById(R.id.rb_cause);
                    if (i == mClickId) {
                        if (!rbCause.isChecked()) rbCause.setChecked(true);
                    } else {
                        if (rbCause.isChecked()) rbCause.setChecked(false);
                    }
                }

                strCauseIdx = arrData1.get(mClickId).getStrIdx();
            }
        }
    };

    /**
     * 회원탈퇴
     */
    public void Unregister() {
        String strCause="";
        final String url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_member_seceder);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if(strCauseIdx.equals("0")){
            strCause=etEtc.getText().toString();

            if(strCause.equals("")){
                Toast.makeText(UnregisterActivity.this, getResources().getString(R.string.str_input_cause_unregiste_err), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_CAUSE, strCauseIdx);
        k_param.put(SEND_CAUSE_ETC_TXT, strCause);

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
                listParams.add(new BasicNameValuePair(STR_CAUSE, params[SEND_CAUSE]));
                listParams.add(new BasicNameValuePair(STR_CAUSE_ETC, params[SEND_CAUSE_ETC_TXT]));

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
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                msg.what = StaticDataInfo.RESULT_CODE_200;
                msg.arg1 = PARSER_NUM_3;
            }else{
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
            }

            handler.sendMessage(msg);
        }
    }
}
