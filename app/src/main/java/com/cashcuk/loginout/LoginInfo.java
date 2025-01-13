package com.cashcuk.loginout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lgoin class
 */
public class LoginInfo {
    private Context mContext;
    private String strEmail="";
    private String strPWD="";
    private String strAppVersion ="";
    private Handler mHandler = null;
    private Message msg;

    private final int SEND_ID = 1;
    private final int SEND_PWD = 2;
    private final int SEND_REG_ID = 3;
    private final int SEND_APPVER = 4;

    //서버에 전송 값
    private final String STR_ID = "id";
    private final String STR_PWD = "pass";
    private final String STR_REG_ID = "reg_id";
    private final String STR_APPVER = "appver";

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;

    //서버에서 받는 값
    private final String STR_IDX = "login_idx";
    private final String STR_SI = "login_si";
    private final String STR_GU = "login_gu";
    private final String STR_SEX = "login_sex";
    private final String STR_AGE = "login_age";
    private final String STR_TOKEN = "token";

    //결과 저장 info
    private LoginResultInfo mLoginInfo = new LoginResultInfo();

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public LoginInfo(Context c, String id, String pwd, String appVersion, Handler handler) {
        mContext = c;
        strEmail = id;
        strPWD = pwd;
        strAppVersion = appVersion;

        mHandler = handler;

        msg = mHandler.obtainMessage();
        Check_Login();
    }

    public void Check_Login() {
        // 서비스가 시작되지 않았을 때만 시작 - 한번만 시작함.
        if (!CheckLoginService.start_service) {
            Intent mServiceIntent = new Intent(CheckLoginService.class.getName());
            mServiceIntent.setPackage("com.cashcuk.loginout");
            mContext.startService(mServiceIntent);
        }

        Action_Login();
    }

    public void Action_Login() {
        SharedPreferences prefs = mContext.getSharedPreferences("SaveRegId", mContext.MODE_PRIVATE);
        //String regId = prefs.getString("setRegId","");
        String regId = prefs.getString("setRegId","1");

        Log.d("temp", "Action_Login token: " + regId);

        final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_member_login);

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(SEND_ID, strEmail);
        k_param.put(SEND_PWD, strPWD);
        k_param.put(SEND_REG_ID, regId);
        k_param.put(SEND_APPVER, strAppVersion);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        executor.execute(() -> {
            String result = doInBackground(strTask);
            mainHandler.post(() -> onPostExecute(result));
        });
    }

    private String doInBackground(String... params) {
        String retMsg = "";
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(params[StaticDataInfo.SEND_URL]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(StaticDataInfo.TIME_OUT);
            urlConnection.setReadTimeout(StaticDataInfo.TIME_OUT);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            List<NameValuePair> listParams = new ArrayList<>();
            listParams.add(new BasicNameValuePair(STR_ID, params[SEND_ID]));
            listParams.add(new BasicNameValuePair(STR_PWD, params[SEND_PWD]));
            listParams.add(new BasicNameValuePair(STR_REG_ID, params[SEND_REG_ID]));
            listParams.add(new BasicNameValuePair(STR_APPVER, params[SEND_APPVER]));

            String postData = getPostDataString(listParams);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                retMsg = readStream(urlConnection.getInputStream());
            } else {
                retMsg = "Error: " + responseCode;
            }
        } catch (Exception e) {
            retMsg = e.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return retMsg;
    }

    private String getPostDataString(List<NameValuePair> params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private void onPostExecute(String result) {
        if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_USER))){
            msg.what = StaticDataInfo.RESULT_NO_USER;
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_PWD_ERR))){
            msg.what = StaticDataInfo.RESULT_PWD_ERR;
        }else if (result.startsWith(StaticDataInfo.TAG_LIST)){
            resultLogin(result);
            return;
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_OVERLAP_ERR))) {
            msg.what = StaticDataInfo.RESULT_OVERLAP_ERR;
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
            msg.what = StaticDataInfo.RESULT_NO_DATA;
        } else {
            msg.what = StaticDataInfo.RESULT_CODE_ERR;
        }

        if(mHandler!=null && msg!=null) {
            mHandler.sendMessage(msg);
        }
    }

    public void resultLogin(String result){
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

                        if(parser.getName().equals(STR_IDX)){
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_TOKEN)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_SI)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_GU)) {
                            k_data_num = PARSER_NUM_3;
                        } else if (parser.getName().equals(STR_SEX)) {
                            k_data_num = PARSER_NUM_4;
                        } else if (parser.getName().equals(STR_AGE)) {
                            k_data_num = PARSER_NUM_5;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;


                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    mLoginInfo.setStrIdx(parser.getText());
                                    break;
                                case PARSER_NUM_1:
                                    mLoginInfo.setStrToken(parser.getText());
                                    break;
                                case PARSER_NUM_2:
                                    mLoginInfo.setStrSi(parser.getText());
                                    break;
                                case PARSER_NUM_3:
                                    mLoginInfo.setStrGu(parser.getText());
                                    break;
                                case PARSER_NUM_4:
                                    mLoginInfo.setStrSex(parser.getText());
                                    break;
                                case PARSER_NUM_5:
                                    mLoginInfo.setStrAge(parser.getText());
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
            LoginSuccess(mLoginInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LoginSuccess(LoginResultInfo strLoginResult) {
        SharedPreferences prefs = mContext.getSharedPreferences("SaveLoginInfo", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LogIn_ID", strEmail);
        editor.putString("LogIn_PWD", strPWD);
        editor.putBoolean("IsLogin", true);
        editor.commit();

        SharedPreferences prefToken = mContext.getSharedPreferences("TokenInfo", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editorToken = prefToken.edit();
        editorToken.putString("token", mLoginInfo.getStrToken());
        editorToken.commit();

        if (mHandler != null) {
             mHandler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
        }
    }
}
