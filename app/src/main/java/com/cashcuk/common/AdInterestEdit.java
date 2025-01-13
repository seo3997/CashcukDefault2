package com.cashcuk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.ad.adlist.ListADInfo;

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
 * 관심광고 등록/해제
 */
public class AdInterestEdit {
    private Handler handler;
    private Message msg;

    private final int SEND_AD_IDX = 2;
    private final int SEND_EDIT_MY_AD_TYPE = 3;

    private final String STR_IDX = "idx"; //개인 고유 값
    private final String STR_AD_IDX = "ad_idx"; //광고 idx
    private final String STR_LOGIN_TYPE = "type"; //로그인 type(U: 일반, A: 광고주)
    private final String STR_EDIT_TYPE = "editType"; //마이광고 등록 여부

    private final String STR_ADD = "A"; //마이광고 등록
    private final String STR_CANCEL = "D"; //마이광고 등록 취소

    private boolean bADAddMode; //마이광고 등록, 해제 모드
    private String strADAddMode = STR_ADD;
    private ListADInfo mADAddTemp;
    private Context mContext;

    public AdInterestEdit(Context context, ListADInfo mADAddTemp, boolean bADAddMode, Handler handler){
        mContext = context;
        this.mADAddTemp = mADAddTemp;
        this.bADAddMode = bADAddMode;
        this.handler = handler;
        msg = this.handler.obtainMessage();

        if(this.bADAddMode) {
            strADAddMode = STR_ADD;
        }else{
            strADAddMode = STR_CANCEL;
        }

        setMyADAdd(this.mADAddTemp);
    }

    public void setMyADAdd(ListADInfo mADAdd){
        final String url = mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_advertise_like);
        SharedPreferences pref = mContext.getSharedPreferences("TokenInfo", mContext.MODE_PRIVATE);
        final String token = pref.getString(mContext.getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_AD_IDX, mADAdd.getStrIdx());
        k_param.put(SEND_EDIT_MY_AD_TYPE, strADAddMode);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new MyADTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class MyADTask extends AsyncTask<String, Void, String> {
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
                listParams.add(new BasicNameValuePair(STR_AD_IDX, params[SEND_AD_IDX]));
                listParams.add(new BasicNameValuePair(STR_EDIT_TYPE, params[SEND_EDIT_MY_AD_TYPE]));

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
            Message msg = new Message();
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                msg.what = StaticDataInfo.ADVER_INTEREST;
                msg.arg1 = StaticDataInfo.RESULT_CODE_200;
                if(bADAddMode){
                    msg.arg2 = StaticDataInfo.TRUE;
                }else if(!bADAddMode){
                    msg.arg2 = StaticDataInfo.FALSE;
                }
            }else{
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
            }

            handler.sendMessage(msg);
        }
    }
}
