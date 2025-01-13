package com.cashcuk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

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
import java.util.List;

/**
 * 나의 포인트 || 포인트로 전환
 */
public class GetChargePoint {
    private Context mContext;
    private Handler mHandler;

    private final String STR_MY_POINT = "m_point"; //나의 포인트
    private final String STR_CHANGE_AMOUNT = "point_chgamnt"; //전환금액
    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;

    private String strMyPoint = "";
    private String strChangeCharge = ""; //전환금액

    public GetChargePoint(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;

        new DataTask().execute();
    }

    public GetChargePoint(Context context, String strChangeCharge, Handler handler) {
        mContext = context;
        mHandler = handler;
        this.strChangeCharge = strChangeCharge;

        new DataTask().execute();
    }

    /**
     * 서버에 값 요청
     */
    private class DataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_charges_point_page);
            SharedPreferences pref = mContext.getSharedPreferences("TokenInfo", mContext.MODE_PRIVATE);
            final String token = pref.getString(mContext.getResources().getString(R.string.str_token), "");

            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(url);
                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(mContext.getResources().getString(R.string.str_token), token));

                if(!strChangeCharge.equals("")){
                    listParams.add(new BasicNameValuePair(STR_CHANGE_AMOUNT, strChangeCharge));
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

            if(!strChangeCharge.equals("") && result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                mHandler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
            }else if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                resultInfo(result);
            } else {
                mHandler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    public void resultInfo(String result) {
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
                        if (parser.getName().equals(STR_MY_POINT)) {
                            k_data_num = PARSER_NUM_0;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    strMyPoint = parser.getText();
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

        if(!strMyPoint.equals("")){
            Message msg = new Message();
            msg.what = StaticDataInfo.RESULT_CODE_200;
            msg.arg1 = StaticDataInfo.RESULT_CODE_MY_POINT;
            msg.obj = strMyPoint;
            mHandler.sendMessage(msg);
        }
    }
}
