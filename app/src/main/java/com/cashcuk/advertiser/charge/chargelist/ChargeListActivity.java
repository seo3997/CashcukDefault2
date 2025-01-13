package com.cashcuk.advertiser.charge.chargelist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.charge.ChargeActivity;
import com.cashcuk.advertiser.refund.RefundRequestActivity;
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
 * 충전금 내역
 */
public class ChargeListActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private final int REQUEST_CHK_PWD = 999;
    private final int REQUEST_REFUND_MONEY = 888;
    private final int REQUEST_REFUND_MONEY_COMPLETE = 777;

    private LinearLayout llAccrueChargeLIst;
    private LinearLayout llAccrueCharge;

    private TextView txtMyChargeMoney; //충전금
    private String strMyChargeMoney;


    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;

    private final String STR_ACCOUNT_HOLDER = "sys_host"; //예금주
    private final String STR_BANK = "sys_bank"; //입금은행
    private final String STR_ACCOUNT = "sys_account"; //입금계좌

    private final String STR_CHARGE_AMT = "chargeAmt";          //충전금

    private final int DISPLAY_LIST_ACCRUE = 0;

    private int mAccrueListCnt = 0;
    private int mUseListCnt = 0;


    private LinearLayout llProgress;
    private final String STR_MODE_REQUEST_MAIN = "Main";
    private final String STR_MODE_REQUEST_ACCOUNT_INFO="AccountInfo";
    private String strMode=STR_MODE_REQUEST_MAIN;

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
                    Toast.makeText(ChargeListActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    if(msg.arg1==-1){
                        if(strMode.equals(STR_MODE_REQUEST_MAIN)){
                            dataRequest(STR_MODE_REQUEST_ACCOUNT_INFO);
                        }
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1 == DISPLAY_LIST_ACCRUE){
                        addAccruePointList((ArrayList<ChargeListDataInfo>)msg.obj);
                    }else{
                        getItemData(DISPLAY_LIST_ACCRUE);

                        if(strMode.equals(STR_MODE_REQUEST_MAIN)){
                            dataRequest(STR_MODE_REQUEST_ACCOUNT_INFO);
                        }
                    }
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(msg.arg1==-1){
                        if(strMode.equals(STR_MODE_REQUEST_MAIN)){
                            dataRequest(STR_MODE_REQUEST_ACCOUNT_INFO);
                        }
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
        setContentView(R.layout.advertiser_charge_newlist);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(getResources().getString(R.string.str_charge_money_list));

        llAccrueChargeLIst = (LinearLayout)findViewById(R.id.ll_accrue_charge_list);
        llAccrueCharge = (LinearLayout)findViewById(R.id.ll_accrue_charge);

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(this);

        ImageButton ibMenu = (ImageButton) titleBar.findViewById(R.id.ib_menu);
        ibMenu.setVisibility(View.VISIBLE);
        ibMenu.setOnClickListener(this);

        MenuList();

        View v = (View) findViewById(R.id.layout_charge);
        txtMyChargeMoney = (TextView) v.findViewById(R.id.txt_title_charge);


        ((Button) findViewById(R.id.btn_accrue_charge_more)).setOnClickListener(this);

        WebView wbInfo = (WebView) findViewById(R.id.wv_info_msg);
        wbInfo.getSettings().setJavaScriptEnabled(true);
        wbInfo.setBackgroundColor(0);
        wbInfo.loadUrl(getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_link_chargelist_info));
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
        dataRequest(STR_MODE_REQUEST_MAIN);
    }

    /**
     * 상단 메뉴 버튼
     */
    private Dialog mMenuDlg;
    private ArrayList<String> arrString;
    private Button btn1; //취소
    public void MenuList() {
        mMenuDlg = new Dialog(this);
        mMenuDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMenuDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mMenuDlg.setContentView(R.layout.dlg_list_title);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mMenuDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mMenuDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ((TextView) mMenuDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_menu));
        ListView lvDlgMsg = (ListView) mMenuDlg.findViewById(R.id.lv_dlg);

        arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_charge_request)); //충전요청
        arrString.add(getResources().getString(R.string.str_refund_request)); //환불요청
        arrString.add(getResources().getString(R.string.str_charge_account_info)); //충전계좌정보

        btn1 = (Button) mMenuDlg.findViewById(R.id.btn1);
        btn1.setOnTouchListener(this);
        ((LinearLayout) mMenuDlg.findViewById(R.id.ll1)).setOnTouchListener(this);

        DlgListAdapter dlgAdapter = new DlgListAdapter(ChargeListActivity.this, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (arrString.get(position).equals(getResources().getString(R.string.str_charge_request))) { //충전요청
                    intent = new Intent(ChargeListActivity.this, ChargeActivity.class);
                } else if (arrString.get(position).equals(getResources().getString(R.string.str_refund_request))) { //환불요청
                    intent = new Intent(ChargeListActivity.this, RefundRequestActivity.class);
                } else if(arrString.get(position).equals(getResources().getString(R.string.str_charge_account_info))) { //충전 계좌 정보
                    intent = new Intent(ChargeListActivity.this, DlgBtnActivity.class);
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_account_info_title));
                    intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_account_info), strAccountHolder, strBank, strAccount));
                }

                if(intent!=null){
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                if (mMenuDlg != null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CHK_PWD){
                DlgRefund();
            }else if(requestCode == REQUEST_REFUND_MONEY){
                Intent intent = new Intent(ChargeListActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_input_refund_money_chk_complete), StaticDataInfo.makeStringComma(etInputRefundMoney.getText().toString())));
                startActivityForResult(intent, REQUEST_REFUND_MONEY_COMPLETE);
            }else if(requestCode == REQUEST_REFUND_MONEY_COMPLETE){
                finish();
            }
        }
    }

    private Dialog mDlg;
    private EditText etInputRefundMoney;
    private Button btnCancel;
    private Button btnOk;

    /**
     * 환불요청 금액 입력 dialog
     */
    public void DlgRefund (){
        mDlg = new Dialog(this);

        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDlg.setContentView(R.layout.dlg_charge_money_input);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        etInputRefundMoney = (EditText) mDlg.findViewById(R.id.et_charge_money_input);
        etInputRefundMoney.addTextChangedListener(mTextWatcher);

        ((TextView) mDlg.findViewById(R.id.txt_msg)).setText(getResources().getString(R.string.str_refund_money_input_err));
        LinearLayout llCancel = (LinearLayout) mDlg.findViewById(R.id.ll_cancel);
        LinearLayout llOk = (LinearLayout) mDlg.findViewById(R.id.ll_ok);
        btnCancel = (Button) mDlg.findViewById(R.id.btn_cancel);
        btnOk = (Button) mDlg.findViewById(R.id.btn_ok);
        llCancel.setOnTouchListener(this);
        llOk.setOnTouchListener(this);
        btnCancel.setOnTouchListener(this);
        btnOk.setOnTouchListener(this);

        mDlg.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll1 || v.getId() == R.id.btn1){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                if(mMenuDlg!=null && mMenuDlg.isShowing()) mMenuDlg.dismiss();
            }
            return true;
        }else if(v.getId() == R.id.ll_cancel || v.getId()==R.id.btn_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) if(mDlg!=null && mDlg.isShowing()) mDlg.dismiss();
            return true;
        }else if(v.getId() == R.id.ll_ok  || v.getId()==R.id.btn_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                btnOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                ChkInputChargeMoney();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.ib_menu) {
            if (mMenuDlg != null && !mMenuDlg.isShowing()) mMenuDlg.show();
        } else if (viewId == R.id.ib_refresh) {
            dataRequest(STR_MODE_REQUEST_MAIN);
        } else if (viewId == R.id.btn_accrue_charge_more) {
            intent = new Intent(ChargeListActivity.this, ChargeAccrueListActivity.class);
        }

        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

    }

    private String strCommaResult="";
    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(strCommaResult)){     // StackOverflow를 막기위해,
                strCommaResult = StaticDataInfo.makeStringComma(s.toString().replaceAll(",", ""));   // 에딧텍스트의 값을 변환하여, result에 저장.
                etInputRefundMoney.setText(strCommaResult);    // 결과 텍스트 셋팅.
                etInputRefundMoney.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * 요청 금액 체크
     */
    public void ChkInputChargeMoney() {
        if(txtMyChargeMoney.getText().toString().contains(",")){
            strMyChargeMoney = txtMyChargeMoney.getText().toString().replace(",", "");
        }else{
            strMyChargeMoney = txtMyChargeMoney.getText().toString();
        }

        String strInputChargeMoney = etInputRefundMoney.getText().toString();
        float mInputChargeMoney = 0;

        if (strInputChargeMoney.trim().equals("") || strInputChargeMoney.equals("0")) {
            Toast.makeText(ChargeListActivity.this, getResources().getString(R.string.str_refund_money_input_err), Toast.LENGTH_SHORT).show();
            return;
        }else{
            if(strInputChargeMoney.contains(",")) {
                mInputChargeMoney =Long.parseLong(strInputChargeMoney.replace(",", ""));
            }else{
                mInputChargeMoney = Long.parseLong(strInputChargeMoney);
            }

            if(!strMyChargeMoney.equals("0") && mInputChargeMoney>Long.parseLong(strMyChargeMoney)){
                Intent intent = new Intent(ChargeListActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_refund_money_input_err1));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }else {
//                RefundRequestChk(strInputChargeMoney);
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
    public void dataRequest(String mode){
        strMode = mode;
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        String url = "";
        if(mode.equals(STR_MODE_REQUEST_MAIN)){
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_newmain_myamt);
        }else if(mode.equals(STR_MODE_REQUEST_ACCOUNT_INFO)){
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_charges_account_page);
        }
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
            if(strMode.equals(STR_MODE_REQUEST_MAIN)) {
                resultData(result);
            }else if(strMode.equals(STR_MODE_REQUEST_ACCOUNT_INFO)){
                resultAccountInfo(result);
            }
        }
    }

    /**
     * 결과 값 parsing
     * @param result
     */
    public void resultData(String result) {
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
                            if (parser.getName().equals(STR_CHARGE_AMT)) {
                                k_data_num = PARSER_NUM_0;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        txtMyChargeMoney.setText(StaticDataInfo.makeStringComma(parser.getText()));
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Message msg = new Message();
            msg.what = StaticDataInfo.RESULT_CODE_200;
            msg.arg1 = -1;
//            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
            handler.sendMessage(msg);
        } else {
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }

    public void addAccruePointList(ArrayList<ChargeListDataInfo> listData) {
        if (llAccrueChargeLIst != null) llAccrueChargeLIst.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mAccrueListCnt = listData.size();

        Log.d("temp","listData.size()["+listData.size()+"]");
        for (int i = 0; i < listData.size(); i++) {
            LinearLayout llChargeLIstView = (LinearLayout) inflater.inflate(R.layout.point_list_accruenew_item, null);
            llChargeLIstView.setId(i);

            ((TextView) llChargeLIstView.findViewById(R.id.txt_nm)).setText(listData.get(i).getStrText());
            ((TextView) llChargeLIstView.findViewById(R.id.txt_date)).setText(listData.get(i).getStrDate());

            if(StaticDataInfo.STR_CHARGE_CODE_TAKE.equals(listData.get(i).getStrChargeCode())){
                ((TextView) llChargeLIstView.findViewById(R.id.txt_amtcd)).setText("-");
            }else{
                ((TextView) llChargeLIstView.findViewById(R.id.txt_amtcd)).setText("+");

            }


            ((TextView) llChargeLIstView.findViewById(R.id.txt_point)).setText(StaticDataInfo.makeStringComma(listData.get(i).getStrCharge()));


            ((TextView) llChargeLIstView.findViewById(R.id.txt_point_en)).setText(getResources().getString(R.string.str_won));

            if (i == listData.size() - 1) {
                ((LinearLayout) llChargeLIstView.findViewById(R.id.ll_point_list_divider)).setVisibility(View.GONE);
            }
            llAccrueChargeLIst.addView(llChargeLIstView);
        }
    }


    public void getItemData(int mMode){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        if(mMode == DISPLAY_LIST_ACCRUE){
            new RequestChargeList(ChargeListActivity.this, StaticDataInfo.MODE_POINT_LIST_ACCRUE, "T", handler, 1);
        }
    }


    //충전요청 기본 data
    /**
     * 결과 값 parsing
     * @param result
     */
    private String strAccountHolder = "";
    private String strBank = "";
    private String strAccount = "";
    public void resultAccountInfo(String result) {
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
                            if (parser.getName().equals(STR_ACCOUNT_HOLDER)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_BANK)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_ACCOUNT)) {
                                k_data_num = PARSER_NUM_2;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        strAccountHolder = parser.getText();
                                        break;
                                    case PARSER_NUM_1:
                                        strBank = parser.getText();
                                        break;
                                    case PARSER_NUM_2:
                                        strAccount = parser.getText();
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }

}
