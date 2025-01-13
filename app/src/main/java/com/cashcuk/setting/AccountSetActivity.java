package com.cashcuk.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.common.AddrUser;
import com.cashcuk.membership.MobileAuthenticationWebActivity;
import com.cashcuk.membership.txtlistdata.TxtListAdapter;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;
import com.cashcuk.sendinfo.SendInfo;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 계정 설정
 */
public class AccountSetActivity extends Activity implements View.OnClickListener {
    private final String STR_VIEW_MODE = "V";
    private final String STR_MODIFY_MODE = "E";

    private final int SEND_GUBUN = 2; //View or Edit
    private final int SEND_HP = 3;
    private final int SEND_SIDO = 4;
    private final int SEND_SIGUNGU = 5;
    private final int SEND_MEMBERSHIP_AD_SEND_INFO = 6;

    //서버에 전송 값
    private final String STR_GUBUN = "gubun";
    private final String STR_HP = "hp";
    private final String STR_SIDO = "sido";
    private final String STR_SIGUNGU = "sigun";
    private final String STR_MEMBERSHIP_AD_SEND_INFO = "codes";

    //서버에서 받는 값
    private final String STR_PHONE_NUM = "login_hp";
    private final String STR_SEX = "login_sex";
    private final String STR_BIRTH = "login_birth";
    private String STR_AD = "set_ad";
    private final String STR_RECOMMENDER = "login_reccom";
    private final String STR_SI_DO = "login_si";
    private final String STR_SI_GUN_GU = "login_gu";
    private final String STR_SEND_INFO_TITLE = "login_title";
    private final String STR_SEND_INFO_SUB = "login_sub";

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;
    private final int PARSER_NUM_7 = 7;

    private String strMode = STR_VIEW_MODE;
    private final String STR_MODE_AD_SEND_DEFAULT_INFO_TITLE = "ADSendDefaultInfoTitle"; //광고 발송 기본정보 title
    private final String STR_MODE_AD_SEND_DEFAULT_INFO = "ADSendDefaultInfo"; //광고 발송 기본정보

    private int mCodeIndex = -1; //광고발송 기본 정보
    private int mCodeIndexSize = 0; //광고발송 기본 정보 총 개수
    private String[] strADSendTitleIdx; //title idx
    private TextView txtADSendInfoItem;
    private LinearLayout llProgress;
    private ArrayList<String> strADSendIdxTmp = new ArrayList<String>();

    //광고 발송 기본정보 return
    private final String STRING_CODE_TITLE = "code_title"; //광고 발송 기본 정보
    private final String STRING_CODE_IDX = "code_idx"; //광고 발송 기본 정보 고유 값

    private ArrayList<TxtListDataInfo> arrADSendDefaultInfoTemp = new ArrayList<TxtListDataInfo>();
    private ArrayList<TxtListDataInfo> arrADSendDefaultInfoTitleData = new ArrayList<TxtListDataInfo>();

    private ListView lvADInfoList;
    private LinearLayout llADSendInfo;
    private LayoutInflater inflater;

    private final int DIALOG_MODE_AD_SEND_INFO = 0;
    private final int DIALOG_MODE_SI_DO = 3;
    private final int DIALOG_MODE_SI_GUN = 4;

    private TextView txtEmail;
    private TextView txtPhoneNum;
    private TextView txtSex;
    private TextView txtBirthDate;
    private RelativeLayout rlResidence; //사는 곳
    private TextView txtSiDo; //시,도
    private TextView txtSiGunGu; //시,군, 구
    private ImageButton ibResidence;
    private TextView txtRecommender; //추천인

    //시도, 시군구
    private ArrayList<TxtListDataInfo> arrHangOutCitiesData = new ArrayList<TxtListDataInfo>();
    private ArrayList<TxtListDataInfo> arrHangOutTownData = new ArrayList<TxtListDataInfo>();

    private final String STR_SI_MODE = "S"; //시,도 선택
    private final String STR_GUN_MODE = "G"; //시,군,구 선택
    private String strSiDoMode=STR_SI_MODE;
    private String selSiDoIdx=""; // 시,도 선택 idx
    private String strSelSiDoIdxTemp=""; //새로운 시,도 선택 전의 data
    private String selSiGunGuIdx=""; //선택 된 시,군,구의 idx
    private String strSiDoTemp=""; //선택 전 시,도 string
    private String strSiDo=""; //선택 된 시,도 string
    private String strHp = "";

    private Dialog mDialog; // 사는 곳, 광고 기본 발송 정보 dialog
    private ListView lvHangOutList; // 시도, 시군구 list
    private Dialog mDlgChangePhoneNum; //폰 번호 수정 요청 dialog

    private final int REQUEST_PHONE_NUM = 999;
    private boolean isSelSiGun=false;

    private ArrayList<TxtListDataInfo> arrADSendInfoChild = new ArrayList<TxtListDataInfo>();

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(AccountSetActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if (strMode.equals(STR_VIEW_MODE)) {
                        new SendInfo(AccountSetActivity.this, handler, STR_MODE_AD_SEND_DEFAULT_INFO_TITLE, mCodeIndex++, StaticDataInfo.STRING_M);
                    } else if (strMode.equals(STR_MODIFY_MODE)) {
                        Toast.makeText(AccountSetActivity.this, getResources().getString(R.string.str_modify_succese), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case StaticDataInfo.RESULT_SI_DO:
                    arrHangOutCitiesData.addAll((ArrayList<TxtListDataInfo>)msg.obj);
                    break;
                case StaticDataInfo.RESULT_SI_GUN_GU:
                    arrHangOutTownData.clear();
                    arrHangOutTownData.addAll((ArrayList<TxtListDataInfo>) msg.obj);

                    OpenDialog(DIALOG_MODE_SI_GUN);
                    break;
                case StaticDataInfo.RESULT_SEND_AD_INFO:
                    arrADSendDefaultInfoTemp.clear();
                    arrADSendDefaultInfoTemp = (ArrayList<TxtListDataInfo>)msg.obj;

                    if(msg.arg1<0){
                        if(arrADSendDefaultInfoTemp!=null && arrADSendDefaultInfoTemp.size()>0){
                            mCodeIndexSize = arrADSendDefaultInfoTemp.size();
                            strADSendTitleIdx = new String[mCodeIndexSize];
                            arrADSendDefaultInfoTitleData = new ArrayList<TxtListDataInfo>();
                            arrADSendDefaultInfoTitleData.addAll(arrADSendDefaultInfoTemp);
                            new SendInfo(AccountSetActivity.this, handler, arrADSendDefaultInfoTitleData.get(mCodeIndex).getStrIdx(), mCodeIndex, "");
                        }
                    }else if(arrADSendDefaultInfoTemp!=null && arrADSendDefaultInfoTemp.size()>0) {
                        if(mCodeIndex<mCodeIndexSize) {
                            if(arryADSendDefaultInfo==null) arryADSendDefaultInfo = new ArrayList[mCodeIndexSize];
                            arrADSendDefaultInfo = new ArrayList<TxtListDataInfo>();
                            arrADSendDefaultInfo.addAll(arrADSendDefaultInfoTemp);
                            strADSendIdxTmp.add(arrADSendDefaultInfoTitleData.get(mCodeIndex).getStrIdx() + getResources().getString(R.string.str_category_item_gubun) + arrADSendDefaultInfo.get(0).getStrIdx());

                            arryADSendDefaultInfo[mCodeIndex] = arrADSendDefaultInfo;
                        }

                        mCodeIndex++;
                        if(mCodeIndex<mCodeIndexSize && arrADSendDefaultInfoTitleData!=null && arrADSendDefaultInfoTitleData.size()>0) {
                            new SendInfo(AccountSetActivity.this, handler, arrADSendDefaultInfoTitleData.get(mCodeIndex).getStrIdx(), mCodeIndex, "");
                        }else{
                            DisplayAccountInfo();
                        }
                    }
                    break;
                case StaticDataInfo.RESULT_NO_SIGUN:
                    isSelSiGun = true;
                    if(arrHangOutTownData!=null) arrHangOutTownData.clear();

                    if(strSiDoMode==STR_GUN_MODE){
                        strSiDo = strSiDoTemp;
                        selSiDoIdx = strSelSiDoIdxTemp;
                        txtSiDo.setText(strSiDo);
                        txtSiGunGu.setText("");
                        selSiGunGuIdx = "";
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
        setContentView(R.layout.activity_setting_account_detail);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_set_account));
        ((Button)findViewById(R.id.btn_info_change)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_modify_phone_num)).setOnClickListener(this);

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        txtEmail = (TextView) findViewById(R.id.txt_email_id);
        txtPhoneNum = (TextView) findViewById(R.id.txt_phone_num);
        txtSex = (TextView) findViewById(R.id.txt_sex);
        txtBirthDate = (TextView) findViewById(R.id.txt_birth_date);
        rlResidence = (RelativeLayout) findViewById(R.id.rl_residence);
        txtSiDo = (TextView) findViewById(R.id.txt_si_do);
        txtSiGunGu = (TextView) findViewById(R.id.txt_si_gun_gu);
        ibResidence = (ImageButton) findViewById(R.id.ib_residence);
        txtRecommender = (TextView) findViewById(R.id.txt_recommender);
        rlResidence.setOnClickListener(this);
        ibResidence.setOnClickListener(this);

        llADSendInfo = (LinearLayout) findViewById(R.id.ll_ad_send_info);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        new AddrUser(AccountSetActivity.this, handler, STR_SI_MODE, selSiDoIdx);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendDataSet(strMode);
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
        if (viewId == R.id.rl_residence || viewId == R.id.ib_residence) {
            ibResidence.setImageDrawable(getResources().getDrawable(R.drawable.bb_bt_press));
            OpenDialog(DIALOG_MODE_SI_DO);
        } else if (viewId == R.id.btn_info_change) {
            strMode = STR_MODIFY_MODE;
            strHp = txtPhoneNum.getText().toString();
            SendDataSet(STR_MODIFY_MODE);
        } else if (viewId == R.id.btn_modify_phone_num) {
            //휴대폰 본인인증 호출
            Intent intent = new Intent(AccountSetActivity.this, MobileAuthenticationWebActivity.class);
            intent.putExtra("ReturnCd",4);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_PHONE_NUM);
        }
    }

    private ArrayList<TxtListDataInfo> arrADSendDefaultInfo; //광고기본발송정보 item 들
    private ArrayList<TxtListDataInfo>[] arryADSendDefaultInfo;
    private int mClickId;
    public View.OnClickListener mClickADSendInfo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mClickId = (int)v.getTag();
            OpenDialog(DIALOG_MODE_AD_SEND_INFO);
        }
    };

    /**
     * 사는 곳, 광고 기본 발송 정보 dialog
     * @param mDlgMode
     */
    public void OpenDialog(final int mDlgMode){
        mDialog = new Dialog(AccountSetActivity.this);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDialog.getWindow().setAttributes((WindowManager.LayoutParams) params);
        mDialog.setContentView(R.layout.dlg_txt_list);

        lvHangOutList = (ListView) mDialog.findViewById(R.id.lv_txt);
        TextView txtTitle = (TextView) mDialog.findViewById(R.id.txt_dlg_title);
        final LinearLayout llADSendInfoID = (LinearLayout) findViewById(mClickId);

        switch(mDlgMode){
            case DIALOG_MODE_AD_SEND_INFO:
                if(arryADSendDefaultInfo!=null){
                    TextView txtADSendInfoTitle = (TextView) llADSendInfoID.findViewById(R.id.txt_ad_send_info_title);
                    txtTitle.setText(txtADSendInfoTitle.getText().toString());

                    ArrayList<TxtListDataInfo> arrADSendInfo = new ArrayList<TxtListDataInfo>();
                    ArrayList<TxtListDataInfo> arrADSendInfoTemp = new ArrayList<TxtListDataInfo>();
                    for(int j=0; j<arryADSendDefaultInfo[mClickId].size(); j++) {
                        arrADSendInfoTemp.add(arryADSendDefaultInfo[mClickId].get(j));
                    }
                    arrADSendInfo.addAll(arrADSendInfoTemp);
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrADSendInfo));
                }
                break;
            case DIALOG_MODE_SI_DO:
                ibResidence.setImageDrawable(getResources().getDrawable(R.drawable.bb_bt));
                isSelSiGun = false;
                if(arrHangOutCitiesData!=null){
                    txtTitle.setText(getResources().getString(R.string.str_cities_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrHangOutCitiesData));
                }
                break;
            case DIALOG_MODE_SI_GUN:
                if(arrHangOutTownData!=null && arrHangOutTownData.size()>0){
                    txtTitle.setText(getResources().getString(R.string.str_town_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrHangOutTownData));
                }else{
                    Toast.makeText(AccountSetActivity.this, getResources().getString(R.string.str_selector_cities), Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }

        lvHangOutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mDlgMode) {
                    case DIALOG_MODE_AD_SEND_INFO:
                        TextView txtADSendInfo = (TextView) llADSendInfoID.findViewById(R.id.txt_ad_send_info_msg);
                        txtADSendInfo.setText(arryADSendDefaultInfo[mClickId].get(position).getStrMsg());
                        strADSendIdxTmp.set(mClickId, arrADSendDefaultInfoTitleData.get(mClickId).getStrIdx() + getResources().getString(R.string.str_category_item_gubun) + arryADSendDefaultInfo[mClickId].get(position).getStrIdx());
                        break;
                    case DIALOG_MODE_SI_DO:
                        strSiDo = strSiDoTemp;
                        strSiDoTemp = arrHangOutCitiesData.get(position).getStrMsg();
                        selSiDoIdx = strSelSiDoIdxTemp;
                        strSelSiDoIdxTemp = arrHangOutCitiesData.get(position).getStrIdx();
                        strSiDoMode = STR_GUN_MODE;

                        new AddrUser(AccountSetActivity.this, handler, strSiDoMode, strSelSiDoIdxTemp);
                        break;
                    case DIALOG_MODE_SI_GUN:
                        strSiDo = strSiDoTemp;
                        selSiDoIdx = strSelSiDoIdxTemp;

                        txtSiDo.setText(strSiDo);
                        txtSiGunGu.setText(arrHangOutTownData.get(position).getStrMsg());
                        selSiGunGuIdx = arrHangOutTownData.get(position).getStrIdx();
                        break;
                }
                if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            }
        });

        mDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PHONE_NUM:
                    Log.d("temp","data.getStringExtra(\"PHONE_NUM\")["+data.getStringExtra("PHONE_NUM")+"]");
                    txtPhoneNum.setText(data.getStringExtra("PHONE_NUM"));
                    break;
            }
        }
    }

    /**
     * 서버에 값 전달 및 요청
     * @param strVEMode
     */
    public void SendDataSet(String strVEMode){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_member);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_GUBUN, strVEMode);

        if(strVEMode.equals(STR_MODIFY_MODE)){
            String strSendInfo = new String();
            for(int i=0; i<strADSendIdxTmp.size(); i++){
                strSendInfo += "["+ strADSendIdxTmp.get(i) +"]"+",";
            }
            k_param.put(SEND_HP, strHp);
            k_param.put(SEND_SIDO, selSiDoIdx);
            k_param.put(SEND_SIGUNGU, selSiGunGuIdx);
            k_param.put(SEND_MEMBERSHIP_AD_SEND_INFO, strSendInfo);
        }

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new UserSetTask().execute(strTask);
    }

    private class UserSetTask extends AsyncTask<String, Void, String> {
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
                listParams.add(new BasicNameValuePair(STR_GUBUN, params[SEND_GUBUN]));

                if(params[SEND_GUBUN].equals(STR_MODIFY_MODE)){
                    listParams.add(new BasicNameValuePair(STR_HP, params[SEND_HP]));
                    listParams.add(new BasicNameValuePair(STR_SIDO, params[SEND_SIDO]));
                    listParams.add(new BasicNameValuePair(STR_SIGUNGU, params[SEND_SIGUNGU]));
                    listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_AD_SEND_INFO, params[SEND_MEMBERSHIP_AD_SEND_INFO]));
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
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
            } else if(result.startsWith(StaticDataInfo.TAG_LIST)){
                ResultUserSetting(result);
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    private AccountSetInfo mAccountSet;
    private int mADSendInfoCnt = 1;
    private String strAD;
    private int mParserNum=-1;
    /**
     * 계정 설정 view d/p data
     * @param result
     */
    public void ResultUserSetting(String result){
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
                        if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                            mAccountSet = new AccountSetInfo();
                        }

                        if(parser.getName().equals(STR_PHONE_NUM)){
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_SEX)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_BIRTH)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_RECOMMENDER)) {
                            k_data_num = PARSER_NUM_3;
                        } else if (parser.getName().equals(STR_SI_DO)) {
                            k_data_num = PARSER_NUM_4;
                        } else if (parser.getName().equals(STR_SI_GUN_GU)) {
                            k_data_num = PARSER_NUM_5;
                        } else if (parser.getName().equals(STR_SEND_INFO_TITLE)) {
                            k_data_num = PARSER_NUM_6;
                        } else if (parser.getName().equals(STR_SEND_INFO_SUB)) {
                            k_data_num = PARSER_NUM_7;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    mAccountSet.setStrHp(parser.getText());
                                    break;
                                case PARSER_NUM_1:
                                    mAccountSet.setStrSex(parser.getText());
                                    break;
                                case PARSER_NUM_2:
                                    mAccountSet.setStrBirth(parser.getText());
                                    break;
                                case PARSER_NUM_3:
                                    mAccountSet.setStrRecommender(parser.getText());
                                    break;
                                case PARSER_NUM_4:
                                    mAccountSet.setStrSiDo(parser.getText());
                                    break;
                                case PARSER_NUM_5:
                                    mAccountSet.setStrSiGunGu(parser.getText());
                                    break;
                                case PARSER_NUM_6:
                                    mAccountSet.setStrSendInfoTitle(parser.getText());
                                    break;
                                case PARSER_NUM_7:
                                    mAccountSet.setStrSendInfoSub(parser.getText());
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 계정 정보 d/p
     */
    public void DisplayAccountInfo(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat dateFormatterChange = new SimpleDateFormat("yyyy-MM-dd");
        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        txtEmail.setText(prefs.getString("LogIn_ID", ""));
        txtPhoneNum.setText(PhoneNumberUtils.formatNumber(mAccountSet.getStrHp()));
        if(mAccountSet.getStrSex().equals("M")) {
            txtSex.setText(getResources().getString(R.string.str_man));
        }else if(mAccountSet.getStrSex().equals("F")) {
            txtSex.setText(getResources().getString(R.string.str_woman));
        }
        try {
            Date dateTmp = dateFormatter.parse(mAccountSet.getStrBirth().replaceAll("-", ""));
            txtBirthDate.setText(dateFormatterChange.format(dateTmp));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String[] strSiDoInfo = mAccountSet.getStrSiDo().split(",");
        selSiDoIdx = strSiDoInfo[0];
        txtSiDo.setText(strSiDoInfo[1]);
        if(mAccountSet.getStrSiGunGu()==null || mAccountSet.getStrSiGunGu().equals("")){
            isSelSiGun = true;
        }else {
            String[] strSiGunGuInfo = mAccountSet.getStrSiGunGu().split(",");
            selSiGunGuIdx = strSiGunGuInfo[0];
            txtSiGunGu.setText(strSiGunGuInfo[1]);
        }
//        ibResidence = (ImageButton) findViewById(R.id.ib_residence);
        txtRecommender.setText(mAccountSet.getStrRecommender());

        for(int i=0; i<mCodeIndexSize; i++){
            LinearLayout llADSendInfoView = (LinearLayout) inflater.inflate(R.layout.view_ad_send_info, null);
            llADSendInfoView.setId(i);

            TextView txtADSendInfoTitle = (TextView) llADSendInfoView.findViewById(R.id.txt_ad_send_info_title);
            txtADSendInfoTitle.setText(arrADSendDefaultInfoTitleData.get(i).getStrMsg());
            strADSendTitleIdx[i] = arrADSendDefaultInfoTitleData.get(i).getStrIdx();

            txtADSendInfoItem = (TextView) llADSendInfoView.findViewById(R.id.txt_ad_send_info_msg);
            for(int j=0; j<mAccountSet.getStrSendInfoTitle().size(); j++){
                if(strADSendTitleIdx[i].equals(mAccountSet.getStrSendInfoTitle().get(j).getStrIdx())){
                    txtADSendInfoItem.setText(mAccountSet.getStrSendInfoSub().get(j).getStrMsg());
                    strADSendIdxTmp.set(i, mAccountSet.getStrSendInfoTitle().get(j).getStrIdx() + getResources().getString(R.string.str_category_item_gubun) + mAccountSet.getStrSendInfoSub().get(j).getStrIdx());
                }else{
                }
            }
            txtADSendInfoItem.setTag((int)i);
            txtADSendInfoItem.setOnClickListener(mClickADSendInfo);

            llADSendInfo.addView(llADSendInfoView);
        }
//        DisplayADSendInfo();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        }, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }
}
