package com.cashcuk.ad.admylist;

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
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.ad.adlist.ListADAdapter;
import com.cashcuk.ad.adlist.ListADInfo;
import com.cashcuk.ad.adlist.SwipeDismissListViewTouchListener;
import com.cashcuk.ad.detailview.ADDetailViewActivity;
import com.cashcuk.common.AdInterestEdit;
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
 * 관심광고 list
 */
public class FrMyListAD extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private Activity mActivity;
    private LinearLayout llProgress;
    private CheckBox chkNoView;

    private final int SEND_PAGE_NO = 2;
    private final int SEND_AD_CODE = 3;

    private final String STR_IDX = "idx";
    private final String STR_PAGE_NO = "pageno";
    private final String STR_AD_CODE = "ad_code";
    private final String STR_AD_CODE_LIKE = "lk";

    //서버 리턴 값
    private final String STR_AD_IDX = "ad_idx"; //광고 idx
    private final String STR_AD_MAIN_IMG_URL = "ad_mainurl"; //광고메인 이미지 url
    private final String STR_AD_NM = "ad_nm"; //광고명
    private final String STR_AD_GRADE = "ad_rating"; //평점
    private final String STR_AD_POINT = "ad_point"; //광고 클릭 시 적립포인트
    private final String STR_AD_ETC = "ad_etc"; //이용방법 및 주의사항
    private final String STR_AD_DETAIL = "ad_detail"; //link url
    private final String STR_AD_EVENT = "ad_event"; //이벤트
    private final String STR_AD_DEFAULT_ETC = "ad_content"; //기본 이용방법 및 주의사항

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

    private ListView lvAd; //광고 list
    private LinearLayout llADEmpty; //광고 없을 시 d/p
    private ListADAdapter lvAdapter = null;

    private ListADInfo mADAddTemp;
    private int REQUEST_CODE = 999;

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.
    private boolean isListUpdate = false;

    private final int FR_PAGE = 1;
    private int mFrPage=0;

    private FrameLayout layoutBG;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.ADVER_INTEREST:
                    if(msg.arg1== StaticDataInfo.RESULT_CODE_200) {
                        Toast.makeText(mActivity, getResources().getString(R.string.str_my_ad_minus_succese), Toast.LENGTH_SHORT).show();
                        if(arrListADInfo!=null && arrListADInfo.size()>0){
                            for(int i=0; i<arrListADInfo.size(); i++) {
                                if(arrListADInfo.get(i).getStrIdx().equals(mADAddTemp.getStrIdx())){
                                    arrListADInfo.remove(i);
                                    if(arrListADInfo.size()<=0){
                                        lvAd.setVisibility(View.GONE);
                                        llADEmpty.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                        if (lvAdapter != null) lvAdapter.notifyDataSetChanged();
                    }else {
                        mADAddTemp = new ListADInfo();
                        mADAddTemp = (ListADInfo) msg.obj;

                        Intent intent = new Intent(mActivity, DlgBtnActivity.class);
                        intent.putExtra("DlgMode", "Two");
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_my_ad_minus_chk));
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    lvAd.setVisibility(View.GONE);
                    llADEmpty.setVisibility(View.VISIBLE);
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(isListUpdate){
                        isListUpdate=false;
                    }else if(arrListADInfo!=null && msg.arg1 == StaticDataInfo.RESULT_NO_DATA && arrListADInfo.size()<0) {
                        if (arrListADInfo != null) arrListADInfo.clear();

                        if(mLockListView) {
                            lvAd.setVisibility(View.GONE);
                            llADEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                        if (arrListADInfo != null && arrListADInfo.size() > 0) {
                            lvAd.setVisibility(View.VISIBLE);
                            llADEmpty.setVisibility(View.GONE);

                            if (mPageNo == 1) {
                                lvAdapter = new ListADAdapter(mActivity, arrListADInfo, StaticDataInfo.MODE_MY_AD, handler);
                                lvAd.setAdapter(lvAdapter);
                                lvAd.setOnItemClickListener(mItemClick);
                                lvAd.setOnScrollListener(mListScroll);
                            } else {
                                if (lvAdapter != null) lvAdapter.notifyDataSetChanged();
                            }
                            mPageNo++;
                        }
                    break;
            }

            mLockListView=false;
            if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
        }
    };

    public static FrMyListAD newInstance(int mPage){
        FrMyListAD fragment = new FrMyListAD();
        Bundle args =  new Bundle();
        args.putInt("Page", mPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            mFrPage = getArguments().getInt("Page");
        }
    }

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
    public void onStart() {
        super.onStart();

        if (mFrPage == FR_PAGE) {
            mPageNo = 1;
            getADList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_list_ad, null);

        layoutBG = (FrameLayout) view.findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((LinearLayout) view.findViewById(R.id.ll_category)).setVisibility(View.GONE);
        ((LinearLayout) view.findViewById(R.id.ll_ad_shadow)).setVisibility(View.GONE);
        llProgress = (LinearLayout) view.findViewById(R.id.ll_progress_circle);
        lvAd = (ListView) view.findViewById(R.id.lv_ad);
        llADEmpty = (LinearLayout) view.findViewById(R.id.ll_ad_empty);

        return view;
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
    public AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ADListClickDlg(arrListADInfo.get(position));
            mListPosition = position;
        }
    };

    private Dialog mDlgADCareMsg;
    private Button btnCareCancel;
    private Button btnCareOk;
    public void ADListClickDlg(ListADInfo adInfo){
        mDlgADCareMsg = new Dialog(mActivity);
        mDlgADCareMsg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlgADCareMsg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDlgADCareMsg.setContentView(R.layout.dlg_ad_list_click);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlgADCareMsg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlgADCareMsg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ImageView ivAD = (ImageView)mDlgADCareMsg.findViewById(R.id.iv_ad);
        Glide
                .with(mActivity)
                .load(adInfo.getStrTitleImgUrl())
                .centerCrop()
                .placeholder(R.drawable.image_none)
                .into(ivAD);

        ((TextView) mDlgADCareMsg.findViewById(R.id.txt_ad_nm)).setText(adInfo.getStrName());
        ((TextView) mDlgADCareMsg.findViewById(R.id.txt_point)).setText(adInfo.getStrPoint());
        TextView txtInfo = (TextView) mDlgADCareMsg.findViewById(R.id.txt_ad_info);
        if (adInfo.getStrDetail().equals("")) {
            txtInfo.setVisibility(View.GONE);
        }else{
            txtInfo.setText(adInfo.getStrDetail());
        }

        ((TextView) mDlgADCareMsg.findViewById(R.id.txt_grade)).setText(mActivity.getResources().getString(R.string.str_ad_average)+ " " +adInfo.getStrGrade());
        TextView txtMsg = (TextView) mDlgADCareMsg.findViewById(R.id.txt_case_msg);
        LinearLayout llMsgDiv = (LinearLayout) mDlgADCareMsg.findViewById(R.id.ll_msg_divider);
        if(adInfo.getStrContents().equals("") && adInfo.getStrEtc().equals("")){
            txtMsg.setVisibility(View.GONE);
            llMsgDiv.setVisibility(View.VISIBLE);
        }else{
            llMsgDiv.setVisibility(View.GONE);
            if(adInfo.getStrContents().equals("") || adInfo.getStrEtc().equals("")){
                txtMsg.setText(adInfo.getStrContents()+adInfo.getStrEtc());
            }else {
                txtMsg.setText(adInfo.getStrContents() + "\n" + adInfo.getStrEtc());
            }
        }

        btnCareCancel = (Button) mDlgADCareMsg.findViewById(R.id.btn_care_cancel);
        btnCareOk = (Button) mDlgADCareMsg.findViewById(R.id.btn_care_ok);
        ((LinearLayout) mDlgADCareMsg.findViewById(R.id.ll_care_ok)).setOnTouchListener(this);
        ((LinearLayout) mDlgADCareMsg.findViewById(R.id.ll_care_cancel)).setOnTouchListener(this);
        btnCareCancel.setOnTouchListener(this);
        btnCareOk.setOnTouchListener(this);

        mDlgADCareMsg.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId()==R.id.ll_care_cancel || v.getId()==R.id.btn_care_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCareCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) if(mDlgADCareMsg!=null && mDlgADCareMsg.isShowing()) mDlgADCareMsg.dismiss();
            return true;
        }else if(v.getId()==R.id.ll_care_ok || v.getId()==R.id.btn_care_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCareOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                if(mListPosition!=-1 && arrListADInfo!=null){
                    Intent intent = new Intent(mActivity, ADDetailViewActivity.class);
                    intent.putExtra("AD_IDX", arrListADInfo.get(mListPosition).getStrIdx());
                    intent.putExtra("AD_KIND", getResources().getString(R.string.str_user_en));
                    startActivity(intent);
                }

                if(mDlgADCareMsg!=null && mDlgADCareMsg.isShowing()) mDlgADCareMsg.dismiss();
            }
            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDlgADCareMsg != null && mDlgADCareMsg.isShowing()) mDlgADCareMsg.dismiss();
        recycleView(layoutBG);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_care_ok) {
            if (mDlgADCareMsg != null && mDlgADCareMsg.isShowing()) {
                mDlgADCareMsg.dismiss();
            }
        } else if (v.getId() == R.id.btn_care_cancel) {
            if (mDlgADCareMsg != null && mDlgADCareMsg.isShowing()) {
                mDlgADCareMsg.dismiss();
            }
        }
    }

    public AbsListView.OnScrollListener mListScroll = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !mLockListView) {
                if(lastitemVisibleFlag){
                    isListUpdate = true;
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

    public void getADList(){
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise);
        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        final String token = pref.getString(mActivity.getResources().getString(R.string.str_token), "");

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        mLockListView = true;

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PAGE_NO, String.valueOf(mPageNo));
        k_param.put(SEND_AD_CODE, STR_AD_CODE_LIKE);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
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
                listParams.add(new BasicNameValuePair(mActivity.getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_PAGE_NO, params[SEND_PAGE_NO]));
                listParams.add(new BasicNameValuePair(STR_AD_CODE, params[SEND_AD_CODE]));

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
            ResultMyADList(result);
        }
    }

    private ListADInfo mListADInfo;
    private ArrayList<ListADInfo> arrListADInfo;
    public void ResultMyADList(String result){
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
                            } else if (parser.getName().equals(STR_AD_MAIN_IMG_URL)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_AD_GRADE)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_AD_POINT)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_AD_ETC)) {
                                k_data_num = PARSER_NUM_5;
                            } else if (parser.getName().equals(STR_AD_EVENT)) {
                                k_data_num = PARSER_NUM_6;
                            } else if (parser.getName().equals(STR_AD_DETAIL)) {
                                k_data_num = PARSER_NUM_7;
                            } else if (parser.getName().equals(STR_AD_DEFAULT_ETC)) {
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
                                        mListADInfo.setStrTitleImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        if(parser.getText()!=null && parser.getText().equals("0")){
                                            mListADInfo.setStrGrade("0.0");
                                        }else {
                                            mListADInfo.setStrGrade(parser.getText());
                                        }
                                        break;
                                    case PARSER_NUM_4:
                                        mListADInfo.setStrPoint(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mListADInfo.setStrEtc(parser.getText());
                                        break;
                                    case PARSER_NUM_6:
                                        mListADInfo.setStrEventYN(parser.getText());
                                        break;
                                    case PARSER_NUM_7:
                                        mListADInfo.setStrDetail(parser.getText());
                                        break;
                                    case PARSER_NUM_8:
                                        mListADInfo.setStrContents(parser.getText());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==mActivity.RESULT_OK){
            if(requestCode == REQUEST_CODE){
                new AdInterestEdit(mActivity, mADAddTemp, false, handler);
            }
        }else{
//            if(mADAddTemp.getStrMyADYN().equals(StaticDataInfo.STRING_N)){
//                mADAddTemp.setStrMyADYN(StaticDataInfo.STRING_Y);
//            }else {
//                mADAddTemp.setStrMyADYN(StaticDataInfo.STRING_N);
//            }
            if (lvAdapter != null) lvAdapter.notifyDataSetChanged();
        }
    }
}
