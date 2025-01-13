package com.cashcuk.advertiser.adcost;

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
import android.util.TypedValue;
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
 * 광고현황
 */
public class TotalADCostList extends Activity {
    private String sMyADTotalCost="0";
    private ListView lvCost;
    private LinearLayout llListEmpty; //내역이 존재 하지 않을 때 d/p

    private TextView txtMyADTotalCost;
    private LinearLayout llProgress;

    private final int SEND_PAGE_NO = 2;
    private final String STR_PAGE_NO = "pageno";
    private int mPageNo = 1;

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;

    private final String STR_TOTAL_COST = "ad_sumamnt";
    private final String STR_AD_NAME = "ad_nm";
    private final String STR_AD_MONEY = "ad_willamnt";
    private final String STR_USE_AD_COST = "ad_usedamnt";
    private final String STR_RETURN_COST = "ad_return";
    private final String STR_VIEW_CNT = "ad_view";

    private TotalADCostInfo mTotalADCostInfo;
    private ArrayList<TotalADCostInfo> mArrTotalADCostInfo;
    private TotalADCostAdapter mTotalAdapter;

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.\
    private boolean isListUpdate = false;


    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent i = null;
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(TotalADCostList.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(mPageNo==1){
                        mArrTotalADCostInfo= new ArrayList<TotalADCostInfo>();
                        mArrTotalADCostInfo.clear();
                    }

                    mArrTotalADCostInfo.addAll((ArrayList<TotalADCostInfo>) msg.obj);
                    //arrTotalADCostInfo = (ArrayList<TotalADCostInfo>) msg.obj;
                    if(mArrTotalADCostInfo!=null){
                        if(mPageNo==1){
                            mTotalAdapter = new TotalADCostAdapter(TotalADCostList.this, mArrTotalADCostInfo);
                            lvCost.setAdapter(mTotalAdapter);
                            lvCost.setOnItemClickListener(mItemClick);
                            lvCost.setOnScrollListener(mListScroll);
                        }else{
                            mTotalAdapter.updateTotalCostList(mArrTotalADCostInfo);
                            //mTotalAdapter.notifyDataSetChanged();
                        }

                        mPageNo++;
                        mLockListView = false;
                    }

                    lvCost.setVisibility(View.VISIBLE);
                    llListEmpty.setVisibility(View.GONE);
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(mPageNo==1){
                        llListEmpty.setVisibility(View.VISIBLE);
                        lvCost.setVisibility(View.GONE);
                    }else{
                        mLockListView = true;
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
        setContentView(R.layout.advertiser_charge_list_activity);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageNo = 1;
                DataRequest();
            }
        });

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_total_ad_cost_list));

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);
        Intent intent = getIntent();
        sMyADTotalCost = StaticDataInfo.makeStringComma(intent.getStringExtra("MyADTotalCost"));

        View vMyADTotalCost = (View)findViewById(R.id.layout_charge);
        TextView txtTotalCostTitle = (TextView) vMyADTotalCost.findViewById(R.id.txt_title);
        txtTotalCostTitle.setText(getResources().getString(R.string.str_total_ad_cost));
        float scale = getResources().getDisplayMetrics().density;
        int txtSize = (int) (20*scale);
        txtTotalCostTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
        txtMyADTotalCost = (TextView) vMyADTotalCost.findViewById(R.id.txt_title_charge);

        lvCost = (ListView) findViewById(R.id.lv_charge);
        llListEmpty = (LinearLayout) findViewById(R.id.ll_list_empty);

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


    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest() {
        if (llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_advertisefee);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PAGE_NO, String.valueOf(mPageNo));

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
            resultData(result);
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
                lvCost.setScrollContainer(false);
                touchListener.setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount) >= totalItemCount;
            //firstitemVisibleFlag = (firstVisibleItem==0) && view.getChildAt(0)!=null && view.getChildAt(0).getTop()==0;
        }
    };

    public AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        }
    };


    /**
     * 결과 값 parsing
     *
     * @param result
     */
    public void resultData(String result) {

        Log.d("temp","result["+result+"]");
        ArrayList<TotalADCostInfo> arrTotalADCostInfo=null;

        Message msg = new Message();

        if (result.startsWith(StaticDataInfo.TAG_LIST)) {
            try {

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(result));

                int eventType = parser.getEventType();
                int k_data_num = 0;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.END_TAG:
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrTotalADCostInfo != null && mTotalADCostInfo != null) {
                                arrTotalADCostInfo.add(mTotalADCostInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrTotalADCostInfo = new ArrayList<TotalADCostInfo>();
                            arrTotalADCostInfo.clear();
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mTotalADCostInfo = new TotalADCostInfo();
                            }

                            if (parser.getName().equals(STR_TOTAL_COST)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_AD_NAME)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_AD_MONEY)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_USE_AD_COST)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_RETURN_COST)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_VIEW_CNT)) {
                                k_data_num = PARSER_NUM_5;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        txtMyADTotalCost.setText(StaticDataInfo.makeStringComma(parser.getText()));
                                        break;
                                    case PARSER_NUM_1:
                                        mTotalADCostInfo.setStrADName(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mTotalADCostInfo.setStrADMomey(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mTotalADCostInfo.setStrUseADCost(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mTotalADCostInfo.setStrReturn(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mTotalADCostInfo.setStrViewCnt(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                msg.what = StaticDataInfo.RESULT_CODE_200;
                msg.obj = arrTotalADCostInfo;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
            msg.what = StaticDataInfo.RESULT_NO_DATA;
        }else{
            msg.what = StaticDataInfo.RESULT_CODE_ERR;
        }

        if (msg != null && handler != null) {
            handler.sendMessage(msg);
        }
    }
}
