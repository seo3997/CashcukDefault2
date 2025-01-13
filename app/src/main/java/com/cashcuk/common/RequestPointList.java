package com.cashcuk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.pointlist.PointListDataInfo;

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
 * 누적 or 사용 포인트 내역 요청
 */
public class RequestPointList {
    private Context mContext;
    private Handler handler;
    private Message msg;
    private String strMode; //누적 인지 사용인지
    private String strPageGubun; //포인트 내역에서 인지, 각 activity에서 인지 T: 리스트 개수 3, D: 리스트 개수 10개

    private final int SEND_PAGE_NO = 2;
    private final int SEND_PAGE_GUBUN = 3;

    private final String STR_IDX = "idx";
    private final String STR_PAGE_NO = "pageno";
    private final String STR_PAGE_GUBUN = "pagegubun";

    private final String STR_POINT_DATE = "point_date"; //일자
    private final String STR_POINT_CONTENT = "point_content"; //내용 or 사용처
    private final String STR_POINT_ACCRUE = "point_val"; //누적 or 사용 포인트

    private final String STR_POINT_REQUEST_STATE = "point_yn"; //신청상태
    private final String STR_POINT_ACCOUNT = "point_account"; //계좌번호
    private final String STR_POINT_ACCOUNT_HOLDER = "point_nm"; //예금주
    private final String STR_POINT_ETC = "point_etc"; //입금처리 설명

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;

    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;
    private final int PARSER_NUM_7 = 7;

    private PointListDataInfo mPointInfo;
    private ArrayList<PointListDataInfo> arrPointInfo;

    private int mPageNo = 1; //요청 페이지

    public RequestPointList(Context context, String mode, String pageGubun, Handler handler, int pageNo) {
        mContext = context;
        this.strMode = mode;
        this.strPageGubun = pageGubun;
        this.handler = handler;
        this.mPageNo = pageNo;
        msg = this.handler.obtainMessage();

        DataRequest();
    }

    /**
     * 서버로 전송하는 값
    */
    public void DataRequest() {
        String url="";
        SharedPreferences pref = mContext.getSharedPreferences("TokenInfo", mContext.MODE_PRIVATE);
        final String token = pref.getString(mContext.getResources().getString(R.string.str_token), "");

        if(strMode.equals(StaticDataInfo.MODE_POINT_LIST_ACCRUE)) {
            //2017.05.10 soohyun.Seo Offerwal Point
            //url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_charges_acumdpoint);
            url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_offerwall_acumdpoint);
        }else if(strMode.equals(StaticDataInfo.MODE_POINT_LIST_USE)) {
            url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_charges_usedpoint);
        }

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PAGE_NO, String.valueOf(mPageNo));
        k_param.put(SEND_PAGE_GUBUN, strPageGubun);

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
                listParams.add(new BasicNameValuePair(STR_PAGE_NO, params[SEND_PAGE_NO]));
                listParams.add(new BasicNameValuePair(STR_PAGE_GUBUN, params[SEND_PAGE_GUBUN]));

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
            ResultAccruePointList(result);
        }
    }

    /**
     * 결과 값 parsing
     *
     * @param result
     */
    public void ResultAccruePointList(String result) {
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrPointInfo != null && mPointInfo != null) {
                                arrPointInfo.add(mPointInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrPointInfo = new ArrayList<PointListDataInfo>();
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mPointInfo = new PointListDataInfo();
                            }
                            if (parser.getName().equals(STR_POINT_DATE)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_POINT_CONTENT)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_POINT_ACCRUE)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_POINT_REQUEST_STATE)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_POINT_ACCOUNT)) {
                                k_data_num = PARSER_NUM_5;
                            } else if (parser.getName().equals(STR_POINT_ACCOUNT_HOLDER)) {
                                k_data_num = PARSER_NUM_6;
                            } else if (parser.getName().equals(STR_POINT_ETC)) {
                                k_data_num = PARSER_NUM_7;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_1:
                                        mPointInfo.setStrDate(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mPointInfo.setStrContent(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mPointInfo.setStrPoint(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mPointInfo.setStrRequestState(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mPointInfo.setStrAccount(parser.getText());
                                        break;
                                    case PARSER_NUM_6:
                                        mPointInfo.setStrAccountHolder(parser.getText());
                                        break;
                                    case PARSER_NUM_7:
                                        mPointInfo.setStrEtc(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                msg.what = StaticDataInfo.RESULT_CODE_200;
                //포인트 list에서 뿌릴 data (미리보기 같은..)
                if(strMode.equals(StaticDataInfo.MODE_POINT_LIST_ACCRUE) && strPageGubun.equals("T")){
                    msg.arg1 = StaticDataInfo.FALSE;
                }else if(strMode.equals(StaticDataInfo.MODE_POINT_LIST_USE) && strPageGubun.equals("T")){
                    msg.arg1 = StaticDataInfo.TRUE;
                }
                msg.obj = arrPointInfo;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
            if(strMode.equals(StaticDataInfo.MODE_POINT_LIST_ACCRUE) && strPageGubun.equals("T")){
                msg.arg1 = StaticDataInfo.FALSE;
            }else if(strMode.equals(StaticDataInfo.MODE_POINT_LIST_USE) && strPageGubun.equals("T")){
                msg.arg1 = StaticDataInfo.TRUE;
            }
            msg.what = StaticDataInfo.RESULT_NO_DATA;
        }else{
            msg.what = StaticDataInfo.RESULT_CODE_ERR;
        }

        if (msg != null && handler != null) {
            handler.sendMessage(msg);
        }
    }
}
