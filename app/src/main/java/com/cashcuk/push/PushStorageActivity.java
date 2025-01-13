package com.cashcuk.push;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainActivity;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.detailview.ADDetailViewActivity;
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
 * Push 보관함
 */
public class PushStorageActivity extends Activity {
    private ListView lvPush;
    private LinearLayout llPushEmpty;
    private PushAdapter mPushAdapter;
    private PushDataInfo mDataInfo;
    private ArrayList<PushDataInfo> arrDataInfo;
    private TextView txtMsg;
    private final int REQUEST_VIEW_AD = 999;
    private String strADIdx="";
    private String strPushIdx = "";
    private int mPageNo = 1;

    private final int SEND_PAGE_NUM = 2;
    private final String STR_PAGE_NUM = "pageno";

    private final String STR_AD_IDX = "ad_idx";
    private final String STR_PUSH_IDX = "push_idx";
    private final String STR_AD_NAME = "ad_name";
    private final String STR_DATE = "push_date";
    private final String STR_SAVE_POINT = "ad_point";
    private final String STR_AD_INFO_MSG = "sys_adcau";

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;

    private final String STR_MODE_PUSH_TIME="push_time";
    private final String STR_MODE_PUSH_STORAGE="storage";
    private String strMode = STR_MODE_PUSH_TIME;
    private LinearLayout llProgress;
    private boolean isPush = false; //push를 통해 접속

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = null;
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(PushStorageActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                case StaticDataInfo.RESULT_NO_DATA:
                    if(mPageNo==1) {
                        llPushEmpty.setVisibility(View.VISIBLE);
                        lvPush.setVisibility(View.GONE);
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
        setContentView(R.layout.activity_push_storage);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        Intent intent = getIntent();
        if (intent != null) {
            isPush = intent.getBooleanExtra("PUSH_MODE", false);
        }

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_push_list));
        lvPush = (ListView) findViewById(R.id.lv_push);
        llPushEmpty = (LinearLayout) findViewById(R.id.ll_push_empty);
        txtMsg = (TextView) findViewById(R.id.txt_msg);

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageNo=1;
                requestData(STR_MODE_PUSH_STORAGE);
            }
        });

        requestData(STR_MODE_PUSH_TIME);
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

    public void requestData(String mode){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        strMode = mode;
        String url="";
        if(mode.equals(STR_MODE_PUSH_STORAGE)) {
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_push_repository);
        }else if(mode.equals(STR_MODE_PUSH_TIME)) {
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_sysconfig);
        }
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        if(mode.equals(STR_MODE_PUSH_STORAGE)) {
            k_param.put(SEND_PAGE_NUM, String.valueOf(mPageNo));
        }

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
                if(strMode.equals(STR_MODE_PUSH_STORAGE)) {
                    listParams.add(new BasicNameValuePair(STR_PAGE_NUM, params[SEND_PAGE_NUM]));
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
                if(strMode.equals(STR_MODE_PUSH_STORAGE)) {
                    resultData(result);
                }else if(strMode.equals(STR_MODE_PUSH_TIME)){
                    resultPushTime(result);
                }
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    public void resultData(String result){
        //result.endsWith("out") : 타임아웃
        if(result.startsWith(StaticDataInfo.TAG_LIST)) {
            try {

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(result));

                int eventType = parser.getEventType();
                int k_data_num = 0;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.END_TAG:
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrDataInfo != null && mDataInfo != null) {
                                arrDataInfo.add(mDataInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            if (mPageNo == 1) {
                                arrDataInfo = new ArrayList<PushDataInfo>();
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mDataInfo = new PushDataInfo();
                            }

                            if (parser.getName().equals(STR_AD_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_AD_NAME)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_DATE)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_SAVE_POINT)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_AD_INFO_MSG)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_PUSH_IDX)) {
                                k_data_num = PARSER_NUM_5;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mDataInfo.setStrIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mDataInfo.setStrADName(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mDataInfo.setStrDate(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mDataInfo.setStrPoint(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        if(parser.getText()!=null && !parser.getText().equals("null")) {
                                            mDataInfo.setStrInfoMsg(parser.getText());
                                        }
                                        break;
                                    case PARSER_NUM_5:
                                        mDataInfo.setStrPushIdx(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                displayList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void displayList(){
        llPushEmpty.setVisibility(View.GONE);
        lvPush.setVisibility(View.VISIBLE);
        if(mPageNo==1) {
            mPushAdapter = new PushAdapter(PushStorageActivity.this, arrDataInfo);
            lvPush.setAdapter(mPushAdapter);
            lvPush.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    strADIdx = arrDataInfo.get(position).getStrIdx();
                    strPushIdx = arrDataInfo.get(position).getStrPushIdx();
                    String msg = arrDataInfo.get(position).getStrInfoMsg();
                    if(!msg.equals("")) {
                        Intent intent = new Intent(PushStorageActivity.this, DlgBtnActivity.class);
                        intent.putExtra("BtnDlgMsg", msg);
                        intent.putExtra("DlgMode", "Two");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, REQUEST_VIEW_AD);
                    }else{
                        Intent intent = new Intent(PushStorageActivity.this, ADDetailViewActivity.class);
                        intent.putExtra("AD_IDX", strADIdx);
                        intent.putExtra("PUSH_IDX", strPushIdx);
                        intent.putExtra("PUSH_MODE", true);
                        intent.putExtra("AD_KIND", getResources().getString(R.string.str_user_en));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
        }else{
            mPushAdapter.notifyDataSetChanged();
        }

        mPageNo++;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (llProgress != null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        }, 500);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_VIEW_AD){
                Intent intent = new Intent(PushStorageActivity.this, ADDetailViewActivity.class);
                intent.putExtra("AD_IDX", strADIdx);
                intent.putExtra("PUSH_IDX", strPushIdx);
                intent.putExtra("PUSH_MODE", true);
                intent.putExtra("AD_KIND", getResources().getString(R.string.str_user_en));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    private String strPushTime = "";
    public void resultPushTime(String result){
        //result.endsWith("out") : 타임아웃
        if(result.startsWith(StaticDataInfo.TAG_LIST)) {
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
                            if (parser.getName().equals(STR_MODE_PUSH_TIME)) {
                                k_data_num = PARSER_NUM_0;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        strPushTime = parser.getText();
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                txtMsg.setText(getResources().getString(R.string.str_push_info));
                requestData(STR_MODE_PUSH_STORAGE);
            } catch (Exception e) {
                e.printStackTrace();
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
}
