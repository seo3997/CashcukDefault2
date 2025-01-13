package com.cashcuk.advertiser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.common.GetImage;
import com.cashcuk.common.ImageLoader;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgFirstPhoneNumActivity;
import com.cashcuk.dialog.DlgSelImg;
import com.cashcuk.findaddr.FindAddressActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 광고주 등록
 */
public class AdvertiserRegistrationActivity extends Activity implements View.OnClickListener {
    private LinearLayout llProgress;
    private RelativeLayout rlAdvertiserImg;
    private ProgressBar pbAdvertiserImg;
    private ImageView ivAdvertiser; //광고주 이미지
    private ImageView ivAdvertiserPlus; //광고주 이미지 없을 때
    private TextView txtAdvertiser; //사진 필수 안내 text

    private EditText etTradeName; //상호명
    private EditText etRepresentativeName; //대표자
    private TextView txtRepresentativeFirstNum; //대표전화
    private EditText etRepresentativeMiddleNum; //대표전화
    private EditText etRepresentativeLasttNum; //대표전화
    private EditText etBusinessmanNum; //사업자 등록번호
    private EditText etHomepage; //홈페이지
    private TextView txtAddr; //주소
    private DlgSelImg mSelDlg; //사진찍기, 앨범 선택 popup

    private final int FIND_ADDRESS = 999;
    private final int RESULT_REQUEST_ADVERITSER = 888;
    private final int REQUEST_CODE_FIRST_NUM = 777;

    private final int SEND_TRADE_NM = 2; //상호명
    private final int SEND_REPRESENTATIVE_NM = 3; //대표자
    private final int SEND_REPRESENTATIVE_TEL = 4; //대표전화
    private final int SEND_BUSINESSMAN_REGISTERED_NUM = 5; //사업자 등록번호
    private final int SEND_HOME_PAGE= 6; //홈페이지
    private final int SEND_ADDRESS = 7; //주소
    private final int SEND_REGI_MODE = 8; //쓰기모드
    private final int SEND_REPRESENTATIVE_IDX = 9; //광고주 idx

    private final String STR_REPRESENTATIVE_IDX = "biz_idx"; //광고주 idx
    private final String STR_TRADE_NM = "biz_nm"; //상호명
    private final String STR_REPRESENTATIVE_NM = "biz_rep"; //대표자
    private final String STR_REPRESENTATIVE_TEL = "biz_tel"; //대표전화
    private final String STR_BUSINESSMAN_REGISTERED_NUM = "biz_no"; //사업자 등록번호
    private final String STR_HOME_PAGE= "biz_url"; //홈페이지
    private final String STR_ADDRESS = "biz_geo"; //주소
    private final String STR_IMG_URL = "biz_img"; //광고주 이미지 url
    private final String STR_REGI_MODE = "biz_ae"; //쓰기 모드 (A: 등록요청, E: 수정)

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;
    private final int PARSER_NUM_7 = 7;

    private final String REQUEST_STR_EDIT_ADVERTISER = "edit";
    private final String STR_EDIT_ADVERTISER = "E";
    private final String STR_EDIT_ADVERTISER_REJECT = "R";
    private final String STR_REQUEST_REGI_ADVERTISER = "A";
    private String strMode = STR_REQUEST_REGI_ADVERTISER; //쓰기모드

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(AdvertiserRegistrationActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.obj!=null && ((String)msg.obj).equals(REQUEST_STR_EDIT_ADVERTISER)){
                        displayAdvertiser();
                    }else {
                        Intent intent = new Intent(AdvertiserRegistrationActivity.this, DlgBtnActivity.class);
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_regi_request_success));
                        startActivityForResult(intent, RESULT_REQUEST_ADVERITSER);
                        StaticDataInfo.delDir(mSelDlg.getFilePath());
                    }
                    break;
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_registration_activity);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        Intent intent = getIntent();
        if(intent!=null) {
            strMode = intent.getStringExtra("PageMode");
        }

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        etTradeName = (EditText) findViewById(R.id.et_trade_nm); //상호명
        etRepresentativeName = (EditText) findViewById(R.id.et_representative_nm); //대표자
        txtRepresentativeFirstNum = (TextView) findViewById(R.id.txt_regi_first_num); //대표전화
        txtRepresentativeFirstNum.setOnClickListener(this);
        etRepresentativeMiddleNum = (EditText) findViewById(R.id.et_regi_middle_num); //대표전화
        etRepresentativeLasttNum = (EditText) findViewById(R.id.et_regi_last_num); //대표전화
        etBusinessmanNum = (EditText) findViewById(R.id.et_businessman_registered_num); //사업자 등록번호
        etHomepage = (EditText) findViewById(R.id.et_homepage); //홈페이지
        txtAddr = (TextView) findViewById(R.id.txt_address); //주소
        txtAddr.setOnClickListener(this);
        ((Button) findViewById(R.id.btn_find_postcode)).setOnClickListener(this); //우편번호 찾기

        txtAdvertiser = (TextView) findViewById(R.id.txt_essential_advertiser);
        ivAdvertiserPlus = (ImageView) findViewById(R.id.iv_advertiser_plus);
        pbAdvertiserImg = (ProgressBar) findViewById(R.id.pb_img);
        ivAdvertiserPlus.setOnClickListener(this);
        rlAdvertiserImg = (RelativeLayout) findViewById(R.id.rl_img);
        ivAdvertiser = (ImageView) findViewById(R.id.iv_advertiser);
        ivAdvertiser.setOnClickListener(this);
        rlAdvertiserImg.setOnClickListener(this);

        txtAdvertiser.setText(Html.fromHtml(getResources().getString(R.string.str_essential_img)));
        Button btnSave = (Button) findViewById(R.id.btn_advertiser_info_save);
        btnSave.setOnClickListener(this);

        mSelDlg = new DlgSelImg(this);
        GetImage getImg = new GetImage(this);

        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(getResources().getString(R.string.str_registration_advertiser));
        btnSave.setText(getResources().getString(R.string.str_registration_request));

        if(strMode != null && (strMode.equals(STR_EDIT_ADVERTISER)) || strMode.equals(STR_EDIT_ADVERTISER_REJECT)) {
                titleBar.setTitle(getResources().getString(R.string.str_set_change_advertiser));
                btnSave.setText(getResources().getString(R.string.str_set_info_change));
            if(strMode.equals(STR_EDIT_ADVERTISER)) {
                etBusinessmanNum.setEnabled(false);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fixed2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    etBusinessmanNum.setBackground(getImg.DrawableNinePatch(bitmap));
                } else {
                    etBusinessmanNum.setBackgroundDrawable(getImg.DrawableNinePatch(bitmap));
                }
            }

            strMode = STR_EDIT_ADVERTISER;
                getAdvertiserInfo();
        } else {
            strMode = STR_REQUEST_REGI_ADVERTISER;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.fl_bg));

        if(mSelDlg != null) mSelDlg.DeleteDir(mSelDlg.STR_DIR);
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


    @Override
    public void onClick(View v) {
        Intent intent;
        int viewId = v.getId();
        if (viewId == R.id.iv_advertiser_plus || viewId == R.id.iv_advertiser || viewId == R.id.rl_img) {
            if (mSelDlg != null && !mSelDlg.isShowing()) mSelDlg.show();
        } else if (viewId == R.id.btn_advertiser_info_save) {
            ChkInputItem();
        } else if (viewId == R.id.btn_find_postcode || viewId == R.id.txt_address) {
            intent = new Intent(AdvertiserRegistrationActivity.this, FindAddressActivity.class);
            startActivityForResult(intent, FIND_ADDRESS);
        } else if (viewId == R.id.txt_regi_first_num) {
            intent = new Intent(this, DlgFirstPhoneNumActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_CODE_FIRST_NUM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case DlgSelImg.PICK_FROM_CAMERA:
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mSelDlg.mImageUri, "image/*");
                    // Crop한 이미지를 저장할 Path
                    intent.putExtra("output", mSelDlg.storeCropImage(true, mSelDlg.STR_SECERT_FOLDER_NAME));

                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 16); // crop 박스의 x축 비율
                    intent.putExtra("aspectY", 9); // crop 박스의 y축 비율
                    startActivityForResult(intent, DlgSelImg.CROP_FROM_CAMERA);
                    break;
                case DlgSelImg.CROP_FROM_CAMERA:
                    File file = new File(mSelDlg.mImageUri.getPath());
                    if(file.exists()){
                        file.delete();
                    }
                case DlgSelImg.PICK_FROM_GALLERY:
                    String strFilePathName = mSelDlg.getFilePath()+mSelDlg.getFileName();
                    if(!strFilePathName.equals("")) {
                        contentImg = decodeSampledBitmapFromPath(strFilePathName, Integer.parseInt(getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(getResources().getString(R.string.str_ad_h)));
                        Bitmap bitImgSize = decodeSampledPreviewBitmapFromPath(strFilePathName, Integer.parseInt(getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(getResources().getString(R.string.str_ad_h)));
                        if (!rlAdvertiserImg.isShown()) {
                            ivAdvertiserPlus.setVisibility(View.GONE);
                            txtAdvertiser.setVisibility(View.GONE);
                            rlAdvertiserImg.setVisibility(View.VISIBLE);
                            ivAdvertiser.setVisibility(View.GONE);
                        }

                        if (data != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                rlAdvertiserImg.setBackground(new BitmapDrawable(bitImgSize));
                            } else {
                                rlAdvertiserImg.setBackgroundDrawable(new BitmapDrawable(bitImgSize));
                            }
                        }
                    }
                    break;
                case FIND_ADDRESS:
                    txtAddr.setText(data.getStringExtra("Addr"));
                    txtAddr.setSelected(true);
                    break;

                case RESULT_REQUEST_ADVERITSER:
                    finish();
                    break;

                case REQUEST_CODE_FIRST_NUM:
                    txtRepresentativeFirstNum.setText(data.getStringExtra("FirstPhoneNum"));
                    break;
            }
        }
    }

    private ContentBody contentImg=null;
    private ByteArrayBody decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeFile(path, options);
        src = src.createScaledBitmap(src, reqWidth, reqHeight, false);

        ByteArrayBody bab = null;
        if (src != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            src.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] data = bos.toByteArray();
            File oFile = new File(path);
            bab = new ByteArrayBody(data, oFile.getName());
            src.recycle();
            src = null;
        }

        return bab;
    }

    /**
     * 필수 항목 입력 체크
     */
    public void ChkInputItem(){
        if(etTradeName.getText().toString().trim().equals("")){ //상호명
            Toast.makeText(AdvertiserRegistrationActivity.this, getResources().getString(R.string.str_essential_trade_name_input_err), Toast.LENGTH_SHORT).show();
            etTradeName.requestFocus();
        }else if(etRepresentativeName.getText().toString().trim().equals("")) { //대표자
            Toast.makeText(AdvertiserRegistrationActivity.this, getResources().getString(R.string.str_essential_representative_name_input_err), Toast.LENGTH_SHORT).show();
            etRepresentativeName.requestFocus();
            etRepresentativeName.setSelection(etRepresentativeName.getText().length());
        }else if(etBusinessmanNum.getText().toString().trim().equals("")){ //사업자 등록번호
            Toast.makeText(AdvertiserRegistrationActivity.this, getResources().getString(R.string.str_essential_businessman_registered_num_input_err), Toast.LENGTH_SHORT).show();
            etBusinessmanNum.requestFocus();
            etBusinessmanNum.setSelection(etBusinessmanNum.getText().length());
        }else if(txtAddr.getText().toString().trim().equals("")){ //주소
            Toast.makeText(AdvertiserRegistrationActivity.this, getResources().getString(R.string.str_essential_address_input_err), Toast.LENGTH_SHORT).show();
        }else if(!rlAdvertiserImg.isShown()){
            Toast.makeText(AdvertiserRegistrationActivity.this, getResources().getString(R.string.str_essential_advertiser_img_input_err), Toast.LENGTH_SHORT).show();
        }else {
            setAdvertiserInfo();
        }
    }

    /**
     * 광고주 신청 정보
     */
    public void setAdvertiserInfo() {
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        final String url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_member_biz);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_TRADE_NM, etTradeName.getText().toString());
        k_param.put(SEND_REPRESENTATIVE_NM, etRepresentativeName.getText().toString());
        k_param.put(SEND_REPRESENTATIVE_TEL, txtRepresentativeFirstNum.getText().toString()+etRepresentativeMiddleNum.getText().toString()+etRepresentativeLasttNum.getText().toString());
        k_param.put(SEND_BUSINESSMAN_REGISTERED_NUM, etBusinessmanNum.getText().toString());
        k_param.put(SEND_HOME_PAGE, etHomepage.getText().toString());
        k_param.put(SEND_ADDRESS, txtAddr.getText().toString());
        k_param.put(SEND_REGI_MODE, strMode);
        if(mAdvertiserInfo!=null){
            k_param.put(SEND_REPRESENTATIVE_IDX, mAdvertiserInfo.getStrBizIdx());
        }

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
    }

    private class DataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addTextBody(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_TRADE_NM, params[SEND_TRADE_NM], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_REPRESENTATIVE_NM, params[SEND_REPRESENTATIVE_NM], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_REPRESENTATIVE_TEL, params[SEND_REPRESENTATIVE_TEL], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_BUSINESSMAN_REGISTERED_NUM, params[SEND_BUSINESSMAN_REGISTERED_NUM], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_HOME_PAGE, params[SEND_HOME_PAGE], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_ADDRESS, params[SEND_ADDRESS], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_REGI_MODE, params[SEND_REGI_MODE], ContentType.create("Multipart/related", "UTF-8"));
            if(mAdvertiserInfo!=null && !mAdvertiserInfo.getStrBizIdx().equals("")) {
                builder.addTextBody(STR_REPRESENTATIVE_IDX, params[SEND_REPRESENTATIVE_IDX], ContentType.create("Multipart/related", "UTF-8"));
            }
            if(contentImg!=null) {
                builder.addPart(STR_IMG_URL, contentImg);
            }

            // Send Request
            InputStream inputStream = null;
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

            httpParams = client.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
            HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },500);

            if(result.trim().equals("")){
                Toast.makeText(AdvertiserRegistrationActivity.this, getResources().getString(R.string.str_advertiser_regi_err), Toast.LENGTH_SHORT).show();
                return;
            }

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

    /**
     * Map 형식으로 Key와 Value를 셋팅한다.
     * @param key : 서버에서 사용할 변수명
     * @param value : 변수명에 해당하는 실제 값
     * @return
     */
    public static String setValue(String key, String value) {
        return "Content-Disposition: form-data; name=\"" + key + "\"r\n\r\n" + value;
    }

    /**
     * 광고주 정보 설정 시 정보 값 불러옴
     */
    public void getAdvertiserInfo() {
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_member_bizdata);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new GetAdvertiserInfo().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class GetAdvertiserInfo extends AsyncTask<String, Void, String> {
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
            ResultAdvertiserInfo(result);
        }
    }

    /**
     * 광고주 정보 set
     */
    private AdvertiserInfo mAdvertiserInfo;
    public void ResultAdvertiserInfo(String result){
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
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mAdvertiserInfo = new AdvertiserInfo();
                            }

                            if (parser.getName().equals(STR_TRADE_NM)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_REPRESENTATIVE_NM)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_REPRESENTATIVE_TEL)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_BUSINESSMAN_REGISTERED_NUM)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_HOME_PAGE)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_ADDRESS )) {
                                k_data_num = PARSER_NUM_5;
                            } else if (parser.getName().equals(STR_IMG_URL)) {
                                k_data_num = PARSER_NUM_6;
                            } else if (parser.getName().equals(STR_REPRESENTATIVE_IDX)) {
                                k_data_num = PARSER_NUM_7;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mAdvertiserInfo.setStrTradeName(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mAdvertiserInfo.setStrRepresentativeName(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mAdvertiserInfo.setStrRepresentativeTel(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mAdvertiserInfo.setStrBusinessmanNum(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mAdvertiserInfo.setStrHomePage(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mAdvertiserInfo.setStrAddr(parser.getText());
                                        break;
                                    case PARSER_NUM_6:
                                        mAdvertiserInfo.setStrImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_7:
                                        mAdvertiserInfo.setStrBizIdx(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                Message msg = new Message();
                msg.what = StaticDataInfo.RESULT_CODE_200;
                msg.obj = REQUEST_STR_EDIT_ADVERTISER;
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }

    /**
     * 광고주 정보 d/p
     */
    public void displayAdvertiser(){
        if(mAdvertiserInfo!=null) {
            etTradeName.setText(mAdvertiserInfo.getStrTradeName());
            etRepresentativeName.setText(mAdvertiserInfo.getStrRepresentativeName());
            String strRepressentativeTel = (PhoneNumberUtils.formatNumber(mAdvertiserInfo.getStrRepresentativeTel()));
            String[] arrRepresentativeTel = strRepressentativeTel.split("-");
            txtRepresentativeFirstNum.setText(arrRepresentativeTel[0]);
            etRepresentativeMiddleNum.setText(arrRepresentativeTel[1]);
            etRepresentativeLasttNum.setText(arrRepresentativeTel[2]);
            etBusinessmanNum.setText(mAdvertiserInfo.getStrBusinessmanNum());
            etHomepage.setText(mAdvertiserInfo.getStrHomePage());
            txtAddr.setText(mAdvertiserInfo.getStrAddr());
            if(!mAdvertiserInfo.getStrImgUrl().equals("")) {
                    if (!rlAdvertiserImg.isShown()) {
                        ivAdvertiserPlus.setVisibility(View.GONE);
                        txtAdvertiser.setVisibility(View.GONE);
                        rlAdvertiserImg.setVisibility(View.VISIBLE);
                    }


                if(pbAdvertiserImg!=null && !pbAdvertiserImg.isShown()) pbAdvertiserImg.setVisibility(View.VISIBLE);

                ImageLoader.loadImage(this, mAdvertiserInfo.getStrImgUrl(), ivAdvertiser,pbAdvertiserImg);
            }


        }
    }

    private Bitmap decodeSampledPreviewBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeFile(path, options);
        src = src.createScaledBitmap(src, reqWidth, reqHeight, false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return src;
    }
}
