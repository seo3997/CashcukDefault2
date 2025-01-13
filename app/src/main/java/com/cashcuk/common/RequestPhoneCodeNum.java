package com.cashcuk.common;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 폰 인증번호 요청
 */
public class RequestPhoneCodeNum {
    private Context mContext;
    private String strPhoneNum;
    private Handler mHandler;

    private final int SEND_PNONE_NUM = 1;
    private final String STR_PNONE_NUM = "hp";

    public RequestPhoneCodeNum(Context context, String PhoneNum, Handler handler){
        mContext = context;
        strPhoneNum = PhoneNum;
        mHandler = handler;

        RequestCodeNum();
    }

    /**
     * 인증번호 요청
     */
    public void RequestCodeNum(){
        final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_member_message);

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(SEND_PNONE_NUM, strPhoneNum);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new RequestTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class RequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);
                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(STR_PNONE_NUM, params[SEND_PNONE_NUM]));

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
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_USER))){
                mHandler.sendEmptyMessage(StaticDataInfo.RESULT_NO_USER);
            }else if(!result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                mHandler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }
}
