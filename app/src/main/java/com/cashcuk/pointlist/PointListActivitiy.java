package com.cashcuk.pointlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.GetChargePoint;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.charge.chargelist.ChargeListAdapter;
import com.cashcuk.common.DefaultData;
import com.cashcuk.common.RequestPointList;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgListAdapter;

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
 * 포인트 내역
 */
public class PointListActivitiy extends Activity implements View.OnClickListener, View.OnTouchListener {
    private LinearLayout llAccruePointLIst;
    private LinearLayout llAccruePoint;
    private LinearLayout llUsePointLIst;
    private LinearLayout llUsePoint;

    private LinearLayout llProgress;

    private ImageView ivAccrue;
    private ImageView ivUse;

    private final int SEND_IDX = 2;
    private final String STR_IDX = "idx";

    private final String STR_POINT_ACCRUE = "point_accrue"; //누적포인트
    private final String STR_POINT_USE = "point_up"; //사용 포인트
	
    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
	
    private final int DISPLAY_LIST_ACCRUE = 0;
    private final int DISPLAY_LIST_USE = 1;

    private PointListInfo mPointInfo;

    private TextView txtMyPoint;
    private TextView txtAccruePoint;
    private TextView txtUsePoint;

    private int mAccrueListCnt = 0;
    private int mUseListCnt = 0;

    private int mPageNo = 1;
    private ChargeListAdapter adapter;

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
                    Toast.makeText(PointListActivitiy.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1 == StaticDataInfo.RESULT_CODE_MY_POINT){
                        txtMyPoint.setText(StaticDataInfo.makeStringComma((String)msg.obj));
                    }else if(msg.arg1 == DISPLAY_LIST_ACCRUE){
                        addAccruePointList((ArrayList<PointListDataInfo>)msg.obj);
                        getItemData(DISPLAY_LIST_USE);
                    } else if (msg.arg1 == DISPLAY_LIST_USE){
                        addUsePointList((ArrayList<PointListDataInfo>) msg.obj);
                        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                    }else{
                        DisplayPointList();
                        getItemData(DISPLAY_LIST_ACCRUE);
                    }
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(msg.arg1 == DISPLAY_LIST_USE) {
                        addUsePointList((ArrayList<PointListDataInfo>) msg.obj);
                    }
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
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
        setContentView(R.layout.activity_point_list);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_point_list_title));

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetChargePoint(PointListActivitiy.this, handler);
                DataRequest();
            }
        });

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);
        ((LinearLayout) findViewById(R.id.ll_accrue_point_title)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.ll_use_point_title)).setOnClickListener(this);
        llAccruePointLIst = (LinearLayout)findViewById(R.id.ll_accrue_point_list);
        llAccruePoint = (LinearLayout)findViewById(R.id.ll_accrue_point);
        llUsePointLIst = (LinearLayout)findViewById(R.id.ll_use_point_list);
        llUsePoint = (LinearLayout)findViewById(R.id.ll_use_point);
        ((Button) findViewById(R.id.btn_accrue_point_more)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_use_point_more)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_use)).setOnClickListener(this);

        View vMyPoint = (View) findViewById(R.id.layout_my_point);
        txtMyPoint = (TextView) vMyPoint.findViewById(R.id.txt_title_my_point);
        txtAccruePoint = (TextView) findViewById(R.id.txt_accrue_point);
        txtUsePoint = (TextView) findViewById(R.id.txt_use_point);

        ivAccrue = (ImageView) findViewById(R.id.iv_accrue);
        ivUse = (ImageView) findViewById(R.id.iv_use);

        WebView wbPointInfo = (WebView) findViewById(R.id.wv_info_msg);
        wbPointInfo.getSettings().setJavaScriptEnabled(true);
        wbPointInfo.setBackgroundColor(0);
        wbPointInfo.loadUrl(getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_link_point_list));

        DataRequest();
        UsePointDlg();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetChargePoint(PointListActivitiy.this, handler);
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
     * 포인트 list display
     */
    public void DisplayPointList(){
        if(mPointInfo!=null) {
            txtAccruePoint.setText(StaticDataInfo.makeStringComma(mPointInfo.getStrAccruePoint()));
            txtUsePoint.setText(StaticDataInfo.makeStringComma(mPointInfo.getStrUsePoint()));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.ll_accrue_point_title) {
            //누적 포인트
            ShowItem(DISPLAY_LIST_ACCRUE);
        } else if (viewId == R.id.ll_use_point_title) {
            //사용 포인트
            ShowItem(DISPLAY_LIST_USE);
        } else if (viewId == R.id.btn_accrue_point_more) {
            //누적 포인트 더보기
            intent = new Intent(getApplicationContext(), AccruePointListActivity.class);
        } else if (viewId == R.id.btn_use_point_more) {
            //사용 포인트 더보기
            intent = new Intent(PointListActivitiy.this, UsePointListActivity.class);
        } else if (viewId == R.id.btn_use) {
            //포인트 사용
            if(mDlg!=null && !mDlg.isShowing()) mDlg.show();
        }

        if(intent!=null){
            if(mPointInfo!=null) intent.putExtra("MyPoint", txtMyPoint.getText());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**
     * 포인트 사용하기
     */
    private Dialog mDlg;
    private ArrayList<String> arrString;
    private Button btn1; //취소
    public void UsePointDlg(){
        mDlg = new Dialog(this);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);
        mDlg.setContentView(R.layout.dlg_list_title);

        ((TextView) mDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_point_use_dlg_title));
        ListView lvDlgMsg = (ListView) mDlg.findViewById(R.id.lv_dlg);

        arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_point_use_input_my_account)); //내 계좌로 입금
//        arrString.add(getResources().getString(R.string.str_down_character)); //캐릭터 다운

        btn1 = (Button) mDlg.findViewById(R.id.btn1);
        btn1.setOnTouchListener(this);
        ((LinearLayout) mDlg.findViewById(R.id.ll1)).setOnTouchListener(this);

        DlgListAdapter dlgAdapter = new DlgListAdapter(PointListActivitiy.this, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrString.get(position).equals(getResources().getString(R.string.str_point_use_input_my_account))) { //내 계좌로 입금
                    RequestInputPoint();
//                }else if(position==1){ //캐릭터 받기
//                    Intent intent = new Intent(PointListActivitiy.this, DlgCharaterCategory.class);
//                    intent.putExtra(StaticDataInfo.STR_CHARATER_DOWN_OTHER, StaticDataInfo.STR_CHARATER_DOWN);
//                    startActivity(intent);
                }
                if (mDlg != null && mDlg.isShowing()) mDlg.dismiss();
            }
        });

        new DefaultData(PointListActivitiy.this);
    }

    /**
     * 내 계좌로 입금 신청
     */
    public void RequestInputPoint(){
        SharedPreferences prefs = getSharedPreferences("SaveDefaultSetInfo", MODE_PRIVATE);
        String strMinMoney = prefs.getString("ChangeMinMoney", "");

        if (strMinMoney == null ||strMinMoney.equals("")) {
            strMinMoney = "20000";
        }

        String strMyPoint = txtMyPoint.getText().toString();

        Intent intent = null;
        float mMyPoint = 0;
        if(strMyPoint.contains(",")){
            mMyPoint = Float.parseFloat(strMyPoint.replace(",", ""));
        }else{
            mMyPoint = Float.parseFloat(strMyPoint);
        }
        if(mMyPoint<Integer.parseInt(strMinMoney)){
            intent = new Intent(PointListActivitiy.this, DlgBtnActivity.class);
            intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
            intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_input_money_request_err), StaticDataInfo.makeStringComma(strMinMoney)));
        }else {
            intent = new Intent(getApplicationContext(), PointInputAccountActivity.class);
            intent.putExtra("MyPoint", strMyPoint);
        }

        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void getItemData(int mMode){
        if(mMode == DISPLAY_LIST_ACCRUE){
            new RequestPointList(PointListActivitiy.this, StaticDataInfo.MODE_POINT_LIST_ACCRUE, "T", handler, 1);
        }else if(mMode == DISPLAY_LIST_USE){
            new RequestPointList(PointListActivitiy.this, StaticDataInfo.MODE_POINT_LIST_USE, "T", handler, 1);
        }
    }

    public void ShowItem(int mMode){
        if(mMode == DISPLAY_LIST_ACCRUE){
            if(llAccruePoint.isShown()){
                llAccruePoint.setVisibility(View.GONE);
                ivAccrue.setImageDrawable(getResources().getDrawable(R.drawable.down));
            }else{
                if(llUsePoint.isShown()){
                    llUsePoint.setVisibility(View.GONE);
                    ivUse.setImageDrawable(getResources().getDrawable(R.drawable.down));
                }
                if(mAccrueListCnt>0) {
                    llAccruePoint.setVisibility(View.VISIBLE);
                    ivAccrue.setImageDrawable(getResources().getDrawable(R.drawable.up));
                }
            }
        }else if(mMode == DISPLAY_LIST_USE){
            if(llUsePoint.isShown()){
                llUsePoint.setVisibility(View.GONE);
                ivUse.setImageDrawable(getResources().getDrawable(R.drawable.down));
            }else {
                if (llAccruePoint.isShown()) {
                    llAccruePoint.setVisibility(View.GONE);
                    ivAccrue.setImageDrawable(getResources().getDrawable(R.drawable.down));
                }
                if (mUseListCnt > 0) {
                    llUsePoint.setVisibility(View.VISIBLE);
                    ivUse.setImageDrawable(getResources().getDrawable(R.drawable.up));
                }
            }
        }
    }

    public void addAccruePointList(ArrayList<PointListDataInfo> listData) {
        if (llAccruePointLIst != null) llAccruePointLIst.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (listData != null && listData.size() > 0) {
            mAccrueListCnt = listData.size();
            for (int i = 0; i < listData.size(); i++) {
                LinearLayout llPointLIstView = (LinearLayout) inflater.inflate(R.layout.point_list_accrue_item, null);
                llPointLIstView.setId(i);

                ((TextView) llPointLIstView.findViewById(R.id.txt_nm)).setText(listData.get(i).getStrContent());
                ((TextView) llPointLIstView.findViewById(R.id.txt_date)).setText(listData.get(i).getStrDate());
                ((TextView) llPointLIstView.findViewById(R.id.txt_point)).setText(StaticDataInfo.makeStringComma(listData.get(i).getStrPoint()));

                if (i == listData.size() - 1) {
                    ((LinearLayout) llPointLIstView.findViewById(R.id.ll_point_list_divider)).setVisibility(View.GONE);
                }
                llAccruePointLIst.addView(llPointLIstView);
            }
        }
    }

    public void addUsePointList(ArrayList<PointListDataInfo> listData){
        if(llUsePointLIst!=null) llUsePointLIst.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(listData!=null && listData.size()>0) {
            mUseListCnt = listData.size();
            for (int i = 0; i < listData.size(); i++) {
                LinearLayout llPointLIstView = (LinearLayout) inflater.inflate(R.layout.point_list_accrue_item, null);
                llPointLIstView.setId(i);


                ((TextView) llPointLIstView.findViewById(R.id.txt_nm)).setText(listData.get(i).getStrContent());
                ((TextView) llPointLIstView.findViewById(R.id.txt_date)).setText(listData.get(i).getStrDate());
                ((TextView) llPointLIstView.findViewById(R.id.txt_point)).setText(StaticDataInfo.makeStringComma(listData.get(i).getStrPoint()));
                if(listData.get(i).getStrRequestState().equals(StaticDataInfo.STRING_Y) || listData.get(i).getStrRequestState().equals(StaticDataInfo.STRING_N)) {
                    ((LinearLayout) llPointLIstView.findViewById(R.id.ll_request_account_info)).setVisibility(View.VISIBLE);
                }
                ((TextView) llPointLIstView.findViewById(R.id.txt_account)).setText(listData.get(i).getStrAccount());
                ((TextView) llPointLIstView.findViewById(R.id.txt_request_nm)).setText(listData.get(i).getStrAccountHolder());

                if (i == listData.size() - 1) {
                    ((LinearLayout) llPointLIstView.findViewById(R.id.ll_point_list_divider)).setVisibility(View.GONE);
                }
                llUsePointLIst.addView(llPointLIstView);
            }
        }
    }

    /**
     * Ninepatch image 만듬.
     * @param bitmap
     * @return NinePatchDrawable
     */
    public NinePatchDrawable DrawableNinePatch(Bitmap bitmap){
        byte[] chunk = bitmap.getNinePatchChunk();

        if(chunk==null) return null;

        NinePatch npPatch = new NinePatch(bitmap , chunk, null);
        NinePatchDrawable npNineDra = new NinePatchDrawable(npPatch);

        return npNineDra;
    }

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
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_charges_pointlist);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.ll1 || v.getId() == R.id.btn1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if (event.getAction() == MotionEvent.ACTION_UP) {
                btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                if (mDlg != null && mDlg.isShowing()) mDlg.dismiss();
            }
            return true;
        }

        return false;
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
            ResultPointList(result);
        }
    }

    /**
     * 결과 값 parsing
     * @param result
     */
    public void ResultPointList(String result) {
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
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mPointInfo = new PointListInfo();
                            }
                            if (parser.getName().equals(STR_POINT_ACCRUE)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_POINT_USE)) {
                                k_data_num = PARSER_NUM_2;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_1:
                                        mPointInfo.setStrAccruePoint(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mPointInfo.setStrUsePoint(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                msg.what = StaticDataInfo.RESULT_CODE_200;
                msg.arg1 = -1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            msg.what = StaticDataInfo.RESULT_CODE_ERR;
        }

        if (msg != null && handler != null) {
            handler.sendMessage(msg);
        }
    }
}
