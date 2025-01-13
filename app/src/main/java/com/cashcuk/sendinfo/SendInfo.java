package com.cashcuk.sendinfo;

import android.content.Context;
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
 * 발송기본정보
 */
public class SendInfo {
    private final String STR_MODE_AD_SEND_DEFAULT_INFO_TITLE = "ADSendDefaultInfoTitle"; //광고 발송 기본정보 title
    private final String STR_MODE_AD_SEND_DEFAULT_INFO = "ADSendDefaultInfo"; //광고 발송 기본정보
    private String strMode = STR_MODE_AD_SEND_DEFAULT_INFO_TITLE;

    private final int SEND_TYPE = 1; //회원가입 시 or push - M:회원가입, P: push
    private final int SEND_CCODE = 1; //발송기본정보 구분값
    private final String STR_TYPE = "type";
    private final String STR_CCODE = "cccode";

    private TxtListDataInfo mADSendDefaultInfo;
    private ArrayList<TxtListDataInfo> arrADSendDefaultInfoTemp;

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;

    //광고 발송 기본정보 return
    private final String STRING_CODE_TITLE = "code_title"; //광고 발송 기본 정보
    private final String STRING_CODE_IDX = "code_idx"; //광고 발송 기본 정보 고유 값

    private Context mContext;
    private Handler handler;
    private Message msg;
    private int mIndex;
    private String strType="";

    public SendInfo(Context context, Handler handler, String strMode, int index, String type){
        mContext = context;
        this.handler = handler;
        this.strMode = strMode;
        this.mIndex = index;
        strType = type;

        msg = this.handler.obtainMessage();

        ADSendDefaultInfo(this.strMode);
    }

    /**
     * 발송 기본정보
     */
    public void ADSendDefaultInfo(String strCCode) {
        final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_codes_push);

        if(strCCode.equals("") || strCCode.equals(STR_MODE_AD_SEND_DEFAULT_INFO_TITLE)){
            strMode = STR_MODE_AD_SEND_DEFAULT_INFO_TITLE;
        }else{
            strMode = STR_MODE_AD_SEND_DEFAULT_INFO;
        }
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);

        if(strMode.equals(STR_MODE_AD_SEND_DEFAULT_INFO)) {
            k_param.put(SEND_CCODE, strCCode);
        }else{
            k_param.put(SEND_TYPE, strType);
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

                if (strMode.equals(STR_MODE_AD_SEND_DEFAULT_INFO)) {
                    listParams.add(new BasicNameValuePair(STR_CCODE, params[SEND_CCODE]));
                }else{
                    listParams.add(new BasicNameValuePair(STR_TYPE, params[SEND_TYPE]));
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
            if (strMode != null && strMode.equals(STR_MODE_AD_SEND_DEFAULT_INFO_TITLE) || strMode.equals(STR_MODE_AD_SEND_DEFAULT_INFO)) {
                ResultADSendDefaultInfo(result);
            }
        }
    }

    /**
     * 광고 발송 기본정보 결과 값 parsing
     * @param result 서버에서 받은 결과 값
     */
    private void ResultADSendDefaultInfo(String result){
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrADSendDefaultInfoTemp != null && mADSendDefaultInfo != null) {
                                arrADSendDefaultInfoTemp.add(mADSendDefaultInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrADSendDefaultInfoTemp = new ArrayList<TxtListDataInfo>();
                            break;

                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mADSendDefaultInfo = new TxtListDataInfo();
                            }

                            if(parser.getName().equals(STRING_CODE_TITLE)){
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STRING_CODE_IDX)) {
                                k_data_num = PARSER_NUM_1;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;


                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mADSendDefaultInfo.setStrMsg(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mADSendDefaultInfo.setStrIdx(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                msg.what = StaticDataInfo.RESULT_SEND_AD_INFO;
                msg.arg1 = mIndex;
                msg.obj = arrADSendDefaultInfoTemp;
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
