package com.cashcuk.ad.charactercall.receive;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
 * 전화 수신 시 service
 */
public class ReceiveCallService extends Service implements View.OnClickListener {
    public static View rootView;
    private WindowManager.LayoutParams params;
    public static WindowManager windowManager;
    private SharedPreferences prefs;
    private int x,y;
    private ReceiveCharDisplayInfo mReceiveDisplayInfo  = null;

    private String strIncomingNum;

    private final String STR_INCOMING_NUM = "sender_hp";
    private final String STR_INCOMING_EMAIL = "sender_email";
    private final int SEND_INCOMING_NUM = 2;
    private final int SEND_INCOMING_EMAIL = 3;

    private final String STR_CHAR_IMG_IDX = "char_idx";
    private final String STR_CHAR_IMG_URL = "char_url";
    private final String STR_SEND_MSG = "char_msg";
    private final String STR_CHAR_TITLE = "sys_char_msg";

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;

    ReceiveDBopenHelper mOpenHelper = new ReceiveDBopenHelper(this);
    private ScreenStateBroadCast mScreenStateReceiver;


    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = null;
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
        strIncomingNum = intent.getStringExtra("IncomingNum");
        removePopup();
        chkEmail();
        return START_REDELIVER_INTENT; //재생성과 onStartCommand() 호출(with same intent)
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private String strSenderEmail="";
    public void chkEmail(){
        try {
            mOpenHelper.open();
            Cursor cursor = mOpenHelper.Search(ReceiveDB.HP_NUM + " = " + "'" + strIncomingNum + "'");

            if (cursor.moveToFirst()) {
                strSenderEmail = cursor.getString(cursor.getColumnIndex(ReceiveDB.HP_EMAIL));
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(mOpenHelper!=null) mOpenHelper.close();
        }


        DataRequest();
    }

    public void searchIncomingNum(String incomingNumber) {
        try {
            mOpenHelper.open();
            Cursor cursor = mOpenHelper.Search(ReceiveDB.HP_EMAIL +  " = " + "'"+mReceiveInfo.getStrIncomingEmail().toString()+"'");

            String strDataId = "";
            if (cursor.moveToFirst()) {
                strDataId = cursor.getString(cursor.getColumnIndex(ReceiveDB.CreateDB._ID));

                if(!incomingNumber.equals(cursor.getString(cursor.getColumnIndex(ReceiveDB.HP_NUM)))
                        || !mReceiveInfo.getStrIncomingEmail().equals(cursor.getString(cursor.getColumnIndex(ReceiveDB.HP_EMAIL)))
                        || !mReceiveInfo.getStrCharImgIdx().equals(cursor.getString(cursor.getColumnIndex(ReceiveDB.CHAR_IMG_IDX)))
                        || (mReceiveInfo.getStrCharSendMsg()!=null && !mReceiveInfo.getStrCharSendMsg().equals(cursor.getString(cursor.getColumnIndex(ReceiveDB.CHAR_SEND_MSG))))
                        || !mReceiveInfo.getStrCharTitle().equals(cursor.getString(cursor.getColumnIndex(ReceiveDB.CHAR_TITLE_TXT))))  {

                    String strImgPath = storeImage();
                    if(!strImgPath.equals("")) {
                        File file = new File(strImgPath);
                        //이미지 스캔해서 갤러리 업데이트
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        mOpenHelper.update(strDataId, incomingNumber, strImgPath, mReceiveInfo.getStrCharImgIdx(), mReceiveInfo.getStrCharSendMsg(), mReceiveInfo.getStrCharTitle());
                        mReceiveInfo.setStrCharImgUrl(strImgPath);
                        displayDataSet(incomingNumber, mReceiveInfo.getStrIncomingEmail(), strImgPath, mReceiveInfo.getStrCharImgIdx(), mReceiveInfo.getStrCharSendMsg(), mReceiveInfo.getStrCharTitle());
                    }
                }else{
                    displayDataSet(cursor.getString(cursor.getColumnIndex(ReceiveDB.HP_NUM)),
                            cursor.getString(cursor.getColumnIndex(ReceiveDB.HP_EMAIL)),
                            cursor.getString(cursor.getColumnIndex(ReceiveDB.CHAR_IMG_LOCAL_PATH)),
                            cursor.getString(cursor.getColumnIndex(ReceiveDB.CHAR_IMG_IDX)),
                            cursor.getString(cursor.getColumnIndex(ReceiveDB.CHAR_SEND_MSG)),
                            cursor.getString(cursor.getColumnIndex(ReceiveDB.CHAR_TITLE_TXT)));
                }
            } else {
                String strImgPath = storeImage();
                if(strImgPath!=null && !strImgPath.equals("")) {
//                    mOpenHelper.insert(incomingNumber, mReceiveInfo.getStrIncomingEmail(), strImgPath, mReceiveInfo.getStrCharImgIdx(), mReceiveInfo.getStrCharSendMsg(), mReceiveInfo.getStrCharTitle());
                    displayDataSet(incomingNumber, mReceiveInfo.getStrIncomingEmail(), strImgPath, mReceiveInfo.getStrCharImgIdx(), mReceiveInfo.getStrCharSendMsg(), mReceiveInfo.getStrCharTitle());
                }
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(mOpenHelper!=null) mOpenHelper.close();
        }
    }

    public void displayDataSet(String incomingNum, String incmingID, String localPath, String imgIdx, String sendMsg, String title){
        if(mReceiveDisplayInfo==null) mReceiveDisplayInfo = new ReceiveCharDisplayInfo();
        mReceiveDisplayInfo.setStrIncomingNum(incomingNum);
        mReceiveDisplayInfo.setStrStrIncomingID(incmingID);
        mReceiveDisplayInfo.setStrCharImgPath(localPath);
        mReceiveDisplayInfo.setStrCharImgIdx(imgIdx);
        mReceiveDisplayInfo.setStrCharSendMsg(sendMsg);
        mReceiveDisplayInfo.setStrCharTitle(title);

        display();
    }

    public void display(){
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenStateReceiver = new ScreenStateBroadCast();
        registerReceiver(mScreenStateReceiver, screenStateFilter);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();

        int height = (int) (display.getHeight() * 0.55); //Display 사이즈의 55%

        prefs = getSharedPreferences("PopupPoint", MODE_PRIVATE);
        x = prefs.getInt("X", 0);
        y = prefs.getInt("Y", 0);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                height,
                x, y,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
//        GetImage getImg = new GetImage(this);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        rootView = layoutInflater.inflate(R.layout.dlg_send_receive, null);

        ((TextView) rootView.findViewById(R.id.txt_title)).setText(mReceiveDisplayInfo.getStrCharTitle());
        ((TextView) rootView.findViewById(R.id.txt_send_msg)).setText(mReceiveDisplayInfo.getStrCharSendMsg());
        ((ImageButton) rootView.findViewById(R.id.ib_close)).setOnClickListener(this);

        String strFilePathName = mReceiveDisplayInfo.getStrCharImgPath();
        if(!strFilePathName.equals("")){
            File imgFile = new  File(strFilePathName);
            if(imgFile.exists()) {
                Bitmap bit = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ((ImageView) rootView.findViewById(R.id.iv_ad_img)).setBackground(new BitmapDrawable(getResources(), bit));
                }else{
                    ((ImageView) rootView.findViewById(R.id.iv_ad_img)).setBackgroundDrawable(new BitmapDrawable(getResources(), bit));
                }
            }
        }

        if (rootView != null) {
            windowManager.addView(rootView, params);
//            setExtra(intent);
        }

        rootView.setOnTouchListener(mTouch);
    }

    public String storeImage() {
        String result = "";
        String filePath="";
//        filePath = Environment.getExternalStorageDirectory() + "/."+"CashcukChar";
        filePath = Environment.getExternalStorageDirectory() + "/"+"Cashcuk/ReceiveCashcuk";
        // crop된 이미지를 저장하기 위한 파일 경로
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String fileName = "/"+mReceiveInfo.getStrCharImgIdx() + ".jpg";

        File dir = new File(filePath);
        File file = new File(filePath+fileName);

        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();

            String[] strTask = new String[2];
            strTask[0] = mReceiveInfo.getStrCharImgUrl();
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

//                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
//                bm = BitmapFactory.decodeStream(bis);
//                bis.close();

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        removePopup();

        if(mScreenStateReceiver!=null) unregisterReceiver(mScreenStateReceiver);
    }

    public void removePopup() {
        this.stopSelf();

        if (rootView != null && windowManager != null){
            windowManager.removeView(rootView);
            rootView = null;
            windowManager = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(){
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_character_receive);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

//        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_INCOMING_NUM, strIncomingNum);
        k_param.put(SEND_INCOMING_EMAIL, strSenderEmail);

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
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_INCOMING_NUM, params[SEND_INCOMING_NUM]));
                if (!strSenderEmail.equals("")) {
                    listParams.add(new BasicNameValuePair(STR_INCOMING_EMAIL, params[SEND_INCOMING_EMAIL]));
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
            if (result.startsWith(StaticDataInfo.TAG_LIST)) {
                resultData(result);
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    private ReceiveInfo mReceiveInfo;
    public void resultData(String result){
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
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            mReceiveInfo = new ReceiveInfo();
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(STR_INCOMING_EMAIL)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_CHAR_IMG_IDX)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_CHAR_IMG_URL)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_SEND_MSG)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_CHAR_TITLE)) {
                                k_data_num = PARSER_NUM_4;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mReceiveInfo.setStrIncomingEmail(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mReceiveInfo.setStrCharImgIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mReceiveInfo.setStrCharImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mReceiveInfo.setStrCharSendMsg(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mReceiveInfo.setStrCharTitle(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                searchIncomingNum(strIncomingNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

                    if (rootView != null)
                        windowManager.updateViewLayout(rootView, params);
                    return true;
            }
            return false;
        }
    };

    private final int SEND_MY_PHONE = 2;
    private final int SEND_MODE = 3;
    private final int SEND_SEND_HP = 4;
    private final int SEND_EMAIL = 5;
    private final String STR_MY_PHONE = "host_hp";
    private final String STR_MODE = "call_mode";
    private final String STR_SEND_HP = "send_hp";
    private final String STR_EMAIL = "email";
    private void setCallNum(){
        TelephonyManager telManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String phoneNum = telManager.getLine1Number();

        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_regi_character_call_chk);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        String email = prefs.getString("LogIn_ID","");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        if(phoneNum.startsWith("+82")){
            phoneNum = phoneNum.replace("+82", "0");
        }
        k_param.put(SEND_MY_PHONE, phoneNum);
        k_param.put(SEND_MODE, "R");
        if(strIncomingNum.startsWith("+82")){
            strIncomingNum = phoneNum.replace("+82", "0");
        }
        k_param.put(SEND_SEND_HP, strIncomingNum);
        k_param.put(SEND_EMAIL, email);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new setNumDataTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class setNumDataTask extends AsyncTask<String, Void, String> {
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
                listParams.add(new BasicNameValuePair(STR_MY_PHONE, params[SEND_MY_PHONE]));
                listParams.add(new BasicNameValuePair(STR_MODE, params[SEND_MODE]));
                listParams.add(new BasicNameValuePair(STR_SEND_HP, params[SEND_SEND_HP]));
                listParams.add(new BasicNameValuePair(STR_EMAIL, params[SEND_EMAIL]));


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
        }
    }
}
