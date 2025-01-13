package com.cashcuk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.dialog.DlgSelImg;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 광고주 PUSH 발송 요청
 */
public class PushTargetSend {
    private Context mContext;
    private Handler handler;
    private Message msg;

    //전송 값
    private final int SEND_PUSH_CODES = 2;              // 일반정보 Index
    private final int SEND_PUSH_NUM = 3;                // 발송 건 수 Index
    private final int SEND_AD_IDX = 4;                  // 광고 idx
    private final int SEND_IMG_CHANGE = 6;             // 캐릭터 이미지 변경 유/무
    private final int SEND_PUSH_IDX = 7;               // push idx
    private final int SEND_PUSH_DATE_TIME = 5;               // push 발송 요청 시간

    private final String STR_PUSH_CODES = "push_codes"; // 일반정보 전송 TAG
    private final String STR_PUSH_NUM = "push_num";     // 발송 건 수 TAG
    private final String STR_CHAR_IMG = "push_img";     // 캐릭터 이미지 TAG
    private final String STR_AD_IDX = "ad_idx";     // 광고 idx TAG
    private final String STR_IMG_CHANGE = "img_change";// 캐릭터 이미지 변경 유/무 TAG
    private final String STR_PUSH_IDX = "push_idx";     // push idx TAG
    private final String STR_PUSH_DATE_TIME = "send_date";      // push 발송 요청 시간

    private String strData;
    private String strADIdx;
    private String strPushIdx;
    private String strImgChange;
    private String pushNum;
    private DlgSelImg charImg;
    private String strSendDateTime;

    public PushTargetSend(Context context, Handler handler, String strADIdx, String strPushIdx, boolean isChangeImg, String strData, String pushNum, DlgSelImg charImg, String dateTime){
        this.mContext = context;
        this.strADIdx = strADIdx;
        this.strPushIdx = strPushIdx;
        this.handler = handler;
        this.strData = strData;
        this.pushNum = pushNum;
        this.charImg = charImg;
        this.strSendDateTime = dateTime;

        if(isChangeImg) {
            this.strImgChange = StaticDataInfo.STRING_Y;
            //resizeImg(charImg.getFilePath(), charImg.getFileName());
            resizeImg(charImg.getImageFilePath());
        }else{
            this.strImgChange = StaticDataInfo.STRING_N;
        }

        msg = this.handler.obtainMessage();

        setAdvertiserInfo();
    }

    public void setAdvertiserInfo() {
        final String url= mContext.getResources().getString(R.string.str_new_url) + mContext.getResources().getString(R.string.str_push);
        SharedPreferences pref = mContext.getSharedPreferences("TokenInfo", mContext.MODE_PRIVATE);
        final String token = pref.getString(mContext.getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_PUSH_CODES, strData);
        k_param.put(SEND_PUSH_NUM, pushNum);
        k_param.put(SEND_AD_IDX, strADIdx);
        k_param.put(SEND_IMG_CHANGE, strImgChange);
        k_param.put(SEND_PUSH_IDX, strPushIdx);
        k_param.put(SEND_PUSH_DATE_TIME, strSendDateTime);

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
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addTextBody(mContext.getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_PUSH_CODES, params[SEND_PUSH_CODES], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_PUSH_NUM, params[SEND_PUSH_NUM], ContentType.create("Multipart/related", "UTF-8"));
            if(strImgChange.equals("Y")) {
                builder.addPart(STR_CHAR_IMG, contentMiddleThumbNail);
            }
            builder.addTextBody(STR_AD_IDX, params[SEND_AD_IDX], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_PUSH_DATE_TIME, params[SEND_PUSH_DATE_TIME], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_IMG_CHANGE, params[SEND_IMG_CHANGE], ContentType.create("Multipart/related", "UTF-8"));

            if(strPushIdx!=null && !strPushIdx.equals("")) {
                builder.addTextBody(STR_PUSH_IDX, params[SEND_PUSH_IDX], ContentType.create("Multipart/related", "UTF-8"));
            }

            // Send Request
            InputStream inputStream = null;
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

            httpParams = client.getParams();
            post.setEntity(builder.build());
            HttpResponse responsePOST = null;
            try {
                responsePOST = client.execute(post);
                HttpEntity resEntity = responsePOST.getEntity();

                if (resEntity != null) {
                    retMsg = EntityUtils.toString(resEntity);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return retMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Message msg = new Message();
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))) {
                msg.what = StaticDataInfo.RESULT_CODE_200;
            }else{
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
            }

            if(msg!=null) {
                handler.sendMessage(msg);
            }
        }
    }

    private ByteArrayBody decodeSampledFromPath(String path, int reqWidth,int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        
        Bitmap src = BitmapFactory.decodeFile(path, options);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 80, bos);
        byte[] data = bos.toByteArray();
        File oFile = new File(path);
        ByteArrayBody bab = new ByteArrayBody(data, oFile.getName());

        return bab;
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private ContentBody contentMiddleThumbNail;
    private void resizeImg(String dir, String name){
        String strImgDir = dir+name;
        ByteArrayBody retVal = decodeSampledFromPath(strImgDir, Integer.valueOf(mContext.getResources().getString(R.string.str_ad_char_w)), Integer.valueOf(mContext.getResources().getString(R.string.str_ad_h)));

        if(retVal!=null) {
            contentMiddleThumbNail = retVal;
        }
    }
    private void resizeImg(String dirname){
        String strImgDir = dirname;
        ByteArrayBody retVal = decodeSampledFromPath(strImgDir, Integer.valueOf(mContext.getResources().getString(R.string.str_ad_char_w)), Integer.valueOf(mContext.getResources().getString(R.string.str_ad_h)));

        if(retVal!=null) {
            contentMiddleThumbNail = retVal;
        }
    }
}
