package com.cashcuk.advertiser.sendpush;

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
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.adlist.SwipeDismissListViewTouchListener;
import com.cashcuk.advertiser.sendpush.view.PushSendViewActivity;
import com.cashcuk.dialog.DlgBtnActivity;

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
 * 전체 PUSH 현황 (PUSH 발송 현황)
 */

public class ADPushSendCurrentState extends Activity {
    private final int SEND_PAGE_NO = 2;
    private final int SEND_AD_IDX = 3;
    private final String STR_PAGE_NO = "pageno";
    private final String STR_AD_IDX = "ad_idx";

    private TextView txtTotalPushCost;

    private int mPageNo = 1;
    private String strADIdx = "";

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;
    private final int PARSER_NUM_7 = 7;

    private final String STR_PUSH_IDX = "push_idx";
    private final String STR_AD_NAME = "ad_name";
    private final String STR_SEND_NUM = "push_num";
    private final String STR_PUSH_STATE = "push_status";
    private final String STR_REJECT_CUZ = "push_rejbcoz";
    private final String STR_SEND_DATE = "push_date";
    private final String STR_PUSH_COST = "push_cost"; //개별 push의 총 금액
    private final String STR_TOTAL_COST = "total_cost"; //전체 push 발송 건에 대한 합계

    private ListView lvPush;
    private LinearLayout llADEmpty;
    private LinearLayout llProgress;

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.\
    private boolean isListUpdate = false;

    private String strKind = "";//전체현황인지 개별현황인지
    private String strPushIdx = "";

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_NO_DATA:
                    if(mPageNo==1){
                        lvPush.setVisibility(View.GONE);
                        llADEmpty.setVisibility(View.VISIBLE);
                    }else{
                        mLockListView=true;
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(ADPushSendCurrentState.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            }, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_ad_push_send_current_state_activity);
        CheckLoginService.mActivityList.add(this);

        View v = (View) findViewById(R.id.layout_push_total);
        ((TextView) v.findViewById(R.id.txt_title)).setText(getResources().getString(R.string.str_total_cost));
        txtTotalPushCost = (TextView) v.findViewById(R.id.txt_title_charge);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        Intent intent = getIntent();
        if(intent!=null){
            strKind = intent.getStringExtra("PushKind");
            ((TitleBar)findViewById(R.id.title_bar)).setTitle(strKind);
            strADIdx = intent.getStringExtra("AD_IDX");
        }

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        llADEmpty = (LinearLayout) findViewById(R.id.ll_push_empty);
        lvPush = (ListView) findViewById(R.id.lv_push);

        MainTitleBar mainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        final ImageButton ibRefresh = (ImageButton) mainTitleBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageNo = 1;
                DataRequest();
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


    private int mListPosition = -1;
    private final int REQUEST_REJECT_CUZ = 999;
    public AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mListPosition = position;
            Intent intent = null;
            if(arrData.get(position).getStrPushState().equals(getResources().getString(R.string.str_ad_status_chk_r_push))){
                intent = new Intent(ADPushSendCurrentState.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", arrData.get(position).getStrRejectCuz());
                intent.putExtra("BtnDlgOneText", getResources().getString(R.string.str_push_detail_view));
                strPushIdx = arrData.get(position).getStrPushIdx();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_REJECT_CUZ);
            }else{
                intent = new Intent(ADPushSendCurrentState.this, PushSendViewActivity.class);
                intent.putExtra("PUSH_IDX", arrData.get(position).getStrPushIdx());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_REJECT_CUZ){
                Intent intent = new Intent(ADPushSendCurrentState.this, PushSendViewActivity.class);
                intent.putExtra("PUSH_IDX", strPushIdx);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    public AbsListView.OnScrollListener mListScroll = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !mLockListView) {
                if(lastitemVisibleFlag){
                    isListUpdate = true;
                    DataRequest();
                }else if(firstitemVisibleFlag){
                    mPageNo = 1;
                    DataRequest();
                }
            }

            if(touchListener!=null && !mLockListView){
                lvPush.setScrollContainer(false);
                touchListener.setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount) >= totalItemCount;
            firstitemVisibleFlag = (firstVisibleItem==0) && view.getChildAt(0)!=null && view.getChildAt(0).getTop()==0;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_push_mine);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PAGE_NO, String.valueOf(mPageNo));
            k_param.put(SEND_AD_IDX, strADIdx);

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
                listParams.add(new BasicNameValuePair(STR_PAGE_NO, params[SEND_PAGE_NO]));
                if(strADIdx!=null && !strADIdx.equals("")){
                    listParams.add(new BasicNameValuePair(STR_AD_IDX, params[SEND_AD_IDX]));
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
            if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                resultData(result);
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

    private ADPushSendCurrentInfo mDataInfo;
    private ArrayList<ADPushSendCurrentInfo> arrData;
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
                        if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrData != null && mDataInfo != null) {
                            arrData.add(mDataInfo);
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_DOCUMENT:
                        if(mPageNo==1){
                            arrData = new ArrayList<ADPushSendCurrentInfo>();
                            arrData.clear();
                        }
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                            mDataInfo = new ADPushSendCurrentInfo();
                        }

                        if (parser.getName().equals(STR_PUSH_IDX)) {
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_AD_NAME)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_SEND_NUM)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_PUSH_STATE)) {
                            k_data_num = PARSER_NUM_3;
                        } else if (parser.getName().equals(STR_REJECT_CUZ)) {
                            k_data_num = PARSER_NUM_4;
                        } else if (parser.getName().equals(STR_SEND_DATE)) {
                            k_data_num = PARSER_NUM_5;
                        } else if (parser.getName().equals(STR_PUSH_COST)) {
                            k_data_num = PARSER_NUM_6;
                        } else if (parser.getName().equals(STR_TOTAL_COST)) {
                            k_data_num = PARSER_NUM_7;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    mDataInfo.setStrPushIdx(parser.getText());
                                    break;
                                case PARSER_NUM_1:
                                    mDataInfo.setStrADName(parser.getText());
                                    break;
                                case PARSER_NUM_2:
                                    mDataInfo.setStrSendNum(parser.getText());
                                    break;
                                case PARSER_NUM_3:
                                    mDataInfo.setStrPushState(parser.getText());
                                    break;
                                case PARSER_NUM_4:
                                    mDataInfo.setStrRejectCuz(parser.getText());
                                    break;
                                case PARSER_NUM_5:
                                    mDataInfo.setStrSendDate(parser.getText());
                                    break;
                                case PARSER_NUM_6:
                                    mDataInfo.setStrSendPushCost(parser.getText());
                                    break;
                                case PARSER_NUM_7:
                                    mDataInfo.setStrSendTotalCost(parser.getText());
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

    private ADPushSendCurrentStateAdapter mAdapter=null;

    public void display(){
        lvPush.setVisibility(View.VISIBLE);
        llADEmpty.setVisibility(View.GONE);
        if(mDataInfo.getStrSendTotalCost()==null || mDataInfo.getStrSendTotalCost().equals("")) {
            txtTotalPushCost.setText("0");
        }else{
            txtTotalPushCost.setText(StaticDataInfo.makeStringComma(mDataInfo.getStrSendTotalCost()));
        }
        if (mPageNo == 1) {
            mAdapter = new ADPushSendCurrentStateAdapter(ADPushSendCurrentState.this, arrData);
            lvPush.setAdapter(mAdapter);
            lvPush.setOnItemClickListener(mItemClick);
            lvPush.setOnScrollListener(mListScroll);
        } else {
            if (mAdapter != null) mAdapter.notifyDataSetChanged();
        }
        mPageNo++;
        mLockListView = false;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (llProgress != null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        }, 500);

    }
}
