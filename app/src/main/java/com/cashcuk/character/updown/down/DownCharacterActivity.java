package com.cashcuk.character.updown.down;

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
import com.cashcuk.character.dlg.DlgCharaterCategory;
import com.cashcuk.character.dlg.DlgCharaterSet;
import com.cashcuk.character.updown.CharacterAdapter;
import com.cashcuk.character.updown.CharacterUpDownInfo;
import com.cashcuk.dialog.DlgBtnActivity;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 캐릭터 구매 (다운)
 */
public class DownCharacterActivity extends Activity {
    private ListView lvBuy;
    private CharacterAdapter adapter;
    private LinearLayout llEmpty;
    private String strCategoryIdx="";

    private ArrayList<TxtListDataInfo> arrCharCategory;
    private int mSelPoint = -1;

    private final int SEND_CATEGORY_IDX = 2;
    private final int SEND_PAGE_NO = 3;

    private final String STR_CATEGORY_IDX = "cat_idx";
    private final String STR_PAGE_NO = "pageno";

    private int mPageNo=1;

    private final String STR_CHAR_IDX = "char_idx";
    private final String STR_CHAR_IMG_URL = "char_img";
    private final String STR_CHAR_THUMBNAIL_IMG_URL = "char_minimg";
    private final String STR_CHAR_NAME = "sha_nm";
    private final String STR_CHAR_COMPANY_NAME = "sha_biznm";
    private final String STR_CHAR_SELLER_ID = "sha_sid";
    private final String STR_CHAR_DOWN_CHK = "sha_chk";

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;

    private LinearLayout llProgress;
    private final int REQUEST_DOWN = 234;
    private final int REQUEST_SEND_MSG = 777;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(DownCharacterActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(mPageNo==1) {
                        llEmpty.setVisibility(View.VISIBLE);
                        lvBuy.setVisibility(View.GONE);
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_200:
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
        setContentView(R.layout.activity_buy_character);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_down_character));
        llEmpty = (LinearLayout) findViewById(R.id.ll_character_empty);

        Intent getIntent = getIntent();
        if(getIntent!=null) {
            arrCharCategory = (ArrayList<TxtListDataInfo>) getIntent.getSerializableExtra("Category");
            mSelPoint = getIntent.getIntExtra("CategoryIndex", -1);
        }

        if(mSelPoint!=-1 && arrCharCategory!=null && arrCharCategory.size()>mSelPoint) {
            ((TextView) findViewById(R.id.txt_category_title)).setText(arrCharCategory.get(mSelPoint).getStrMsg());
            strCategoryIdx = arrCharCategory.get(mSelPoint).getStrIdx();
        }else{
            llEmpty.setVisibility(View.VISIBLE);
        }
        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelPoint!=-1 && arrCharCategory!=null && arrCharCategory.size()>mSelPoint) {
                    ((TextView) findViewById(R.id.txt_category_title)).setText(arrCharCategory.get(mSelPoint).getStrMsg());
                    strCategoryIdx = arrCharCategory.get(mSelPoint).getStrIdx();
                    mPageNo = 1;
                    DataRequest();
                }else{
                    llEmpty.setVisibility(View.VISIBLE);
                }
            }
        });

        lvBuy = (ListView) findViewById(R.id.lv_buy);
        ((Button) findViewById(R.id.btn_other_category)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownCharacterActivity.this, DlgCharaterCategory.class);
                intent.putExtra(StaticDataInfo.STR_CHARATER_DOWN_OTHER, StaticDataInfo.STR_CHARATER_DOWN);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        lvBuy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDownPos = position;
                if(arrDataInfo!=null && arrDataInfo.size()>0) {
                    if(arrDataInfo.get(position).getStrDownState().equals(String.valueOf(StaticDataInfo.TRUE))){
                        Toast.makeText(DownCharacterActivity.this, getResources().getString(R.string.str_char_down_success), Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(DownCharacterActivity.this, DownPreviewCharaterActivity.class);
                        intent.putExtra("ImgPath", arrDataInfo.get(position).getStrCharImg());
                        intent.putExtra("ImgIdx", arrDataInfo.get(position).getStrIdx());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, REQUEST_DOWN);
                    }
                }
            }
        });
    }

    private boolean isDown = false;
    private int mDownPos;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_DOWN){
                mPageNo=1;
                isDown = true;

                Intent intent = new Intent(DownCharacterActivity.this, DlgCharaterSet.class);
                intent.putExtra("CharIdx", data.getStringExtra("CharIdx"));
                intent.putExtra("SendMsgMode", "New");
                startActivityForResult(intent, REQUEST_SEND_MSG);
            }else if(requestCode == REQUEST_SEND_MSG){
                Intent intent = new Intent(DownCharacterActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_character_down_success));
                startActivity(intent);
            }
        }
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

        if(!isDown) {
            DataRequest();
        }else{
            arrDataInfo.get(mDownPos).setStrDownState("1");
            if(adapter!=null) adapter.notifyDataSetChanged();
        }
    }

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.\
    private boolean isListUpdate = false;
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
                lvBuy.setScrollContainer(false);
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

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(){
        mLockListView = true;
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_character_downlist);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

//        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_CATEGORY_IDX, strCategoryIdx);
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
                listParams.add(new BasicNameValuePair(STR_CATEGORY_IDX, params[SEND_CATEGORY_IDX]));
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
//            Message msg = new Message();
//            msg.what = -1;
//
            if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                resultData(result);
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    private CharacterUpDownInfo mDataInfo;
    private ArrayList<CharacterUpDownInfo> arrDataInfo;
    public void resultData(String result){
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && mDataInfo != null && mDataInfo != null) {
                                arrDataInfo.add(mDataInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            if(mPageNo==1) {
                                arrDataInfo = new ArrayList<CharacterUpDownInfo>();
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mDataInfo = new CharacterUpDownInfo();
                            }

                            if (parser.getName().equals(STR_CHAR_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_CHAR_IMG_URL)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_CHAR_NAME)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_CHAR_COMPANY_NAME)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_CHAR_SELLER_ID)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_CHAR_DOWN_CHK)) {
                                    k_data_num = PARSER_NUM_5;
                            } else if (parser.getName().equals(STR_CHAR_THUMBNAIL_IMG_URL)) {
                                k_data_num = PARSER_NUM_6;
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
                                        mDataInfo.setStrCharImg(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mDataInfo.setStrCharName(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mDataInfo.setStrCompayName(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mDataInfo.setStrSellerId(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mDataInfo.setStrDownState(parser.getText());
                                        break;
                                    case PARSER_NUM_6:
                                        mDataInfo.setStrCharThumbnail(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                displayDownCharacter(arrDataInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void displayDownCharacter(ArrayList<CharacterUpDownInfo> arrData){
        lvBuy.setVisibility(View.VISIBLE);
        llEmpty.setVisibility(View.GONE);

        if(mPageNo==1) {
            adapter = new CharacterAdapter(this, arrData, getResources().getString(R.string.str_char_download));
            lvBuy.setAdapter(adapter);
            lvBuy.setOnScrollListener(mListScroll);
        }else{
            if(adapter!=null) adapter.notifyDataSetChanged();
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
