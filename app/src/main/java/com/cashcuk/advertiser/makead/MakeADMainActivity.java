package com.cashcuk.advertiser.makead;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.cashcuk.CheckLoginService;
import com.cashcuk.FileManager;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.GetChargeAmount;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgSelImg;
import com.cashcuk.dialog.DlgTimePicker;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageView;
import com.yalantis.ucrop.UCrop;

/**
 * 광고제작
 * 1. 세부정보, 2. 이미지 등록
 */
public class MakeADMainActivity extends Activity implements View.OnClickListener, DialogInterface.OnDismissListener {
    private final int MAKE_AD_COMPLETE = 999;

    private LinearLayout llProgress;
    //tab (1.세부정보, 2.이미지 등록)
    private TextView txtDetailInfo;
    private LinearLayout llDetailInfoUnder;
    private TextView txtRegiImg;
    private LinearLayout llRegiImgUnder;

    private MakeADDetail1 makeADDetail;//1. 세부정보 view
    private MakeADImgRegi2 makeADImgRegi;//2. 이미지 등록 view

    private DlgSelImg mSelDlg; //사진찍기, 앨범 선택 popup

    //광고 등록 기본 설정 값
    private String strMinAmount = ""; //광고 할 최소 금액
    private String strMinPoint = ""; //최소 적립 포인트
    private String strChargeMoney=""; //충전금

    private final String STR_MIN_AMOUNT = "sys_minamnt"; //광고 할 최소 금액
    private final String STR_MIN_POINT = "sys_minpoint"; //최소 적립 포인트

    private final int SEND_AD_IDX = 2;
    private final String STR_AD_IDX = "ad_idx";
    private String strADIdx=""; //광고 idx
    private String strADStatus=""; //광고 상태
    private boolean isModify = false; //수정 모드 여부
    private boolean isModifyTmp = false; //수정 모드 여부

    private final String STR_AD_NAME = "ad_nm"; //상품명
    private final String STR_AD_DETAIL = "ad_txt"; //상세설명
    private final String STR_AD_CATEGORY = "ad_ctg"; //카테고리
    private final String STR_AD_AMOUNT = "ad_amnt"; //광고 할 금액
    private final String STR_AD_POINT = "ad_pnt"; //적립포인트
    private final String STR_AD_DATE_S = "ad_str"; //광고 시작날
    private final String STR_AD_DATE_E = "ad_end"; //광고 마감날
    private final String STR_AD_ADDRESS = "ad_geo"; //주소
    private final String STR_AD_HOME_PAGE = "ad_url"; //홈페이지
    private final String STR_AD_SELLER = "ad_seller"; //영업 광고주 추천
    private final String STR_AD_TITLE_IMG = "ad_titleimg"; //타이틀 이미지
    private final String STR_AD_DETAIL_IMG1 = "ad_dtlimg1"; //세부이미지1
    private final String STR_AD_DETAIL_IMG2 = "ad_dtlimg2"; //세부이미지2
    private final String STR_AD_DETAIL_IMG3 = "ad_dtlimg3"; //세부이미지3
    private final String STR_AD_EVENT = "ad_event"; //이벤트 여부
    private final String STR_AD_USE_COST = "ad_usedamnt"; //광고 소비액

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;
    private final int PARSER_NUM_7 = 7;
    private final int PARSER_NUM_8 = 8;
    private final int PARSER_NUM_9 = 9;
    private final int PARSER_NUM_10 = 10;
    private final int PARSER_NUM_11 = 11;
    private final int PARSER_NUM_12 = 12;
    private final int PARSER_NUM_13 = 13;
    private final int PARSER_NUM_14 = 14;
    private final int PARSER_NUM_15 = 15;

    public final int REQUEST_MONEY_CHECK = 123;
    private boolean isRejudged = false; //재심사 여부
    private Context mContext;
    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(MakeADMainActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1== GetChargeAmount.MSG_ARG) {
                        strChargeMoney = (String)msg.obj;

                        DataRequest(isModify);
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
        setContentView(R.layout.advertiser_make_ad_main_activity);
        CheckLoginService.mActivityList.add(this);
        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_make_ad));

        mContext = this;

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        txtDetailInfo = (TextView) findViewById(R.id.txt_ad_detail_info);
        llDetailInfoUnder = (LinearLayout) findViewById(R.id.ll_ad_detail_info_under);
        txtRegiImg = (TextView) findViewById(R.id.txt_regi_img);
        llRegiImgUnder = (LinearLayout) findViewById(R.id.ll_regi_img_under);

        Intent intent = getIntent();
        if(intent!=null){
            strADIdx = intent.getStringExtra("AD_IDX");
            if(strADIdx!=null && !strADIdx.equals("")) isModify = true;
            strADStatus = intent.getStringExtra("AD_STATUS");
        }

        //1. 세부정보 view
        makeADDetail =  (MakeADDetail1) findViewById(R.id.make_ad_detail);
        makeADDetail.getOnInfoData(mNextInfo);

        //2. 이미지 등록 view
        makeADImgRegi =  (MakeADImgRegi2) findViewById(R.id.make_ad_img_regi);
        ((Button) makeADImgRegi.findViewById(R.id.btn_make_ad_img_registration_pre)).setOnClickListener(this);
        makeADImgRegi.setOnDetailImgClickListener(mDetailClick);
        makeADImgRegi.getOnData(mNextClick);

        if(!isModify){
            makeADDetail.makeAD();
        }
        checkPermissionAndPickImage();
        checkCameraPermission();

        new GetChargeAmount(MakeADMainActivity.this, handler);
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
        if (v.getId() == R.id.btn_make_ad_img_registration_pre) {
            if(!makeADDetail.isShown()) makeADDetail.setVisibility(View.VISIBLE);
            if(makeADImgRegi.isShown()) makeADImgRegi.setVisibility(View.GONE);

            llRegiImgUnder.setVisibility(View.GONE);
            llDetailInfoUnder.setVisibility(View.VISIBLE);
            txtRegiImg.setBackgroundColor(getResources().getColor(R.color.color_white));
            txtDetailInfo.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
        }
    }

    private ArrayList<String> arrDetailData; //세부정보들
    private String strCategory; //카테고리
    private MakeADDetail1.OnGetInfoData mNextInfo = new MakeADDetail1.OnGetInfoData() {
        @Override
        public void onGetInfoData(ArrayList<String> arrData, String strCategoryItem) {
            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

            arrDetailData = arrData;
            strCategory = strCategoryItem;

            if(arrDetailData.get(makeADDetail.INDEX_AD_RECOMMEND)!=null && !arrDetailData.get(makeADDetail.INDEX_AD_RECOMMEND).equals("")) {
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                if(ChkEmailType(arrDetailData.get(makeADDetail.INDEX_AD_RECOMMEND))) {
                    chkRecommend(arrDetailData.get(makeADDetail.INDEX_AD_RECOMMEND));
                }
            }else{
                nextInfo();
            }
        }
    };

    /**
     * e-mail 형식이 맞는지 체크함.
     * @param email
     */
    public boolean ChkEmailType(String email){

        if(checkPattern(email)) {
            return true;
        } else {
            Toast.makeText(MakeADMainActivity.this, R.string.str_email_type_err, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 이메일 정규식 체크
     * @param strPattern : 값
     * @return true: 형식에 맞음, false: 형식에 맞지 않음
     */
    private boolean checkPattern(String strPattern)
    {
        Pattern p = Pattern.compile("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$");
        Matcher m = p.matcher(strPattern);

        return m.matches();
    }

    private final int SEND_RECOMMEND_EMAIL = 2;
    private final String STR_RECOMMEND_EMAIL = "email";
    /**
     * 추천영업인 체크
     */
    public void chkRecommend(String strRecommend){
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_recommend_chk);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_RECOMMEND_EMAIL, strRecommend);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new chkRecommendDataTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class chkRecommendDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        }

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
                listParams.add(new BasicNameValuePair(STR_RECOMMEND_EMAIL, params[SEND_RECOMMEND_EMAIL]));


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

            if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                nextInfo();
            }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))){
                Toast.makeText(MakeADMainActivity.this, getResources().getString(R.string.str_recommend_empty), Toast.LENGTH_SHORT).show();
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    public void nextInfo(){
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
        
        if(mModifyInfo!=null) {
            makeADImgRegi.modifyAD(mModifyInfo);
        }


            if (makeADDetail.isShown()) makeADDetail.setVisibility(View.GONE);
            if (!makeADImgRegi.isShown()) makeADImgRegi.setVisibility(View.VISIBLE);

            llRegiImgUnder.setVisibility(View.VISIBLE);
            llDetailInfoUnder.setVisibility(View.GONE);
            txtRegiImg.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
            txtDetailInfo.setBackgroundColor(getResources().getColor(R.color.color_white));

    }

    //2. 이미지 등록 관련
    private String strImgKind; // title img OR detail img
    private MakeADImgRegi2.onDetailItemClick mDetailClick = new MakeADImgRegi2.onDetailItemClick() {
        @Override
        public void onDetailImgClick(boolean isImg, String strKind) {
            strImgKind = strKind;
            mSelDlg = new DlgSelImg(MakeADMainActivity.this, isImg, false);
            mSelDlg.setonDismissListener(MakeADMainActivity.this);
            if(mSelDlg!=null && !mSelDlg.isShowing()) mSelDlg.show();
        }
    };

    private MakeADImgRegi2.OnGetData mNextClick = new MakeADImgRegi2.OnGetData() {
        @Override
        public void onGetData(ArrayList<Boolean> arrIsChangeDetailImg, boolean isChangeTitleImg, String strTitle, ArrayList<String> arrDetailImg) {
            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            DetailImgGetData(arrIsChangeDetailImg, isChangeTitleImg, strTitle, arrDetailImg);
        }
    };

    //공통
    /**
     * 광고제작 정보 입력 data
     * @param arrDetailImg
     */
    public void DetailImgGetData(ArrayList<Boolean> arrIsChangeDetailImg, boolean isChangeTitleImg, String strTitle, ArrayList<String> arrDetailImg){
        //1. 세부정보
        Intent intent = new Intent(MakeADMainActivity.this, MakeADPreviewActivity.class);

        intent.putExtra(makeADDetail.STR_PUT_AD_IDX, strADIdx); //광고 idx
        intent.putExtra(makeADDetail.STR_PUT_AD_NAME, arrDetailData.get(makeADDetail.INDEX_AD_NAME)); //광고명
        intent.putExtra(makeADDetail.STR_PUT_AD_DETAIL, arrDetailData.get(makeADDetail.INDEX_AD_DETAIL)); //상세설명
        intent.putExtra(makeADDetail.STR_PUT_AD_CATEGORY, strCategory);
        intent.putExtra(makeADDetail.STR_PUT_AD_AMOUNT, arrDetailData.get(makeADDetail.INDEX_AD_AMOUNT)); //광고 할 금액
        intent.putExtra(makeADDetail.STR_PUT_AD_SAVE_POINT, arrDetailData.get(makeADDetail.INDEX_AD_SAVE_POINT)); //적립포인트
        intent.putExtra(makeADDetail.STR_PUT_AD_DATE_S, arrDetailData.get(makeADDetail.INDEX_AD_DATE_S)); //광고기간 start
        intent.putExtra(makeADDetail.STR_PUT_AD_DATE_E, arrDetailData.get(makeADDetail.INDEX_AD_DATE_E)); //광고기간 end
        intent.putExtra(makeADDetail.STR_PUT_AD_MY_CHARGE, arrDetailData.get(makeADDetail.INDEX_AD_MY_CHARGE)); //충전금
        intent.putExtra(makeADDetail.STR_PUT_AD_ADDR, arrDetailData.get(makeADDetail.INDEX_AD_ADDRESS)); //주소
        intent.putExtra(makeADDetail.STR_PUT_AD_HOMEPAGE, arrDetailData.get(makeADDetail.INDEX_AD_HOMEPAGE)); //홈페이지
        intent.putExtra(makeADDetail.STR_PUT_AD_RECOMMEND, arrDetailData.get(makeADDetail.INDEX_AD_RECOMMEND)); //영업 광고주 추천
        intent.putExtra(makeADDetail.STR_PUT_AD_EVENT, arrDetailData.get(makeADDetail.INDEX_AD_EVENT)); //이벤트 여부
        intent.putExtra(makeADDetail.STR_PUT_AD_STATUS, strADStatus); //광고 상태 여부

        intent.putExtra(makeADDetail.STR_PUT_AD_UP_COST, ""); //증액 금액


        if(isModify) {
            if (isRejudged || arrDetailData.get(makeADDetail.INDEX_AD_REJUDGED).startsWith(StaticDataInfo.STRING_Y)) {
                intent.putExtra(makeADDetail.STR_PUT_AD_REJUDGED, StaticDataInfo.STRING_Y); //재심사 여부
            } else if (!isRejudged && arrDetailData.get(makeADDetail.INDEX_AD_REJUDGED).startsWith(StaticDataInfo.STRING_N)) {
                intent.putExtra(makeADDetail.STR_PUT_AD_REJUDGED, StaticDataInfo.STRING_N); //재심사 여부
            }
            intent.putExtra(makeADDetail.STR_PUT_AD_MY_CHARGE, arrDetailData.get(makeADDetail.INDEX_AD_MY_CHARGE)); //충전금
            intent.putExtra(makeADDetail.STR_PUT_AD_UP_COST, arrDetailData.get(makeADDetail.INDEX_AD_UP_COST)); //증액 금액
        } else if (arrDetailData.get(makeADDetail.INDEX_AD_REJUDGED).startsWith(MakeADDetail1.STR_NOT_MODIFY)) {
            intent.putExtra(makeADDetail.STR_PUT_AD_REJUDGED, MakeADDetail1.STR_NOT_MODIFY);
        }

        //2. 이미지 등록
        if(!strTitle.equals("")){
            intent.putExtra("Title_Img", strTitle);
            intent.putExtra("ChangeTitleImg", isChangeTitleImg);
        }
        if(arrDetailImg!=null && arrDetailImg.size()>0){
            intent.putStringArrayListExtra("Detail_Img", arrDetailImg);
            intent.putExtra("ChangeDetailImg", arrIsChangeDetailImg);
        }

        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, MAKE_AD_COMPLETE);
    }

    private int mYear, mMonth, mDay;
    private int mHour, mMin;
    private  String[] strDate;
    private String[] strTime;
    private Uri imageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            Uri cropUri = null;
            Intent intent = null;
            int mRequestCode = -1;
            switch (requestCode) {
                case MAKE_AD_COMPLETE:
                    finish();
                    break;
                case DlgSelImg.PICK_FROM_CAMERA:
                    intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mSelDlg.mImageUri, "image/*");

                    // Crop한 이미지를 저장할 Path
                    File cropFile = null;
                    try {
                        cropFile = mSelDlg.createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        ex.printStackTrace();
                    }
                    if (cropFile != null) {
                         cropUri = FileProvider.getUriForFile(this,
                                getPackageName() + ".fileprovider",
                                cropFile);
                        //자르기 앱에 cropUri에 대한 권한을 부여합니다.
                        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            grantUriPermission(packageName, cropUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        //grantUriPermission(getPackageName(), cropUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra("output", cropUri);
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 16); // crop 박스의 x축 비율
                    intent.putExtra("aspectY", 9); // crop 박스의 y축 비율
                    //intent.putExtra("return-data", false);
                    //mRequestCode = DlgSelImg.CROP_FROM_CAMERA;
                    mRequestCode = DlgSelImg.CROP_FROM_GALLERY;

                    isRejudged = true;
                    /*
                    intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mSelDlg.mImageUri, "image/*");
                    // Crop한 이미지를 저장할 Path
                    intent.putExtra("output", mSelDlg.storeCropImage(true, mSelDlg.STR_SECERT_FOLDER_NAME));

                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 16); // crop 박스의 x축 비율
                    intent.putExtra("aspectY", 9); // crop 박스의 y축 비율
                    mRequestCode = DlgSelImg.CROP_FROM_CAMERA;

                    isRejudged = true;
                    */
                    break;
                case DlgSelImg.CROP_FROM_CAMERA:
                    File file = new File(mSelDlg.mImageUri.getPath());
                    if(file.exists()){
                        file.delete();
                    }
                case DlgSelImg.PICK_FROM_GALLERY:
                    isRejudged = true;
                    imageUri = data.getData();
                    Log.d("Gallery URI", "Selected URI: " + imageUri);
                    if (imageUri != null) {
                        Log.d("Gallery URI", "packageName: " +getPackageName());
                        //cropImage(imageUri);
                        startCrop(imageUri);
                    }
                    break;
                case DlgSelImg.CROP_FROM_GALLERY:
                    cropUri = null;
                    if(data != null){
                        cropUri = data.getData();
                    }
                    if(cropUri == null){
                        //이미지 자르기 앱이 data에 아무것도 반환하지 않은 경우
                        //cropUri에 이미지가 저장되었는지 확인
                        cropFile = null;
                        try {
                            cropFile = mSelDlg.createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            ex.printStackTrace();
                        }
                        if(cropFile != null){
                            cropUri = FileProvider.getUriForFile(this,
                                    getPackageName() + ".fileprovider",
                                    cropFile);
                        }
                    }
                    if (cropUri != null) {
                        // cropUri를 사용하여 이미지를 처리
                        // getRealPathFromURI() 메서드를 사용할 필요가 없습니다.
                        // makeADImgRegi.setImg(strFilePathName, strImgKind); // 기존 코드
                        makeADImgRegi.setImg(cropUri, strImgKind); // 수정된 코드
                    }
                    /*
                    String strFilePathName=FileManager.get(this).getRealPathFromURI(this,data.getData());
                    if(!strFilePathName.equals("")) {
                        makeADImgRegi.setImg(strFilePathName, strImgKind);
                    }
                    */

                    Log.d("temp","**************MakeADMainActivity onActivityResult CROP_FROM_GALLERY");
                    break;
                case  UCrop.REQUEST_CROP:
                    Uri croppedImageUri = UCrop.getOutput(data);
                    if (croppedImageUri != null) {
                        makeADImgRegi.setImg(croppedImageUri, strImgKind); // 수정된 코드
                    }
                    break;
                case  UCrop.RESULT_ERROR:
                    Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        cropError.printStackTrace();
                        Toast.makeText(this, "크롭 오류 발생: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MakeADDetail1.REQUEST_CODE_ADDRESS:
                    makeADDetail.setAddress(data.getStringExtra("Addr"));
                    break;
                case MakeADDetail1.REQUEST_DATE_S:
                    strDate = makeADDetail.getSDate().split("-");
                    mYear = data.getIntExtra("Year", Integer.valueOf(strDate[0]));
                    mMonth = data.getIntExtra("Month", Integer.valueOf(strDate[1]));
                    mDay = data.getIntExtra("Day", Integer.valueOf(strDate[2]));

                    strTime = makeADDetail.getSTime().split(":");
                    intent = new Intent(MakeADMainActivity.this, DlgTimePicker.class);
                    intent.putExtra("Hour", Integer.parseInt(strTime[0]));
                    intent.putExtra("Min", Integer.parseInt(strTime[1]));
                    mRequestCode = MakeADDetail1.REQUEST_TIME_S;
                    break;
                case MakeADDetail1.REQUEST_DATE_E:
                    strDate = makeADDetail.getEDate().split("-");

                    mYear = data.getIntExtra("Year", Integer.valueOf(strDate[0]));
                    mMonth = data.getIntExtra("Month", Integer.valueOf(strDate[1]));
                    mDay = data.getIntExtra("Day", Integer.valueOf(strDate[2]));

                    strTime = makeADDetail.getETime().split(":");
                    intent = new Intent(MakeADMainActivity.this, DlgTimePicker.class);
                    intent.putExtra("Hour", Integer.parseInt(strTime[0]));
                    intent.putExtra("Min", Integer.parseInt(strTime[1]));

                    mRequestCode = MakeADDetail1.REQUEST_TIME_E;
                    break;
                case MakeADDetail1.REQUEST_TIME_S:
                case MakeADDetail1.REQUEST_TIME_E:
                    mHour = data.getIntExtra("Hour", Integer.valueOf(strTime[0]));
                    mMin = data.getIntExtra("Min", Integer.valueOf(strTime[1]));
                    makeADDetail.setDate(requestCode, mYear, mMonth, mDay, mHour, mMin);
                    break;
                case MakeADDetail1.REQUEST_CHARGE_ERR:
                    finish();
                    break;
                case REQUEST_MONEY_CHECK:
                    finish();
                    break;
                case MakeADDetail1.REQUEST_CHK_AD_AMOUNT:
                    makeADDetail.showKeyboard(true);
                    break;
                case MakeADDetail1.REQUEST_CHK_AD_SAVE_POINT:
                    makeADDetail.showKeyboard(false);
                    break;
            }

            if(intent!=null && mRequestCode!=-1){
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, mRequestCode);
            }
        }
    }

    //사진 자르기
    private void cropImage(Uri sourceUri) {
        try {
            // 크롭 결과를 저장할 파일 생성
            File cropFile = mSelDlg.createImageFile();
            Uri cropUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cropFile);
            Uri localUri = copyUriToFile(sourceUri);

            // 로그 출력
            Log.d("Crop URI", "Source URI: " + sourceUri);
            Log.d("Crop URI", "Local URI: " + localUri);
            Log.d("Crop URI", "Crop URI: " + cropUri);

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(localUri, "image/*");
            //cropIntent.setDataAndType(sourceUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // 크롭 앱에 URI 권한 부여
            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cropIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;

                // URI 권한 부여 로그
                Log.d("Grant URI Permission", "Granting permission to: " + packageName);
                Log.d("Grant URI Permission", "Crop URI: " + cropUri);
                Log.d("Grant URI Permission", "Local URI: " + localUri);
                Log.d("Grant URI Permission", "SourceUri URI: " + sourceUri);

                // sourceUri에 대한 권한 부여
                grantUriPermission(packageName, localUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //grantUriPermission(packageName, sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // cropUri에 대한 권한 부여
                grantUriPermission(packageName, cropUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            // 크롭 작업 실행
            startActivityForResult(cropIntent, DlgSelImg.CROP_FROM_GALLERY);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 크롭 실패", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri getScopedStorageUri(Uri contentUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String[] projection = {MediaStore.Images.Media._ID};
            Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                long id = cursor.getLong(idColumn);
                cursor.close();

                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            }
        }
        return contentUri; // Fallback for Android 9 이하
    }
    private Uri copyUriToFile(Uri sourceUri) throws IOException {
        // 저장할 파일 생성
        File tempFile = mSelDlg.createImageFile();

        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // FileProvider를 통해 content:// URI 반환
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);
    }

    private void startCrop(Uri sourceUri) {
        // 크롭 결과 저장 파일 생성
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "CroppedImage.jpg"));

        // UCrop 옵션 설정
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setToolbarTitle("이미지 크롭");
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorAccent));

        // UCrop 실행
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1) // 원하는 비율 설정 (1:1)
                .withOptions(options) // 옵션 적용
                .start(this);
    }

 /*   public void cropImage(Intent data, Uri photoUri){
        mSelDlg.storeCropImage(true, mSelDlg.STR_SECERT_FOLDER_NAME);

        Uri orgphotoUri=data.getData();
        //Uri photoUri=mSelDlg.photoUri;

        //Log.d("temp","orgphotoUri()["+orgphotoUri+"]");
        Log.d("temp","getPhotoUri()["+photoUri+"]");


        grantUriPermission(getPackageName(), photoUri , Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setDataAndType(orgphotoUri , "image/*");
        intent.putExtra("aspectX", 16);
        intent.putExtra("aspectY", 9);
        intent.putExtra("scale", true);
        intent.putExtra("output", photoUri);
        //intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, DlgSelImg.CROP_FROM_GALLERY);

    }
 */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if(mSelDlg.getDelImg()){
            makeADImgRegi.DelImg(strImgKind);
        };
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(boolean modify){
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        isModifyTmp = modify;
        String url="";

        if(modify){
            url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertises_ad);
        }else{
            url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_page);
        }
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

//        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);

        if(modify && strADIdx!=null && !strADIdx.equals("")) {
            k_param.put(SEND_AD_IDX, strADIdx);
        }

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
                if(isModifyTmp && strADIdx!=null && !strADIdx.equals("")) {
                    listParams.add(new BasicNameValuePair(STR_AD_IDX, params[SEND_AD_IDX]));
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
            if(result.startsWith(StaticDataInfo.TAG_LIST)) {
                if(!isModifyTmp) {
                    getDefaultData(result);
                }else{
                    resultModifyData(result);
                }
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },500);
        }
    }

    /**
     * 광고 등록 시 필요한 기본 data
     * @param result
     */
    public void getDefaultData(String result){
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
                        if (parser.getName().equals(STR_MIN_AMOUNT)) {
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_MIN_POINT)) {
                            k_data_num = PARSER_NUM_1;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    strMinAmount = parser.getText();
                                    break;
                                case PARSER_NUM_1:
                                    strMinPoint = parser.getText();
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
            adRegiTermsChk();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 현재 충전금이 광고 최소 금액보다 많은 지 chk
     */
    public void adRegiTermsChk(){
        if(strChargeMoney!=null && strMinAmount!=null
                && !strChargeMoney.equals("") && !strMinAmount.equals("")){
            long chargeMoney = Long.parseLong(strChargeMoney.replace(",", ""));
            long minMoney = Long.parseLong(strMinAmount.replace(",", ""));

            if(chargeMoney < minMoney){
                Intent intent = new Intent(MakeADMainActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_make_ad_err));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_MONEY_CHECK);
            } else {
                makeADDetail.setDetailInfo(strMinAmount, strMinPoint, strChargeMoney);
            }
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        },500);
    }

    private ModifyADInfo mModifyInfo;
    public void resultModifyData(String result){
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
                        mModifyInfo = new ModifyADInfo();
                        break;

                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(STR_AD_NAME)) {
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_AD_DETAIL)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_AD_CATEGORY)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_AD_AMOUNT)) {
                            k_data_num = PARSER_NUM_3;
                        } else if (parser.getName().equals(STR_AD_POINT)) {
                            k_data_num = PARSER_NUM_4;
                        } else if (parser.getName().equals(STR_AD_DATE_S)) {
                            k_data_num = PARSER_NUM_5;
                        } else if (parser.getName().equals(STR_AD_DATE_E)) {
                            k_data_num = PARSER_NUM_6;
                        } else if (parser.getName().equals(STR_AD_ADDRESS)) {
                            k_data_num = PARSER_NUM_7;
                        } else if (parser.getName().equals(STR_AD_HOME_PAGE)) {
                            k_data_num = PARSER_NUM_8;
                        } else if (parser.getName().equals(STR_AD_SELLER)) {
                            k_data_num = PARSER_NUM_9;
                        } else if (parser.getName().equals(STR_AD_TITLE_IMG)) {
                            k_data_num = PARSER_NUM_10;
                        } else if (parser.getName().equals(STR_AD_DETAIL_IMG1)) {
                            k_data_num = PARSER_NUM_11;
                        } else if (parser.getName().equals(STR_AD_DETAIL_IMG2)) {
                            k_data_num = PARSER_NUM_12;
                        } else if (parser.getName().equals(STR_AD_DETAIL_IMG3)) {
                            k_data_num = PARSER_NUM_13;
                        } else if (parser.getName().equals(STR_AD_EVENT)) {
                            k_data_num = PARSER_NUM_14;
                        } else if (parser.getName().equals(STR_AD_USE_COST)) {
                            k_data_num = PARSER_NUM_15;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    mModifyInfo.setStrADNanme(parser.getText());
                                    break;
                                case PARSER_NUM_1:
                                    mModifyInfo.setStrADDetail(parser.getText());
                                    break;
                                case PARSER_NUM_2:
                                    mModifyInfo.setStrADCategory(parser.getText());
                                    break;
                                case PARSER_NUM_3:
                                    mModifyInfo.setStrADAmount(parser.getText());
                                    break;
                                case PARSER_NUM_4:
                                    mModifyInfo.setStrADSavePoint(parser.getText());
                                    break;
                                case PARSER_NUM_5:
                                    mModifyInfo.setStrADDateS(parser.getText());
                                    break;
                                case PARSER_NUM_6:
                                    mModifyInfo.setStrADDateE(parser.getText());
                                    break;
                                case PARSER_NUM_7:
                                    mModifyInfo.setStrADAddress(parser.getText());
                                    break;
                                case PARSER_NUM_8:
                                    mModifyInfo.setStrADHomePage(parser.getText());
                                    break;
                                case PARSER_NUM_9:
                                    mModifyInfo.setStrADSeller(parser.getText());
                                    break;
                                case PARSER_NUM_10:
                                    mModifyInfo.setStrADTitleImgUrl(parser.getText());
                                    break;
                                case PARSER_NUM_11:
                                    mModifyInfo.setStrADDetailImgUrl1(parser.getText());
                                    break;
                                case PARSER_NUM_12:
                                    mModifyInfo.setStrADDetailImgUrl2(parser.getText());
                                    break;
                                case PARSER_NUM_13:
                                    mModifyInfo.setStrADDetailImgUrl3(parser.getText());
                                    break;
                                case PARSER_NUM_14:
                                    mModifyInfo.setStrADEvent(parser.getText());
                                    break;
                                case PARSER_NUM_15:
                                    mModifyInfo.setStrADUseCost(parser.getText());
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
            getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getData() {
        DataRequest(false);
        makeADDetail.modifyData(mModifyInfo);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    private static final int REQUEST_READ_MEDIA_IMAGES = 101;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 102; //
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(mContext,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions((Activity)mContext,new String[]{android.Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 있는 경우 카메라 앱 실행
            //startCamera();
        }
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (TIRAMISU) 이상
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_MEDIA_IMAGES);
            } else {
                //pickImageFromGallery(); // 권한이 이미 있는 경우 호출
            }
        } else  {
            // Android 12 이하
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE); // 새로운 상수 사용
            } else {
                //pickImageFromGallery(); // 권한이 이미 있는 경우 호출
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //pickImageFromGallery();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 거부되었습니다.1", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //pickImageFromGallery();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 거부되었습니다.2", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
