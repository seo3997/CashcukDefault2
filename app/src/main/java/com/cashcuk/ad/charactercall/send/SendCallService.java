package com.cashcuk.ad.charactercall.send;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.ad.charactercall.ScreenStateBroadCast;
import com.cashcuk.ad.charactercall.receive.ReceiveCharDisplayInfo;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 전화 발신 시 service
 */
public class SendCallService extends Service implements View.OnClickListener {
    public static View rootSendNewView=null;
    private WindowManager.LayoutParams params;
    public static WindowManager windowSendNewManager=null;
    private SharedPreferences prefs;
    private int x,y;

    private ReceiveCharDisplayInfo mDisplayInfo;
    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final String STR_RECEIVE_NUM = "receive_hp";
    private final String STR_CHAR_REP = "char_rep";

    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;

    private ScreenStateBroadCast mScreenStateReceiver;
    private String strReceiveNum = "";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent!=null) {
            strReceiveNum = intent.getStringExtra("ReceiveNum");
            getTitle();
        }

        return START_REDELIVER_INTENT; //재생성과 onStartCommand() 호출(with same intent)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removePopup();

        if(mScreenStateReceiver!=null) unregisterReceiver(mScreenStateReceiver);
    }

    public void removePopup() {
        if (rootSendNewView != null && windowSendNewManager != null){
            windowSendNewManager.removeView(rootSendNewView);
            rootSendNewView = null;
            windowSendNewManager = null;
        }

        this.stopSelf();
    }

    public void chkDBData(String receiveNum){
        mDisplayInfo = new ReceiveCharDisplayInfo();
        String strDataId = "";
        SendDBopenHelper mOpenHelper = new SendDBopenHelper(this);

        try {
            mOpenHelper.open();
            Cursor cursor = mOpenHelper.Search(SendDB.HP_NUM + " = " + "'" + receiveNum + "'");

            if (cursor.moveToFirst()) {
                strDataId = cursor.getString(cursor.getColumnIndex(SendDB.CreateDB._ID));

                mDisplayInfo.setStrCharImgIdx(cursor.getString(cursor.getColumnIndex(SendDB.CHAR_IMG_IDX)));
                if(strTitle.equals("")) {
                    mDisplayInfo.setStrCharTitle(cursor.getString(cursor.getColumnIndex(SendDB.CHAR_TITLE_TXT)));
                }else{
                    mDisplayInfo.setStrCharTitle(strTitle);
                }
                mDisplayInfo.setStrCharSendMsg(cursor.getString(cursor.getColumnIndex(SendDB.CHAR_SEND_MSG)));
                mDisplayInfo.setStrCharImgPath(cursor.getString(cursor.getColumnIndex(SendDB.CHAR_IMG_PATH)));
            } else {
                TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String phoneNum = telManager.getLine1Number();

                final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_regi_character_send);
                SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
                final String token = pref.getString(getResources().getString(R.string.str_token), "");

                HashMap<Integer, String> k_param = new HashMap<Integer, String>();
                k_param.put(StaticDataInfo.SEND_URL, url);
                k_param.put(StaticDataInfo.SEND_TOKEN, token);
                k_param.put(SEND_SENDER_HP, phoneNum);
                if (receiveNum.startsWith("+82")) {
                    receiveNum = phoneNum.replace("+82", "0");
                }
                k_param.put(SENT_RECEIVE_HP, receiveNum);

                String[] strTask = new String[k_param.size()];
                for (int i = 0; i < strTask.length; i++) {
                    strTask[i] = k_param.get(i);
                }

                new DataTask().execute(strTask);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mOpenHelper != null) mOpenHelper.close();
        }

        String strImgLocalPath = mDisplayInfo.getStrCharImgPath();
        if (strImgLocalPath != null && !strImgLocalPath.equals("")) {
            File imgFile = new File(strImgLocalPath);

            if (!imgFile.exists()) {
                strImgLocalPath = storeImage(mDisplayInfo.getStrCharImgIdx(), mDisplayInfo.getStrCharImgPath());
                //이미지 스캔해서 갤러리 업데이트
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imgFile)));
                mDisplayInfo.setStrCharImgPath(strImgLocalPath);
            }
            display(mDisplayInfo.getStrCharTitle(), mDisplayInfo.getStrCharSendMsg(), strImgLocalPath);
        }
    }

    public String storeImage(String imgIdx, String imgPath) {
        String result = "";
        String filePath="";
        filePath = Environment.getExternalStorageDirectory() + "/"+"Cashcuk/SendCashcuk";
        // crop된 이미지를 저장하기 위한 파일 경로
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String fileName = "/"+imgIdx + ".jpg";

        File dir = new File(filePath);
        File file = new File(filePath+fileName);

        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();

            String[] strTask = new String[2];
            strTask[0] = imgPath;
            strTask[1] = filePath + fileName;

            result = new ProcessTask().execute(strTask).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private class ProcessTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";
            try {
                URL url = new URL(params[0]);
                //서버와 접속하는 클라이언트 객체 생성
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                //입력 스트림을 구한다
                InputStream is = conn.getInputStream();

                //파일 저장 스트림 생성
                FileOutputStream fos = new FileOutputStream(params[1]);
                int read;
                //입력 스트림을 파일로 저장
                for (;;) {
                    read = is.read(tmpByte);
                    if (read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, read); //file 생성
                }
                is.close();
                fos.close();
                conn.disconnect();

                retMsg = params[1];
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return retMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ib_close){
            removePopup();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void display(String title, String msg, String path){
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenStateReceiver = new ScreenStateBroadCast();
        registerReceiver(mScreenStateReceiver, screenStateFilter);


        windowSendNewManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Display display = windowSendNewManager.getDefaultDisplay();
        int height = (int) (display.getHeight() * 0.55); //Display 사이즈의 55%

        prefs = getSharedPreferences("PopupPoint", MODE_PRIVATE);
        x = prefs.getInt("X", 0);
        y = prefs.getInt("Y", 0);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                height,
                x, y,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        rootSendNewView = layoutInflater.inflate(R.layout.dlg_send_receive, null);

        ((TextView) rootSendNewView.findViewById(R.id.txt_title)).setText(title);
        ((TextView) rootSendNewView.findViewById(R.id.txt_send_msg)).setText(msg);
        ((ImageButton) rootSendNewView.findViewById(R.id.ib_close)).setOnClickListener(this);

        ImageView ivCharImg = (ImageView)rootSendNewView.findViewById(R.id.iv_ad_img);
        String strFilePathName = path;
        if(!strFilePathName.equals("")){
            File imgFile = new  File(strFilePathName);
            if(imgFile.exists()) {
                Bitmap bit = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ivCharImg.setBackground(new BitmapDrawable(getResources(), bit));
                }else{
                    ivCharImg.setBackgroundDrawable(new BitmapDrawable(getResources(), bit));
                }
            }
        }

        if (rootSendNewView != null) {
            windowSendNewManager.addView(rootSendNewView, params);

        }

        rootSendNewView.setOnTouchListener(mTouch);
    }

    int initialX;
    int initialY;
    float initialTouchX;
    float initialTouchY;
    private View.OnTouchListener mTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = x;
                    initialY = y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();

                    return true;
                case MotionEvent.ACTION_UP:
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("X", params.x);
                    editor.putInt("Y", params.y);
                    editor.commit();
                    x = params.x;
                    y = params.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = initialX + (int) (event.getRawX() - initialTouchX);
                    params.y = initialY + (int) (event.getRawY() - initialTouchY);

                    if (rootSendNewView != null)
                        windowSendNewManager.updateViewLayout(rootSendNewView, params);
                    return true;
            }
            return false;
        }
    };

    private final String STR_SENDER_HP = "sender_hp";
    private final String STR_RECEIVE_HP = "receiver_hp";
    private final int SEND_SENDER_HP = 2;
    private final int SENT_RECEIVE_HP = 3;

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
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_SENDER_HP, params[SEND_SENDER_HP]));
                listParams.add(new BasicNameValuePair(STR_RECEIVE_HP, params[SENT_RECEIVE_HP]));

                httpParams = client.getParams();
//                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
//                HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
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
            resultSend(result);
        }
    }

    private final String STR_CHAR_IDX = "char_idx";
    private final String STR_CHAR_URL = "char_url";
    private final String STR_CHAR_MSG = "char_msg";
    private final String STR_CHAR_TITLE = "sys_char_msg";
    private void resultSend(String result){
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
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mDisplayInfo = new ReceiveCharDisplayInfo();
                            }

                            if (parser.getName().equals(STR_CHAR_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_CHAR_URL)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_CHAR_MSG)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_CHAR_TITLE)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_CHAR_REP)) {
                                k_data_num = PARSER_NUM_4;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mDisplayInfo.setStrCharImgIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mDisplayInfo.setStrCharImgPath(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mDisplayInfo.setStrCharSendMsg(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mDisplayInfo.setStrCharTitle(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mDisplayInfo.setStrRep(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                setData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setData(){
        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        String strEmail = prefs.getString("LogIn_ID", "");

        TelephonyManager telManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String phoneNum = telManager.getLine1Number();

        SendDBopenHelper mOpenHelper = new SendDBopenHelper(this);
        try {
            mOpenHelper.open();
            if(mDisplayInfo.getStrRep().equals(String.valueOf(StaticDataInfo.TRUE))) {
                mOpenHelper.insert(getResources().getString(R.string.str_rep_img), strEmail, phoneNum, mDisplayInfo.getStrCharImgPath(), mDisplayInfo.getStrCharImgIdx(), mDisplayInfo.getStrCharSendMsg(), mDisplayInfo.getStrCharTitle());
            }else{
                mOpenHelper.insert("", strEmail, phoneNum, mDisplayInfo.getStrCharImgPath(), mDisplayInfo.getStrCharImgIdx(), mDisplayInfo.getStrCharSendMsg(), mDisplayInfo.getStrCharTitle());
            }

            String strImgLocalPath = mDisplayInfo.getStrCharImgPath();
            if (strImgLocalPath != null && !strImgLocalPath.equals("")) {
                File imgFile = new File(strImgLocalPath);

                if (!imgFile.exists()) {
                    if(strImgLocalPath!=null && !strImgLocalPath.equals("")) {
                        strImgLocalPath = storeImage(mDisplayInfo.getStrCharImgIdx(), mDisplayInfo.getStrCharImgPath());
                        //이미지 스캔해서 갤러리 업데이트
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imgFile)));
                        mDisplayInfo.setStrCharImgPath(strImgLocalPath);
                    }
                }
                display(mDisplayInfo.getStrCharTitle(), mDisplayInfo.getStrCharSendMsg(), strImgLocalPath);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            mOpenHelper.close();
        }
    }

    /**
     * 서버에 값 요청
     */
    private class titleDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);
                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));

                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
                HttpConnectionParams.setSoTimeout(httpParams, 10000);
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
            resultTitle(result);
        }
    }

    public void getTitle(){
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_regi_character_call_title);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new titleDataTask().execute(strTask);
    }

    private final String STR_SYS_CHAR_MSG = "sys_char_msg";
    private String strTitle="";
    private void resultTitle(String result){
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
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(STR_SYS_CHAR_MSG)) {
                                k_data_num = PARSER_NUM_0;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        strTitle = parser.getText();
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
        }

        chkDBData(strReceiveNum);
    }
}
