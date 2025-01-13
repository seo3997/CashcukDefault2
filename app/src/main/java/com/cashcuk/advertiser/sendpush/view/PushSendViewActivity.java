package com.cashcuk.advertiser.sendpush.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;

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
 * push view
 */
public class PushSendViewActivity extends Activity {
    private final String STR_PUSH_IDX = "push_idx";
    private final int SEND_PUSH_IDX = 2;

    private final String STR_PUSH_ADDR = "push_addr";
    private final String STR_PUSH_ADDR_SUB = "push_addr_sub";
    private final String STR_PUSH_INFO = "push_info";
    private final String STR_PUSH_INFO_SUB = "push_info_sub";
    private final String STR_PUSH_TARGET_NUM = "target_num";
    private final String STR_PUSH_SEND_NUM = "push_num";
    private final String STR_PUSH_COST = "unit_amnt";
    private final String STR_PUSH_STATE = "push_status";
    private final String STR_PUSH_IMG_URL = "push_url";
    private final String STR_PUSH_SEND_DATE = "send_date";
    private final String STR_PUSH_SEND_YN = "push_yn";
    private final String STR_AD_IDX = "ad_idx";

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

    private ADTargetSetViewLinear1 targetSetLinear; //1. 대상 설정
    private ADTargetChkViewLinear2 targetChkLinear; //2. 발송 대상
    private ViewPager mViewPager;
    private ViewPageAdapter mSectionsPagerAdapter;

    private final int THIS_PAGE_1 = 0; //대상 설정
    private final int THIS_PAGE_2 = 1; //세부 설정

    private String strPushIdx = "";
    private LinearLayout llProgress;
    private ImageView ivPage1; //대상 설정
    private ImageView ivPage2; //세부 설정

    private TextView txtTargetSet; //대상설정
    private LinearLayout llTargetSetUnder;
    private TextView txtSendTarget; //발송대상
    private LinearLayout llSendTargetUnder;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(PushSendViewActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.advertiser_send_push_view_activity);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        Intent intent = getIntent();
        if (intent != null) {
            strPushIdx = intent.getStringExtra("PUSH_IDX");
        }

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_push_detail_view));

        txtTargetSet = (TextView) findViewById(R.id.txt_ad_taget_set);
        llTargetSetUnder = (LinearLayout) findViewById(R.id.ll_ad_taget_set_under);
        txtSendTarget = (TextView) findViewById(R.id.txt_ad_send_target);
        llSendTargetUnder = (LinearLayout) findViewById(R.id.ll_ad_send_target_under);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        targetSetLinear = (ADTargetSetViewLinear1) findViewById(R.id.target_set_view_linear);
        targetChkLinear = (ADTargetChkViewLinear2) findViewById(R.id.target_chk_view_linear);

        ivPage1 = (ImageView) findViewById(R.id.iv_page1);
        ivPage2 = (ImageView) findViewById(R.id.iv_page2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ivPage1.setBackground(getResources().getDrawable(R.drawable.one_press));
            ivPage2.setBackground(getResources().getDrawable(R.drawable.one));
        }else{
            ivPage1.setBackgroundDrawable(getResources().getDrawable(R.drawable.one_press));
            ivPage2.setBackgroundDrawable(getResources().getDrawable(R.drawable.one));
        }

        mSectionsPagerAdapter = new ViewPageAdapter();
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == THIS_PAGE_1) {
                    ivPage1.setImageDrawable(getResources().getDrawable(R.drawable.one_press));
                    ivPage2.setImageDrawable(getResources().getDrawable(R.drawable.one));

                    llTargetSetUnder.setVisibility(View.VISIBLE);
                    llSendTargetUnder.setVisibility(View.GONE);
                    txtTargetSet.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
                    txtSendTarget.setBackgroundColor(getResources().getColor(R.color.color_white));
                } else if (position == THIS_PAGE_2) {
                    ivPage1.setImageDrawable(getResources().getDrawable(R.drawable.one));
                    ivPage2.setImageDrawable(getResources().getDrawable(R.drawable.one_press));

                    llTargetSetUnder.setVisibility(View.GONE);
                    llSendTargetUnder.setVisibility(View.VISIBLE);
                    txtTargetSet.setBackgroundColor(getResources().getColor(R.color.color_white));
                    txtSendTarget.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        DataRequest();
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


    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_push_view);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PUSH_IDX, strPushIdx);

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
                if(strPushIdx!=null&&!strPushIdx.equals("")) {
                    listParams.add(new BasicNameValuePair(STR_PUSH_IDX, params[SEND_PUSH_IDX]));
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
            if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                resultData(result);
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    private PushSendViewInfo mPushInfo;
    public void resultData(String result){
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
                        mPushInfo = new PushSendViewInfo();
                        mPushInfo.setStrPushIdx(strPushIdx);
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(STR_PUSH_ADDR)) {
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_PUSH_ADDR_SUB)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_PUSH_INFO)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_PUSH_INFO_SUB)) {
                            k_data_num = PARSER_NUM_3;
                        } else if (parser.getName().equals(STR_PUSH_TARGET_NUM)) {
                            k_data_num = PARSER_NUM_4;
                        } else if (parser.getName().equals(STR_PUSH_SEND_NUM)) {
                            k_data_num = PARSER_NUM_5;
                        } else if (parser.getName().equals(STR_PUSH_COST)) {
                            k_data_num = PARSER_NUM_6;
                        } else if (parser.getName().equals(STR_PUSH_STATE)) {
                            k_data_num = PARSER_NUM_7;
                        } else if (parser.getName().equals(STR_PUSH_IMG_URL)) {
                            k_data_num = PARSER_NUM_8;
                        } else if (parser.getName().equals(STR_PUSH_SEND_YN)) {
                            k_data_num = PARSER_NUM_9;
                        } else if (parser.getName().equals(STR_AD_IDX)) {
                            k_data_num = PARSER_NUM_10;
                        } else if (parser.getName().equals(STR_PUSH_SEND_DATE)) {
                            k_data_num = PARSER_NUM_11;
                        }else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    mPushInfo.setStrAddr(parser.getText());
                                    break;
                                case PARSER_NUM_1:
                                    mPushInfo.setStrAddrSub(parser.getText());
                                    break;
                                case PARSER_NUM_2:
                                    mPushInfo.setStrInfo(parser.getText());
                                    break;
                                case PARSER_NUM_3:
                                    mPushInfo.setStrInfoSub(parser.getText());
                                    break;
                                case PARSER_NUM_4:
                                    mPushInfo.setStrTargetNum(parser.getText());
                                    break;
                                case PARSER_NUM_5:
                                    mPushInfo.setStrPushSendNum(parser.getText());
                                    break;
                                case PARSER_NUM_6:
                                    mPushInfo.setStrSendCost(parser.getText());
                                    break;
                                case PARSER_NUM_7:
                                    mPushInfo.setStrPushState(parser.getText());
                                    break;
                                case PARSER_NUM_8:
                                    mPushInfo.setStrPushImgUrl(parser.getText());
                                    break;
                                case PARSER_NUM_9:
                                    mPushInfo.setStrPushYN(parser.getText());
                                    break;
                                case PARSER_NUM_10:
                                    mPushInfo.setStrADIdx(parser.getText());
                                    break;
                                case PARSER_NUM_11:
                                    mPushInfo.setStrPushDate(parser.getText());
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }

            display();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void display(){
        targetSetLinear.setData(mPushInfo);
        targetChkLinear.setData(mPushInfo);
//        mSectionsPagerAdapter.notifyDataSetChanged();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        },500);
    }

    class ViewPageAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int resID = 0;

            switch(position){
                case THIS_PAGE_1:
                    resID = R.id.target_set_view_linear;
                    break;
                case THIS_PAGE_2:
                    resID = R.id.target_chk_view_linear;
                    break;
            }
            return findViewById(resID);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }
}
