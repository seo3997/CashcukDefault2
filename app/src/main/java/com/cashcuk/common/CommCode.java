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
 * 공통 코드 data
 * gb: 분류, ds; 광고기본발송정보, bk: 은행, jd: 회원탈퇴 사유, ch: 캐릭터 카테고리
 */
public class CommCode {
    private Context mContext;
    private Handler handler;
    private Message msg;

    private final int SEND_GUBUN = 2;
    private final int SEND_STEP_NUM = 3;
    private final int SEND_CODE_STEP_1 = 4;

    //공통코드 [[
    private final String STR_COMMON_CODE_TYPE = "gubun"; //코드 타입 -  gb: 분류, jd: 회원탈퇴 사유
    private final String STR_COMMON_STEP_NUM = "step_num"; //차시 분류 - 1차 일때 1, 2차 일때 2, 3차 일때 3
    private final String STR_COMMON_CODE_SETP_1 = "code_step1"; //2차 항목 분류 - step_num이 2일 때 1차 분류의 idx 값
    private final String STR_COMMON_CODE_SETP_2 = "code_step2"; //3차 항목 분류 - step_num이 3일 때 2차 분류의 idx 값
    private String strCommonCodeStep = STR_COMMON_CODE_SETP_1;

    //서버 리턴 값
    private final String STR_COMMON_CODE_IDX = "code_idx"; //link url
    private final String STR_COMMON_CODE_TITLE = "code_title"; //link url
    //공통코드 ]]

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;

    private final int SETP_NUM_1 = 1; //step num
    private final int SETP_NUM_2 = 2; //step num
    private final int SETP_NUM_3 = 3; //step num

    private String strGubun=""; //코드 타입
    private int mStepNum; //1차 분류
    private String strCodeStep1=""; //2차 항목 분류

    public CommCode(Context context, String gubun, int stepNum, String codeStep1, Handler handler){
        mContext = context;
        strGubun = gubun;
        mStepNum = stepNum;
        strCodeStep1 = codeStep1;

        if(stepNum==SETP_NUM_3) strCommonCodeStep = STR_COMMON_CODE_SETP_2;

        this.handler = handler;
        msg = this.handler.obtainMessage();

        CommonCode();
    }

    /**
     * 구분에 따른 공통 코드 요청 (분류, 광고발송기본정보, 은행)
     */
    public void CommonCode(){
//        final String url = StaticDataInfo.STR_URL + "Code/commcode.asp";
        final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_codes_code);
        SharedPreferences pref = mContext.getSharedPreferences("TokenInfo", mContext.MODE_PRIVATE);
        final String token = pref.getString(mContext.getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_GUBUN, strGubun);
        k_param.put(SEND_STEP_NUM, String.valueOf(mStepNum));
        k_param.put(SEND_CODE_STEP_1, strCodeStep1);

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
                listParams.add(new BasicNameValuePair(STR_COMMON_CODE_TYPE, params[SEND_GUBUN]));
                listParams.add(new BasicNameValuePair(STR_COMMON_STEP_NUM, params[SEND_STEP_NUM]));
                listParams.add(new BasicNameValuePair(strCommonCodeStep, params[SEND_CODE_STEP_1]));

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
            Result(result);
        }
    }

    private ArrayList<TxtListDataInfo> arrTempData;
    private TxtListDataInfo mDataInfo;
    /**
     * 결과 값
     */
    public void Result(String result){
        Message msg = new Message();
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrTempData != null && mDataInfo != null) {
                                arrTempData.add(mDataInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrTempData = new ArrayList<TxtListDataInfo>();
                            break;

                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mDataInfo = new TxtListDataInfo();
                            }

                            if (parser.getName().equals(STR_COMMON_CODE_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_COMMON_CODE_TITLE)) {
                                k_data_num = PARSER_NUM_1;
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
                                        mDataInfo.setStrMsg(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                msg.what = StaticDataInfo.RESULT_CODE_200;
                msg.arg1 = mStepNum;
                msg.obj = arrTempData;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_SIGUN))) {
            msg.what = StaticDataInfo.RESULT_NO_SIGUN;
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
            msg.what = StaticDataInfo.RESULT_NO_DATA;
            msg.arg1 = mStepNum;
        }else{
            msg.what = StaticDataInfo.RESULT_CODE_ERR;
        }

        if (msg != null && handler != null) {
            handler.sendMessage(msg);
        }
    }
}
