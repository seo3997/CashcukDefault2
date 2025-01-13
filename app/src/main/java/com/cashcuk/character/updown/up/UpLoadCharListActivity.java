 package com.cashcuk.character.updown.up;

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
import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.adlist.SwipeDismissListViewTouchListener;
import com.cashcuk.character.CharacterInfo;
import com.cashcuk.character.view.CharacterGridAdapter;

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
 * 비구매 캐릭터 d/p 캐릭터 공유
 */
public class UpLoadCharListActivity extends Activity {
    private final String STR_CHARACTER_IDX = "cat_code";
    private final String STR_PAGE_NO = "pageno";
    private final String STR_SHAR_GUBUN = "shar_gubun";

    private final int SEND_CHARACTER_IDX = 2;
    private final int SEND_PAGE = 3;
    private final int SEND_SHAR_GUBUN = 4;

    private int mPageNo = 1;

    private LinearLayout llCharEmpty;
    private GridView gvChar;
    private CharacterGridAdapter mGVAdapter;
    private LinearLayout llProgress;

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.

    private final int REQUEST_UPLOAD = 321;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(UpLoadCharListActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if (mPageNo == 1) {
                        llCharEmpty.setVisibility(View.VISIBLE);
                        gvChar.setVisibility(View.GONE);
                    }
                    break;
            }

            mLockListView = false;
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
        setContentView(R.layout.activity_character_list);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(getResources().getString(R.string.str_my_character));

        ((FrameLayout) findViewById(R.id.fl_title)).setVisibility(View.GONE);
        gvChar = (GridView) findViewById(R.id.gv_image);
        llCharEmpty = (LinearLayout) findViewById(R.id.ll_character_empty);

        ((TextView) findViewById(R.id.txt_info)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.btn_other_character)).setVisibility(View.GONE);

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageNo=1;
                sendDataSet();
            }
        });

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
        if(!isUploadOk) {
            sendDataSet();
        }else{
            setResult(RESULT_OK);
            finish();
        }
    }

    public void sendDataSet() {
        if (llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        mLockListView = true;
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_character_page);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_CHARACTER_IDX, StaticDataInfo.STRING_N);
        k_param.put(SEND_PAGE, String.valueOf(mPageNo));
        k_param.put(SEND_SHAR_GUBUN, "N");

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
                HttpParams httpParams = new BasicHttpParams(); //접속을 하기 위한 기존 환경설정
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); //웹 통신 프로토콜 버전 설정
                HttpClient client = new DefaultHttpClient(httpParams); //접속 기능 객체
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_CHARACTER_IDX, params[SEND_CHARACTER_IDX]));
                listParams.add(new BasicNameValuePair(STR_PAGE_NO, params[SEND_PAGE]));
                listParams.add(new BasicNameValuePair(STR_SHAR_GUBUN, params[SEND_SHAR_GUBUN]));


                //접속 제한시간 설정
                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                //응답 제한시간 설정
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
                resultChar(result);
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                handler.sendEmptyMessage(Integer.parseInt(result));
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    private ArrayList<CharacterInfo> arrChar;
    private CharacterInfo mCharInfo=null;
//    private ArrayList<CharacterInfo> arrDelCharTemp = new ArrayList<CharacterInfo>();
//    private String[] strDelIdx;
//    private ArrayList<String> arrDelIdx;

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;

    private final String STR_CHAR_IDX = "char_idx"; //item idx
    private final String STR_CHAR_MIDDLE_IMG_URL = "char_middle"; //이미지 url
    private final String STR_CHAR_IMG_URL = "char_imgurl"; //이미지 url

    public void resultChar(String result) {
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && mCharInfo != null && mCharInfo != null) {
                                arrChar.add(mCharInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            if (mPageNo == 1) {
                                arrChar = new ArrayList<CharacterInfo>();
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mCharInfo = new CharacterInfo();
                                mCharInfo.setIsChk(false);
                            }

                            if (parser.getName().equals(STR_CHAR_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_CHAR_MIDDLE_IMG_URL)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_CHAR_IMG_URL)) {
                                k_data_num = PARSER_NUM_2;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mCharInfo.setStrIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mCharInfo.setStrMiddleImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mCharInfo.setStrImgUrl(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                displayChar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void displayChar() {
        llCharEmpty.setVisibility(View.GONE);
        gvChar.setVisibility(View.VISIBLE);

        if (mPageNo == 1) {
            mGVAdapter = new CharacterGridAdapter(this, arrChar);
            gvChar.setAdapter(mGVAdapter);

            mGVAdapter.modeState(false);

            gvChar.setClickable(true);
            gvChar.setFocusable(true);
            gvChar.setFocusableInTouchMode(true);
            gvChar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(UpLoadCharListActivity.this, UpLoadPreviewCharaterActivity.class);
                    intent.putExtra("CharInfo", arrChar.get(position));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, REQUEST_UPLOAD);
                }
            });
            gvChar.setOnScrollListener(mListScroll);
        } else {
            if (mGVAdapter != null) mGVAdapter.notifyDataSetChanged();
        }

        mLockListView = false;
        mPageNo++;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        }, 500);
    }

    public AbsListView.OnScrollListener mListScroll = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !mLockListView) {
                if (lastitemVisibleFlag) {
                    sendDataSet();
                } else if (firstitemVisibleFlag) {
                    mPageNo = 1;
                    sendDataSet();
                }
            }

            if (touchListener != null && !mLockListView) {
                gvChar.setScrollContainer(false);
                touchListener.setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount) >= totalItemCount;
            firstitemVisibleFlag = (firstVisibleItem == 0) && view.getChildAt(0) != null && view.getChildAt(0).getTop() == 0;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    private boolean isUploadOk = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_UPLOAD){
                isUploadOk = true;
            }
        }
    }
}
