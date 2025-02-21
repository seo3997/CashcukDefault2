package com.cashcuk.common;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

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
 * 휴대폰 문자 인증 - 비밀번호 찾기 시
 * 인증번호 요청 및 인증
 */
public class FindPassword {
    private Context mContext;
    private Handler handler;
    private Message msg;

    private String strPassword="";
    private String strHp;
    private String strEmail;

    private final int SEND_HP = 1; //폰 번호 (ex 010-0000-0000)
    private final int SEND_EMAIL = 2; //이메일

    private final String STR_HP = "hp";
    private final String STR_EMAIL = "mail";


    private final String STR_PASSWORD = "password"; //패스워드
    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    public static final int MSG_ARG = 987;


    public FindPassword(Context context, String hp, String mail, Handler handler){
        mContext = context;
        strHp = hp;
        strEmail = mail;
        this.handler = handler;

        msg = this.handler.obtainMessage();

        FindPasswordData();
    }

    /**
     * 문자 인증 요청
     */
    public void FindPasswordData(){
        final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_newmain_mypassword);

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(SEND_HP, strHp);
        k_param.put(SEND_EMAIL, strEmail);

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
                listParams.add(new BasicNameValuePair(STR_HP, params[SEND_HP]));
                listParams.add(new BasicNameValuePair(STR_EMAIL, params[SEND_EMAIL]));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(listParams, HTTP.UTF_8);
                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
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

    public void Result(String result){
        Message msg = new Message();

        if (result.startsWith(StaticDataInfo.TAG_LIST)) {
            resultInfo(result);
        } else {
            msg.what = Integer.parseInt(result);
            this.handler.sendMessage(msg);
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
                        if (parser.getName().equals(STR_PASSWORD)) {
                            k_data_num = PARSER_NUM_0;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    strPassword = parser.getText();
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

        if(!strPassword.equals("")){
            Message msg = new Message();
            msg.what = StaticDataInfo.RESULT_CODE_200;
            msg.arg1 = MSG_ARG;
            msg.obj = strPassword;
            this.handler.sendMessage(msg);
        }
    }



}
