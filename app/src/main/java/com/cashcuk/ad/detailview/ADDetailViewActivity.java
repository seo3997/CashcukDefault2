package com.cashcuk.ad.detailview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainActivity;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.ad.adlist.ListADInfo;
import com.cashcuk.advertiser.makead.FrMakeADPreviewAdvertiserInfo3;
import com.cashcuk.common.CommonDataTask;
import com.cashcuk.common.SavePoint;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgChkPwdActivity;

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
 * 광고보기(상세보기)
 */
public class ADDetailViewActivity extends FragmentActivity {
    private LinearLayout llProgress;
    private ViewPager vpDetail;
    private DetailPagerAdapter mAdapter;

    private final int THIS_PAGE_1=1; //광고보기(상세)
    private final int THIS_PAGE_2=0; //2번째 페이지가 왼쪽에 보이기 위해서 값이 '0' 임 - 광고주 정보

    private LinearLayout llPage;
    private ImageView ivPage1; //광고주 정보
    private ImageView ivPage2; //광고보기(상세)

    private String strADIdx=""; //광고 idx
    private String strADStatus= "";//광고 상태
    private String strADKind=""; //광고주/일반
    private boolean isPush = false; //push를 통해 접속
    private String strPushIdx=""; //push idx

    private final int SEND_AD_IDX = 2;
    private final int SEND_AD_KIND = 3;

    private final String STR_AD_IDX = "ad_idx"; //광고 idx
    private final String STR_AD_KIND = "ad_au"; //광고주/일반

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;
    private final int PARSER_NUM_7 = 7;
    private final int PARSER_NUM_8 = 8;
    private final int PARSER_NUM_9 = 9;
    private final int PARSER_NUM_10 = 10;
    private final int PARSER_NUM_11 = 11;
    private final int PARSER_NUM_12 = 12;
    private final int PARSER_NUM_13 = 13;
    private final int PARSER_NUM_14 = 14;
    private final int PARSER_NUM_15 = 15;
    private final int PARSER_NUM_16 = 16;
    private final int PARSER_NUM_17 = 17;
    private final int PARSER_NUM_18 = 18;
    private final int PARSER_NUM_19 = 19;

    private final String STR_AD_TITLE_IMG = "ad_titleimg"; //광고 title 이미지 url
    private final String STR_AD_GRADE = "ad_rating"; //평점
    private final String STR_AD_NAME = "ad_nm"; //광고명
    private final String STR_AD_DETAIL = "ad_txt"; //광고간단설명
    private final String STR_AD_TEL = "ad_tel"; //광고주 전화번호
    private final String STR_AD_HOMEPAGE_URL = "ad_url"; //광고 홈페이지 url
    private final String STR_AD_DETAIL_IMG1 = "ad_dtlimg1"; //광고 세부이미지1 url
    private final String STR_AD_DETAIL_IMG2 = "ad_dtlimg2"; //광고 세부이미지2 url
    private final String STR_AD_DETAIL_IMG3 = "ad_dtlimg3"; //광고 세부이미지3 url
    private final String STR_AD_DATE_S = "ad_str"; //광고 시작날
    private final String STR_AD_DATE_E = "ad_end"; //광고 마감날
    private final String STR_AD_ADDRESS = "ad_geo"; //광고 주소
    private final String STR_ADVERTISER_IMG = "biz_img";//광고주 사진
    private final String STR_ADVERTISER_TRADE_NM = "biz_nm"; //상호명
    private final String STR_ADVERTISER_REPRESENTATIVE_NM = "biz_rep"; //대표자
    private final String STR_ADVERTISER_REPRESENTATIVE_TEL = "biz_tel"; //대표전화
    private final String STR_ADVERTISER_ADDRESS = "biz_geo"; //광고주 주소
    private final String STR_AD_AMOUNT= "ad_amount"; //광고 남은 금액(광고주 일때만 return, 재시작 버튼 d/p위해 필요)
    private final String STR_AD_MIN_AMOUNT = "ad_min_amount"; //광고 최소 금액(광고주 일때만 return, 재시작 버튼 d/p위해 필요)
    private final String STR_AD_LIKE = "ad_like"; //관심광고 여부 (0: 관심광고 아님, 1: 관심광고)

    public static final int SAVE_POINT = 111;
    private final int REQUEST_PWD_CHK = 123;

    private ListADInfo getADInfo = new ListADInfo();

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    if(msg.arg1!=SAVE_POINT){
                        Toast.makeText(ADDetailViewActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(ADDetailViewActivity.this, getResources().getString(R.string.str_http_error_no_save), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1 == SAVE_POINT) {
                        Toast.makeText(ADDetailViewActivity.this, getResources().getString(R.string.str_save_point), Toast.LENGTH_SHORT).show();
                    }else {
                        if(mAdapter==null) {
                            mAdapter = new DetailPagerAdapter(getSupportFragmentManager());
                            vpDetail.setAdapter(mAdapter);
                            vpDetail.setCurrentItem(THIS_PAGE_1);
                            vpDetail.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                }

                                @Override
                                public void onPageSelected(int position) {
                                    if (position == THIS_PAGE_1) {
                                        ivPage1.setImageDrawable(getResources().getDrawable(R.drawable.one_press));
                                        ivPage2.setImageDrawable(getResources().getDrawable(R.drawable.one));
                                    } else if (position == THIS_PAGE_2) {
                                        ivPage1.setImageDrawable(getResources().getDrawable(R.drawable.one));
                                        ivPage2.setImageDrawable(getResources().getDrawable(R.drawable.one_press));
                                    }
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {

                                }
                            });

                        }else{
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case StaticDataInfo.RESULT_OVER_SAVE_POINT:
                    /*
                    Intent intent = new Intent(ADDetailViewActivity.this, DlgBtnActivity.class);
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_save_point_over));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                     */
                    break;
                case StaticDataInfo.RESULT_NO_SAVE_POINT:
                    Toast.makeText(ADDetailViewActivity.this, getResources().getString(R.string.str_no_save_point), Toast.LENGTH_SHORT).show();
                    break;
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },300);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_ad_detail);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        Intent intent = getIntent();
        if(intent!=null){
            strADIdx = intent.getStringExtra("AD_IDX");
            strADKind = intent.getStringExtra("AD_KIND");
            getADInfo = (ListADInfo) intent.getSerializableExtra("AD_INFO");

            isPush = intent.getBooleanExtra("PUSH_MODE", false);
            if(isPush){
                strPushIdx = intent.getStringExtra("PUSH_IDX");
                new pushChkTask().execute();
            }
            if(strADKind.equals(getResources().getString(R.string.str_advertiser_en))) {
                strADStatus = intent.getStringExtra("AD_STATUS");
            }else{
                new SavePoint(ADDetailViewActivity.this, strADIdx, handler);
            }
        }

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);
        ivPage1 = (ImageView) findViewById(R.id.iv_page1);
        ivPage2 = (ImageView) findViewById(R.id.iv_page2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ivPage1.setBackground(getResources().getDrawable(R.drawable.one_press));
            ivPage2.setBackground(getResources().getDrawable(R.drawable.one));
        }else{
            ivPage1.setBackgroundDrawable(getResources().getDrawable(R.drawable.one_press));
            ivPage2.setBackgroundDrawable(getResources().getDrawable(R.drawable.one));
        }

        llPage = (LinearLayout) findViewById(R.id.ll_page);
        llPage.setVisibility(View.VISIBLE);
        vpDetail = (ViewPager) findViewById(R.id.vp_ad_detail);

        getADData();
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

    public class DetailPagerAdapter extends FragmentStatePagerAdapter {
        private int TOTAL_PAGE = 2;
        private int mThisPage = THIS_PAGE_1;

        public DetailPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            mThisPage = position;
            switch (position) {
                case THIS_PAGE_1:
                    fragment = new FrADDetailMain1();
                    targetFragment = (FrADDetailMain1) fragment;
                    break;
                case THIS_PAGE_2:
                    fragment = new FrMakeADPreviewAdvertiserInfo3();
                    break;
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("AD_IDX", strADIdx);
            bundle.putSerializable("AD_DATA", mADDetailInfo);
            bundle.putString("AD_KIND", strADKind);
            bundle.putSerializable("AD_INFO", getADInfo);
            bundle.putString("AD_STATUS", strADStatus);
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getCount() {
            return TOTAL_PAGE;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void getADData(){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_advertiseview);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<String, String> k_param = new HashMap<String, String>();
        k_param.put(getResources().getString(R.string.str_token), token);
        k_param.put(STR_AD_IDX, strADIdx);
        k_param.put(STR_AD_KIND, strADKind);

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
                ResultADList(result);
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
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_AD_IDX, params[SEND_AD_IDX]));
                listParams.add(new BasicNameValuePair(STR_AD_KIND, params[SEND_AD_KIND]));

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
            ResultADList(result);
        }
    }
*/

    private ADDetailInfo mADDetailInfo;
    public void ResultADList(String result){
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
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mADDetailInfo = new ADDetailInfo();
                            }

                            if (parser.getName().equals(STR_AD_TITLE_IMG)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_AD_GRADE)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_AD_NAME)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_AD_DETAIL)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_AD_TEL)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_AD_HOMEPAGE_URL)) {
                                k_data_num = PARSER_NUM_5;
                            } else if (parser.getName().equals(STR_AD_DETAIL_IMG1)) {
                                k_data_num = PARSER_NUM_6;
                            } else if (parser.getName().equals(STR_AD_DETAIL_IMG2)) {
                                k_data_num = PARSER_NUM_7;
                            } else if (parser.getName().equals(STR_AD_DETAIL_IMG3)) {
                                k_data_num = PARSER_NUM_8;
                            } else if (parser.getName().equals(STR_AD_DATE_S)) {
                                k_data_num = PARSER_NUM_9;
                            } else if (parser.getName().equals(STR_AD_ADDRESS)) {
                                k_data_num = PARSER_NUM_10;
                            } else if (parser.getName().equals(STR_ADVERTISER_IMG)) {
                                k_data_num = PARSER_NUM_11;
                            } else if (parser.getName().equals(STR_ADVERTISER_TRADE_NM)) {
                                k_data_num = PARSER_NUM_12;
                            } else if (parser.getName().equals(STR_ADVERTISER_REPRESENTATIVE_NM)) {
                                k_data_num = PARSER_NUM_13;
                            } else if (parser.getName().equals(STR_ADVERTISER_REPRESENTATIVE_TEL)) {
                                k_data_num = PARSER_NUM_14;
                            } else if (parser.getName().equals(STR_ADVERTISER_ADDRESS)) {
                                k_data_num = PARSER_NUM_15;
                            } else if (parser.getName().equals(STR_AD_LIKE)) {
                                k_data_num = PARSER_NUM_16;
                            } else if (parser.getName().equals(STR_AD_DATE_E)) {
                                k_data_num = PARSER_NUM_17;
                            } else if (parser.getName().equals(STR_AD_AMOUNT)) {
                                k_data_num = PARSER_NUM_18;
                            } else if (parser.getName().equals(STR_AD_MIN_AMOUNT)) {
                                k_data_num = PARSER_NUM_19;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mADDetailInfo.setStrTitleImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mADDetailInfo.setStrGrade(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mADDetailInfo.setStrADName(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mADDetailInfo.setStrDetailTxt(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mADDetailInfo.setStrADTel(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mADDetailInfo.setStrHomepageUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_6:
                                        mADDetailInfo.setStrDetailImgUrl1(parser.getText());
                                        break;
                                    case PARSER_NUM_7:
                                        mADDetailInfo.setStrDetailImgUrl2(parser.getText());
                                        break;
                                    case PARSER_NUM_8:
                                        mADDetailInfo.setStrDetailImgUrl3(parser.getText());
                                        break;
                                    case PARSER_NUM_9:
                                        mADDetailInfo.setStrDateS(parser.getText());
                                        break;
                                    case PARSER_NUM_10:
                                        mADDetailInfo.setStrADAddress(parser.getText());
                                        break;
                                    case PARSER_NUM_11:
                                        mADDetailInfo.setStrAdvertiserImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_12:
                                        mADDetailInfo.setStrTradeName(parser.getText());
                                        break;
                                    case PARSER_NUM_13:
                                        mADDetailInfo.setStrRepresentativeName(parser.getText());
                                        break;
                                    case PARSER_NUM_14:
                                        mADDetailInfo.setStrStrRepresentativeTel(parser.getText());
                                        break;
                                    case PARSER_NUM_15:
                                        mADDetailInfo.setStrAdvertiserAddres(parser.getText());
                                        break;
                                    case PARSER_NUM_16:
                                        mADDetailInfo.setStrADLike(parser.getText());
                                        break;
                                    case PARSER_NUM_17:
                                        mADDetailInfo.setStrDateE(parser.getText());
                                        break;
                                    case PARSER_NUM_18:
                                        mADDetailInfo.setStrADAmount(parser.getText());
                                        break;
                                    case PARSER_NUM_19:
                                        mADDetailInfo.setStrADMinAmount(parser.getText());
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
        }else{
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }
    private FrADDetailMain1 targetFragment;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(targetFragment!=null) {
                if (requestCode == targetFragment.REQUEST_STOP_OK){ //중지
                    targetFragment.requestMode(targetFragment.STR_AD_STOP);
                }else if(requestCode == targetFragment.REQUEST_RETURN){ //환급
                    Intent intent = new Intent(ADDetailViewActivity.this, DlgChkPwdActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, REQUEST_PWD_CHK);
                }else if(requestCode == targetFragment.REQUEST_AD_CANCEL){
                    targetFragment.requestMode(targetFragment.STR_AD_REQUEST_CANCEL);
                }else if(requestCode == REQUEST_PWD_CHK){
                    targetFragment.requestMode(targetFragment.STR_AD_RETURN);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(isPush) {
            CheckLoginService.Close_All();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            super.onBackPressed();
        }
    }

    /**
     * 서버에 값 요청
     */
    private class pushChkTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_push_chk);
            SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
            final String token = pref.getString(getResources().getString(R.string.str_token), "");

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(url);

                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), token));
                if(isPush && strPushIdx!=null && !strPushIdx.equals("")){
                    listParams.add(new BasicNameValuePair("push_idx", strPushIdx));
                }

                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(listParams, HTTP.UTF_8);
                post.setEntity(ent);
                HttpResponse responsePOST = client.execute(post);
            } catch (Exception e) {
            }

            return null;
        }
    }
}
