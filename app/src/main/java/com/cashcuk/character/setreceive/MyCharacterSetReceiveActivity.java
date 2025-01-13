package com.cashcuk.character.setreceive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.charactercall.send.SendDB;
import com.cashcuk.ad.charactercall.send.SendDBopenHelper;
import com.cashcuk.character.CharacterInfo;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 나의 캐릭터 - 수신 대상 설정
 */
public class MyCharacterSetReceiveActivity extends Activity implements View.OnClickListener {
    private ListView lvSetReceive; //수신 대상 list
    private LinearLayout llNumEmpty;

    private final String STR_TARGET_HP = "tar_hp";
    private final int SEND_TARGET_HP = 2;

    private final int SEND_TARGET_IMG_IDX = 3;
    private final String STR_TARGET_IMG_IDX = "char_idx";

    private String[] arrChkContact;

    private final String STR_CHK_EMAIL = "tar_id";
    private final String STR_CHK_HP = "chk_hp";
    private final String STR_CHK_YN = "tar_chk";
    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;

    private Button btnSetReceiveOk;

    private ArrayList<ContactsInfo> arrContactTemp;

    private final String STR_CHK_CONTACTS = "contacts";
    private final String STR_SET_TARGET = "taget";
    private String strMode = STR_CHK_CONTACTS;

    //    private String strCharIdx;
    private CharacterInfo mCharInfo;

    private int mPageNo = 1;
    private ContactsAdapter adapter = null;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(MyCharacterSetReceiveActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    llNumEmpty.setVisibility(View.GONE);
                    lvSetReceive.setVisibility(View.VISIBLE);
                    btnSetReceiveOk.setVisibility(View.VISIBLE);

                    setData(arrContactsDisplay);
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(mPageNo==1){
                        llNumEmpty.setVisibility(View.VISIBLE);
                        lvSetReceive.setVisibility(View.GONE);
                        btnSetReceiveOk.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_character_set_receive);
        CheckLoginService.mActivityList.add(this);

        RelativeLayout layoutBG = (RelativeLayout) findViewById(R.id.rl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_set_receive));
        lvSetReceive = (ListView) findViewById(R.id.lv_set_receive);
        llNumEmpty = (LinearLayout) findViewById(R.id.ll_num_empty);

        btnSetReceiveOk = (Button) findViewById(R.id.btn_set_receive_ok);
        btnSetReceiveOk.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent != null) {
            mCharInfo = (CharacterInfo) intent.getSerializableExtra("CharTarget");
        }

        getContactList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.rl_bg));
    }

    private void recycleView(View view) {
        if(view != null) {
            Drawable bg = view.getBackground();
            if(bg != null) {
                bg.setCallback(null);
                ((BitmapDrawable)bg).getBitmap().recycle();
                view.setBackgroundDrawable(null);
            }
        }
    }

    /**
     * 연락처를 가져오는 메소드.
     *
     * @return
     */
    private void getContactList() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

        String[] selectionArgs = null;

        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor contactCursor = managedQuery(uri, projection, null, selectionArgs, sortOrder);

        arrContactTemp = new ArrayList<ContactsInfo>();
        if (contactCursor.moveToFirst()) {
            do {
//                String phoneUtil = PhoneNumberUtils.formatNumber(contactCursor.getString(1)); //폰 번호 format 변경 (010-0000-0000)

                ContactsInfo mContactsInfo = new ContactsInfo();
                mContactsInfo.setStrPhoneNum(contactCursor.getString(1).replace("-", ""));
//                mContactsInfo.setStrPhoneNum(phoneUtil);
                mContactsInfo.setStrName(contactCursor.getString(2));
                mContactsInfo.setIsChk(false);

                arrContactTemp.add(mContactsInfo);
            } while (contactCursor.moveToNext());
        }

        if (arrContactTemp != null) {
            if (arrChkContact == null) {
                arrChkContact = new String[arrContactTemp.size()];

                for (int i = 0; i < arrContactTemp.size(); i++) {
                    arrChkContact[i] = arrContactTemp.get(i).getStrPhoneNum();
                }
            }
        }

        DataRequest(STR_CHK_CONTACTS);
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(String mode) {
        strMode = mode;

        String url = "";
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if (mode.equals(STR_CHK_CONTACTS)) {
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_character_chk);
        } else if (mode.equals(STR_SET_TARGET)) {
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_character_tar);
        }

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        if (arrChkContact != null && arrChkContact.length > 0) {
            if(arrChkContact[0].equals("T")){
                k_param.put(SEND_TARGET_HP, "T");
            }else {
                k_param.put(SEND_TARGET_HP, Arrays.toString(arrChkContact));
            }
        }else{
            handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            return;
        }
        k_param.put(SEND_TARGET_IMG_IDX, mCharInfo.getStrIdx());

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
    }

    private ArrayList<String> arrSetTargetTemp;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_set_receive_ok) {
            if (arrContactsDisplay != null && arrContactsDisplay.size() > 0) {
                arrSetTargetTemp = new ArrayList<String>();
                for (int i = 0; i < arrContactsDisplay.size(); i++) {
                    if (arrContactsDisplay.get(i).getIsChk()) {
                        arrSetTargetTemp.add(arrContactsDisplay.get(i).getStrPhoneNum());
                    }
                }

                if (arrSetTargetTemp.size() > 0) {
                    arrChkContact = arrSetTargetTemp.toArray(new String[arrSetTargetTemp.size()]);
                    DataRequest(STR_SET_TARGET);
                }else{
                    Toast.makeText(MyCharacterSetReceiveActivity.this, getResources().getString(R.string.str_set_target_chk_empty), Toast.LENGTH_SHORT).show();
                    arrChkContact = new String[1];
                    arrChkContact[0] = "T";
                    DataRequest(STR_SET_TARGET);

                    SendDBopenHelper mOpenHelper = new SendDBopenHelper(this);
                    String strDataIdKind = "";

                    try {
                        mOpenHelper.open();
                        Cursor cursorKind = mOpenHelper.Search(SendDB.CHAR_KIND + " = '" + getResources().getString(R.string.str_rep_img) + "'");

                        TelephonyManager telManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                        String phoneNum = telManager.getLine1Number();
                        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
                        String strMyEmail = prefs.getString("LogIn_ID", "");

                        if(cursorKind.moveToFirst()){
                            mCharInfo = new CharacterInfo();
                            strDataIdKind = cursorKind.getString(cursorKind.getColumnIndex(SendDB.CreateDB._ID));

                            mCharInfo.setStrImgUrl( cursorKind.getString(cursorKind.getColumnIndex(SendDB.CHAR_IMG_PATH)));
                            mCharInfo.setStrIdx( cursorKind.getString(cursorKind.getColumnIndex(SendDB.CHAR_IMG_IDX)));
                            mCharInfo.setStrTxt( cursorKind.getString(cursorKind.getColumnIndex(SendDB.CHAR_SEND_MSG)));
                        }

                        mOpenHelper.deleteAll();
                        if(!strDataIdKind.equals("")) {
                            mOpenHelper.insert(getResources().getString(R.string.str_rep_img), strMyEmail, phoneNum, mCharInfo.getStrImgUrl(), mCharInfo.getStrIdx(), mCharInfo.getStrTxt(), "얀녕하세요. 캐시쿡입니다.");
                        }

                        cursorKind.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }finally {
                        if(mOpenHelper!=null) mOpenHelper.close();
                    }

                    finish();
                }
            }
        }
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
                listParams.add(new BasicNameValuePair(STR_TARGET_HP, params[SEND_TARGET_HP]));
                listParams.add(new BasicNameValuePair(STR_TARGET_IMG_IDX, params[SEND_TARGET_IMG_IDX]));

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
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
            } else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }


    private ChkSetTargetInfo mDataInfo;
    private ArrayList<ChkSetTargetInfo> arrDataInfo;
    public void resultData(String result) {
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrDataInfo != null && mDataInfo != null) {
                                arrDataInfo.add(mDataInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrDataInfo = new ArrayList<ChkSetTargetInfo>();
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mDataInfo = new ChkSetTargetInfo();
                            }

                            if (parser.getName().equals(STR_CHK_HP)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_CHK_EMAIL)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_CHK_YN)) {
                                k_data_num = PARSER_NUM_2;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mDataInfo.setStrHp(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mDataInfo.setStrEmail(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mDataInfo.setStrChkYN(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                displayHP(arrDataInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<ContactsInfo> arrContactsDisplay;

    public void displayHP(ArrayList<ChkSetTargetInfo> arrData) {
        arrContactsDisplay = new ArrayList<ContactsInfo>();

        for (int i = 0; i < arrData.size(); i++) {
            for (int j = 0; j < arrContactTemp.size(); j++) {
                if (arrData.get(i).getStrHp().equals(arrContactTemp.get(j).getStrPhoneNum())) {
                    if(arrData.get(i).getStrChkYN().equals(String.valueOf(StaticDataInfo.TRUE))) {
                        arrContactTemp.get(j).setIsChk(true);
                    }
                    arrContactTemp.get(j).setStrEmail(arrData.get(i).getStrEmail());
                    arrContactsDisplay.add(arrContactTemp.get(j));
                    arrContactTemp.remove(j);
                    continue;
                }
            }
        }

        lvSetReceive.setVisibility(View.VISIBLE);
        llNumEmpty.setVisibility(View.GONE);
        btnSetReceiveOk.setVisibility(View.VISIBLE);
        if(mPageNo==1) {
            adapter = new ContactsAdapter(MyCharacterSetReceiveActivity.this, R.layout.character_set_receive_item, arrContactsDisplay);
            lvSetReceive.setAdapter(adapter);

            lvSetReceive.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter != null) {
                        adapter.itemChk(position);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }else{
            if(adapter!=null) adapter.notifyDataSetChanged();
        }

        mPageNo++;
    }


    public void setData(ArrayList<ContactsInfo> chkData) {
        SendDBopenHelper mOpenHelper = new SendDBopenHelper(this);
        ArrayList<ContactsInfo> tmp = new ArrayList<ContactsInfo>();
        for (int i = 0; i < chkData.size(); i++) {
            if(chkData.get(i).getIsChk()) {
                tmp.add(chkData.get(i));
            }
        }

        ArrayList<String> strHP = new ArrayList<String>();
        for(int j=0; j<tmp.size(); j++) {
            strHP.add(tmp.get(j).getStrPhoneNum());
        }

        try {
            mOpenHelper.open();
            mOpenHelper.deleteAll();

            for (int i = 0; i < tmp.size(); i++) {
                String strRep = "";
                if (mCharInfo.getStrRep().equals(String.valueOf(StaticDataInfo.TRUE))) {
                    strRep = getResources().getString(R.string.str_rep_img);
                }
                mOpenHelper.insert(strRep, tmp.get(i).getStrEmail(), tmp.get(i).getStrPhoneNum(), mCharInfo.getStrImgUrl(), mCharInfo.getStrIdx(), mCharInfo.getStrTxt(), "안녕하세요. 캐시쿡 입니다.");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (mOpenHelper != null) mOpenHelper.close();
        }
        Toast.makeText(MyCharacterSetReceiveActivity.this, getResources().getString(R.string.str_set_target_success), Toast.LENGTH_SHORT).show();
        finish();
    }

    public String storeImage() {
        String result = "";
        String filePath = "";
        filePath = Environment.getExternalStorageDirectory() + "/" + "SendCashcuk";
        // crop된 이미지를 저장하기 위한 파일 경로
        String fileName = "/" + mCharInfo.getStrIdx() + ".jpg";

        File dir = new File(filePath);
        File file = new File(filePath + fileName);

        try {
            if (!dir.exists()) {
                dir.mkdir();
            }

            file.createNewFile();

            String[] strTask = new String[2];
            strTask[0] = mCharInfo.getStrImgUrl();
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
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                //입력 스트림을 구한다
                InputStream is = conn.getInputStream();

                //파일 저장 스트림 생성
                FileOutputStream fos = new FileOutputStream(params[1]);
                int read;
                //입력 스트림을 파일로 저장
                for (; ; ) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            return retMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

}
