package com.cashcuk.membership;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.common.AddrUser;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.membership.txtlistdata.TxtListAdapter;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;
import com.cashcuk.sendinfo.SendInfo;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 회원가입
 */
public class MembershipActivity extends Activity implements View.OnClickListener {
    //광고 발송 기본정보
    private LinearLayout llADSendInfo;
    private LayoutInflater inflater;

    private TextView txtCities;
    private TextView txtTown;
    private ListView lvHangOutList; // 시도, 시군구 list

    private TextView txtName;
    private EditText etEmailID;
    private EditText etPwd;
    private EditText etPwdChk;
    private TextView txtPhoneNum; //휴대폰 번호
    private TextView txtOverlapResult; //ID 중복체크 결과 값 d/p
    private EditText etRecommender;

    private String strMode;
    private final String STR_MODE_OVERLAP = "OverLap"; //ID 중복체크
    private final String STR_MODE_AD_SEND_DEFAULT_INFO_TITLE = "ADSendDefaultInfoTitle"; //광고 발송 기본정보 title
    private final String STR_MODE_AD_SEND_DEFAULT_INFO = "ADSendDefaultInfo"; //광고 발송 기본정보
    private final String STR_MODE_MEMBERSHIP = "Membership"; //가입하기

    private final String STR_RADIO_SEX_FEMALE = "F";
    private final String STR_RADIO_SEX_MAN = "M";
    private String strSex=STR_RADIO_SEX_MAN;

    private final String STR_SI_MODE = "S"; //시,도 선택
    private final String STR_GUN_MODE = "G"; //시,군,구 선택
    private String strSiDoMode=STR_SI_MODE;
    private String selSiDoIdx="";
    private String selSiGunGuIdx="";

    //시도, 시군구
    private ArrayList<TxtListDataInfo> arrHangOutCitiesData = new ArrayList<TxtListDataInfo>();
    private ArrayList<TxtListDataInfo> arrHangOutTownData = new ArrayList<TxtListDataInfo>();

    private final int SEND_TYPE = 1;
    private final int SEND_MAIL = 2; //ID 중복 값 체크 시 ID

    private final String STR_MAIL = "umail";

    private int mCodeIndex = -1; //광고발송 기본 정보
    private int mCodeIndexSize = 0; //광고발송 기본 정보 총 개수
    private String[] strADSendTitleIdx; // title idx

    private ArrayList<TxtListDataInfo> arrADSendDefaultInfoTemp = new ArrayList<TxtListDataInfo>();
    private ArrayList<TxtListDataInfo> arrADSendDefaultInfoTitleData = new ArrayList<TxtListDataInfo>();

    private final int DIALOG_MODE_AD_SEND_INFO = 0;
    private final int DIALOG_MODE_SI_DO = 1;
    private final int DIALOG_MODE_SI_GUN = 2;

    //생년월일
    private TextView txtYear;
    private TextView txtMonth;
    private TextView txtDay;

    //회원가입 시 서버에 전달 데이터
    private String strName = "";
    private String strEmailID = "";
    private String strPwd = "";
    private String strPhoneNum = "";
    private String strAge = ""; //나이
    private String strBitrhDate = "";
    private String strSiDo = "";
    private String strSiGunGu = "";
    private String strRecommender = ""; //추천인
    private String strDI = ""; //DI
    private String strCI = ""; //CI

    //회원 가입 시 전달 data
    private final String STR_MEMBERSHIP_NAME = "unm";
    private final String STR_MEMBERSHIP_MAIL = "umail";
    private final String STR_MEMBERSHIP_PWD = "upwd";
    private final String STR_MEMBERSHIP_HP = "uhp";
    private final String STR_MEMBERSHIP_SEX = "usex";
    private final String STR_MEMBERSHIP_AGE = "uage";
    private final String STR_MEMBERSHIP_BIRTH_DATE = "ubirth";
    private final String STR_MEMBERSHIP_TYPE = "utype";
    private final String STR_MEMBERSHIP_SI = "uaddr_si";
    private final String STR_MEMBERSHIP_GUN = "uaddr_gun";
    private final String STR_MEMBERSHIP_RECOMMENDER = "urecom";
    private final String STR_MEMBERSHIP_DI = "DI";
    private final String STR_MEMBERSHIP_CI = "CI";
    private final String STR_MEMBERSHIP_AD_SEND_INFO = "ucodes";

    private final int INDEX_MEMBERSHIP_NAME = 1;
    private final int INDEX_MEMBERSHIP_MAIL = 2;
    private final int INDEX_MEMBERSHIP_PWD = 3;
    private final int INDEX_MEMBERSHIP_HP = 4;
    private final int INDEX_MEMBERSHIP_SEX = 5;
    private final int INDEX_MEMBERSHIP_AGE = 6;
    private final int INDEX_MEMBERSHIP_BIRTH_DATE = 7;
    private final int INDEX_MEMBERSHIP_SI = 8;
    private final int INDEX_MEMBERSHIP_GUN = 9;
    private final int INDEX_MEMBERSHIP_RECOMMENDER = 10;
    private final int INDEX_MEMBERSHIP_DI = 11;
    private final int INDEX_MEMBERSHIP_CI = 12;
    private final int INDEX_MEMBERSHIP_AD_SEND_INFO = 13;

    private final int PATTERN_CHK_MODE_EMAIL = 0; //정규식 체크 (이메일)
    private final int PATTERN_CHK_MODE_PWD = 1; //정규식 체크 (비밀번호)

    private ArrayList<TxtListDataInfo>[] arryADSendDefaultInfo;
    private int mClickId; //클릭 된 광고기본발송정보

    private final int REQUEST_CODE_PWD = 888;
    private final int REQUEST_CODE_PWD_CHK = 777;
    private boolean isSelSiGun = false;

    private InputMethodManager imm;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = null;
            switch (msg.what) {
                case StaticDataInfo.RESULT_NO_INPUT_RECOMMEND:
                    intent = new Intent(MembershipActivity.this, DlgBtnActivity.class);
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_no_input_recommender_err));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case StaticDataInfo.RESULT_NO_RECOMMEND:
                    intent = new Intent(MembershipActivity.this, DlgBtnActivity.class);
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_recommender_err));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(strMode.equals(STR_MODE_OVERLAP)) {
                        strEmailID = etEmailID.getText().toString();
                        txtOverlapResult.setVisibility(View.VISIBLE);
                        txtOverlapResult.setText(getResources().getString(R.string.str_use_id));
                        return;
                    }else if(strMode.equals(STR_MODE_MEMBERSHIP)){
                        intent = new Intent(MembershipActivity.this, MembershipSuccessActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    break;
                case StaticDataInfo.RESULT_OVERLAP_ERR:
                    if(strMode.equals(STR_MODE_OVERLAP)) {
                        txtOverlapResult.setVisibility(View.VISIBLE);
                        txtOverlapResult.setText(getResources().getString(R.string.str_not_use_id));
                    }else{
                        intent = new Intent(MembershipActivity.this, DlgBtnActivity.class);
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_member_err));
                    }
                    break;
                case StaticDataInfo.RESULT_SI_DO:
                    if(arrHangOutCitiesData==null){
                        arrHangOutCitiesData = new ArrayList<TxtListDataInfo>();
                    }
                    arrHangOutCitiesData.addAll((ArrayList<TxtListDataInfo>)msg.obj);
                    break;
                case StaticDataInfo.RESULT_SI_GUN_GU:
                    if(arrHangOutTownData==null){
                        arrHangOutTownData = new ArrayList<TxtListDataInfo>();
                    }
                    arrHangOutTownData.clear();
                    arrHangOutTownData.addAll((ArrayList<TxtListDataInfo>)msg.obj);
                    break;
                case StaticDataInfo.RESULT_SEND_AD_INFO:
                    if(txtSendInfoTitle!=null && !txtSendInfoTitle.isShown()) txtSendInfoTitle.setVisibility(View.VISIBLE);
                    arrADSendDefaultInfoTemp.clear();
                    arrADSendDefaultInfoTemp = (ArrayList<TxtListDataInfo>)msg.obj;

                    if(msg.arg1<0){
                        if(arrADSendDefaultInfoTemp!=null && arrADSendDefaultInfoTemp.size()>0){
                            mCodeIndexSize = arrADSendDefaultInfoTemp.size();
                            strADSendTitleIdx = new String[mCodeIndexSize];
                            sendSubIdxTemp = new String[mCodeIndexSize];
                            for(int i=0; i<sendSubIdxTemp.length; i++){
                                sendSubIdxTemp[i] = "0";
                            }

                            arrADSendDefaultInfoTitleData = new ArrayList<TxtListDataInfo>();
                            arrADSendDefaultInfoTitleData.addAll(arrADSendDefaultInfoTemp);

                            for(int i=0; i<mCodeIndexSize; i++){
                                LinearLayout llADSendInfoView = (LinearLayout) inflater.inflate(R.layout.view_ad_send_info, null);
                                llADSendInfoView.setId(i);

                                TextView txtADSendInfoTitle = (TextView) llADSendInfoView.findViewById(R.id.txt_ad_send_info_title);
                                txtADSendInfoTitle.setText(arrADSendDefaultInfoTitleData.get(i).getStrMsg());
                                strADSendTitleIdx[i] = arrADSendDefaultInfoTitleData.get(i).getStrIdx();

                                TextView txtADSendInfo = (TextView) llADSendInfoView.findViewById(R.id.txt_ad_send_info_msg);
                                txtADSendInfo.setTag((int)i);
                                txtADSendInfo.setOnClickListener(mClickADSendInfo);

                                llADSendInfo.addView(llADSendInfoView);
                            }
                            new SendInfo(MembershipActivity.this, handler, arrADSendDefaultInfoTitleData.get(mCodeIndex).getStrIdx(), mCodeIndex, "");
                        }
                    }else if(arrADSendDefaultInfoTemp!=null && arrADSendDefaultInfoTemp.size()>0) {
                        if(mCodeIndex<mCodeIndexSize) {
                            if(arryADSendDefaultInfo==null) arryADSendDefaultInfo = new ArrayList[mCodeIndexSize];
                            ArrayList<TxtListDataInfo> arrADSendDefaultInfo = new ArrayList<TxtListDataInfo>();
                            arrADSendDefaultInfo.addAll(arrADSendDefaultInfoTemp);

                            arryADSendDefaultInfo[mCodeIndex] = arrADSendDefaultInfo;
                        }
                        mCodeIndex++;
                        if(mCodeIndex<mCodeIndexSize && arrADSendDefaultInfoTitleData!=null && arrADSendDefaultInfoTitleData.size()>0) {
                            new SendInfo(MembershipActivity.this, handler, arrADSendDefaultInfoTitleData.get(mCodeIndex).getStrIdx(), mCodeIndex, "");
                        }
                    }
                    break;
                case StaticDataInfo.RESULT_NO_SIGUN:
                    isSelSiGun = true;
                    break;
            }
        }
    };

    private TextView txtSendInfoTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        MainTitleBar mMainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_refresh)).setVisibility(View.GONE);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_home)).setVisibility(View.GONE);

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_membership));
        ((Button) findViewById(R.id.btn_overlap)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_membership)).setOnClickListener(this);

        txtSendInfoTitle = (TextView) findViewById(R.id.txt_send_info_title);
        txtName = (TextView) findViewById(R.id.txt_name);
        etEmailID = (EditText) findViewById(R.id.et_email_id);
        txtPhoneNum = (TextView) findViewById(R.id.txt_phone_num);
        txtOverlapResult = (TextView) findViewById(R.id.txt_id_overlap_chk_result);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        etPwdChk = (EditText) findViewById(R.id.et_pwd_chk);
        etRecommender = (EditText) findViewById(R.id.et_recommender);
        etEmailID.addTextChangedListener(txtWatch);
        etPwd.setOnFocusChangeListener(mFocusChange);
        etPwdChk.setOnFocusChangeListener(mFocusChange);

        txtYear = (TextView) findViewById(R.id.txt_year);
        txtMonth = (TextView) findViewById(R.id.txt_month);
        txtDay = (TextView) findViewById(R.id.txt_day);
        llADSendInfo = (LinearLayout) findViewById(R.id.ll_ad_send_info);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //사는 곳
        txtCities = (TextView) findViewById(R.id.txt_cities);
        txtTown = (TextView) findViewById(R.id.txt_town);
        txtCities.setOnClickListener(this);
        txtTown.setOnClickListener(this);

        RadioGroup rgSex = (RadioGroup) findViewById(R.id.rg_sex);

        Intent intent = getIntent();
        txtName.setText(intent.getStringExtra("Name"));
        txtPhoneNum.setText(intent.getStringExtra("PhoneNum"));
        String[] strBirthDay = new String[3];
        strBirthDay = intent.getStringExtra("BirthDate").split("-");
        txtYear.setText(strBirthDay[0]);
        txtMonth.setText(strBirthDay[1]);
        txtDay.setText(strBirthDay[2]);
        String strGetSex = intent.getStringExtra("Sex");
        if(strGetSex.equals("01")){
            strSex = STR_RADIO_SEX_MAN;
            rgSex.check(R.id.rb_man);
        }else if(strGetSex.equals("02")){
            strSex = STR_RADIO_SEX_FEMALE;
            rgSex.check(R.id.rb_woman);
        }
        strDI = intent.getStringExtra("DI");
        strCI = intent.getStringExtra("CI");


        new SendInfo(MembershipActivity.this, handler, STR_MODE_AD_SEND_DEFAULT_INFO_TITLE, mCodeIndex++, StaticDataInfo.STRING_M);
        new AddrUser(MembershipActivity.this, handler, STR_SI_MODE, selSiDoIdx);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.ll_bg));
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


    public View.OnClickListener mClickADSendInfo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mClickId = (int)v.getTag();
            OpenDialog(DIALOG_MODE_AD_SEND_INFO);
        }
    };

    private boolean isFocus = false;
    public View.OnFocusChangeListener mFocusChange = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Intent i = new Intent(MembershipActivity.this, DlgBtnActivity.class);
            i.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));

            if(!hasFocus) {
                if (v.getId() == R.id.et_pwd && !isFocus) {
                    strPwd = etPwd.getText().toString();
                    if (!strPwd.trim().equals("")){
                        if(strPwd.length() < 6 || strPwd.length() > 16 || !checkPattern(PATTERN_CHK_MODE_PWD, strPwd)) {
                            isFocus = true;
                            i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_pwd_type_err));
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivityForResult(i, REQUEST_CODE_PWD);
                        }
                    }else{
                        isFocus = false;
                    }
                } else if (v.getId() == R.id.et_pwd_chk && !isFocus) {
                    if (!etPwdChk.getText().toString().equals("") && !strPwd.equals(etPwdChk.getText().toString())) {
                        isFocus = true;
                        i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_input_pwd_chk));
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(i, REQUEST_CODE_PWD_CHK);
                    }else{
                        isFocus = false;
                    }
                }
            }
        }
    };

    public TextWatcher txtWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(txtOverlapResult.isShown()) txtOverlapResult.setVisibility(View.GONE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * e-mail 형식이 맞는지 체크함.
     * @param email
     */
    public boolean ChkEmailType(String email){

        if(checkPattern(PATTERN_CHK_MODE_EMAIL, email)) {
            return true;
        } else {
            Toast.makeText(MembershipActivity.this, R.string.str_email_type_err, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 추천인 e-mail 형식이 맞는지 체크함.
     * @param email
     */
    public boolean ChkRecommenderEmailType(String email){

        if(checkPattern(PATTERN_CHK_MODE_EMAIL, email)) {
            return true;
        } else {
            Toast.makeText(MembershipActivity.this, R.string.str_recommender_type_err, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 이메일, 비밀번호 정규식 체크
     * @param patternMode : 이메일 or 비밀번호 mode
     * @param strPattern : 값
     * @return true: 형식에 맞음, false: 형식에 맞지 않음
     */
    private boolean checkPattern(int patternMode, String strPattern)
    {
        Pattern p = null;

        if(patternMode == PATTERN_CHK_MODE_EMAIL) {
            p = Pattern.compile("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$");
        }else if(patternMode == PATTERN_CHK_MODE_PWD){
            p = Pattern.compile("([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~])|([!,@,#,$,%,^,&,*,?,_,~].*[a-zA-Z0-9])");
        }
        Matcher m = p.matcher(strPattern);

        if(patternMode==PATTERN_CHK_MODE_PWD) {
            if (m.find()) {
                return true;
            } else {
                return false;
            }
        }

        return m.matches();
    }

    /**
     * 만 나이계산
     * @return age
     */
    public String Age(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(txtYear.getText().toString()));
        cal.set(Calendar.MONTH, Integer.parseInt(txtMonth.getText().toString()));
        cal.set(Calendar.DATE, Integer.parseInt(txtDay.getText().toString()));

        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - cal.get(Calendar.YEAR);
        if ((cal.get(Calendar.MONTH) > now.get(Calendar.MONTH))
                || (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                && cal.get(Calendar.DAY_OF_MONTH) > now.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return String.valueOf(age);
    }

    /**
     * 가입하기 클릭 시 회원 정보 확인 및 서버로 전달 데이터
     */
    public void MemberShipInfo(){
        boolean bSendInfoChk = true;
        if(!txtOverlapResult.isShown()){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_overlap_chk_err), Toast.LENGTH_SHORT).show();
            return;
        }else if(txtOverlapResult.getText().toString().equals(getResources().getString(R.string.str_not_use_id))){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_input_again_id), Toast.LENGTH_SHORT).show();
            return;
        }

        strName = txtName.getText().toString();
        strPwd = etPwd.getText().toString();
        strPhoneNum = txtPhoneNum.getText().toString();
        strBitrhDate = txtYear.getText().toString()+"-"+txtMonth.getText().toString()+"-"+txtDay.getText().toString();
        strAge = Age();
        strSiDo = txtCities.getText().toString();
        strSiGunGu = txtTown.getText().toString();
        strRecommender = etRecommender.getText().toString();

        if(strName.trim().equals("")){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_input_name), Toast.LENGTH_SHORT).show();
            return;
        }else if(strEmailID.trim().equals("")){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_input_id_err), Toast.LENGTH_SHORT).show();
            return;
        }else if(strPwd.trim().equals("")){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_input_pwd_err), Toast.LENGTH_SHORT).show();
            return;
        }else if(!strPwd.equals(etPwdChk.getText().toString())){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_input_pwd_chk), Toast.LENGTH_SHORT).show();
            return;
//        }else if(strPhoneNum.trim().equals("")){
//            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.), Toast.LENGTH_SHORT).show();
//            return;
        }else if(strSiDo.trim().equals("")){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_no_residence), Toast.LENGTH_SHORT).show();
            return;
        } else if(!isSelSiGun && strSiGunGu.trim().equals("")){
            Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_no_residence), Toast.LENGTH_SHORT).show();
            return;
        }else if(strPwd.length()<6 || strPwd.length()>16 || !checkPattern(PATTERN_CHK_MODE_PWD, strPwd)) {
            Intent i = new Intent(MembershipActivity.this, DlgBtnActivity.class);
            i.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
            i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_pwd_type_err));
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return;

        }else if(bSendInfoChk){
            for(int i=0; i<llADSendInfo.getChildCount(); i++){
                TextView txtSendInfo = (TextView) llADSendInfo.getChildAt(i).findViewById(R.id.txt_ad_send_info_msg);
                if(txtSendInfo.getText().equals("")){
                    Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_sel_ad_send_default_info_err), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            bSendInfoChk = false;
        }
        if(!strRecommender.trim().equals("")){
            if(!ChkRecommenderEmailType(strRecommender)){
                return;
            }
        }

        strMode = STR_MODE_MEMBERSHIP;
        ArrayList<String> arrADSendIdx = new ArrayList<String>();
        arrADSendIdx.clear();
        for(int i=0; i<mCodeIndexSize; i++){
            arrADSendIdx.add(arrADSendDefaultInfoTitleData.get(i).getStrIdx()+getResources().getString(R.string.str_category_item_gubun)+sendSubIdxTemp[i]);
        }

        final String url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_member_member);

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(INDEX_MEMBERSHIP_NAME, strName);
        k_param.put(INDEX_MEMBERSHIP_MAIL, strEmailID);
        k_param.put(INDEX_MEMBERSHIP_PWD, strPwd);
        k_param.put(INDEX_MEMBERSHIP_HP, strPhoneNum);
        k_param.put(INDEX_MEMBERSHIP_SEX, strSex);
        k_param.put(INDEX_MEMBERSHIP_AGE, strAge);
        k_param.put(INDEX_MEMBERSHIP_BIRTH_DATE, strBitrhDate);
        k_param.put(INDEX_MEMBERSHIP_SI, selSiDoIdx);
        k_param.put(INDEX_MEMBERSHIP_GUN, selSiGunGuIdx);
        k_param.put(INDEX_MEMBERSHIP_RECOMMENDER, strRecommender);
        k_param.put(INDEX_MEMBERSHIP_DI, strDI);
        k_param.put(INDEX_MEMBERSHIP_CI, strCI);
        String tmp = new String();
        for(int i=0; i<arrADSendIdx.size(); i++){
            tmp += "["+ arrADSendIdx.get(i) +"]"+",";
        }
        k_param.put(INDEX_MEMBERSHIP_AD_SEND_INFO, tmp);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new MembershipTask().execute(strTask);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_overlap) {
            //ID 중복체크
            if(etEmailID.getText().toString().trim().equals("")) {
                Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_input_id_err), Toast.LENGTH_SHORT).show();
            }else if(ChkEmailType(etEmailID.getText().toString())) {
                ChkIDOverLap();
            }
        } else if (viewId == R.id.txt_cities) {
            // 시,도
            OpenDialog(DIALOG_MODE_SI_DO);
        } else if (viewId == R.id.txt_town) {
            // 시,군,구
            if(isSelSiGun){
                Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_no_sigun), Toast.LENGTH_SHORT).show();
            }else if(txtCities!=null && !txtCities.getText().equals("")){
                OpenDialog(DIALOG_MODE_SI_GUN);
            }else{
                Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_selector_cities), Toast.LENGTH_SHORT).show();
            }
        } else if (viewId == R.id.btn_membership) {
            MemberShipInfo();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode){
                case REQUEST_CODE_PWD:
                    etPwd.requestFocus();
                    etPwd.setFocusable(true);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //키보드 보이게 하는 부분
                            imm.showSoftInput(etPwd, InputMethodManager.SHOW_IMPLICIT);
                            isFocus = false;
                        }
                    }, 300);
                    break;
                case REQUEST_CODE_PWD_CHK:
                    etPwdChk.requestFocus();
                    etPwdChk.setFocusable(true);
                    etPwdChk.setText("");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //키보드 보이게 하는 부분
                            imm.showSoftInput(etPwdChk, InputMethodManager.SHOW_IMPLICIT);
                            isFocus = false;
                        }
                    }, 300);
                    break;
            }
        }
    }

    public RadioGroup.OnCheckedChangeListener mChkChange = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_man) {
                strSex = STR_RADIO_SEX_MAN;
            } else if (checkedId == R.id.rb_woman) {
                strSex = STR_RADIO_SEX_FEMALE;
            }
        }
    };

    /**
     * ID 중복체크
     */
    public void ChkIDOverLap() {
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_member_id);

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(SEND_TYPE, STR_MODE_OVERLAP);
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(SEND_MAIL, etEmailID.getText().toString());

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
            strMode = params[SEND_TYPE];

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                if (strMode.equals(STR_MODE_OVERLAP)) {
                    listParams.add(new BasicNameValuePair(STR_MAIL, params[SEND_MAIL]));
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
            if (strMode != null && strMode.equals(STR_MODE_OVERLAP)) {
                ResultOverLap(result);
            }
        }
    }

    /**
     * ID 중복 체크 결과 값
     * @param result 서버에서 받은 결과 값
     */
    public void ResultOverLap(String result){
        Message msg = new Message();
        if(result.endsWith("out") || result.equals("") || result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_ERR))){
            msg.what = StaticDataInfo.RESULT_CODE_ERR;
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
            msg.what = StaticDataInfo.RESULT_CODE_200;
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_OVERLAP_ERR))){
            msg.what = StaticDataInfo.RESULT_OVERLAP_ERR;
        }

        if(handler!=null && msg!=null) {
            handler.sendMessage(msg);
        }
    }

    private Dialog mDialog;
    private String[] sendSubIdxTemp;
    public void OpenDialog(final int mDlgMode){
        mDialog = new Dialog(MembershipActivity.this);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDialog.getWindow().setAttributes((WindowManager.LayoutParams) params);
        mDialog.setContentView(R.layout.dlg_txt_list);

        lvHangOutList = (ListView) mDialog.findViewById(R.id.lv_txt);
        TextView txtTitle = (TextView) mDialog.findViewById(R.id.txt_dlg_title);
        final LinearLayout llADSendInfoID = (LinearLayout) findViewById(mClickId);

        switch(mDlgMode){
            case DIALOG_MODE_AD_SEND_INFO:
                if(arryADSendDefaultInfo!=null){
                    TextView txtADSendInfoTitle = (TextView) llADSendInfoID.findViewById(R.id.txt_ad_send_info_title);
                    txtTitle.setText(txtADSendInfoTitle.getText().toString());

                    ArrayList<TxtListDataInfo> arrADSendInfo = new ArrayList<TxtListDataInfo>();
                    ArrayList<TxtListDataInfo> arrADSendInfoTemp = new ArrayList<TxtListDataInfo>();
                        for(int j=0; j<arryADSendDefaultInfo[mClickId].size(); j++) {
                            arrADSendInfoTemp.add(arryADSendDefaultInfo[mClickId].get(j));
                        }
                    arrADSendInfo.addAll(arrADSendInfoTemp);
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrADSendInfo));
                }
                break;
            case DIALOG_MODE_SI_DO:
                isSelSiGun = false;
                if(arrHangOutCitiesData!=null){
                    txtTitle.setText(getResources().getString(R.string.str_cities_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrHangOutCitiesData));
                }
                break;
            case DIALOG_MODE_SI_GUN:
                if(arrHangOutTownData!=null && arrHangOutTownData.size()>0){
                    txtTitle.setText(getResources().getString(R.string.str_town_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrHangOutTownData));
                }else{
                    Toast.makeText(MembershipActivity.this, getResources().getString(R.string.str_selector_cities), Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }

        lvHangOutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mDlgMode) {
                    case DIALOG_MODE_AD_SEND_INFO:
                        TextView txtADSendInfo = (TextView) llADSendInfoID.findViewById(R.id.txt_ad_send_info_msg);
                        txtADSendInfo.setText(arryADSendDefaultInfo[mClickId].get(position).getStrMsg());
                        sendSubIdxTemp[mClickId] = arryADSendDefaultInfo[mClickId].get(position).getStrIdx();
                        break;
                    case DIALOG_MODE_SI_DO:
                        txtCities.setText(arrHangOutCitiesData.get(position).getStrMsg());

                        txtTown.setText("");
                        selSiDoIdx = arrHangOutCitiesData.get(position).getStrIdx();
                        strSiDoMode = STR_GUN_MODE;

                        new AddrUser(MembershipActivity.this, handler, strSiDoMode, selSiDoIdx);
                        break;
                    case DIALOG_MODE_SI_GUN:
                        txtTown.setText(arrHangOutTownData.get(position).getStrMsg());
                        selSiGunGuIdx = arrHangOutTownData.get(position).getStrIdx();
                        break;
                }
                if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            }
        });

        mDialog.show();
    }

    /**
     * 회원가입 데이터 서버에 전달
     */
    private class MembershipTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);
                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_NAME, params[INDEX_MEMBERSHIP_NAME]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_MAIL, params[INDEX_MEMBERSHIP_MAIL]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_PWD, params[INDEX_MEMBERSHIP_PWD]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_HP, params[INDEX_MEMBERSHIP_HP]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_SEX, params[INDEX_MEMBERSHIP_SEX]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_AGE, params[INDEX_MEMBERSHIP_AGE]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_BIRTH_DATE, params[INDEX_MEMBERSHIP_BIRTH_DATE]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_SI, params[INDEX_MEMBERSHIP_SI]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_GUN, params[INDEX_MEMBERSHIP_GUN]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_RECOMMENDER, params[INDEX_MEMBERSHIP_RECOMMENDER]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_DI, params[INDEX_MEMBERSHIP_DI]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_CI, params[INDEX_MEMBERSHIP_CI]));
                listParams.add(new BasicNameValuePair(STR_MEMBERSHIP_AD_SEND_INFO, params[INDEX_MEMBERSHIP_AD_SEND_INFO]));

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

            if(result.equals("") || result.startsWith("<")){
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
                handler.sendMessage(msg);
                return;
            }

            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200)) || result.equals(String.valueOf(StaticDataInfo.RESULT_NO_RECOMMEND)) || result.equals(String.valueOf(StaticDataInfo.RESULT_NO_INPUT_RECOMMEND))) {
                msg.what = Integer.parseInt(result);
            }else{
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
            }

            if(handler!=null && msg!=null) {
                handler.sendMessage(msg);
            }
        }
    }
}
