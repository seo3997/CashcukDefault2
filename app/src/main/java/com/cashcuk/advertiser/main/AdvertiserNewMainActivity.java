package com.cashcuk.advertiser.main;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.adcost.TotalADCostList;
import com.cashcuk.advertiser.charge.ChangePointActivity;
import com.cashcuk.advertiser.charge.ChargeActivity;
import com.cashcuk.advertiser.charge.chargelist.ChargeListActivity;
import com.cashcuk.advertiser.makead.MakeADMainActivity;
import com.cashcuk.advertiser.myad.MyADActivity;
import com.cashcuk.advertiser.sendpush.ADPushSendCurrentState;
import com.cashcuk.advertiser.sendpush.ADTargetSendActivity;

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
 * 광고주 메인
 */
public class AdvertiserNewMainActivity extends Activity implements View.OnClickListener {
    // 총 광고비용
    private String strTotalADCoast;
    private TextView txtTotalADCoast;
    // 충전금
    private TextView txtChargeMoney;

    // PUSH 광고비용
    private TextView txtTotalPushCost;

    // PUSH 광고총액
    private TextView txtTotalAdpushCost;

    private final String STR_CHARGE_AMT = "chargeAmt";          //충전금
    private final String STR_AD_AMT     = "adAmt";              //광고현황 금액
    private final String STR_PUSH_AMT   = "pushAmt";            //푸시현황 금액
    private final String STR_ADAMT_HAP  = "adAmtHap";           //광고총액

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;

    private LinearLayout llProgress;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(AdvertiserNewMainActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.advertiser_newmain_activity);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_adtitle));

        ((LinearLayout) findViewById(R.id.li_total_ad_cost_list)).setOnClickListener(this); //총 광고비용 내역 상세보기
        ((Button) findViewById(R.id.btn_charge)).setOnClickListener(this); //충전하기
        ((LinearLayout) findViewById(R.id.ll_charge_details)).setOnClickListener(this); //충전금 내역
        ((LinearLayout) findViewById(R.id.btn_change_point)).setOnClickListener(this); //포인트로 전환

        ((Button) findViewById(R.id.btn_my_ad_list)).setOnClickListener(this); //내 광고 리스트
        ((Button) findViewById(R.id.btn_make_push)).setOnClickListener(this); //푸시광고


        ((LinearLayout) findViewById(R.id.li_all_push_state)).setOnClickListener(this); //전체 push 현황
        ((Button) findViewById(R.id.btn_make_ad)).setOnClickListener(this); //광고제작

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        txtChargeMoney = (TextView) findViewById(R.id.txt_charge_money);
        txtTotalADCoast = (TextView) findViewById(R.id.txt_total_ad_cost);
        txtTotalPushCost = (TextView) findViewById(R.id.txt_total_push_cost);
        txtTotalAdpushCost = (TextView) findViewById(R.id.txt_total_adpush_cost);



        MainTitleBar mainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mainTitleBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAdvertiserInfo();
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

        setAdvertiserInfo();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.li_all_push_state) {
            intent = new Intent(AdvertiserNewMainActivity.this, ADPushSendCurrentState.class);
            intent.putExtra("PushKind", getResources().getString(R.string.str_all_push_pressent_condition));
        } else if (viewId == R.id.li_total_ad_cost_list) {
            intent = new Intent(AdvertiserNewMainActivity.this, TotalADCostList.class);
            intent.putExtra("MyADTotalCost", txtTotalADCoast.getText());
        } else if (viewId == R.id.btn_charge) {
            intent = new Intent(AdvertiserNewMainActivity.this, ChargeActivity.class);
        } else if (viewId == R.id.ll_charge_details) {
            intent = new Intent(AdvertiserNewMainActivity.this, ChargeListActivity.class);
        } else if (viewId == R.id.btn_change_point) {
            intent = new Intent(AdvertiserNewMainActivity.this, ChangePointActivity.class);
        } else if (viewId == R.id.btn_my_ad_list) {
            intent = new Intent(AdvertiserNewMainActivity.this, MyADActivity.class);
        } else if (viewId == R.id.btn_make_ad) {
            intent = new Intent(AdvertiserNewMainActivity.this, MakeADMainActivity.class);
        } else if (viewId == R.id.btn_make_push) {
            intent = new Intent(AdvertiserNewMainActivity.this, ADTargetSendActivity.class);
            intent.putExtra("AD_IDX","");
        }

        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**
     * 광고주 Main 정보 (총 광고 비용, 충전금)
     */
    public void setAdvertiserInfo() {
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        String url = "";
        url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_newmain_myamt);
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
            if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                displayInfo(result);
            } else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))){
                handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    /**
     * 총 광고비용
     */
    public void displayInfo(String result) {
        try {

            Log.d("temp","result["+result+"]");
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
                        } else if (parser.getName().equals(STR_AD_AMT)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_PUSH_AMT)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_ADAMT_HAP)) {
                            k_data_num = PARSER_NUM_3;
                        } else  {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    txtChargeMoney.setText(StaticDataInfo.makeStringComma(parser.getText()));
                                    break;
                                case PARSER_NUM_1:
                                    txtTotalADCoast.setText(StaticDataInfo.makeStringComma(parser.getText()));
                                    break;
                                case PARSER_NUM_2:
                                    txtTotalPushCost.setText(StaticDataInfo.makeStringComma(parser.getText()));
                                    break;
                                case PARSER_NUM_3:
                                    txtTotalAdpushCost.setText(StaticDataInfo.makeStringComma(parser.getText()));
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

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        },500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }
}
