package com.cashcuk.advertiser.myad;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.adlist.ListADAdapter;
import com.cashcuk.ad.adlist.ListADInfo;
import com.cashcuk.ad.adlist.SwipeDismissListViewTouchListener;
import com.cashcuk.ad.detailview.ADDetailViewActivity;
import com.cashcuk.advertiser.makead.MakeADMainActivity;
import com.cashcuk.common.CommCode;
import com.cashcuk.common.CommCodeAdapter;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgListAdapter;
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
 * 내 광고
 */
public class MyADActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private ImageButton ibTitleBarMenu;
    private LinearLayout llProgress;

    private RelativeLayout rlCategory1; //1차 분류 layout
    private RelativeLayout rlCategory2; //2차 분류 layout
    private RelativeLayout rlCategory3; //3차 분류 layout
    private LinearLayout llLowCategory; //2차 분류 & 3차 분류 layout
    private TextView txtCategory1;
    private TextView txtCategory2;
    private TextView txtCategory3;
    private ListView lvAd; //광고 list
    private LinearLayout llADEmpty; //광고 없을 시 d/p
    private ImageView ivSearch; //검색 이미지

    private final int DIALOG_MODE_1 = 0; //1차분류 dlg
    private final int DIALOG_MODE_2 = 1; //2차분류 dlg
    private final int DIALOG_MODE_3 = 2; //3차분류 dlg

    private ArrayList<TxtListDataInfo> arrData1; //1차 분류 data
    private ArrayList<TxtListDataInfo> arrData2; //2차 분류 data
    private ArrayList<TxtListDataInfo> arrData3; //3차 분류 data
    private TxtListDataInfo mStepData;
    private String strDataType = "";

    private String strDlgChildTitle1; //1차 선택 된 text
    private String strDlgChildTitle2; //2차 선택 된 text
    private String strGroupIdx; //1차 분류 idx
    private String strChildIdx1 = ""; //2차 분류 idx
    private String strChildIdx2 = ""; //3차 분류 idx

    private boolean isSelSiGun = false;
    private boolean isFirst = true;

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.\
    private boolean isListUpdate = false;

    private ListADInfo mListADInfo;
    private ArrayList<ListADInfo> arrListADInfo;
    private ListADAdapter lvAdapter = null;

    private int mPageNo = 1; //요청 페이지

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

    private final int SEND_PAGE_NO = 2;
    private final int SEND_SCH_STEP1 = 3;
    private final int SEND_SCH_STEP2 = 4;
    private final int SEND_SCH_STEP3 = 5;

    private final String STR_PAGE_NO = "pageno";
    private final String STR_SCH_STEP1 = "ad_depth1";
    private final String STR_SCH_STEP2 = "ad_depth2";
    private final String STR_SCH_STEP3 = "ad_depth3";

    //서버 리턴 값
    private final String STR_AD_IDX = "ad_idx"; //광고 idx
    private final String STR_AD_NM = "ad_nm"; //광고명
    private final String STR_AD_TXT = "ad_txt"; //광고 설명
    private final String STR_AD_TITLE_IMG_URL = "ad_titleimg"; //광고title 이미지 url
    private final String STR_AD_POINT = "ad_pnt"; //광고 클릭 시 적립포인트
    private final String STR_AD_EVENT = "ad_event"; //행사여부
    private final String STR_AD_STATUS = "ad_status"; //심사여부
    private final String STR_AD_REJECT_CUZ = "ad_rejbcoz"; //승인거부 사유
    private final String STR_AD_IS_STOP = "ad_stop"; //광고 중지 가능 여부(모바일에서 REQA(승인요청)일 때 사용. 0: 취소버튼 d/p, 1: 중지 버튼 d/p)

    private final int REQUEST_REJECT = 999;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_NO_DATA:
                    if(isListUpdate){
                        isListUpdate=false;
                    }else if(msg.arg1 == StaticDataInfo.RESULT_NO_DATA && mPageNo==1) {
                        if (arrListADInfo != null) arrListADInfo.clear();

                        lvAd.setVisibility(View.GONE);
                        llADEmpty.setVisibility(View.VISIBLE);
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                        Toast.makeText(MyADActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_SIGUN:
                    isSelSiGun = true;
                    if(arrData3!=null) arrData3.clear();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if (msg.arg1 == PARSER_NUM_1 && ((ArrayList<TxtListDataInfo>) msg.obj).size()>0) {
                        arrData1 = new ArrayList<TxtListDataInfo>();
                        arrData1.add(mStepData);
                        arrData1.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                    } else if (msg.arg1 == PARSER_NUM_2 && ((ArrayList<TxtListDataInfo>) msg.obj).size()>0) {
                        arrData2 = new ArrayList<TxtListDataInfo>();
                        arrData2.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                    } else if (msg.arg1 == PARSER_NUM_3 && (((ArrayList<TxtListDataInfo>) msg.obj).size()>0) && !txtCategory2.getText().toString().equals("")) {
                        arrData3 = new ArrayList<TxtListDataInfo>();
                        arrData3.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                    } else if (arrListADInfo != null && arrListADInfo.size() > 0) {
                        lvAd.setVisibility(View.VISIBLE);
                        llADEmpty.setVisibility(View.GONE);

                        if (mPageNo == 1) {
                            lvAdapter = new ListADAdapter(MyADActivity.this, arrListADInfo, "", handler);
                            lvAd.setAdapter(lvAdapter);
                            lvAd.setOnItemClickListener(mItemClick);
                            lvAd.setOnScrollListener(mListScroll);
                        } else {
                            if (lvAdapter != null) lvAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
            }

            mLockListView = false;
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
        setContentView(R.layout.advertiser_my_ad_activity);
        CheckLoginService.mActivityList.add(this);
        isFirst = true;

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        TitleBar titlebar = (TitleBar) findViewById(R.id.title_bar);
        titlebar.setTitle(getResources().getString(R.string.str_my_ad));
        ibTitleBarMenu = (ImageButton) titlebar.findViewById(R.id.ib_menu);
        ibTitleBarMenu.setVisibility(View.VISIBLE);
        ibTitleBarMenu.setOnClickListener(this);

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);
        llADEmpty = (LinearLayout) findViewById(R.id.ll_ad_empty);
        lvAd = (ListView) findViewById(R.id.lv_my_ad);

        ((LinearLayout) findViewById(R.id.ll_search)).setOnClickListener(this);
        ivSearch = (ImageView) findViewById(R.id.iv_search);
        RelativeLayout rlCategory1 = (RelativeLayout) findViewById(R.id.rl_category1);
        RelativeLayout rlCategory2 = (RelativeLayout) findViewById(R.id.rl_category2);
        RelativeLayout rlCategory3 = (RelativeLayout) findViewById(R.id.rl_category3);
        rlCategory1.setOnClickListener(this);
        rlCategory2.setOnClickListener(this);
        rlCategory3.setOnClickListener(this);

        llLowCategory = (LinearLayout) findViewById(R.id.ll_low_category);
        txtCategory1 = (TextView) findViewById(R.id.txt_category1);
        txtCategory2 = (TextView) findViewById(R.id.txt_category2);
        txtCategory3 = (TextView) findViewById(R.id.txt_category3);

        MenuList();

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageNo = 1;
                isFirst = true;
                getADList();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_REJECT){
                if (mListPosition != -1 && arrListADInfo != null) {

                    Intent intent = new Intent(MyADActivity.this, MakeADMainActivity.class);
                    intent.putExtra("AD_IDX", arrListADInfo.get(mListPosition).getStrIdx());
                    intent.putExtra("AD_STATUS", arrListADInfo.get(mListPosition).getStrStatus());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getStepData();
        mPageNo = 1;
        getADList();
    }

    public void getStepData(){
        mStepData = new TxtListDataInfo();
        mStepData.setStrIdx("0");
        mStepData.setStrMsg(getResources().getString(R.string.str_sel_ad_send_default_info));
        strDataType = StaticDataInfo.COMMON_CODE_TYPE_AD;

        strGroupIdx = mStepData.getStrIdx();
        txtCategory1.setText(mStepData.getStrMsg());

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        new CommCode(MyADActivity.this, strDataType, PARSER_NUM_1, "", handler);
    }

    private int mListPosition = -1;
    public AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mListPosition = position;
            Intent intent = null;
            if (mListPosition != -1 && arrListADInfo != null) {
                if (arrListADInfo.get(mListPosition).getStrStatus().equals(getResources().getString(R.string.str_ad_status_chk_r))) {
                    intent = new Intent(MyADActivity.this, DlgBtnActivity.class);
                    intent.putExtra("BtnDlgMsg", arrListADInfo.get(mListPosition).getStrRejectCuz());
                    intent.putExtra("BtnDlgOneText", getResources().getString(R.string.str_rejcet_modify_ad));
                    intent.putExtra("DlgMode", "Two");
                    intent.putExtra("BtnDlgCancelText", getResources().getString(R.string.str_close));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, REQUEST_REJECT);
                } else {
                    intent = new Intent(MyADActivity.this, ADDetailViewActivity.class);
                    intent.putExtra("AD_IDX", arrListADInfo.get(mListPosition).getStrIdx());
                    intent.putExtra("AD_STATUS", arrListADInfo.get(mListPosition).getStrStatus());
                    intent.putExtra("AD_INFO", arrListADInfo.get(mListPosition));
                    intent.putExtra("AD_KIND", getResources().getString(R.string.str_advertiser_en));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        }
    };

    public AbsListView.OnScrollListener mListScroll = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !mLockListView) {
                if(lastitemVisibleFlag){
                    isListUpdate = true;
                    mPageNo++;
                    getADList();
                }else if(firstitemVisibleFlag){
                    mPageNo = 1;
                    getADList();
                }
            }

            if(touchListener!=null && !mLockListView){
                lvAd.setScrollContainer(false);
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
     * 상단 오른쪽 메뉴 (내 상품, 광고제작)
     */
    private Dialog mMenuDlg;
    private ArrayList<String> arrString;
    private Button btn1; //취소
    public void MenuList() {
        mMenuDlg = new Dialog(MyADActivity.this);
        mMenuDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMenuDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mMenuDlg.setContentView(R.layout.dlg_list_title);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mMenuDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mMenuDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ((TextView) mMenuDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_ad_title));
        ListView lvDlgMsg = (ListView) mMenuDlg.findViewById(R.id.lv_dlg);
        arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_my_ad));
        arrString.add(getResources().getString(R.string.str_make_ad));

        btn1 = (Button) mMenuDlg.findViewById(R.id.btn1);
        btn1.setOnTouchListener(this);
        ((LinearLayout) mMenuDlg.findViewById(R.id.ll1)).setOnTouchListener(this);

        DlgListAdapter dlgAdapter = new DlgListAdapter(MyADActivity.this, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (arrString.get(position).equals(getResources().getString(R.string.str_my_ad))) {
                    intent = new Intent(MyADActivity.this, MyADActivity.class);
                    finish();
                } else if (arrString.get(position).equals(getResources().getString(R.string.str_make_ad))) {
                    intent = new Intent(MyADActivity.this, MakeADMainActivity.class);
            }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                if (mMenuDlg != null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll1 || v.getId()==R.id.btn1){
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

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.ib_menu) {
            if (mMenuDlg != null && !mMenuDlg.isShowing()) mMenuDlg.show();
        } else if (viewId == R.id.rl_category1) {
            OpenDlg(DIALOG_MODE_1);
        } else if (viewId == R.id.rl_category2) {
            OpenDlg(DIALOG_MODE_2);
        } else if (viewId == R.id.rl_category3) {
            OpenDlg(DIALOG_MODE_3);
        } else if (viewId == R.id.ll_search) {
            mPageNo = 1;
            getADList();
        }
    }

    public void getADList(){
        isFirst = false;

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        mLockListView = true;

        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_list);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PAGE_NO, String.valueOf(mPageNo));
        k_param.put(SEND_SCH_STEP1, strGroupIdx);
        k_param.put(SEND_SCH_STEP2, strChildIdx1);
        k_param.put(SEND_SCH_STEP3, strChildIdx2);

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
                listParams.add(new BasicNameValuePair(STR_SCH_STEP1, params[SEND_SCH_STEP1]));
                if(!strChildIdx1.equals("")) {
                    listParams.add(new BasicNameValuePair(STR_SCH_STEP2, params[SEND_SCH_STEP2]));
                }
                if(!strChildIdx2.equals("")){
                    listParams.add(new BasicNameValuePair(STR_SCH_STEP3, params[SEND_SCH_STEP3]));
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
            ResultADList(result);
        }
    }

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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrListADInfo != null && mListADInfo != null) {
                                arrListADInfo.add(mListADInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            if(mPageNo==1){
                                arrListADInfo = new ArrayList<ListADInfo>();
                                arrListADInfo.clear();
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mListADInfo = new ListADInfo();
                            }

                            if (parser.getName().equals(STR_AD_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_AD_NM)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_AD_TXT)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_AD_TITLE_IMG_URL)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_AD_POINT)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_AD_EVENT)) {
                                k_data_num = PARSER_NUM_5;
                            } else if (parser.getName().equals(STR_AD_STATUS)) {
                                k_data_num = PARSER_NUM_6;
                            } else if (parser.getName().equals(STR_AD_REJECT_CUZ)) {
                                k_data_num = PARSER_NUM_7;
                            } else if (parser.getName().equals(STR_AD_IS_STOP)) {
                                k_data_num = PARSER_NUM_8;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mListADInfo.setStrIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mListADInfo.setStrName(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mListADInfo.setStrDetail(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mListADInfo.setStrTitleImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mListADInfo.setStrPoint(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mListADInfo.setStrEventYN(parser.getText());
                                        break;
                                    case PARSER_NUM_6:
                                        mListADInfo.setStrStatus(parser.getText());
                                        break;
                                    case PARSER_NUM_7:
                                        mListADInfo.setStrRejectCuz(parser.getText());
                                        break;
                                    case PARSER_NUM_8:
                                        mListADInfo.setStrIsDel(parser.getText());
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
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))){
            Message msg = new Message();
            msg.what = StaticDataInfo.RESULT_NO_DATA;
            msg.arg1 = StaticDataInfo.RESULT_NO_DATA;
            handler.sendMessage(msg);
        }else{
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }

    private Dialog mDlg;
    /**
     * 1차분류, 2차분류, 3차분류 선택 popup
     * @param mMode
     */
    public void OpenDlg(final int mMode){
        mDlg = new Dialog(MyADActivity.this);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);
        mDlg.setContentView(R.layout.dlg_txt_list);
        TextView txtDlgTitle = (TextView) mDlg.findViewById(R.id.txt_dlg_title);
        ListView lvText = (ListView) mDlg.findViewById(R.id.lv_txt);
        CommCodeAdapter commAdapter = null;

        switch(mMode){
            case DIALOG_MODE_1: //1차 분류
                if(arrData1!=null && arrData1.size()>0) {
                    txtDlgTitle.setVisibility(View.GONE);
                    ((LinearLayout) mDlg.findViewById(R.id.ll_dlg_title_devider)).setVisibility(View.GONE);
                    commAdapter = new CommCodeAdapter(MyADActivity.this, R.layout.list_txt_item, arrData1);
                }
                break;
            case DIALOG_MODE_2: //2차 분류
                if(arrData2!=null && arrData2.size()>0) {
                    txtDlgTitle.setText(txtCategory1.getText().toString());
                    commAdapter = new CommCodeAdapter(MyADActivity.this, R.layout.list_txt_item, arrData2);
                }else{
                    Toast.makeText(MyADActivity.this, getResources().getString(R.string.str_select_empty), Toast.LENGTH_SHORT).show();
                }
                break;
            case DIALOG_MODE_3: //3차 분류
                if(arrData3!=null && arrData3.size()>0) {
                    txtDlgTitle.setText(txtCategory2.getText().toString());
                    commAdapter = new CommCodeAdapter(MyADActivity.this, R.layout.list_txt_item, arrData3);
                } else {
                    isSelSiGun = true;
                    Toast.makeText(MyADActivity.this, getResources().getString(R.string.str_select_empty), Toast.LENGTH_SHORT).show();
                }
                break;
        }

        if(commAdapter!=null){
            lvText.setAdapter(commAdapter);
            mDlg.show();
        }

        lvText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mMode) {
                    case DIALOG_MODE_1:
                        strDlgChildTitle1 = arrData1.get(position).getStrMsg();
                        strGroupIdx = arrData1.get(position).getStrIdx();
                        txtCategory1.setText(strDlgChildTitle1);
                        if(arrData2!=null) arrData2.clear();
                        if(arrData3!=null) arrData3.clear();

                        txtCategory2.setText("");
                        txtCategory3.setText("");
                        strChildIdx1 = "";
                        strChildIdx2 = "";

                        if (strDlgChildTitle1.equals(getResources().getString(R.string.str_sel_ad_send_default_info))) {
                            llLowCategory.setVisibility(View.GONE);
                            ivSearch.setVisibility(View.GONE);
                        } else {
                            llLowCategory.setVisibility(View.VISIBLE);
                            ivSearch.setVisibility(View.VISIBLE);
                            new CommCode(MyADActivity.this, StaticDataInfo.COMMON_CODE_TYPE_AD, 2, strGroupIdx, handler);
                        }
                        break;
                    case DIALOG_MODE_2:
                        strDlgChildTitle2 = arrData2.get(position).getStrMsg();
                        strChildIdx1 = arrData2.get(position).getStrIdx();
                        new CommCode(MyADActivity.this, StaticDataInfo.COMMON_CODE_TYPE_AD, 3, strChildIdx1, handler);
                        txtCategory2.setText(strDlgChildTitle2);
                        txtCategory3.setText("");
                        strChildIdx2="";
                        isSelSiGun = false;
                        break;
                    case DIALOG_MODE_3:
                        txtCategory3.setText(arrData3.get(position).getStrMsg());
                        strChildIdx2 = arrData3.get(position).getStrIdx();
                        break;
                }
                mDlg.dismiss();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }
}
