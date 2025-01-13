package com.cashcuk.common;

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
 * 사는 곳
 */
public class AddrUser {
    private Context mContext;
    private Handler handler;
    private Message msg;

    private final int SEND_SI_DO_GUBUN = 1; //사는 곳 시도, 시군 구분
    private final int SEND_SI_DO_IDX = 2; //사는 곳 시,군,구 선택 시 시도 idx 값 넘김.

    private final String STR_SI_DO_GUBUN = "gubun";
    private final String STR_SI_DO_IDX = "sido_idx";

    private final String STR_SI_MODE = "S"; //시,도 받아옴.
    private final String STR_GUN_MODE = "G"; //시,군,구 받아옴.
    private String strSiDoMode=STR_SI_MODE;

    private String selSiDoIdx="";

    //시도, 시군구
    private TxtListDataInfo mHangOutDataInfo;
    private ArrayList<TxtListDataInfo> arrHangOutTempData;

    //사는 곳 return
    private final String STRING_ADDR_IDX = "addr_idx"; //선택한 고유 값
    private final String STRING_ADDR_NM = "addr_title"; //주소 명

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;

    public AddrUser(Context context, Handler handler, String strMode, String selSiDoIdx){
        this.strSiDoMode = strMode;
        this.handler = handler;
        this.selSiDoIdx = selSiDoIdx;
        mContext = context;

        msg = this.handler.obtainMessage();

        AddrUser();
    }

    /**
     * 사는 곳
     */
    public void AddrUser() {
        final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_codes_addr);

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(SEND_SI_DO_GUBUN, strSiDoMode);
        if(strSiDoMode.equals(STR_GUN_MODE) && selSiDoIdx!=null && !selSiDoIdx.equals("")) {
            k_param.put(SEND_SI_DO_IDX, selSiDoIdx);
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

                listParams.add(new BasicNameValuePair(STR_SI_DO_GUBUN, params[SEND_SI_DO_GUBUN]));
                if (selSiDoIdx != null && !selSiDoIdx.equals("")) {
                    listParams.add(new BasicNameValuePair(STR_SI_DO_IDX, params[SEND_SI_DO_IDX]));
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
                ResultHangOut(result);
        }
    }

    /**
     * 사는 곳 결과 값 parsing
     * @param result 서버에서 받은 결과 값
     */
    public void ResultHangOut(String result){
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrHangOutTempData != null && mHangOutDataInfo != null) {
                                arrHangOutTempData.add(mHangOutDataInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrHangOutTempData = new ArrayList<TxtListDataInfo>();
                            break;

                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mHangOutDataInfo = new TxtListDataInfo();
                            }

                            if (parser.getName().equals(STRING_ADDR_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STRING_ADDR_NM)) {
                                k_data_num = PARSER_NUM_1;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mHangOutDataInfo.setStrIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mHangOutDataInfo.setStrMsg(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                if(strSiDoMode.equals(STR_SI_MODE)) {
                    msg.what = StaticDataInfo.RESULT_SI_DO;
                }else if(strSiDoMode.equals(STR_GUN_MODE)){
                    msg.what = StaticDataInfo.RESULT_SI_GUN_GU;
                }
                msg.obj = arrHangOutTempData;
//                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_SIGUN)) || (strSiDoMode.equals(STR_GUN_MODE) && result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA)))) {
            msg.what = StaticDataInfo.RESULT_NO_SIGUN;
        }else{
            msg.what = StaticDataInfo.RESULT_CODE_ERR;

        }

        handler.sendMessage(msg);
    }
}
