package com.cashcuk.membership;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.TitleBar;
import com.cashcuk.dialog.DlgBtnActivity;

/**
 * 약관동의
 */
public class TermsAgreeActivity extends Activity implements View.OnClickListener {
    private CheckBox chkAgreeAll;
    private CheckBox chkAgree1;
    private CheckBox chkAgree2;

    private final int NOT_TERMS_AGREE = 0;
    private final int MOBILE_AUTHENTICATION = 1;

    private final int REQUEST_OK = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_agree);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        MainTitleBar mMainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_refresh)).setVisibility(View.GONE);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_home)).setVisibility(View.GONE);
        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_terms_agree_title));

        chkAgreeAll = (CheckBox) findViewById(R.id.chk_agree_all);
        chkAgree1 = (CheckBox) findViewById(R.id.chk_agree1); //이용약관
        chkAgree2 = (CheckBox) findViewById(R.id.chk_agree2); //개인정보 수집.이용.제 3자 제공동의

        chkAgreeAll.setOnClickListener(this);
        chkAgree1.setOnClickListener(this);
        chkAgree2.setOnClickListener(this);

        TextView txtAgree1 = (TextView) findViewById(R.id.txt_agree1);
        TextView txtAgree2 = (TextView) findViewById(R.id.txt_agree2);
        txtAgree1.setOnClickListener(this);
        txtAgree2.setOnClickListener(this);

        Button btnAgreeOk = (Button) findViewById(R.id.btn_agree_ok);
        Button btnAgreeCancel = (Button) findViewById(R.id.btn_agree_cancel);
        btnAgreeOk.setOnClickListener(this);
        btnAgreeCancel.setOnClickListener(this);

        WebView wvTerms1 = (WebView) findViewById(R.id.wv_terms_1);
        WebView wvTerms2 = (WebView) findViewById(R.id.wv_terms_2);
        wvTerms1.loadUrl(getResources().getString(R.string.str_url_join_terms1));
        wvTerms2.loadUrl(getResources().getString(R.string.str_url_join_terms2));

        wvTerms1.setBackgroundColor(0);
        wvTerms2.setBackgroundColor(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.fl_bg));
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
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.chk_agree_all) {
            chkAgree1.setChecked(chkAgreeAll.isChecked());
            chkAgree2.setChecked(chkAgreeAll.isChecked());
        } else if (viewId == R.id.txt_agree1 || viewId == R.id.txt_agree2 || viewId == R.id.chk_agree1 || viewId == R.id.chk_agree2) {
            if(chkAgree1.isChecked() && chkAgree2.isChecked()){
                chkAgreeAll.setChecked(true);
            }else{
                chkAgreeAll.setChecked(false);
            }
        } else if (viewId == R.id.btn_agree_ok) {
            if(chkAgreeAll.isChecked()){
                DialogShow(MOBILE_AUTHENTICATION);
            }else{
                DialogShow(NOT_TERMS_AGREE);
            }
        } else if (viewId == R.id.btn_agree_cancel) {
            finish();
        }
        if(intent!=null){
            startActivity(intent);
            finish();
        }
    }

    /**
     *  약관 비동의, 휴대폰 인증 팝업
     * @param type 약관 비동의, 휴대폰 인증
     */
    public void DialogShow(int type){
        Intent i = new Intent(TermsAgreeActivity.this, DlgBtnActivity.class);
        if(type==NOT_TERMS_AGREE){ //이용약관 비 동의 시
            i.putExtra("DlgTitle", getResources().getString(R.string.str_terms_agree_title));
            i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_agree_err));
            startActivity(i);
        }else if(type==MOBILE_AUTHENTICATION){ //휴대폰 인증
            i.putExtra("DlgTitle", getResources().getString(R.string.str_mobile_authentication_title));
            i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_mobile_authentication_msg));
            i.putExtra("DlgMode", "Two");

            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, REQUEST_OK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_OK){
                Intent intent = new Intent(TermsAgreeActivity.this, MobileAuthenticationWebActivity.class);
                intent.putExtra("ReturnCd",1);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }
}
