package com.cashcuk.advertiser.sendpush;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cashcuk.CheckLoginService;
import com.cashcuk.FileManager;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.makead.MakeADDetail1;
import com.cashcuk.advertiser.sendpush.view.PushSendViewInfo;
import com.cashcuk.common.PushTargetSend;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgChkPwdActivity;
import com.cashcuk.dialog.DlgSelImg;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;
import com.cashcuk.membership.txtlistdata.TxtPushTargetInfo;

import org.apache.http.entity.mime.content.ByteArrayBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static androidx.core.content.FileProvider.getUriForFile;

/**
 * 광고 대상 및 push 발송
 */
public class ADTargetSendActivity extends Activity implements View.OnClickListener {
    private ADTargetSetLinear1 targetSetLinear; //1. 대상 설정
    private ADTargetChkLinear2 targetChkLinear; //2. 발송 대상
    private ImageView ivAddPushImg; //push 광고 이미지
    private ImageView ivPushImg; //push 광고 이미지(icon)
    private Button btnPushPreview; //push 광고 미리보기
    private Button btnSend; //발송 버튼

    private EditText etSendCnt; //발송 건 수

    private final int REQUEST_CHK_PWD = 999;
    private final int REQUEST_SEND_OK = 987;

    private final int PAGE_TAGET_SET = 1; //1. 대상 설정
    private final int PAGE_TAGET_CHK = 2; //2. 발송 대상

    private TextView txtTargetSet; //대상설정
    private LinearLayout llTargetSetUnder;
    private TextView txtSendTarget; //발송대상
    private LinearLayout llSendTargetUnder;

    private String pushTargetSetInfo;     // 발송 대상 일반정보
    private String pushSendCnt;             // 발송 건 수

    private DlgSelImg mSelDlg; //사진찍기, 앨범 선택 popup

    private String strPushIdx = "";
    private String strADIdx = "";
    private String strADName = "";

    private String strRequestMode="";
    private PushSendViewInfo mPushInfo;

    private boolean isImgChange = false; //이미지 변경 체크 flag
    private LinearLayout llProgress;
    TxtListDataInfo mTxtAdInfo =new TxtListDataInfo();

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_ad_target_set_activity);
        CheckLoginService.mActivityList.add(this);
        checkPermissions();

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_ad_push_send));

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        txtTargetSet = (TextView) findViewById(R.id.txt_ad_taget_set);
        llTargetSetUnder = (LinearLayout) findViewById(R.id.ll_ad_taget_set_under);
        txtSendTarget = (TextView) findViewById(R.id.txt_ad_send_target);
        llSendTargetUnder = (LinearLayout) findViewById(R.id.ll_ad_send_target_under);

        targetSetLinear = (ADTargetSetLinear1) findViewById(R.id.target_set_linear);
        targetChkLinear = (ADTargetChkLinear2) findViewById(R.id.target_chk_linear);
        ivPushImg = (ImageView) targetChkLinear.findViewById(R.id.iv_target_set_push);
        ivAddPushImg = (ImageView) targetChkLinear.findViewById(R.id.iv_target_set_push_add);
        btnPushPreview = (Button) targetChkLinear.findViewById(R.id.btn_push_img_preview);
        ivPushImg.setOnClickListener(this);
        ivAddPushImg.setOnClickListener(this);
        btnPushPreview.setOnClickListener(this);
        ((Button) targetChkLinear.findViewById(R.id.btn_push_send_pre)).setOnClickListener(this);
        btnSend = (Button) targetChkLinear.findViewById(R.id.btn_push_send);
        btnSend.setOnClickListener(this);
        btnSend.setText(getResources().getString(R.string.str_push_send_request));

        etSendCnt = (EditText) targetChkLinear.findViewById(R.id.et_push_send_cnt);
        etSendCnt.addTextChangedListener(mTextWatcher);

        targetSetLinear.getOnData(mNextTarget);
        targetChkLinear.getOnData(mNextPush);

        mSelDlg = new DlgSelImg(this);


        Intent intent = getIntent();
        if (intent != null) {
            strRequestMode = intent.getStringExtra("RequestMode");
            mPushInfo = (PushSendViewInfo) intent.getSerializableExtra("DataInfo");
            strADIdx = intent.getStringExtra("AD_IDX");
            strPushIdx = intent.getStringExtra("PUSH_IDX");

            if(intent.getStringExtra("AD_NAME")!=null){
                strADName= intent.getStringExtra("AD_NAME");

                Log.d("temp","strADIdx["+strADIdx+"]");
                Log.d("temp","strADName["+strADName+"]");
                mTxtAdInfo=new TxtListDataInfo();
                mTxtAdInfo.setStrIdx(strADIdx);
                mTxtAdInfo.setStrMsg(strADName);
                targetSetLinear.setTxtAdInfo(mTxtAdInfo);

            }


            if(strRequestMode!=null&&!strRequestMode.equals("")){
                if(mPushInfo!=null) {
                    chkPushState(strRequestMode);
                    strADIdx = mPushInfo.getStrADIdx();
                    strPushIdx = mPushInfo.getStrPushIdx();
                }
            }
        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    //아래는 권한 요청 Callback 함수입니다. PERMISSION_GRANTED로 권한을 획득했는지 확인할 수 있습니다. 아래에서는 !=를 사용했기에
    //권한 사용에 동의를 안했을 경우를 if문으로 코딩되었습니다.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }
    //권한 획득에 동의를 하지 않았을 경우 아래 Toast 메세지를 띄우며 해당 Activity를 종료시킵니다.
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
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
     * 재발송 or 재요청
     * @param - R: 재요청, A: 재발송
     */
    public void chkPushState(String mode){
        if(mode.equals(getResources().getString(R.string.str_ad_status_chk_r_push))) { //승인거부
            btnSend.setText(getResources().getString(R.string.str_push_again_request));
        }else if(mode.equals(getResources().getString(R.string.str_ad_status_chk_a_push))) { //재발송
            btnSend.setText(getResources().getString(R.string.str_push_resend));
        }

        if(mPushInfo!=null) {
//            targetSetLinear.setDataAgainRequest(mPushInfo);   // 재발송 재요청시 Target을 새로 설정하도록 주석처리

            if (!ivAddPushImg.isShown()) {
                ivPushImg.setVisibility(View.GONE);
                ivAddPushImg.setVisibility(View.VISIBLE);
            }

            Glide
                    .with(this)
                    .load(mPushInfo.getStrPushImgUrl())
                    .centerCrop()
                    .placeholder(R.drawable.image_none)
                    .into(ivAddPushImg);
        }
    }

    private String strCommaResult = "";
    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().equals(strCommaResult)) {     // StackOverflow를 막기위해,
                strCommaResult = StaticDataInfo.makeStringComma(s.toString().replaceAll(",", ""));   // 에딧텍스트의 값을 변환하여, result에 저장.
                etSendCnt.setText(strCommaResult);    // 결과 텍스트 셋팅.
                etSendCnt.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private int mYear, mMonth, mDay;
    private int mHour;
    private  String[] strDate;
    private String strHour;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("temp","**************AdTargetSenActivity onActivityResult requestCode["+requestCode+"]resultCode["+resultCode+"]data["+data+"]");

        if (resultCode == RESULT_OK) {
            int mRequestCode = -1;
            Intent intentPushDate=null;
            switch (requestCode) {
                case REQUEST_SEND_OK:
                    finish();
                    break;
                case REQUEST_CHK_PWD:
                    if(llSendTargetUnder.isShown()) {
                        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

                        String strDateTime = targetChkLinear.getDate()+" "+targetChkLinear.getTime();

                        // PUSH 요청 발송
                        new PushTargetSend(this,
                                pushHandler,
                                strADIdx,
                                strPushIdx,
                                isImgChange,
                                pushTargetSetInfo,
                                pushSendCnt,
                                mSelDlg,
                                strDateTime);
                    }
                    break;
                case DlgSelImg.PICK_FROM_CAMERA:
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mSelDlg.mImageUri, "image/*");
                    // Crop한 이미지를 저장할 Path
                    intent.putExtra("output", mSelDlg.storeCropImage(true, mSelDlg.STR_SECERT_FOLDER_NAME));

                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 16); // crop 박스의 x축 비율
                    intent.putExtra("aspectY", 9); // crop 박스의 y축 비율
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, DlgSelImg.CROP_FROM_CAMERA);
                    break;
                case DlgSelImg.CROP_FROM_CAMERA:
                    File file = new File(mSelDlg.mImageUri.getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                case DlgSelImg.PICK_FROM_GALLERY:
                    Log.d("temp","**************AdTargetSenActivity onActivityResult PICK_FROM_GALLERY");
                    //resizeImg(data, mSelDlg.getImageFilePath());
                    isImgChange = true;
                    cropImage(data, FileManager.get(this).photoUri);
                    break;
                case DlgSelImg.CROP_FROM_GALLERY:
                    Log.d("temp","**************AdTargetSenActivity onActivityResult CROP_FROM_GALLERY");
                    viewImg(data);
                    break;

                case ADTargetChkLinear2.REQUEST_DATE:
                    strDate = targetChkLinear.getDate().split("-");
                    mYear = data.getIntExtra("Year", Integer.valueOf(strDate[0]));
                    mMonth = data.getIntExtra("Month", Integer.valueOf(strDate[1]));
                    mDay = data.getIntExtra("Day", Integer.valueOf(strDate[2]));

                    strHour = targetChkLinear.getTime();
                    intentPushDate = new Intent(ADTargetSendActivity.this, DlgPushTimePicker.class);
                    intentPushDate.putExtra("Hour", Integer.parseInt(strHour));
                    mRequestCode = MakeADDetail1.REQUEST_TIME_S;
                    break;
                case ADTargetChkLinear2.REQUEST_TIME:
                    mHour = data.getIntExtra("Hour", Integer.valueOf(strHour));
                    targetChkLinear.setDate(requestCode, mYear, mMonth, mDay, mHour);
                    break;
            }

            if(intentPushDate!=null && mRequestCode!=-1){
                intentPushDate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intentPushDate, mRequestCode);
            }
        }
    }

    //사진 자르기
    public void cropImage(Intent data, Uri photoUri){
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
        //intent.putExtra("output", photoUri);
        intent.putExtra("return-data", false);
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        /**
         * getUriforFile()이 return한 content URI에 대한 접근권한을 승인하려면 grantUriPermission을 호출한다.
         * mode_flags 파라미터의 값에 따라. 지정한 패키지에 대해 content URI를 위한 임시 접근을 승인한다.
         * 권한은 기기가 리부팅 되거나 revokeUriPermission()을 호출하여 취소할때까지 유지.
         *
         */
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, DlgSelImg.CROP_FROM_GALLERY);

    }

    private void resizeImg(Intent data, String dirFile){

        Log.d("temp","resizeImg dirFile["+dirFile+"]");
        Log.d("temp","resizeImg data["+data+"]");

        String strImgDir = dirFile;
        if (data != null) {
            Bitmap bitImg = decodeSampledBitmapFromPath(strImgDir, Integer.valueOf(getResources().getString(R.string.str_ad_char_w)), Integer.valueOf(getResources().getString(R.string.str_ad_h)));
            if (bitImg!=null) {
                if (!ivAddPushImg.isShown()) {
                    ivPushImg.setVisibility(View.GONE);
                    ivAddPushImg.setVisibility(View.VISIBLE);
                }

                if (data != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ivAddPushImg.setBackground(new BitmapDrawable(bitImg));
                    } else {
                        ivAddPushImg.setBackgroundDrawable(new BitmapDrawable(bitImg));
                    }

                    if (!btnPushPreview.isShown())
                        btnPushPreview.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    private void viewImg(Intent data){

        Log.d("temp","resizeImg data["+data+"]");

        mSelDlg.setImageFilePath(getRealPathFromURI(this,data.getData()));
        //mSelDlg.setImageFilePath(getRealPathFromURI(this,mSelDlg.photoUri));
        Log.d("temp","getImageFilePath["+mSelDlg.getImageFilePath()+"]");

        //mSelDlg.photoUri=data.getData();

        if (data != null) {
            Bitmap bitmap=null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mSelDlg.getPhotoUri());
            } catch (IOException e) {
                Log.d("temp","error:"+e);
                e.printStackTrace();
            }

                if (!ivAddPushImg.isShown()) {
                    ivPushImg.setVisibility(View.GONE);
                    ivAddPushImg.setVisibility(View.VISIBLE);
                }

                if (data != null) {
                        ivAddPushImg.setImageBitmap(bitmap);

                    if (!btnPushPreview.isShown())
                        btnPushPreview.setVisibility(View.VISIBLE);
                }
        }
    }
    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("temp", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private ADTargetSetLinear1.OnGetData mNextTarget = new ADTargetSetLinear1.OnGetData() {
        public void onGetData(String pushTargetSetInfoData, TxtPushTargetInfo pushTargetRetrunValue) {
            pushTargetSetInfo = pushTargetSetInfoData;

            if (targetSetLinear.isShown()) targetSetLinear.setVisibility(View.GONE);
            if (!targetChkLinear.isShown()) targetChkLinear.setVisibility(View.VISIBLE);

            targetChkLinear.setPage(pushTargetSetInfoData, pushTargetRetrunValue);

            llTargetSetUnder.setVisibility(View.GONE);
            llSendTargetUnder.setVisibility(View.VISIBLE);
            txtTargetSet.setBackgroundColor(getResources().getColor(R.color.color_white));
            txtSendTarget.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
        }
    };

    private ADTargetChkLinear2.OnGetData mNextPush = new ADTargetChkLinear2.OnGetData() {
        @Override
        public void onGetData(String pushSendCntData) {
            pushSendCnt = pushSendCntData;
        }
    };

    public boolean DataChk(int mPage) {
        String strMsg = "";

        if (mPage == PAGE_TAGET_CHK) {
            if (!ivAddPushImg.isShown()) {
                Intent intent = new Intent(ADTargetSendActivity.this, DlgBtnActivity.class);
                strMsg = getResources().getString(R.string.str_push_send_img_err);
                intent.putExtra("BtnDlgMsg", strMsg);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return false;
            }

            String strSendCnt = etSendCnt.getText().toString();
            if (strSendCnt.contains(",")) {
                strSendCnt = strSendCnt.replace(",", "");
            }
            if (strSendCnt.equals("") || (!strSendCnt.equals("") && Integer.parseInt(strSendCnt) <= 0)) {
                Toast.makeText(ADTargetSendActivity.this, getResources().getString(R.string.str_push_send_cnt_err), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Intent intentPwd = new Intent(ADTargetSendActivity.this, DlgChkPwdActivity.class);
                intentPwd.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intentPwd, REQUEST_CHK_PWD);
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        //strADIdx
        mTxtAdInfo=targetSetLinear.getTxtAdInfo();
        if(mTxtAdInfo.getStrIdx()!=null || !mTxtAdInfo.getStrIdx().equals("")) {
            strADIdx = mTxtAdInfo.getStrIdx();
        }

        Log.d("temp","*****************onClick strADIdx["+strADIdx+"]*******************");
        int viewId = v.getId();
        if (viewId == R.id.btn_push_send_pre) {
            if (!targetSetLinear.isShown()) targetSetLinear.setVisibility(View.VISIBLE);
            if (targetChkLinear.isShown()) targetChkLinear.setVisibility(View.GONE);

            llTargetSetUnder.setVisibility(View.VISIBLE);
            llSendTargetUnder.setVisibility(View.GONE);
            txtTargetSet.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
            txtSendTarget.setBackgroundColor(getResources().getColor(R.color.color_white));
        } else if (viewId == R.id.btn_push_send) {
            DataChk(PAGE_TAGET_CHK);
        } else if (viewId == R.id.iv_target_set_push || viewId == R.id.iv_target_set_push_add) {
            if (mSelDlg != null && !mSelDlg.isShowing()) mSelDlg.show();
        } else if (viewId == R.id.btn_push_img_preview) {
            String strFilePathName = mSelDlg.getFilePath() + mSelDlg.getFileName();
            if (!strFilePathName.equals("")) {
                Intent intent = new Intent(ADTargetSendActivity.this, ADPushPreviewActivity.class);
                intent.putExtra("PushImgPath", strFilePathName);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    /**
     * PUSH 발송 결과 값 받는 pushHandler
     */
    private Handler pushHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(ADTargetSendActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    Intent intent = new Intent(ADTargetSendActivity.this, DlgBtnActivity.class);
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_push_send_request_ok));
                    startActivityForResult(intent, REQUEST_SEND_OK);
                    break;
            }

            pushHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },500);
        }
    };

    private Bitmap decodeSampledBitmapFromPath(String path, int reqWidth,int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeFile(path, options);
        src = src.createScaledBitmap(src, reqWidth, reqHeight, false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, bos);

        return src;
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
