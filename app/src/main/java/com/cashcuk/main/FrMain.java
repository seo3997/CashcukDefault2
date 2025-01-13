package com.cashcuk.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.MainActivity;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.advertiser.AdvertiserRegistrationActivity;
import com.cashcuk.advertiser.main.AdvertiserNewMainActivity;
import com.cashcuk.advertiser.sendpush.ADTargetSendActivity;
import com.cashcuk.character.MyCharacterMainActivity;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgListTwoAdapter;
import com.cashcuk.pointlist.PointListActivitiy;
import com.cashcuk.push.PushStorageActivity;
import com.igaworks.IgawCommon;
import com.igaworks.adpopcorn.IgawAdpopcorn;
import com.igaworks.adpopcorn.interfaces.IAdPOPcornEventListener;
import com.igaworks.adpopcorn.style.ApStyleManager;
import com.nextapps.naswall.NASWall;
import com.nextapps.naswall.NASWall.OnCloseListener;

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
 * Main(Home)
 */
public class FrMain extends Fragment implements View.OnClickListener {
    private Activity mActivity;
    private String strPublicRelations="안녕하세요 리워드 어플리케이션,캐시쿡입니다. https://play.google.com/store/apps/details?id=com.cashcuk"; //홍보내용
    private TextView txtMyPoint; //내 포인트
    private LinearLayout llProgress;

    private final String STR_CONTENT = "content";       //홍보 내용 문구
    private final String STR_MAIN_MY_POINT = "m_point"; //내 포인트
    private final String STR_MEMID = "m_memid";         //내 포인트
    private final String STR_AREA = "m_area";           //내 지역
    private final String STR_ROLEID = "m_roleid";       //사용자 이용중지 상태

    private final String STR_ADVERTISER_STATUS = "biz_status"; //광고주 등록 상태 값
    private final String STR_REJECT_CUZ = "biz_rejbcoz"; //광고주 승인거부 사유

    private final int DATA_REQUEST_PUBLIC_RELATIONS = 1; //홍보문구 가져오기 (통신)
    private final int DATA_REQUEST_MODE_MAIN = 2; //MAIN 필요 값 가져오기 (통신)
    private final int DATA_REQUEST_ADVERTISER_STATE = 3; //광고주 등록여부 (통신)

    private int mDataRequestMode; //통신 모드
    private int mDlgRequest = -1;

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;

    // 아래의 값을 설정하세요.
    public static final boolean TEST_MODE = false;

    private  int mPushAdCd = 0;                                                                     //1:전단지 바로가기 2:PUSH광고 화면으로 이동


    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent(mActivity, DlgBtnActivity.class);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    if (msg.arg1 == DATA_REQUEST_PUBLIC_RELATIONS) {
                        strPublicRelations = "";
                        SavePublicRelation(strPublicRelations);
                    }else{
                        Toast.makeText(mActivity, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if (msg.arg1 == DATA_REQUEST_PUBLIC_RELATIONS) {
                        ResultPublicRelations((String) msg.obj);
                    } else if (msg.arg1 == DATA_REQUEST_MODE_MAIN) {
                        ResultMain((String) msg.obj);
                    }
                    break;
                case StaticDataInfo.RESULT_OVER_SAVE_POINT:
                    Toast.makeText(mActivity, getResources().getString(R.string.str_save_point_over), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_ADVERTISER: //광고주
                    //광고주 PUSH광고 호출
                    if(mPushAdCd==1) {
                        intent = new Intent(mActivity, AdvertiserNewMainActivity.class);
                        startActivity(intent);
                    }else if(mPushAdCd==2){
                        intent = new Intent(mActivity, ADTargetSendActivity.class);
                        intent.putExtra("AD_IDX", "");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                    return;
                case StaticDataInfo.RESULT_CODE_NO_ADVERTISER: //비광고주
                    intent.putExtra("DlgMode", "Two");
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_no_advertiser));
                    mDlgRequest = StaticDataInfo.RESULT_CODE_NO_ADVERTISER;
                    break;
                case StaticDataInfo.RESULT_CODE_WAIT_ADVERTISER: //승인대기
                    intent.putExtra("BtnDlgOneText", getResources().getString(R.string.str_close));
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_wait_advertiser));
                    mDlgRequest = StaticDataInfo.RESULT_CODE_WAIT_ADVERTISER;
                    break;
                case StaticDataInfo.RESULT_CODE_REJECT_ADVERTISER: //승인거부
                    if(!strRejectMsg.trim().equals("")) {
                        intent.putExtra("DlgTitle", getResources().getString(R.string.str_reject_cus_title));
                        intent.putExtra("BtnDlgCancelText", getResources().getString(R.string.str_cancel));
                        intent.putExtra("BtnDlgOneText", getResources().getString(R.string.str_redemand_advertiser));
                        intent.putExtra("DlgMode", "Two");
                        intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_reject_advertiser), strRejectMsg));
                        mDlgRequest = StaticDataInfo.RESULT_CODE_REJECT_ADVERTISER;
                    }else{
                        intent.putExtra("DlgTitle", getResources().getString(R.string.str_reject_cus_title));
                        intent.putExtra("BtnDlgOneText", getResources().getString(R.string.str_close));
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_reject_empty));
                        startActivity(intent);
                        return;
                    }
                    break;
            }

            if(intent != null && mDlgRequest!=-1){
                startActivityForResult(intent, mDlgRequest);
                mDlgRequest = -1;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(mActivity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initNas();

        MenuList();
    }

    @Override
    public void onResume() {
        super.onResume();
        IgawCommon.startSession(mActivity);
    }

    private void initNas() {{

        //		NASWall.init(this, TEST_MODE, USER_ID); // NAS 서버에서 적립금 관리하는 경우
        NASWall.init(getActivity(), TEST_MODE); // 개발자 서버에서 적립금 관리하는 경우

        // 내장 오퍼월 Close 이벤트 등록
        NASWall.setOnCloseListener(new OnCloseListener() {
            @Override
            public void OnClose() {
                DataRequest(DATA_REQUEST_MODE_MAIN);
                //Toast.makeText(mActivity, "NASWall - closed", Toast.LENGTH_SHORT).show();
            }
        });

    }}


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fr_main, container, false);
        View view = inflater.inflate(R.layout.fr_main, container, false);

        llProgress = (LinearLayout) view.findViewById(R.id.ll_progress_circle);

        ((LinearLayout) view.findViewById(R.id.ll_push_list)).setOnClickListener(this);
        ((LinearLayout) view.findViewById(R.id.ll_event)).setOnClickListener(this);
        ((LinearLayout) view.findViewById(R.id.ll_point_cuk)).setOnClickListener(this);            //적립하기
        ((LinearLayout) view.findViewById(R.id.li_friend)).setOnClickListener(this);               //친구소개
        ((LinearLayout) view.findViewById(R.id.ll_push_add)).setOnClickListener(this);               //푸시광고


        ((ImageButton) view.findViewById(R.id.ib_send_kakao)).setOnClickListener(this);
        ((ImageButton) view.findViewById(R.id.ib_send_msg)).setOnClickListener(this);
        ((ImageButton) view.findViewById(R.id.ib_send_url)).setOnClickListener(this);

        LinearLayout llPointListDetail = (LinearLayout) view.findViewById(R.id.ll_point_list_detail);
        llPointListDetail.setOnClickListener(this);

        LinearLayout ll_point_add1 = (LinearLayout) view.findViewById(R.id.ll_point_add1);
        ll_point_add1.setOnClickListener(this);

        LinearLayout ll_point_add2 = (LinearLayout) view.findViewById(R.id.ll_point_add2);
        ll_point_add2.setOnClickListener(this);


        txtMyPoint = (TextView) view.findViewById(R.id.txt_my_point);
        txtMyPoint.setSelected(true);

        ((LinearLayout) view.findViewById(R.id.ll_my_character)).setOnClickListener(this);
        ((LinearLayout) view.findViewById(R.id.ib_ad)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn_regi_advertiser)).setOnClickListener(this); //삭제

        return view;
    }


    /**
     * 상단 메뉴 버튼
     */
    private Dialog mMenuDlg;
    private ArrayList<String> arrString;
    private Button btn1; //취소
    private LinearLayout ll1; //취소
    public void MenuList() {
        mMenuDlg = new Dialog(mActivity);
        mMenuDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMenuDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mMenuDlg.setContentView(R.layout.dlg_list_title);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mMenuDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mMenuDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ((TextView) mMenuDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_main_friend)); //친구소개
        ListView lvDlgMsg = (ListView) mMenuDlg.findViewById(R.id.lv_dlg);

        arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_kakako));                              //카카오톡
        arrString.add(getResources().getString(R.string.str_sms));                                 //문자
        arrString.add(getResources().getString(R.string.str_copy_url));                            //URL문자


        ll1= (LinearLayout) mMenuDlg.findViewById(R.id.ll1);
        ll1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mMenuDlg!=null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
        });

        btn1 = (Button) mMenuDlg.findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mMenuDlg!=null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
        });
        //btn1.setOnTouchListener(this);
        //((LinearLayout) mMenuDlg.findViewById(R.id.ll1)).setOnTouchListener(this);

        DlgListTwoAdapter dlgAdapter = new DlgListTwoAdapter(mActivity, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (arrString.get(position).equals(getResources().getString(R.string.str_kakako))) { //카카오톡
                    ChkPublicRelations();
                    /*
                    try {
                        final KakaoLink kakaoLink = KakaoLink.getKakaoLink(mActivity);
                        final KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
                        kakaoTalkLinkMessageBuilder.addText(strPublicRelations);
                        kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder.build(), mActivity);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                     */
                } else if (arrString.get(position).equals(getResources().getString(R.string.str_sms))) { //SMS
                    ChkPublicRelations();
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.putExtra("sms_body", strPublicRelations);
                    intent.setType("vnd.android-dir/mms-sms");
                } else if(arrString.get(position).equals(getResources().getString(R.string.str_copy_url))) { //문자복사
                    ChkPublicRelations();
                    ClipboardManager clipboardManager =  (ClipboardManager) mActivity.getSystemService(mActivity.CLIPBOARD_SERVICE);
                    clipboardManager.setText(getResources().getString(R.string.str_new_url));
                    Toast.makeText(mActivity, getResources().getString(R.string.str_clipboard_copy), Toast.LENGTH_SHORT).show();
                }

                if(intent!=null){
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                if (mMenuDlg != null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
        });
    }



    @Override
    public void onStart() {
        super.onStart();
        //DataRequest(DATA_REQUEST_PUBLIC_RELATIONS);
        //2017-05-27 통신에러가 발생해서 Mypoint가져오기로 수정함
        DataRequest(DATA_REQUEST_MODE_MAIN);
    }

    @Override
    public void onPause() {
        super.onPause();
        IgawCommon.endSession();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 홍보문구 데이터 확인
     */
    public void ChkPublicRelations(){
        if(strPublicRelations.equals("")){
            Intent intent = new Intent(mActivity, DlgBtnActivity.class);
            intent.putExtra("DlgTitle", mActivity.getResources().getString(R.string.str_setting_alrim));
            intent.putExtra("BtnDlgMsg", mActivity.getResources().getString(R.string.str_msg_send_error));
            startActivity(intent);
            return;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.ll_event) {
            intent = new Intent(mActivity, EventWebViewActivity.class);
        } else if (viewId == R.id.ib_send_kakao) {
            ChkPublicRelations();
        } else if (viewId == R.id.ib_send_msg) {
            ChkPublicRelations();
            intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra("sms_body", strPublicRelations);
            intent.setType("vnd.android-dir/mms-sms");
        } else if (viewId == R.id.ib_send_url) {
            ChkPublicRelations();
            ClipboardManager clipboardManager =  (ClipboardManager) mActivity.getSystemService(mActivity.CLIPBOARD_SERVICE);
            clipboardManager.setText(getResources().getString(R.string.str_new_url));
            Toast.makeText(mActivity, getResources().getString(R.string.str_clipboard_copy), Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.ll_point_list_detail) {
            intent = new Intent(mActivity, PointListActivitiy.class);
        } else if (viewId == R.id.ll_push_list) {
            intent = new Intent(mActivity, PushStorageActivity.class);
        } else if (viewId == R.id.ll_point_add1) {
            NASWall.open(mActivity, strMyMemId);
            //Toast.makeText(mActivity, strMyMemId, Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.ll_point_add2) {
            IgawCommon.setUserId(getActivity().getApplicationContext(),strMyMemId);
            // 오퍼월 타이틀 지정
            ApStyleManager.setOfferwallTitle("무료충전2");
            // 오퍼월 전체 테마 색상 코드 지정
            //ApStyleManager.setThemeColor(ApStyleManager.BLUE_THEME);
            ApStyleManager.setThemeColor(0xff616161);
            IgawAdpopcorn.openOfferWall(mActivity);
            IgawAdpopcorn.setEventListener(mActivity, new IAdPOPcornEventListener() {
                @Override
                public void OnClosedOfferWallPage() {
                    //오퍼월이 종료되었을 때 수행할 액션을 정의할 수 있습니다.
                    DataRequest(DATA_REQUEST_MODE_MAIN);
                    //Toast.makeText(mActivity, getResources().getString(R.string.str_point_add1), Toast.LENGTH_SHORT).show();
                }
            });
            //Toast.makeText(mActivity, getResources().getString(R.string.str_point_add1), Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.ll_my_character) {
            intent = new Intent(mActivity, MyCharacterMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (viewId == R.id.ib_ad) { //바로광고
            mPushAdCd=1;
            DataRequest(DATA_REQUEST_ADVERTISER_STATE);
        } else if (viewId == R.id.btn_regi_advertiser) { //삭제
            intent = new Intent(mActivity, AdvertiserRegistrationActivity.class);
        } else if (viewId == R.id.ll_point_cuk) { //적립하기
            intent = new Intent(mActivity, MainActivity.class);
            intent.putExtra("main", "main") ;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.finish();
        } else if (viewId == R.id.li_friend) { //친구소개
            if (mMenuDlg != null && !mMenuDlg.isShowing()) mMenuDlg.show();
        } else if (viewId == R.id.ll_push_add) { //push광고
            mPushAdCd=2;
            DataRequest(DATA_REQUEST_ADVERTISER_STATE);
    /*
    intent = new Intent(mActivity, ADTargetSendActivity.class);
    intent.putExtra("AD_IDX","");
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //mActivity.finish();
    */
        }
        if(intent!=null){
            startActivity(intent);
        }
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(int mode){
        mDataRequestMode = mode;

        String url="";
        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        final String token = pref.getString(mActivity.getResources().getString(R.string.str_token), "");

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        if(mode == DATA_REQUEST_PUBLIC_RELATIONS){ //홍보문구
            url = mActivity.getResources().getString(R.string.str_new_url) + mActivity.getResources().getString(R.string.str_content_page);
        }else if(mode == DATA_REQUEST_MODE_MAIN) { //main
            url = mActivity.getResources().getString(R.string.str_new_url) + mActivity.getResources().getString(R.string.str_offerwall_point);
        }else if(mode == DATA_REQUEST_ADVERTISER_STATE){
            url = mActivity.getResources().getString(R.string.str_new_url) + mActivity.getResources().getString(R.string.str_member_bizstatus);
        }

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        Log.d("temp","url["+url+"]");
        Log.d("temp","token["+token+"]");

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
            Message msg = new Message();
            msg.what = -1;

            if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                if (mDataRequestMode != DATA_REQUEST_ADVERTISER_STATE) {
                    msg.what = StaticDataInfo.RESULT_CODE_200;
                    msg.obj = result;
                    msg.arg1 = mDataRequestMode;
                } else {
                    ResultStatusAdvertiser(result);
                }
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                msg.what = StaticDataInfo.RESULT_NO_DATA;
            } else {
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
            }

            if (handler != null && msg.what != -1) {
                handler.sendMessage(msg);
            }
        }
    }

    private String strAdvertiserStatus;
    private String strRejectMsg;
    /**
     * 광고주 등록 여부 상태
     */
    public void ResultStatusAdvertiser(String result){
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


    /**
     * 내 포인트 결과 값
     */
    private String strMyPoint="";
    private String strMyMemId="";
    private String strMyArea="";
    private String strMyRoleId="";
    public void ResultMain(String result){
        try {
            Log.d("temp","ResultMain["+result+"]");
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
                        if (parser.getName().equals(STR_MAIN_MY_POINT)) {
                            k_data_num = PARSER_NUM_0;
                        }else  if (parser.getName().equals(STR_MEMID)) {
                            k_data_num = PARSER_NUM_1;
                        }else  if (parser.getName().equals(STR_AREA)) {
                            k_data_num = PARSER_NUM_2;
                        }else  if (parser.getName().equals(STR_ROLEID)) {
                            k_data_num = PARSER_NUM_3;
                        }else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    strMyPoint = parser.getText();
                                    break;
                                case PARSER_NUM_1:
                                    if("".equals(strMyMemId)) strMyMemId = parser.getText();
                                    break;
                                case PARSER_NUM_2:
                                    if("".equals(strMyArea)) {
                                        strMyArea = parser.getText();
                                        ((MainActivity)mActivity).mMyAddress=strMyArea;
                                        //SaveMyArea(strMyArea);
                                    }
                                    break;
                                case PARSER_NUM_3:
                                    if("".equals(strMyRoleId)) {
                                        strMyRoleId= parser.getText();
                                        if("4".equals(strMyRoleId)) ((MainActivity)mActivity).ll_use_stop.setVisibility(View.VISIBLE);
                                    }
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
            Log.d("temp","strMyPoint["+strMyPoint+"]");
            Log.d("temp","strMyMemId["+strMyMemId+"]");
            Log.d("temp","strMyArea["+strMyArea+"]");
            Log.d("temp","strMyRoleId["+strMyRoleId+"]");

            DisplayMainData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * mian 화면 data d/p
     */
    public void DisplayMainData(){
        if (!strMyPoint.trim().equals("")) {
            txtMyPoint.setText(StaticDataInfo.makeStringComma(strMyPoint));
        }else{
            txtMyPoint.setText("0");
        }
    }

    /**
     * 홍보내용 값 저장 preference
     * @param result
     */
    public void SavePublicRelation(String result){
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveRecommend", mActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("RecommendText", strPublicRelations);
        editor.commit();
    }

    /**
     * 마이지역 preference
     * @param pArea
     */
    public void SaveMyArea(String pArea){
        SharedPreferences prefs = mActivity.getSharedPreferences("MyArea", mActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("MyArea", pArea);
        editor.commit();
    }



    /**
     * 홍보내용 결과 값
     */
    public void ResultPublicRelations(String result){
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
                        if (parser.getName().equals(STR_CONTENT)) {
                            k_data_num = PARSER_NUM_0;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    strPublicRelations = parser.getText();
                                    Log.d("temp","strPublicRelations["+strPublicRelations+"]");

                                    SavePublicRelation(strPublicRelations);
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }


            DataRequest(DATA_REQUEST_MODE_MAIN);
            //DataRequest(DATA_REQUEST_GET_MEMID); //getMemId

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == mActivity.RESULT_OK){
            Intent intent = null;
            switch (requestCode){
                case StaticDataInfo.RESULT_CODE_NO_ADVERTISER: //비광고주
                    intent = new Intent(mActivity, AdvertiserRegistrationActivity.class);
                    intent.putExtra("PageMode", "A");
                    break;
                case StaticDataInfo.RESULT_CODE_REJECT_ADVERTISER: //승인거부
                    intent = new Intent(mActivity, AdvertiserRegistrationActivity.class);
                    intent.putExtra("PageMode", "R");
                    break;
            }

            if(intent!=null){
                startActivity(intent);
            }
        }
    }
}
