package com.cashcuk.membership;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.setting.AccountSetActivity;
import com.cashcuk.setting.FindPwdActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 휴대폰 본인인증 (모듈연동)
 */
public class MobileAuthenticationWebActivity extends Activity implements View.OnTouchListener {
    private WebView wvAuthentication;
    private boolean bPointInput=false;
    private LinearLayout llProgress;
    private LinearLayout ll_back;

    private final int REQUEST_CHK_ERR = 999;
    private int mReturnCd=1;                                                                        //본인인증후 넘어가야 할페이지 정의 1:회원가입,2:비밀번호찾기(Setting),3:비밀번호찾기(로그인전),4:휴대폰번호 수정하기

    Intent mIntent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_authentication_web);
        CheckLoginService.mActivityList.add(this);

        Intent getIntent = getIntent();

        mIntent= getIntent();
        bPointInput = getIntent.getBooleanExtra("IsPointInputMode", false);

        mReturnCd=getIntent.getIntExtra("ReturnCd",1);
        Log.d("temp","*******mReturnCd["+mReturnCd+"]********");

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        ll_back    = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        wvAuthentication = (WebView) findViewById(R.id.wv_authentication);

        wvAuthentication.getSettings().setJavaScriptEnabled(true);
        wvAuthentication.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wvAuthentication.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 웹뷰가 캐시를 사용하지 않도록 설정
        wvAuthentication.getSettings().setSupportMultipleWindows(false);
        wvAuthentication.getSettings().setUseWideViewPort(true);
        wvAuthentication.getSettings().setLoadWithOverviewMode(true);
        wvAuthentication.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        wvAuthentication.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        });
        wvAuthentication.setWebChromeClient(new webViewChrome());
        wvAuthentication.addJavascriptInterface(new AndroidBridge(), getResources().getString(R.string.str_cashcuk));

        wvAuthentication.loadUrl(getResources().getString(R.string.str_new_url)+"kcp_start");
//        wvAuthentication.loadUrl("http://103.251.105.176:8080/kcp_start_m");

    }

    class webViewChrome extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if(wvAuthentication!=null){
            wvAuthentication.clearHistory();
            wvAuthentication.clearCache(true);
            wvAuthentication.clearView();
        }*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class AndroidBridge {
        @JavascriptInterface //미 추가 시 4.2버전부터 오류 발생함.
        /**
         * 휴대폰 인증 시 결과 값
         * @param strYN Y: 인증 성공, Y이외의 값 인증 실패
         * @param strName
         * @param strPhoneNum
         * @param strBirthDate
         * @param strSex 01: 남자, 02: 여자
         */
        public void getUserData(final String strYN, final String strName, final String strPhoneNum, final String strBirthDate, final String strSex, final String strDI, final String strCI, final String isAuthentication) {
            Intent intent = null;

            switch (mReturnCd){
                case 1:
                    //회원가입 본인인증
                    if(isAuthentication.equals(StaticDataInfo.STRING_N)){
                        finish();
                    }else if(strYN.equals(StaticDataInfo.STRING_N) || strName.equals("") || strPhoneNum.equals("") || strBirthDate.equals("") || strSex.equals("") || strDI.equals("") || strCI.equals("")){
                        intent = new Intent(MobileAuthenticationWebActivity.this, DlgBtnActivity.class);
                        intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_chk_err));

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else if (strYN.equals("Y")) {
                        String phoneUtil = PhoneNumberUtils.formatNumber(strPhoneNum); //폰 번호 format 변경 (010-0000-0000)

                        //생년월일 형식 변경 1900-01-01
                        try {
                            SimpleDateFormat curFormater = new SimpleDateFormat("yyyyMMdd");
                            Date dateObj = curFormater.parse(strBirthDate);
                            SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd");
                            String bitrhDate = postFormater.format(dateObj);
                            String[] arrBitrhDate = new String[3];
                            arrBitrhDate = bitrhDate.split("-");
                            int mAge = Age(Integer.parseInt(arrBitrhDate[0]), Integer.parseInt(arrBitrhDate[1]), Integer.parseInt(arrBitrhDate[2]));
                            if(mAge<14){
                                intent = new Intent(MobileAuthenticationWebActivity.this, DlgBtnActivity.class);
                                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_use_age));
                            }else {
                                intent = new Intent(MobileAuthenticationWebActivity.this, MembershipActivity.class);
                                intent.putExtra("Name", strName);
                                intent.putExtra("PhoneNum", phoneUtil);
                                intent.putExtra("BirthDate", bitrhDate);
                                intent.putExtra("Sex", strSex);
                                intent.putExtra("DI", strDI);
                                intent.putExtra("CI", strCI);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(intent!=null){
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }else if(strYN.equals("C")){ //중복가입
                        if (bPointInput) {
                            Intent i = (getIntent());
                            i.putExtra("DI", strDI);
                            i.putExtra("CI", strCI);
                            setResult(RESULT_OK, i);
                            finish();
                            return;
                        } else {
                            intent = new Intent(MobileAuthenticationWebActivity.this, DlgBtnActivity.class);
                            intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_overlap_member));
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivityForResult(intent, REQUEST_CHK_ERR);
                        }
                    }
                    break;
                case 2:
                    //비밀번호찾기 본인인증
                    if(isAuthentication.equals(StaticDataInfo.STRING_N)){
                        finish();
                    }else if(strYN.equals(StaticDataInfo.STRING_N) || strName.equals("") || strPhoneNum.equals("") || strBirthDate.equals("") || strSex.equals("") || strDI.equals("") || strCI.equals("")){
                        intent = new Intent(MobileAuthenticationWebActivity.this, DlgBtnActivity.class);
                        intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_chk_err));

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else if (strYN.equals("Y") || strYN.equals("C")) {
                        String phoneUtil = PhoneNumberUtils.formatNumber(strPhoneNum); //폰 번호 format 변경 (010-0000-0000)
                        intent = new Intent(MobileAuthenticationWebActivity.this, FindPwdActivity.class);
                        intent.putExtra("PhoneNum", phoneUtil);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    break;
                case 3:
                    //로그인전 비밀번호찾기  본인인증
                    if(isAuthentication.equals(StaticDataInfo.STRING_N)){
                        finish();
                    }else if(strYN.equals(StaticDataInfo.STRING_N) || strName.equals("") || strPhoneNum.equals("") || strBirthDate.equals("") || strSex.equals("") || strDI.equals("") || strCI.equals("")){
                        intent = new Intent(MobileAuthenticationWebActivity.this, DlgBtnActivity.class);
                        intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_chk_err));

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else if (strYN.equals("Y") || strYN.equals("C")) {
                        String phoneUtil = PhoneNumberUtils.formatNumber(strPhoneNum); //폰 번호 format 변경 (010-0000-0000)
                        mIntent.putExtra("PHONE_NUM", phoneUtil);
                        setResult(RESULT_OK, mIntent);
                        finish();
                    }
                    break;
                case 4:
                    //휴대폰번호 수정하기  본인인증
                    if(isAuthentication.equals(StaticDataInfo.STRING_N)){
                        finish();
                    }else if(strYN.equals(StaticDataInfo.STRING_N) || strName.equals("") || strPhoneNum.equals("") || strBirthDate.equals("") || strSex.equals("") || strDI.equals("") || strCI.equals("")){
                        intent = new Intent(MobileAuthenticationWebActivity.this, DlgBtnActivity.class);
                        intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_chk_err));

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else if (strYN.equals("Y") || strYN.equals("C")) {
                        String phoneUtil = PhoneNumberUtils.formatNumber(strPhoneNum); //폰 번호 format 변경 (010-0000-0000)
                        mIntent.putExtra("PHONE_NUM", phoneUtil);
                        setResult(RESULT_OK, mIntent);
                        finish();
                    }
                    break;
                default:
                    break;
            }



        }
    }

    /**
     * 만 나이계산
     * @return age
     */
    public int Age(int year, int month, int day){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, day);

        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - cal.get(Calendar.YEAR);
        if ((cal.get(Calendar.MONTH) > now.get(Calendar.MONTH))
                || (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                && cal.get(Calendar.DAY_OF_MONTH) > now.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CHK_ERR){
                finish();
            }
        }
    }
    private Dialog mDlg;
    private Button btn1;
    private Button btn2;
    public void ErrDialog(){
        mDlg = new Dialog(this);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDlg.setContentView(R.layout.dlg_btn_layout);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ((TextView) mDlg.findViewById(R.id.txt_dlg_msg)).setText("123456");
        LinearLayout ll1 = (LinearLayout) mDlg.findViewById(R.id.ll1); //오른쪽 버튼
        btn1 = (Button) findViewById(R.id.btn1); //오른쪽 버튼
        TextView txt1 = (TextView) findViewById(R.id.txt1); //오른쪽 버튼
        ll1.setOnTouchListener(this);
        btn1.setOnTouchListener(this);

        LinearLayout llBtnDivider = (LinearLayout) findViewById(R.id.ll_btn_divider);
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.ll2); //왼쪽 버튼
        btn2 = (Button) findViewById(R.id.btn2); //왼쪽 버튼
        TextView txt2 = (TextView) findViewById(R.id.txt2); //왼쪽 버튼
        ll2.setOnTouchListener(this);
        btn2.setOnTouchListener(this);

        llBtnDivider.setVisibility(View.VISIBLE);
        ll2.setVisibility(View.VISIBLE);

        mDlg.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll1 || v.getId() == R.id.btn1){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                mDlg.dismiss();
                finish();
            }
            return true;
        }else if(v.getId() == R.id.ll2 || v.getId() == R.id.btn2){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btn2.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                mDlg.dismiss();
                finish();
            }
            return true;
        }

        return false;
    }
}
