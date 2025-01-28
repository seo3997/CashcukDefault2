package com.cashcuk.ad.detailview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.adlist.ListADInfo;
import com.cashcuk.advertiser.makead.MakeADMainActivity;
import com.cashcuk.advertiser.sendpush.ADPushSendCurrentState;
import com.cashcuk.advertiser.sendpush.ADTargetSendActivity;
import com.cashcuk.common.CommonDataTask;
import com.cashcuk.common.ImageLoader;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgImgZoom;
import com.cashcuk.dialog.DlgListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 광고 보기 - 상세보기
 */
public class FrADDetailMain1 extends Fragment implements View.OnClickListener, View.OnTouchListener, OnMapReadyCallback {
    private Activity mActivity;
    private LinearLayout llDetailImg; //광고 상세 이미지
    private ImageView ivTitle; //광고 타이틀 img
    private ScrollView svAD;

    private TextView txtGrade;
    private ImageView ivGrade1, ivGrade2, ivGrade3, ivGrade4, ivGrade5;
    private TextView txtADName;
    private TextView txtADDetail;
    private TextView txtADHomePage;

    private String strADTel="";
    private String strHomepage="";
    private String strADDuration="";
    private String strADAddress="";
    private String strLike="";
    private boolean isLike=false;

    private Button btnSendCall; //전화걸기기
    private Button btnSendSMS; //문자보내기
    private Button btnHomepage; //홈페이지
    private Button btnAppraisal; //평가하기
    private Button btnAddMyAD; //관심광고 등록

    private ADDetailInfo mADDetailInfo;
    private String strADIdx; //광고 idx
    private String strADKind=""; //광고주/일반
    private String strADStatus=""; //광고 상태
    private String strRating="0"; //내가 평가한 평점
    private ListADInfo getADInfo = new ListADInfo();

    public final String STR_AD_STOP = "PAUZ"; //광고 중지
    public final String STR_AD_REQUEST_CANCEL = "CNCL"; //승인요청 취소
    private final String STR_AD_STOP_CANCEL = "AING"; //재시작
    public final String STR_AD_RETURN = "R"; //환급
    public final String STR_AD_LIKE = "L"; //관심광고
    public final String STR_AD_GREADE = "G"; //평점
    private String strMode="";

    private LinearLayout llTwoBtn; //광고중지 push요청
    private Button btnADPush;
    private Button btnADStop;
    private LinearLayout llThreeBtn; //수정, 재시작, 환급
    private Button btnADModify;
    private Button btnADReturn;
    private Button btnADRestart;

    private LinearLayout llMargin; //수정, 승인요청 취소 버튼 사이의 margin
    private LinearLayout llQTwo; //수정, 승인요청 취소
    private Button btnRequestCancel; //승인요청 취소 버튼
    private Button btnQModify; //승인요청 수정 버튼

    private ImageView ivMapErr; //지도 표시 오류
    private LinearLayout llMap; //지도

    private final int SEND_AD_LIKE_TYPE = 3;
    private final String STR_AD_LIKE_TYPE = "editType";

    private final int SEND_AD_IDX = 2;
    private final int SEND_AD_MODE=3;
    private final String STR_AD_IDX = "ad_idx";
    private final String STR_AD_MODE = "ad_motion";

    private final String STR_AD_RATING = "ad_rating";
    private final int SEND_AD_RATING = 3;

    public final int REQUEST_STOP_OK = 999;
    public final int REQUEST_RETURN = 888;
    public final int REQUEST_AD_CANCEL = 77; //승인요청 취소.

    private final String STR_AD_LIKE_ADD = "A";
    private final String STR_AD_LIKE_DEL = "D";

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;

    private final String STR_AD_GRADE = "ad_rating"; //평점
    private ProgressBar pbTitleImg;

    /*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(mActivity.toString() + " must implement OnArticleSelectedListener");
        }
    }
    */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
        } else {
            // Context가 FragmentActivity가 아닌 경우에 대한 처리
            mActivity = null;
        }
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(mActivity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    strADStatus = strMode;
                    if(strMode.equals(STR_AD_STOP)) {
                        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_ad_stop_ok), Toast.LENGTH_SHORT).show();
                        llTwoBtn.setVisibility(View.GONE);
                        llThreeBtn.setVisibility(View.VISIBLE);
                        llQTwo.setVisibility(View.GONE);
                    }else if(strMode.equals(STR_AD_STOP_CANCEL)){
                        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_ad_restart_ok), Toast.LENGTH_SHORT).show();
                        chkDateDisplayBtn(mADDetailInfo.getStrDateS(), mADDetailInfo.getStrDateE(), true, getADInfo);
                    }else if(strMode.equals(STR_AD_RETURN)){
                        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_ad_return_ok), Toast.LENGTH_SHORT).show();
                        mActivity.finish();
                    }else if(strMode.equals(STR_AD_REQUEST_CANCEL)){
                        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_ad_cancel_ok), Toast.LENGTH_SHORT).show();
                        mActivity.finish();
                    }else if(strMode.equals(STR_AD_LIKE)){
                        if(isLike){ //관심등록
                            isLike = false;
                            btnAddMyAD.setText(mActivity.getResources().getString(R.string.str_interest_add));
                            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_my_ad_minus_succese), Toast.LENGTH_SHORT).show();
                        }else{
                            isLike = true;
                            btnAddMyAD.setText(mActivity.getResources().getString(R.string.str_interest_free));
                            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_my_ad_add_succese), Toast.LENGTH_SHORT).show();
                        }
                    }else if(strMode.equals(STR_AD_GREADE)){
                        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_greade_success), Toast.LENGTH_SHORT).show();
                    }
                    strMode = "";
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            strADIdx = (String) getArguments().getString("AD_IDX");
            mADDetailInfo = (ADDetailInfo) getArguments().getSerializable("AD_DATA");
            strADKind = (String) getArguments().getString("AD_KIND");
            strADStatus = (String) getArguments().getString("AD_STATUS");
            getADInfo = (ListADInfo) getArguments().getSerializable("AD_INFO");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new ratingTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.ad_detail_main_1, container, false );

        TitleBar titleBar = (TitleBar) view.findViewById(R.id.title_bar);
        titleBar.setTitle(getResources().getString(R.string.str_ad_view));

        Arrays.fill(bAppraisalArray, false); //특정 값으로 초기화

        svAD = (ScrollView) view.findViewById(R.id.sv_ad);
        pbTitleImg = (ProgressBar) view.findViewById(R.id.pb_title_img);
        ivTitle = (ImageView) view.findViewById(R.id.iv_ad_title);
        ivTitle.setOnClickListener(this);
        llDetailImg = (LinearLayout) view.findViewById(R.id.ll_ad_detail_img);
        llTwoBtn = (LinearLayout) view.findViewById(R.id.ll_two);
        llThreeBtn = (LinearLayout) view.findViewById(R.id.ll_three);
        btnADPush = (Button) view.findViewById(R.id.btn_ad_push);
        btnADStop = (Button) view.findViewById(R.id.btn_ad_stop);

        btnADModify = (Button) view.findViewById(R.id.btn_ad_modify);
        btnADReturn = (Button) view.findViewById(R.id.btn_ad_return);
        btnADRestart = (Button) view.findViewById(R.id.btn_ad_restart);

        llMargin = (LinearLayout) view.findViewById(R.id.ll_margin);
        llQTwo = (LinearLayout) view.findViewById(R.id.ll_q);
        btnRequestCancel = (Button) view.findViewById(R.id.btn_request_cancel);
        btnQModify = (Button) view.findViewById(R.id.btn_ad_q_modify);

        btnADPush.setOnClickListener(this);
        btnADStop.setOnClickListener(this);
        btnADModify.setOnClickListener(this);
        btnADReturn.setOnClickListener(this);
        btnADRestart.setOnClickListener(this);
        btnRequestCancel.setOnClickListener(this);
        btnQModify.setOnClickListener(this);

        txtGrade = (TextView) view.findViewById(R.id.txt_grade);
        ivGrade1 = (ImageView) view.findViewById(R.id.iv_grade1);
        ivGrade2 = (ImageView) view.findViewById(R.id.iv_grade2);
        ivGrade3 = (ImageView) view.findViewById(R.id.iv_grade3);
        ivGrade4 = (ImageView) view.findViewById(R.id.iv_grade4);
        ivGrade5 = (ImageView) view.findViewById(R.id.iv_grade5);
        txtADName = (TextView) view.findViewById(R.id.txt_ad_name);
        txtADDetail = (TextView) view.findViewById(R.id.txt_ad_detail);

        btnSendCall = (Button) view.findViewById(R.id.btn_send_call);
        btnSendSMS = (Button) view.findViewById(R.id.btn_send_sms);
        btnHomepage = (Button) view.findViewById(R.id.btn_homepage);
        btnAppraisal = (Button) view.findViewById(R.id.btn_appraisal);
        btnAddMyAD = (Button) view.findViewById(R.id.btn_add_my_ad);

        ivMapErr = (ImageView) view.findViewById(R.id.iv_map_err);
        llMap = (LinearLayout) view.findViewById(R.id.ll_map);

        if(strADKind.equals(mActivity.getResources().getString(R.string.str_user_en))) {
            btnSendCall.setClickable(true);
            btnSendSMS.setClickable(true);
            btnHomepage.setClickable(true);
            btnAppraisal.setClickable(true);
            btnAddMyAD.setClickable(true);
            btnSendCall.setOnClickListener(this);
            btnSendSMS.setOnClickListener(this);
            btnHomepage.setOnClickListener(this);
            btnAppraisal.setOnClickListener(this);
            btnAddMyAD.setOnClickListener(this);
        }else{
            ImageButton ibMenu = (ImageButton) titleBar.findViewById(R.id.ib_menu);
            ibMenu.setVisibility(View.VISIBLE);
            ibMenu.setOnClickListener(this);

            MenuList();
        }

        setDisplayData();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        Drawable dTitle = ivTitle.getDrawable();
        if(dTitle instanceof BitmapDrawable){
            Bitmap bitmap = ((BitmapDrawable)dTitle).getBitmap();
            bitmap.recycle();
            bitmap = null;
        }
        */
    }

    /**
     * 상단 메뉴 버튼
     */
    private Dialog mMenuDlg;
    private ArrayList<String> arrString;
    private Button btn1; //취소
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

        ((TextView) mMenuDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_menu));
        ListView lvDlgMsg = (ListView) mMenuDlg.findViewById(R.id.lv_dlg);

        arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_push_pressent_condition)); //발송현황

        btn1 = (Button) mMenuDlg.findViewById(R.id.btn1);
        ((LinearLayout) mMenuDlg.findViewById(R.id.ll1)).setOnTouchListener(this);
        btn1.setOnTouchListener(this);

        DlgListAdapter dlgAdapter = new DlgListAdapter(mActivity, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrString.get(position).equals(getResources().getString(R.string.str_push_pressent_condition))) { //발송현황
                    Intent intent = new Intent(mActivity, ADPushSendCurrentState.class);
                    intent.putExtra("PushKind", getResources().getString(R.string.str_push_send_current_state));
                    intent.putExtra("AD_IDX", strADIdx);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                if (mMenuDlg != null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.ib_menu) {
            if (mMenuDlg != null && !mMenuDlg.isShowing()) mMenuDlg.show();
        } else if (v.getId() == R.id.btn_send_call) {
            if (!strADTel.equals("")) intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strADTel));
        } else if (v.getId() == R.id.btn_send_sms) {
            if (!strADTel.equals("")) intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + strADTel));
        } else if (v.getId() == R.id.btn_homepage) {
            if (!strHomepage.equals("")) {
                if (!strHomepage.startsWith(getResources().getString(R.string.str_ad_homepage))
                        && !strHomepage.startsWith(getResources().getString(R.string.str_ad_homepage_s))) {
                    strHomepage = getResources().getString(R.string.str_ad_homepage) + strHomepage;
                }
                String regex = "^(https?):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/?([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(strHomepage);
                if (m.matches()) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(strHomepage));
                } else {
                    Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_homepage_err), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_homepage_err), Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.btn_appraisal) {
            OpenDialog();
        } else if (v.getId() == R.id.btn_add_my_ad) {
            if (isLike) {
                likeADState(STR_AD_LIKE_DEL);
            } else {
                likeADState(STR_AD_LIKE_ADD);
            }
        } else if (v.getId() == R.id.btn_ad_stop) {
            intent = new Intent(mActivity, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", mActivity.getResources().getString(R.string.str_ad_stop_msg));
            intent.putExtra("DlgMode", "Two");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.startActivityForResult(intent, REQUEST_STOP_OK);
            return;
        } else if (v.getId() == R.id.btn_ad_push) {
            intent = new Intent(mActivity, ADTargetSendActivity.class);
            intent.putExtra("AD_IDX", strADIdx);
            intent.putExtra("AD_NAME", mADDetailInfo.getStrADName());
            mActivity.finish();
        } else if (v.getId() == R.id.btn_ad_modify || v.getId() == R.id.btn_ad_q_modify) {
            intent = new Intent(mActivity, MakeADMainActivity.class);
            intent.putExtra("AD_IDX", strADIdx);
            intent.putExtra("AD_STATUS", strADStatus);
            mActivity.finish();
        } else if (v.getId() == R.id.btn_ad_return) {
            intent = new Intent(mActivity, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", mActivity.getResources().getString(R.string.str_ad_return_msg));
            intent.putExtra("DlgMode", "Two");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.startActivityForResult(intent, REQUEST_RETURN);
            return;
        } else if (v.getId() == R.id.btn_ad_restart) {
            requestMode(STR_AD_STOP_CANCEL);
        } else if (v.getId() == R.id.btn_request_cancel) {
            intent = new Intent(mActivity, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", mActivity.getResources().getString(R.string.str_ad_cancel_msg));
            intent.putExtra("DlgMode", "Two");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.startActivityForResult(intent, REQUEST_AD_CANCEL);
            return;
        } else if (v.getId() == R.id.iv_ad_title) {
            intent = new Intent(mActivity, DlgImgZoom.class);
            intent.putExtra("Path", mADDetailInfo.getStrTitleImgUrl().replace("\\", "//"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        if(intent != null){
            startActivity(intent);
        }
    }

    public void sendGrade(String mode){
        strMode = mode;
        final String url = mActivity.getResources().getString(R.string.str_new_url) + mActivity.getResources().getString(R.string.str_advertise_rating);
        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<String, String> k_param = new HashMap<String, String>();
        k_param.put(mActivity.getResources().getString(R.string.str_token), token);
        k_param.put(STR_AD_IDX, strADIdx);
        k_param.put(STR_AD_RATING, String.valueOf(mAppraisal));


        CommonDataTask.DataTaskCallback callback = new CommonDataTask.DataTaskCallback() {
            @Override
            public void onPreExecute() {
                // 네트워크 요청 시작 전에 UI 업데이트 (예: 프로그레스바 표시)
                Log.d("CommonDataTask", "onPreExecute");
            }

            @Override
            public void onPostExecute(String result) {
                // 네트워크 요청 완료 후 결과 처리
                Log.d("CommonDataTask", "onPostExecute: " + result);
                if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
                }else{
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
                }
            }

            @Override
            public void onError(Exception e) {
                // 네트워크 요청 중 에러 발생 시 처리
                Log.e("CommonDataTask", "onError", e);
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        };

        CommonDataTask task = new CommonDataTask(url, k_param, callback);
        task.execute();

        //new DataTask().execute(strTask);
    }

    public void likeADState(String likeType){
        strMode = STR_AD_LIKE;
        final String url = mActivity.getResources().getString(R.string.str_new_url) + mActivity.getResources().getString(R.string.str_advertise_like);
        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<String, String> k_param = new HashMap<String, String>();

        k_param.put(mActivity.getResources().getString(R.string.str_token), token);
        k_param.put(STR_AD_IDX, strADIdx);
        k_param.put(STR_AD_LIKE_TYPE, likeType);


        CommonDataTask.DataTaskCallback callback = new CommonDataTask.DataTaskCallback() {
            @Override
            public void onPreExecute() {
                // 네트워크 요청 시작 전에 UI 업데이트 (예: 프로그레스바 표시)
                Log.d("CommonDataTask", "onPreExecute");
            }

            @Override
            public void onPostExecute(String result) {
                // 네트워크 요청 완료 후 결과 처리
                Log.d("CommonDataTask", "onPostExecute: " + result);
                if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
                }else{
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
                }
            }

            @Override
            public void onError(Exception e) {
                // 네트워크 요청 중 에러 발생 시 처리
                Log.e("CommonDataTask", "onError", e);
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        };

        CommonDataTask task = new CommonDataTask(url, k_param, callback);
        task.execute();

        //new DataTask().execute(strTask);
    }

    /**
     * 광고중지
     */
    public void requestMode(String mode){
        strMode = mode;
        String url="";
        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if (mode.equals(STR_AD_RETURN)) {
            url = mActivity.getResources().getString(R.string.str_new_url) + mActivity.getResources().getString(R.string.str_advertise_payback);
        } else {
            url = mActivity.getResources().getString(R.string.str_new_url) + mActivity.getResources().getString(R.string.str_advertise_motion);
        }

        HashMap<String, String> k_param = new HashMap<String, String>();
        k_param.put(mActivity.getResources().getString(R.string.str_token), token);
        k_param.put(STR_AD_IDX, strADIdx);
        if(strMode.equals(STR_AD_STOP) || strMode.equals(STR_AD_STOP_CANCEL) || strMode.equals(STR_AD_REQUEST_CANCEL)) {
            k_param.put(STR_AD_MODE, mode);
        }else if(strMode.equals(STR_AD_LIKE)){
            k_param.put(STR_AD_LIKE_TYPE,mode);
        }else if(strMode.equals(STR_AD_GREADE)){
            k_param.put(STR_AD_RATING, mode);
        }

        CommonDataTask.DataTaskCallback callback = new CommonDataTask.DataTaskCallback() {
            @Override
            public void onPreExecute() {
                // 네트워크 요청 시작 전에 UI 업데이트 (예: 프로그레스바 표시)
                Log.d("CommonDataTask", "onPreExecute");
            }

            @Override
            public void onPostExecute(String result) {
                // 네트워크 요청 완료 후 결과 처리
                Log.d("CommonDataTask", "onPostExecute: " + result);
                if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
                    if(strMode.equals(STR_AD_GREADE)){
                        mAppraisal=0;
                        new ratingTask().execute();
                    }
                }else{
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
                }
            }

            @Override
            public void onError(Exception e) {
                // 네트워크 요청 중 에러 발생 시 처리
                Log.e("CommonDataTask", "onError", e);
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        };

        CommonDataTask task = new CommonDataTask(url, k_param, callback);
        task.execute();

        //new DataTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
/*
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
                listParams.add(new BasicNameValuePair(mActivity.getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_AD_IDX, params[SEND_AD_IDX]));
                if(strMode.equals(STR_AD_STOP) || strMode.equals(STR_AD_STOP_CANCEL) || strMode.equals(STR_AD_REQUEST_CANCEL)) {
                    listParams.add(new BasicNameValuePair(STR_AD_MODE, params[SEND_AD_MODE]));
                }else if(strMode.equals(STR_AD_LIKE)){
                    listParams.add(new BasicNameValuePair(STR_AD_LIKE_TYPE, params[SEND_AD_LIKE_TYPE]));
                }else if(strMode.equals(STR_AD_GREADE)){
                    listParams.add(new BasicNameValuePair(STR_AD_RATING, params[SEND_AD_RATING]));
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
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
                if(strMode.equals(STR_AD_GREADE)){
                    mAppraisal=0;
                    new ratingTask().execute();
                }
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }
*/

    private Dialog mDlg;
    private LinearLayout llAppraisal;
    private final int APPRAISAL_ITEM = 5;
    private Button btnCancel;
    private Button btnOk;
    public void OpenDialog(){
        mDlg = new Dialog(mActivity);

        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDlg.setContentView(R.layout.dlg_appraisal);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        llAppraisal = (LinearLayout) mDlg.findViewById(R.id.ll_appraisal);
        ((LinearLayout) mDlg.findViewById(R.id.ll_cancel)).setOnTouchListener(this);
        ((LinearLayout) mDlg.findViewById(R.id.ll_ok)).setOnTouchListener(this);
        btnCancel = (Button) mDlg.findViewById(R.id.btn_cancel);
        btnOk = (Button) mDlg.findViewById(R.id.btn_ok);
        btnCancel.setOnTouchListener(this);
        btnOk.setOnTouchListener(this);

        int mMyRating = 1;
        if(strRating!=null && !strRating.equals("") && !strRating.startsWith("0")){
            mMyRating = Integer.parseInt(strRating);
        }

        for(int i=0; i<APPRAISAL_ITEM; i++){
            ImageButton ibAppraisal = new ImageButton(mActivity);
            ibAppraisal.setId(i);

            if(i<mMyRating){
                ibAppraisal.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star1_press));
                mAppraisal++;
            } else {
                ibAppraisal.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star1_normal));
            }
            ibAppraisal.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT, 1);
            param.gravity = Gravity.CENTER;
            ibAppraisal.setOnClickListener(mBtnAppraisal);
            llAppraisal.addView(ibAppraisal, param);
        }

        mDlg.show();
    }

    private int mAppraisal = 0;
    private boolean[] bAppraisalArray = new boolean[5];
    public View.OnClickListener mBtnAppraisal = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                /*
                for (int i = 0; i <= v.getId(); i++) {
                    ((ImageButton) llAppraisal.getChildAt(i)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star1_press));
                    bAppraisalArray[i] = true;
                }

                for (int j = (APPRAISAL_ITEM - 1); j > v.getId(); j--) {
                    ((ImageButton) llAppraisal.getChildAt(j)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star1_normal));
                    bAppraisalArray[j] = false;
                }

                mAppraisal = v.getId()+1;

                 */

        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_ok || v.getId() == R.id.btn_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                sendGrade(STR_AD_GREADE);
                if(mDlg!=null && mDlg.isShowing()) mDlg.dismiss();
            }
            return true;
        }else if(v.getId() == R.id.ll_cancel || v.getId() == R.id.btn_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) if(mDlg!=null && mDlg.isShowing()) mDlg.dismiss();
            return true;
        }else if (v.getId() == R.id.ll1 || v.getId() == R.id.btn1 ) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if (event.getAction() == MotionEvent.ACTION_UP) {
                btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                if (mMenuDlg != null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
            return true;
        }

        return false;
    }

    public void displayGreade(){
        String strGreade = mADDetailInfo.getStrGrade();
        float flGrade=0;

        Bitmap bitStar = BitmapFactory.decodeResource(getResources(), R.drawable.star_press);
        Bitmap bitHelfStar = BitmapFactory.decodeResource(getResources(), R.drawable.halfstar);

        if(strGreade!=null && !strGreade.equals("") && !strGreade.equals("0")) {
            flGrade = Float.valueOf(mADDetailInfo.getStrGrade());
            if (strGreade.startsWith("0")) {
                if (flGrade > 0 && flGrade < 1) {
                    ivGrade1.setImageBitmap(bitHelfStar);
                }
            } else if (strGreade.startsWith("1")) {
                if (flGrade == 1) {
                    ivGrade1.setImageBitmap(bitStar);
                } else if (flGrade > 1 && flGrade < 2) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitHelfStar);
                }
            } else if (strGreade.startsWith("2")) {
                if (flGrade == 2) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitStar);
                } else if (flGrade > 2 && flGrade < 3) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitStar);
                    ivGrade3.setImageBitmap(bitHelfStar);
                }
            } else if (strGreade.startsWith("3")) {
                if (flGrade == 3) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitStar);
                    ivGrade3.setImageBitmap(bitStar);
                } else if (flGrade > 3 && flGrade < 4) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitStar);
                    ivGrade3.setImageBitmap(bitStar);
                    ivGrade4.setImageBitmap(bitHelfStar);
                }
            } else if (strGreade.startsWith("4")) {
                if (flGrade == 4) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitStar);
                    ivGrade3.setImageBitmap(bitStar);
                    ivGrade4.setImageBitmap(bitStar);
                } else if (flGrade > 4 && flGrade < 5) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitStar);
                    ivGrade3.setImageBitmap(bitStar);
                    ivGrade4.setImageBitmap(bitStar);
                    ivGrade5.setImageBitmap(bitHelfStar);
                }
            } else if (strGreade.startsWith("5")) {
                if (flGrade == 5) {
                    ivGrade1.setImageBitmap(bitStar);
                    ivGrade2.setImageBitmap(bitStar);
                    ivGrade3.setImageBitmap(bitStar);
                    ivGrade4.setImageBitmap(bitStar);
                    ivGrade5.setImageBitmap(bitStar);
                }
            }

            txtGrade.setText(mADDetailInfo.getStrGrade());
        }else{
            txtGrade.setText("0.0");
        }
    }

    public void setDisplayData(){
        if(mADDetailInfo!=null) {
            if(pbTitleImg!=null && !pbTitleImg.isShown()) pbTitleImg.setVisibility(View.VISIBLE);
            String strTitleUrl = mADDetailInfo.getStrTitleImgUrl().replace("\\", "//");



            ImageLoader.loadImage(mActivity, strTitleUrl, ivTitle, pbTitleImg);

            /*
            pbTitleImg.setVisibility(View.GONE);
            Glide
                    .with(mActivity)
                    .load(strTitleUrl.replace("\\", "//"))
                    .centerCrop()
                    .placeholder(R.drawable.image_none)
                    .into(ivTitle);


            Glide.with(mActivity)
            .load(strTitleUrl)
            .centerCrop()
            .addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    if (pbTitleImg != null) {
                        pbTitleImg.setVisibility(View.GONE);
                    }
                    return false; // Glide가 오류를 처리하도록 false 반환
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    if (pbTitleImg != null) {
                        pbTitleImg.setVisibility(View.GONE);
                    }
                    return false; // Glide가 리소스를 처리하도록 false 반환
                }
            })
            .into(ivTitle);

             */

            txtADName.setText(mADDetailInfo.getStrADName());
            txtADDetail.setText(mADDetailInfo.getStrDetailTxt());
            strADTel = mADDetailInfo.getStrADTel();
            strHomepage = mADDetailInfo.getStrHomepageUrl();

            displayGreade();

            if(!mADDetailInfo.getStrDetailImgUrl1().equals("")){
                detailAddView(mADDetailInfo.getStrDetailImgUrl1());
            }
            if(!mADDetailInfo.getStrDetailImgUrl2().equals("")){
                detailAddView(mADDetailInfo.getStrDetailImgUrl2());
            }
            if(!mADDetailInfo.getStrDetailImgUrl3().equals("")){
                detailAddView(mADDetailInfo.getStrDetailImgUrl3());
            }

            if(strADKind.equals(mActivity.getResources().getString(R.string.str_user_en))) {
                strLike = mADDetailInfo.getStrADLike();
                if (strLike.equals(String.valueOf(StaticDataInfo.FALSE))) { //관심광고 아님
                    isLike = false;
                    btnAddMyAD.setText(mActivity.getResources().getString(R.string.str_interest_add));
                } else if (strLike.equals(String.valueOf(StaticDataInfo.TRUE))) { //관심광고
                    isLike = true;
                    btnAddMyAD.setText(mActivity.getResources().getString(R.string.str_interest_free));
                }
            }else{
                btnAddMyAD.setText(mActivity.getResources().getString(R.string.str_interest_add));
            }

            /*
            if(!mADDetailInfo.getStrADAddress().equals("")) {
                new getLatLng().execute(mADDetailInfo.getStrADAddress());
            }
             */

            chkDateDisplayBtn(mADDetailInfo.getStrDateS(), mADDetailInfo.getStrDateE(), false, getADInfo);
        }

    }

    /**
     * 날짜 및 상태 체크로 버튼 d/p
     * @param sDate
     * @param isRequestStart
     */
    public void chkDateDisplayBtn(String sDate, String eDate, boolean isRequestStart, ListADInfo getInfo){
        sDate = sDate.replace(mActivity.getResources().getString(R.string.str_category_item_gubun), " ");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            // 현재 시간을 msec으로 구한다.
            long now = System.currentTimeMillis();
            // 현재 시간을 저장 한다.
            Date today = new Date(now);
            today = dateFormatter.parse(dateFormatter.format(today));

            //dateS: 광고 시작일
            //today: 오늘
            Date dateS = dateFormatter.parse(sDate);
            Date dateE = dateFormatter.parse(eDate);
            int mCompareS = today.compareTo(dateS);
            int mCompareE = today.compareTo(dateE);

            if(strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_a))) { //승인완료
                if (mCompareS > 0) { //기간 지남
                    llThreeBtn.setVisibility(View.GONE);
                    llQTwo.setVisibility(View.GONE);
                    llTwoBtn.setVisibility(View.VISIBLE);
                }else{
                    llThreeBtn.setVisibility(View.GONE);
                    llQTwo.setVisibility(View.VISIBLE);
                    llTwoBtn.setVisibility(View.GONE);
                    btnADPush.setVisibility(View.GONE);
                }
            }else if(strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_i))) { //광고중
                llThreeBtn.setVisibility(View.GONE);
                llQTwo.setVisibility(View.GONE);
                llTwoBtn.setVisibility(View.VISIBLE);
            }else if(strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_p))) { //광고중지
                if(isRequestStart){
                    if (mCompareS > 0) { //기간 지남
                        llThreeBtn.setVisibility(View.GONE);
                        llQTwo.setVisibility(View.GONE);
                        llTwoBtn.setVisibility(View.VISIBLE);
                    }else{
                        llThreeBtn.setVisibility(View.GONE);
                        llQTwo.setVisibility(View.GONE);
                        llTwoBtn.setVisibility(View.VISIBLE);
                        btnADPush.setVisibility(View.GONE);
                    }
                }else {
                    llThreeBtn.setVisibility(View.VISIBLE);
                    if(mADDetailInfo.getStrADAmount()!=null && !mADDetailInfo.getStrADAmount().equals("0") && !mADDetailInfo.getStrADAmount().equals("")){
                        long lADAmount = Long.parseLong(mADDetailInfo.getStrADAmount().replace(",", "")); //광고 남은 금액
                        long lADMinAmount = 0;
                        if(mADDetailInfo!=null && !mADDetailInfo.getStrADMinAmount().equals("") && !mADDetailInfo.getStrADMinAmount().equals("0")) {
                            lADMinAmount = Long.parseLong(mADDetailInfo.getStrADMinAmount().replace(",", "")); //광고 최소 금액
                        }

                        if(lADMinAmount>lADAmount){
                            btnADRestart.setVisibility(View.GONE);
                        }
                    }else if (mCompareE > 0) { //종료기간 지남
                        btnADRestart.setVisibility(View.GONE);
                    }

                    llQTwo.setVisibility(View.GONE);
                    llTwoBtn.setVisibility(View.GONE);
                }
            }else if(strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_q))
                    || strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_eq))) { //승인요청, 심사재요쳥
                llQTwo.setVisibility(View.VISIBLE);

                if(getInfo!=null) {
                    if (getInfo.getStrIsDel().equals(String.valueOf(StaticDataInfo.FALSE))) {
                        btnRequestCancel.setVisibility(View.GONE);
                        llMargin.setVisibility(View.GONE);
                    }else{
                        btnRequestCancel.setVisibility(View.VISIBLE);
                        llMargin.setVisibility(View.VISIBLE);
                    }
                }
            }else if(strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_op))) { //광고 종료
                llThreeBtn.setVisibility(View.GONE);
                llQTwo.setVisibility(View.GONE);
                llTwoBtn.setVisibility(View.GONE);

                btnADReturn.setVisibility(View.GONE);
                btnADRestart.setVisibility(View.GONE);

            } else if (strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_cl)) || strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_r))) {//승인거부
                llQTwo.setVisibility(View.VISIBLE);
                btnRequestCancel.setVisibility(View.GONE);
                btnQModify.setVisibility(View.VISIBLE);

                llMargin.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void detailAddView(String strImgPath){
        strImgPath = strImgPath.replace("\\", "//");
        LayoutInflater inflaterDetailImg = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflaterDetailImg.inflate(R.layout.advertiser_make_ad_detail_img_add, null);


        ((LinearLayout) view.findViewById(R.id.ll_detail_img)).setVisibility(View.GONE);
        ImageView ivDetailImg = (ImageView) view.findViewById(R.id.iv_detail_img);
        ivDetailImg.setVisibility(View.VISIBLE);
        final String finalStrImgPath = strImgPath;
        ivDetailImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, DlgImgZoom.class);
                intent.putExtra("Path", finalStrImgPath);
                startActivity(intent);
            }
        });

        final ProgressBar pbDetailImg = (ProgressBar) view.findViewById(R.id.pb_detail_img);
        if(pbDetailImg!=null && !pbDetailImg.isShown()) pbDetailImg.setVisibility(View.VISIBLE);

        ImageLoader.loadImage(mActivity, strImgPath, ivDetailImg, pbDetailImg);
        llDetailImg.addView(view);
    }

    /** @Author : pppdw
     * @Description : 구글 URL을 이용해 간단하게 lng.lat를 뽑는다. new HttpGet 생성자에 사용된 URL로 뽑고자하는 지역의 네임값만 날리면된다.
     *                  단 네임값에 공백이 있으면 안되며, 공백이 존재 할 시 공백을 +로 변경하여 리퀘스트 요청을 해야한다.
     * @Param : strPlaceName --> 지오코딩 하고자 하는 지역의 이름 (예시 : "서울특별시+강남구+개포동+3421번지")
     **/

    private class getLatLng extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String strPlaceNameClone = null;
            strPlaceNameClone = params[0].replace(" ", "+");
            JSONObject jsonObject = new JSONObject();

            HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?address=" +strPlaceNameClone+"&ka&sensor=false");
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {

                e.printStackTrace();
            }

            Double lng = new Double(0);
            Double lat = new Double(0);
            String strLocation = "";

            try {
                lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                strLocation = String.valueOf(lat)+"/"+String.valueOf(lng);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return strLocation;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result==null || result.equals("")){
                ivMapErr.setVisibility(View.VISIBLE);
                llMap.setVisibility(View.GONE);
            }else {
                ivMapErr.setVisibility(View.GONE);
                llMap.setVisibility(View.VISIBLE);
                displayMap(result);
            }
        }
    }

    /**
     * map에 위치 표시
     * @param location
     */
    private LatLng position;
    private String[] strLocation = null;
    public void displayMap(String location) {
        strLocation = location.split("/");
        position = new LatLng(Double.parseDouble(strLocation[0]), Double.parseDouble(strLocation[1]));

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_ad);
        if (mapFragment == null) {
            ivMapErr.setVisibility(View.VISIBLE);
            llMap.setVisibility(View.GONE);
            return;
        }

        mapFragment.getMapAsync(this);
    }

    private GoogleMap mMap;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //맵 위치이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14));

        //마커설정
        MarkerOptions opt = new MarkerOptions();
        opt.position(position);
        mMap.addMarker(opt).showInfoWindow();

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.setOnMapClickListener(mMapClick);
    }

    private GoogleMap.OnMapClickListener mMapClick = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            // Create a Uri from an intent string. Use the result to create an Intent.
//            Uri gmmIntentUri = Uri.parse("google.streetview:cbll=" + position);
            if(strLocation!=null) {
                Uri gmmIntentUri = Uri.parse("geo:" + strLocation[0] + "," + strLocation[1]+"?q="+strLocation[0] + "," + strLocation[1]);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");
                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);
            }
        }
    };
    private final GoogleMap.OnCameraChangeListener mOnCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            svAD.requestDisallowInterceptTouchEvent(true);
        }
    };


    /**
     * 서버에 값 요청
     */
    private class ratingTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String retMsg="";
            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(mActivity.getResources().getString(R.string.str_new_url)+mActivity.getResources().getString(R.string.str_advertise_rating_get));

                SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
                final String token = pref.getString(mActivity.getResources().getString(R.string.str_token), "");

                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(mActivity.getResources().getString(R.string.str_token), token));
                listParams.add(new BasicNameValuePair(STR_AD_IDX, strADIdx));

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
            if(result.startsWith(StaticDataInfo.TAG_LIST)){
                ratingResult(result);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    public void ratingResult(String result){
        //result.endsWith("out") : 타임아웃
        if(result.startsWith(StaticDataInfo.TAG_LIST)){
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
                            if (parser.getName().equals(STR_AD_GRADE)) {
                                k_data_num = PARSER_NUM_0;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        strRating = parser.getText();
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }
}
