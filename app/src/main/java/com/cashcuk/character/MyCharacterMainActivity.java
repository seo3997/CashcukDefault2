package com.cashcuk.character;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.character.dlg.DlgCharaterCategory;
import com.cashcuk.character.dlg.DlgCharaterMenu;
import com.cashcuk.character.dlg.DlgCharaterSet;
import com.cashcuk.common.CommCode;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgSelImg;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;

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
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 나의 캐릭터 메인
 */
public class MyCharacterMainActivity extends Activity implements View.OnClickListener {
    private boolean setRepresentImg = false; //대표이미지 설정 여부 false: 미설정, true: 설정
    private ImageView ivRepresent; //대표 이미지
    private LinearLayout llChar;
    private LinearLayout llCharEmpty;
    private ImageView ivChar1;
    private ImageView ivChar2;
    private TextView txtRepresentationMsg; //전하는 글

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;

    private final String STR_CHAR_IDX = "char_idx"; //item idx
    private final String STR_CHAR_REP = "char_rep"; //대표이미지 여부(1: 대표이미지, 0: 대표이미지 아님)
    private final String STR_CHAR_IMG_URL = "char_imgurl"; //이미지 url
    private final String STR_CHAR_TXT = "char_txt"; //전하는 글

    private DlgSelImg mSelDlg; //사진찍기, 앨범 선택 popup

    private final int REQUEST_OK = 999;
    private final int REQUEST_SEND_MSG = 777;

    private final String STR_CHARACTER_IMG = "char_img";
    private final String STR_MIN_CHARACTER_IMG = "char_minimg";
    private final String STR_MIDDLE_CHARACTER_IMG = "char_middleimg";

    private final String STR_REP_MIDDLE_CHARACTER_IMG = "char_middle";
    
    private final int SEND_CATEGORY_IDX = 2;
    private final int SEND_PAGE_NUM = 3;
    private final String STR_CHARACTER_IDX = "cat_code"; //카테고리 idx, 메인은 '0' 전달
    private final String STR_PAGE_NUM = "pageno";

    private final String STR_MAIN_IDX = "0";

    private ArrayList<TxtListDataInfo> arrCharCategory;
    private LinearLayout llProgress;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(MyCharacterMainActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();

                    if (!isCategory) {
                        requestMainData(true);
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.obj!=null){
                        arrCharCategory = (ArrayList<TxtListDataInfo>)msg.obj;
                    }

                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(!isCategory){
                        requestMainData(true);
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
        setContentView(R.layout.activity_my_character_main);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(getResources().getString(R.string.str_my_character));
        ImageButton ibMenu = (ImageButton) titleBar.findViewById(R.id.ib_menu);
        ibMenu.setVisibility(View.VISIBLE);
        ibMenu.setOnClickListener(this);

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        ivRepresent = (ImageView) findViewById(R.id.iv_represent);
        ivRepresent.setOnClickListener(this);
        llChar = (LinearLayout) findViewById(R.id.ll_character);
        llCharEmpty = (LinearLayout) findViewById(R.id.ll_character_empty);
        ivChar1 = (ImageView) findViewById(R.id.character1);
        ivChar2 = (ImageView) findViewById(R.id.character2);
        ((Button) findViewById(R.id.btn_other_character)).setOnClickListener(this); //다른 카테고리
        ((Button) findViewById(R.id.btn_regi_character)).setOnClickListener(this); //캐릭터 등록

        txtRepresentationMsg = (TextView) findViewById(R.id.txt_representation_characte_msg);

        new CommCode(MyCharacterMainActivity.this, StaticDataInfo.COMMON_CODE_TYPE_CH, PARSER_NUM_1, "", handler);

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initItem();
            }
        });

        mSelDlg = new DlgSelImg(this, false, true);
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

    /**
     * 대표캐릭터 및 나의캐릭터함 미리보기(이미지2개) 초기화
     */
    public void initItem(){
        llCharEmpty.setVisibility(View.VISIBLE);
        llChar.setVisibility(View.GONE);
        ivChar1.setVisibility(View.GONE);
        ivChar2.setVisibility(View.GONE);

        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.user);
        ivRepresent.setImageBitmap(drawable.getBitmap());
        txtRepresentationMsg.setText("");

        requestMainData(false);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.ib_menu) {
            intent = new Intent(MyCharacterMainActivity.this, DlgCharaterMenu.class);
            intent.putExtra("Mode", getResources().getString(R.string.str_char_main));
        } else if (viewId == R.id.btn_other_character) {
            //다른 카테고리
            if (arrCharCategory != null && arrCharCategory.size() > 0) {
                intent = new Intent(MyCharacterMainActivity.this, DlgCharaterCategory.class);
                intent.putExtra(StaticDataInfo.STR_CHARATER_DOWN_OTHER, StaticDataInfo.STR_CHARATER_OTHER);
                intent.putExtra("Category", arrCharCategory);
            } else {
                new CommCode(MyCharacterMainActivity.this, StaticDataInfo.COMMON_CODE_TYPE_CH, PARSER_NUM_1, "", handler);
            }
        } else if (viewId == R.id.btn_regi_character) {
            //캐릭터 등록
            if (mSelDlg != null && !mSelDlg.isShowing()) mSelDlg.show();
        } else if (viewId == R.id.iv_represent) {
            if (setRepresentImg) {
                intent = new Intent(MyCharacterMainActivity.this, DlgCharaterSet.class);
                intent.putExtra("CharInfo", (Serializable) mSendInfo);
                intent.putExtra("SendMsgMode", STR_REGI_SEND_MSG_MODIFY);
            }
        }
        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private boolean isNewSendMsg = false;
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
                    intent.putExtra("aspectX", 4); // crop 박스의 x축 비율
                    intent.putExtra("aspectY", 3); // crop 박스의 y축 비율
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, DlgSelImg.CROP_FROM_CAMERA);
                    break;
                case DlgSelImg.CROP_FROM_CAMERA:
                    File file = new File(mSelDlg.mImageUri.getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                case DlgSelImg.PICK_FROM_GALLERY:
                    isNewSendMsg = true;
                    resizeImg(data, mSelDlg.getFilePath(), mSelDlg.getFileName());
                    break;
                case REQUEST_OK:
                    if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

                    llCharEmpty.setVisibility(View.VISIBLE);
                    llChar.setVisibility(View.GONE);
                    ivChar1.setVisibility(View.GONE);
                    ivChar2.setVisibility(View.GONE);
                    requestMainData(false);
                    break;
                case REQUEST_SEND_MSG:
                    if(strSendMsgMode.equals(STR_REGI_SEND_MSG_NEW)){
                        openDialg();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!isNewSendMsg) {
            initItem();
        }
    }

    /**
     * 주의 팝업 발생
     */
    public void openDialg(){
        Intent intent = new Intent(MyCharacterMainActivity.this, DlgBtnActivity.class);
        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_character_use_warning));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_OK);
    }

    private class regiCharacterDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_regi_character_page);
            SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
            final String token = pref.getString(getResources().getString(R.string.str_token), "");

            String retMsg = "";
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addTextBody(getResources().getString(R.string.str_token), token, ContentType.create("Multipart/related", "UTF-8"));
            builder.addPart(STR_CHARACTER_IMG, contentOrigin);
            builder.addPart(STR_MIN_CHARACTER_IMG, contentThumbNail);
            builder.addPart(STR_MIDDLE_CHARACTER_IMG, contentMiddleThumbNail);

            // Send Request
            InputStream inputStream = null;
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost post = new HttpPost(url);

            httpParams = client.getParams();
//            HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
//            HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (llProgress != null && llProgress.isShown())
                        llProgress.setVisibility(View.GONE);
                }
            }, 500);

            if(result.trim().equals("")){
                Toast.makeText(MyCharacterMainActivity.this, getResources().getString(R.string.str_char_regi_err), Toast.LENGTH_SHORT).show();
                return;
            }

            if(result.startsWith(StaticDataInfo.TAG_LIST)) {
                regiSetSendMsg(result);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    /*
      전달하는 글 신규등록인지 수정인지
      신규 등록은 캐릭터 등록 시 바로 등록.
      수정은 캐릭터 선택 시 등록
     */
    private final String STR_REGI_SEND_MSG_NEW = "New";
    private final String STR_REGI_SEND_MSG_MODIFY = "Modify";
    private String strSendMsgMode="";
    /**
     * 캐릭터 등록 후 전달하는 글 바로 등록함.
     */
    private void regiSetSendMsg(String result) {
        if (result.startsWith(StaticDataInfo.TAG_LIST)) {
            String strCharIdx = "";
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
                            if (parser.getName().equals(STR_CHAR_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        strCharIdx = parser.getText();
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                strSendMsgMode = STR_REGI_SEND_MSG_NEW;
                Intent intent = new Intent(MyCharacterMainActivity.this, DlgCharaterSet.class);
                intent.putExtra("CharIdx", strCharIdx);
                intent.putExtra("SendMsgMode", strSendMsgMode);
                startActivityForResult(intent, REQUEST_SEND_MSG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isCategory = false;
    public void requestMainData(boolean category){
        isCategory = category;
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        String url="";
        if(category){
            url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_character_page);
        }else{
            url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_regi_character_main);
        }

        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        if(category){
            k_param.put(SEND_CATEGORY_IDX, "N");
            k_param.put(SEND_PAGE_NUM, "1");
        }

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new MainData().execute(strTask);
    }
    
    private class MainData extends AsyncTask<String, Void, String> {
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
                if(isCategory) {
                    listParams.add(new BasicNameValuePair(STR_CHARACTER_IDX, params[SEND_CATEGORY_IDX]));
                    listParams.add(new BasicNameValuePair(STR_PAGE_NUM, params[SEND_PAGE_NUM]));
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
                resultMain(result);
            } else if (result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_NO_DATA);
            } else {
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }

            if(isNewSendMsg) isNewSendMsg=false;
        }
    }

    public void resultMain(String result){
        CharacterInfo mCharInfo = null;
        ArrayList<CharacterInfo> arrChar = null;
        int cnt = 0;

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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && mCharInfo != null && mCharInfo != null) {
                                arrChar.add(mCharInfo);
                            }

                            if(isCategory && cnt<2){
                                cnt+=1;
                            }else if(isCategory && cnt>=2){
                                break;
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            arrChar = new ArrayList<CharacterInfo>();
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mCharInfo = new CharacterInfo();
                            }

                            if (parser.getName().equals(STR_CHAR_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_CHAR_REP)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_CHAR_IMG_URL)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_CHAR_TXT)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_REP_MIDDLE_CHARACTER_IMG)) {
                                k_data_num = PARSER_NUM_4;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mCharInfo.setStrIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mCharInfo.setStrRep(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mCharInfo.setStrImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        mCharInfo.setStrTxt(parser.getText());
                                        break;
                                    case PARSER_NUM_4:
                                        mCharInfo.setStrMiddleImgUrl(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                if(!isCategory) {
                    displayMain(mCharInfo);
                    requestMainData(true);
                }else{
                    displayCategory(arrChar);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private CharacterInfo mSendInfo;
    public void displayMain(CharacterInfo mCharData){
        txtRepresentationMsg.setText(mCharData.getStrTxt());

        String strImgPath = mCharData.getStrMiddleImgUrl().replace("\\", "//");
        Glide
                .with(this)
                .load(strImgPath)
                .centerCrop()
                .placeholder(R.drawable.image_none)
                .centerCrop()
                .skipMemoryCache(true)
                .into(ivRepresent);

        mSendInfo = mCharData;
        setRepresentImg = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        },500);
    }

    public void displayCategory(ArrayList<CharacterInfo> arrChar){
        for(int i=0; i<arrChar.size(); i++) {
            if(i<2) {
                String strImgPath = arrChar.get(i).getStrMiddleImgUrl().replace("\\", "//");
                if (!ivChar1.isShown()) {
                    llCharEmpty.setVisibility(View.GONE);
                    llChar.setVisibility(View.VISIBLE);
                    ivChar1.setVisibility(View.VISIBLE);
                    ivChar2.setVisibility(View.INVISIBLE);
                    Glide
                            .with(this)
                            .load(strImgPath)
                            .centerCrop()
                            .placeholder(R.drawable.image_none)
                            .into(ivChar1);
                } else {
                    ivChar2.setVisibility(View.VISIBLE);
                    Glide
                            .with(this)
                            .load(strImgPath)
                            .centerCrop()
                            .placeholder(R.drawable.image_none)
                            .into(ivChar2);
                }
            }
        }
    }

    private ContentBody contentOrigin;
    private ContentBody contentMiddleThumbNail;
    private ContentBody contentThumbNail;
    private void resizeImg(Intent data, String dir, String name){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        String strImgDir = dir+name;
        ByteArrayBody retVal = decodeSampledBitmapFromPath(strImgDir, Integer.valueOf(getResources().getString(R.string.str_ad_char_w)), Integer.valueOf(getResources().getString(R.string.str_char_h)));
        if(retVal!=null) {
            contentOrigin = retVal;
        }

        contentThumbNail = decodeSampledBitmapFromPath(strImgDir, Integer.valueOf(getResources().getString(R.string.str_char_thumbnail_w)), Integer.valueOf(getResources().getString(R.string.str_char_thumbnail_h)));
        contentMiddleThumbNail = decodeSampledBitmapFromPath(strImgDir, Integer.valueOf(getResources().getString(R.string.str_char_thumbnail_middle_w)), Integer.valueOf(getResources().getString(R.string.str_char_thumbnail_middle_h)));

        if (data != null) {
            new regiCharacterDataTask().execute();
        }

    }

    private ByteArrayBody decodeSampledBitmapFromPath(String path, int reqWidth,int reqHeight) {
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

}
