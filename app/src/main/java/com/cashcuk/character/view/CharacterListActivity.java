package com.cashcuk.character.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.adlist.SwipeDismissListViewTouchListener;
import com.cashcuk.ad.charactercall.send.SendDB;
import com.cashcuk.ad.charactercall.send.SendDBopenHelper;
import com.cashcuk.character.CharacterInfo;
import com.cashcuk.character.dlg.DlgCharaterCategory;
import com.cashcuk.character.dlg.DlgCharaterMenu;
import com.cashcuk.character.dlg.DlgCharaterSet;
import com.cashcuk.character.setreceive.MyCharacterSetReceiveActivity;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgListAdapter;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 캐릭터 list
 */
public class CharacterListActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private int mPageNo = 1; //요청 페이지

    private final int SEND_CATEGORY_IDX = 2;
    private final int SEND_CHARACTER_IDX = 2;
    private final int SEND_PAGE = 3;

    private final String STR_CATEGORY_IDX = "cat_code";
    private final String STR_PAGE_NO = "pageno";

    private final String STR_CHARACTER_IMG = "char_img";
    private final String STR_MIN_CHARACTER_IMG = "char_minimg";
    private final String STR_SEND_MIDDLE_CHARACTER_IMG = "char_middleimg";

    private CharacterInfo mCharInfo;
    private ArrayList<CharacterInfo> arrChar;

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
    private final String STR_GET_MIDDLE_CHARACTER_IMG = "char_middle"; //중간 size 이미지


    private GridView gvChar;
    private CharacterGridAdapter mGVAdapter;
    private LinearLayout llCharEmpty;

    private final String STR_REQUEST_MODE_REGI_CHAR = "A"; //카테고리 등록
    private final String STR_REQUEST_MODE_CATEGORY = "C"; //카테고리 별
    private final String STR_REQUEST_MODE_SET_REP = "R"; //대표이미지
    private final String STR_REQUEST_MODE_DEL = "D"; //캐릭터삭제
    private String strMode=STR_REQUEST_MODE_CATEGORY;

    private LinearLayout llProgress;

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.
    private boolean isListUpdate = false;

    private String strRequestIdx = "";

    private final int REQUEST_CHAR_DEL = 999;
    private final int REQUEST_CHAR_DEL_CHK = 888;
    private LinearLayout llCharBtn;
    private LinearLayout llTitleNormal;
    private RelativeLayout rlTitleDel;
    private TextView txtInfo;
    private ImageButton ibDelCancel;
    private ImageButton ibDel;

    private final String STR_CHARACTER_DEL_IDXES = "char_idxes"; //이미지 idx들
    private final int SEND_CHARACTER_DEL_IDXES = 2;
    private final int SEND_DEL_CATEGORY_IDX = 3;

    private DlgSelImg mSelDlg; //사진찍기, 앨범 선택 popup
    private final int REQUEST_OK = 555;

    private final int REQUEST_SEND_MSG = 777;
    private int mClickCharPos;

    private ArrayList<CharacterInfo> arrDelCharTemp; //삭제 할 char외 char 정보

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(CharacterListActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(isListUpdate){
                        isListUpdate=false;
                    }else if(strMode.equals(STR_REQUEST_MODE_CATEGORY) && mPageNo==1){
                        llCharEmpty.setVisibility(View.VISIBLE);
                        gvChar.setVisibility(View.GONE);
                    }
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(strMode.equals(STR_REQUEST_MODE_SET_REP)){
                        Toast.makeText(CharacterListActivity.this, getResources().getString(R.string.str_set_representation_characte_success), Toast.LENGTH_SHORT).show();
                        saveDBRepImg();
                    } else if(strMode.equals(STR_REQUEST_MODE_DEL)){
                        Toast.makeText(CharacterListActivity.this, getResources().getString(R.string.str_char_del_success), Toast.LENGTH_SHORT).show();

                        arrChar.clear();
                        arrChar.addAll(arrDelCharTemp);
                        mGVAdapter.notifyDataSetChanged();

                        if(arrChar.size()<=0){
                            llCharEmpty.setVisibility(View.VISIBLE);
                        }else{
                            llCharEmpty.setVisibility(View.GONE);
                        }
                    }
                    break;
            }

            mLockListView = false;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },300);
        }
    };

    private ArrayList<TxtListDataInfo> arrCharCategory;
    private int mSelPoint = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_list);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);
        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(getResources().getString(R.string.str_my_character));

        Intent getIntent = getIntent();
        if(getIntent!=null){
            arrCharCategory = (ArrayList<TxtListDataInfo>) getIntent.getSerializableExtra("Category");
            mSelPoint = getIntent.getIntExtra("CategoryIndex", -1);
        }

        if(mSelPoint!=-1 && arrCharCategory!=null && arrCharCategory.size()>mSelPoint) {
            if(mSelPoint!=-1) {
                ((TextView) findViewById(R.id.txt_representation_characte)).setText(arrCharCategory.get(mSelPoint).getStrMsg());
                strRequestIdx = arrCharCategory.get(mSelPoint).getStrIdx();

                if(strRequestIdx.startsWith(StaticDataInfo.STRING_N)){
                    ((Button) findViewById(R.id.btn_regi_character)).setVisibility(View.VISIBLE);
                    ((Button) findViewById(R.id.btn_regi_character)).setOnClickListener(this);
                    mSelDlg = new DlgSelImg(this, false, true);
                }else{
                    ((Button) findViewById(R.id.btn_regi_character)).setVisibility(View.GONE);
                }
            }
        }

        MainTitleBar mainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        final ImageButton ibRefresh = (ImageButton) mainTitleBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageNo = 1;
                sendDataSet(STR_REQUEST_MODE_CATEGORY, arrCharCategory.get(mSelPoint).getStrIdx());
                gvChar.setSelection(0);
            }
        });

        llCharBtn = (LinearLayout) findViewById(R.id.ll_character_btn);
        llTitleNormal = (LinearLayout) findViewById(R.id.ll_category_title);
        rlTitleDel = (RelativeLayout) findViewById(R.id.rl_category_title_del);
        txtInfo = (TextView) findViewById(R.id.txt_info);
        ibDelCancel = (ImageButton) findViewById(R.id.ib_del_cancel);
        ibDel = (ImageButton) findViewById(R.id.ib_char_del);
        ibDelCancel.setOnClickListener(this);
        ibDel.setOnClickListener(this);

        ImageButton ibMenu = (ImageButton) titleBar.findViewById(R.id.ib_menu);
        ibMenu.setVisibility(View.VISIBLE);
        ibMenu.setOnClickListener(this);

        gvChar = (GridView) findViewById(R.id.gv_image);
        llCharEmpty = (LinearLayout) findViewById(R.id.ll_character_empty);

        ((Button) findViewById(R.id.btn_other_character)).setOnClickListener(this); //다른 카테고리
    }

    /**
     * 발신 때 사용할 대표 캐릭터 내부 db에 저장
     */
    private void saveDBRepImg(){
        SendDBopenHelper mOpenHelper = new SendDBopenHelper(this);
        String strDataIdKind = "";
        String strDataIdIdx = "";
        try {
            mOpenHelper.open();

            TelephonyManager telManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            String phoneNum = telManager.getLine1Number();
            SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
            String strMyEmail = prefs.getString("LogIn_ID", "");

            Cursor cursorKind = mOpenHelper.Search(SendDB.CHAR_KIND + " = '" + getResources().getString(R.string.str_rep_img) + "'");
            Cursor cursorIdx = mOpenHelper.Search(SendDB.CHAR_IMG_IDX + " = '"+arrChar.get(mRepPos).getStrIdx()+"'");

            if(cursorKind.moveToFirst()){
                strDataIdKind = cursorKind.getString(cursorKind.getColumnIndex(SendDB.CreateDB._ID));
                mOpenHelper.update(strDataIdKind, "", cursorKind.getString(cursorKind.getColumnIndex(SendDB.CHAR_IMG_PATH)), cursorKind.getString(cursorKind.getColumnIndex(SendDB.CHAR_IMG_IDX)), cursorKind.getString(cursorKind.getColumnIndex(SendDB.CHAR_SEND_MSG)), cursorKind.getString(cursorKind.getColumnIndex(SendDB.CHAR_TITLE_TXT)));

                if(cursorIdx.moveToFirst()){
                    strDataIdIdx = cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CreateDB._ID));
                    mOpenHelper.update(strDataIdIdx, getResources().getString(R.string.str_rep_img), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_IMG_PATH)), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_IMG_IDX)),cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_SEND_MSG)), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_TITLE_TXT)));
                }else{
                    mOpenHelper.insert(getResources().getString(R.string.str_rep_img), strMyEmail, phoneNum, arrChar.get(mRepPos).getStrImgUrl(), arrChar.get(mRepPos).getStrIdx(), arrChar.get(mRepPos).getStrTxt(), "얀녕하세요. 캐시쿡입니다.");
                }
            }else{
                if(cursorIdx.moveToFirst()){
                    strDataIdIdx = cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CreateDB._ID));
                    mOpenHelper.update(strDataIdIdx, getResources().getString(R.string.str_rep_img), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_IMG_PATH)), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_IMG_IDX)),cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_SEND_MSG)), cursorIdx.getString(cursorIdx.getColumnIndex(SendDB.CHAR_TITLE_TXT)));
                }else {
                    mOpenHelper.insert(getResources().getString(R.string.str_rep_img), strMyEmail, phoneNum, arrChar.get(mRepPos).getStrImgUrl(), arrChar.get(mRepPos).getStrIdx(), arrChar.get(mRepPos).getStrTxt(), "얀녕하세요. 캐시쿡입니다.");
                }
            }

            cursorKind.close();
            cursorIdx.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(mOpenHelper!=null) mOpenHelper.close();
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
    protected void onResume() {
        super.onResume();
        if(strMode.equals(STR_REQUEST_MODE_CATEGORY)) {
            mPageNo=1;
            sendDataSet(STR_REQUEST_MODE_CATEGORY, strRequestIdx);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.ib_menu) {
            intent = new Intent(CharacterListActivity.this, DlgCharaterMenu.class);
            if (mGVAdapter != null && mGVAdapter.getCount() > 0) {
                intent.putExtra("Mode", getResources().getString(R.string.str_char_category));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_CHAR_DEL);
            return;
        } else if (viewId == R.id.btn_other_character) {
            //다른 카테고리
            intent = new Intent(CharacterListActivity.this, DlgCharaterCategory.class);
            intent.putExtra(StaticDataInfo.STR_CHARATER_DOWN_OTHER, StaticDataInfo.STR_CHARATER_OTHER);
            intent.putExtra("Category", arrCharCategory);
        } else if (viewId == R.id.ib_del_cancel) {
            isDel = false;
            mGVAdapter.modeState(isDel);
            mGVAdapter.notifyDataSetChanged();

            llCharBtn.setVisibility(View.VISIBLE);
            llTitleNormal.setVisibility(View.VISIBLE);
            rlTitleDel.setVisibility(View.GONE);
            txtInfo.setVisibility(View.VISIBLE);
        } else if (viewId == R.id.ib_char_del) {
            intent = new Intent(CharacterListActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_char_del_chk));
            intent.putExtra("DlgMode", "Two");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_CHAR_DEL_CHK);
            return;
        } else if (viewId == R.id.btn_regi_character) {
            strMode = "";
            if (mSelDlg != null && !mSelDlg.isShowing()) mSelDlg.show();
        }
        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private String strNewSendMsg = "";
    private boolean isDel = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_CHAR_DEL:
                    if(mGVAdapter!=null) {
                        isDel = true;
                        llCharBtn.setVisibility(View.GONE);
                        llTitleNormal.setVisibility(View.GONE);
                        rlTitleDel.setVisibility(View.VISIBLE);
                        txtInfo.setVisibility(View.GONE);
                        mGVAdapter.modeState(isDel);
                        mGVAdapter.notifyDataSetChanged();
                    }
                    break;
                case REQUEST_CHAR_DEL_CHK:
                    ArrayList<String> arrDelIdx = new ArrayList<String>();
                    arrDelCharTemp = new ArrayList<CharacterInfo>();
                    for (int i = 0; i < arrChar.size(); i++) {
                        if (arrChar.get(i).getIsChk()) {
                            arrDelIdx.add(arrChar.get(i).getStrIdx());
                        }else{
                            arrDelCharTemp.add(arrChar.get(i));
                        }
                    }

                    requestCharDel(arrDelIdx);
                    break;

                case DlgSelImg.PICK_FROM_CAMERA:
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mSelDlg.mImageUri, "image/*");
                    // Crop한 이미지를 저장할 Path
                    intent.putExtra("output", mSelDlg.storeCropImage(true, mSelDlg.STR_SECERT_FOLDER_NAME));

                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 4); // crop 박스의 x축 비율
                    intent.putExtra("aspectY", 3); // crop 박스의 y축 비율
                    startActivityForResult(intent, DlgSelImg.CROP_FROM_CAMERA);
                    break;
                case DlgSelImg.CROP_FROM_CAMERA:
                    File file = new File(mSelDlg.mImageUri.getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                case DlgSelImg.PICK_FROM_GALLERY:
                    resizeImg(data, mSelDlg.getFilePath(), mSelDlg.getFileName());
                    break;
                case REQUEST_OK:
                    if (llProgress != null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
                    strMode = STR_REQUEST_MODE_REGI_CHAR;
                    mPageNo = 1;
                    sendDataSet(STR_REQUEST_MODE_REGI_CHAR, arrCharCategory.get(mSelPoint).getStrIdx());
                    gvChar.setSelection(0);
                    break;

                case REQUEST_SEND_MSG:
                    strNewSendMsg = data.getStringExtra("SEND_MSG");
                    if(strSendMsgMode.equals(STR_REGI_SEND_MSG_NEW)){
                        openDialg();
                    }else{
                        arrChar.get(mClickCharPos).setStrTxt(data.getStringExtra("SEND_MSG"));
                    }
                    break;
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
    private void regiSetSendMsg(String result){
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
                Intent intent = new Intent(CharacterListActivity.this, DlgCharaterSet.class);
                intent.putExtra("CharIdx", strCharIdx);
                intent.putExtra("SendMsgMode", strSendMsgMode);
                startActivityForResult(intent, REQUEST_SEND_MSG);
            } catch (Exception e) {
                e.printStackTrace();
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
            strMode = STR_REQUEST_MODE_REGI_CHAR;
            new regiCharacterDataTask().execute();
        }
    }

    /**
     * 주의 팝업 발생
     */
    public void openDialg(){
        Intent intent = new Intent(CharacterListActivity.this, DlgBtnActivity.class);
        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_character_use_warning));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_OK);
    }

    public void sendDataSet(String mode, String idx){
        mLockListView = true;
//        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        strMode = mode;

        String url = "";
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if(mode.equals(STR_REQUEST_MODE_SET_REP)){
            url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_character_rep);
        }else{
            url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_character_page);
        }

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        if(mode.equals(STR_REQUEST_MODE_CATEGORY) || mode.equals(STR_REQUEST_MODE_REGI_CHAR)) {
            k_param.put(SEND_CATEGORY_IDX, idx);
            k_param.put(SEND_PAGE, String.valueOf(mPageNo));
        }else if(mode.equals(STR_REQUEST_MODE_SET_REP)){
            k_param.put(SEND_CHARACTER_IDX, idx);
        }

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll1 || v.getId()==R.id.btn1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                btnCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if (event.getAction() == MotionEvent.ACTION_UP)
                if (mDlg != null && mDlg.isShowing()) mDlg.dismiss();
            return true;
        }
        return false;
    }

    ProgressDialog dlg;
    /**
     * 서버에 값 요청
     */
    private class DataTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        }
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams(); //접속을 하기 위한 기존 환경설정
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); //웹 통신 프로토콜 버전 설정
                HttpClient client = new DefaultHttpClient(httpParams); //접속 기능 객체
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                if(strMode.equals(STR_REQUEST_MODE_CATEGORY) || strMode.equals(STR_REQUEST_MODE_REGI_CHAR)) {
                    listParams.add(new BasicNameValuePair(STR_CATEGORY_IDX, params[SEND_CATEGORY_IDX]));
                    listParams.add(new BasicNameValuePair(STR_PAGE_NO, params[SEND_PAGE]));
                }else if(strMode.equals(STR_REQUEST_MODE_SET_REP)){
                    listParams.add(new BasicNameValuePair(STR_CHAR_IDX, params[SEND_CHARACTER_IDX]));
                } else if(strMode.equals(STR_REQUEST_MODE_DEL)){
                    listParams.add(new BasicNameValuePair(STR_CHARACTER_DEL_IDXES, params[SEND_CHARACTER_DEL_IDXES]));
                    if(strRequestIdx.startsWith(StaticDataInfo.STRING_N)) {
                        listParams.add(new BasicNameValuePair(STR_CATEGORY_IDX, params[SEND_DEL_CATEGORY_IDX]));
                    }
                }

                //접속 제한시간 설정
                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                //응답 제한시간 설정
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
            } else if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200)) || result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))){
                handler.sendEmptyMessage(Integer.parseInt(result));
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    public void resultMain(String result){
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
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && mCharInfo != null) {
                                arrChar.add(mCharInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            if(mPageNo==1){
                                arrChar = new ArrayList<CharacterInfo>();
                                arrChar.clear();
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mCharInfo = new CharacterInfo();
                                mCharInfo.setIsChk(false);
                            }

                            if (parser.getName().equals(STR_CHAR_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_CHAR_REP)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_CHAR_IMG_URL)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_CHAR_TXT)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_GET_MIDDLE_CHARACTER_IMG)) {
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

                displayChar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void displayChar(){
        llCharEmpty.setVisibility(View.GONE);
        gvChar.setVisibility(View.VISIBLE);

        if(mPageNo==1) {
            mGVAdapter = new CharacterGridAdapter(this, arrChar);
            mGVAdapter.modeState(isDel);
            gvChar.setAdapter(mGVAdapter);
            gvChar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (isDel) {
                        CheckBox chkDel = (CheckBox) view.findViewById(R.id.chk_del_char);
                        chkDel.setChecked(!chkDel.isChecked());
                        arrChar.get(position).setIsChk(chkDel.isChecked());
                    } else {
                        openDlg(position);
                    }
                }
            });
            gvChar.setOnScrollListener(mListScroll);
        }else{
            if (mGVAdapter != null) mGVAdapter.notifyDataSetChanged();
        }

        mLockListView = false;
        mPageNo++;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        },500);
    }

    public AbsListView.OnScrollListener mListScroll = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !mLockListView) {
                if(lastitemVisibleFlag){
                    isListUpdate = true;
                    if(!strRequestIdx.equals("")) sendDataSet(STR_REQUEST_MODE_CATEGORY, strRequestIdx);
                }else if(firstitemVisibleFlag){
                    mPageNo = 1;
                    isDel = false;
                    if(!strRequestIdx.equals("")) sendDataSet(STR_REQUEST_MODE_CATEGORY, strRequestIdx);
                }
            }

            if(touchListener!=null && !mLockListView){
                gvChar.setScrollContainer(false);
                touchListener.setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount) >= totalItemCount;
            firstitemVisibleFlag = (firstVisibleItem==0) && view.getChildAt(0)!=null && view.getChildAt(0).getTop()==0;
        }
    };

    private Dialog mDlg;
    private Button btnCancel;
    private int mRepPos=-1; //대표 캐릭터 설정 시 클릭 된 이미지
    public void openDlg(final int charPosition){
        mDlg = new Dialog(this);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDlg.setContentView(R.layout.dlg_list_title);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ((TextView) mDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_character));
        ((LinearLayout) mDlg.findViewById(R.id.ll1)).setOnTouchListener(this);
        btnCancel = (Button) mDlg.findViewById(R.id.btn1);
        btnCancel.setOnTouchListener(this);
        ListView lvDlgMsg = (ListView) mDlg.findViewById(R.id.lv_dlg);

        final ArrayList<String> arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_set_representation_characte));
        arrString.add(getResources().getString(R.string.str_set_character_send_msg));
        arrString.add(getResources().getString(R.string.str_set_receive));

        DlgListAdapter dlgAdapter = new DlgListAdapter(this, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (arrString.get(position).equals(getResources().getString(R.string.str_set_representation_characte))) {
                    mRepPos = charPosition;
                    sendDataSet(STR_REQUEST_MODE_SET_REP, arrChar.get(charPosition).getStrIdx());
                } else if (arrString.get(position).equals(getResources().getString(R.string.str_set_character_send_msg))) {
                    mClickCharPos = charPosition;
                    strSendMsgMode = STR_REGI_SEND_MSG_MODIFY;
                    intent = new Intent(CharacterListActivity.this, DlgCharaterSet.class);
                    intent.putExtra("CharInfo", (Serializable) arrChar.get(charPosition));
                    intent.putExtra("SendMsgMode", strSendMsgMode);
                    startActivityForResult(intent, REQUEST_SEND_MSG);
                    if (mDlg != null && mDlg.isShowing()) mDlg.dismiss();
                    return;
                } else if (arrString.get(position).equals(getResources().getString(R.string.str_set_receive))) {
                    intent = new Intent(CharacterListActivity.this, MyCharacterSetReceiveActivity.class);
                    intent.putExtra("CharTarget", arrChar.get(charPosition));
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                if (mDlg != null && mDlg.isShowing()) mDlg.dismiss();
            }
        });

        mDlg.show();
    }

    public void requestCharDel(ArrayList<String> arrDelIdxes){
//        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        mLockListView = true;
        strMode = STR_REQUEST_MODE_DEL;

        String strDelIdx = "";
        for(int i=0; i<arrDelIdxes.size(); i++){
            strDelIdx += arrDelIdxes.get(i)+",";
        }

        final String url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_character_decharacter);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_CHARACTER_DEL_IDXES, strDelIdx);
        if(strRequestIdx.startsWith(StaticDataInfo.STRING_N)) {
            k_param.put(SEND_DEL_CATEGORY_IDX, strRequestIdx);
        }

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
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
            builder.addPart(STR_SEND_MIDDLE_CHARACTER_IMG, contentMiddleThumbNail);
            // Send Request
            InputStream inputStream = null;
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost post = new HttpPost(url);

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
            if(result.trim().equals("")){
                Toast.makeText(CharacterListActivity.this, getResources().getString(R.string.str_char_regi_err), Toast.LENGTH_SHORT).show();
                return;
            }

            if(result.startsWith(StaticDataInfo.TAG_LIST)) {
                regiSetSendMsg(result);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }
    private ByteArrayBody decodeSampledBitmapFromPath(String path, int reqWidth,int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeFile(path, options);
        src = src.createScaledBitmap(src, reqWidth, reqHeight, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, bos);
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
