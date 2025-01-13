package com.cashcuk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
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
 * 은행명 list
 */
public class BankList {
    private Context mContext;
    private Handler handler;
    private Message msg;

    //은행 명 return
    private final String STRING_BANK_IDX = "bank_idx"; //선택한 고유 값
    private final String STRING_BANK_NM = "bank_nm"; //은행 명

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;

    //은행 명
    private TxtListDataInfo mBankListDataInfo;
    private ArrayList<TxtListDataInfo> arrBankTempData;

    public BankList(Context context, Handler handler){
        mContext = context;
        this.handler = handler;

        msg = this.handler.obtainMessage();

        BankList();
    }

    /**
     * 은행 명
     */
    public void BankList() {
        final String url = mContext.getResources().getString(R.string.str_new_url)+mContext.getResources().getString(R.string.str_codes_bank);
        SharedPreferences pref = mContext.getSharedPreferences("TokenInfo", mContext.MODE_PRIVATE);
        String token = pref.getString(mContext.getResources().getString(R.string.str_token), "");

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
                listParams.add(new BasicNameValuePair(mContext.getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
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
            ResultBankList(result);
        }
    }

    /**
     * 은행 명 결과 값 parsing
     * @param result 서버에서 받은 결과 값
     */
    private void ResultBankList(String result){
        Message msg = new Message();
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrBankTempData != null && mBankListDataInfo != null) {
                                arrBankTempData.add(mBankListDataInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrBankTempData = new ArrayList<TxtListDataInfo>();
                            break;

                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mBankListDataInfo = new TxtListDataInfo();
                            }

                            if (parser.getName().equals(STRING_BANK_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STRING_BANK_NM)) {
                                k_data_num = PARSER_NUM_1;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mBankListDataInfo.setStrIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mBankListDataInfo.setStrMsg(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                msg.what = StaticDataInfo.RESULT_CODE_200;
                msg.obj = arrBankTempData;
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            msg.what = StaticDataInfo.RESULT_CODE_ERR;
            handler.sendMessage(msg);
        }
    }
}
