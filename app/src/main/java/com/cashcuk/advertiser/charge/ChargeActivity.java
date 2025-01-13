package com.cashcuk.advertiser.charge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.dialog.DlgBtnActivity;

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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 충전요청 정보입력 및 확인
 */
public class ChargeActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private CheckBox chkReceiptTaxInvoice; //현금영수증 / 세금계산서 신청
    private LinearLayout llReceiptTaxInvoice; //현금영수증/세금계산서 layout
    private RadioGroup rgReceiptCash; //현금영수증 layout
    private LinearLayout llTaxInvoice; //세금계산서 layout

    private RadioButton rbReceiptCash; //현금영수증
    private RadioButton rbTaxInvoice; //세금계산서
    private RadioButton rbIndividual; //개인
    private RadioButton rbBusinessman; //사업자

    private TextView txtEmail; //이메일
    private TextView txtBusinessNo; //사업자 번호
    private TextView txtAccountHolder; //예금주
    private TextView txtDepositBank; //입금은행
    private TextView txtDepositAccount; //입금계좌

    private EditText etRemitter; //송금자 명
    private EditText etFirstNum; //대표전화
    private EditText etMiddleNum; //대표전화
    private EditText etLastNum; //대표전화
    private EditText etMemoEtc; //기타메모
    private EditText etChargeRequestAmount; //요청 금액

//    private String strBusinessNo=""; //사업자 번호
    private String strMinCharge=""; //충전요청 최소 금액
    private EditText etRequestAmount; //충전 요청 금액
    private TextView txtTax; //부가세
    private TextView txtInputAmount; //입금 금액
    private EditText etReceiptName; //현금영수증 이름
    private TextView txtUserInfo; //핸드폰/주민번호
    private boolean isPhone = true; //핸드폰/주민번호

    private LinearLayout llReceiptPhoneNum; //현금영수증 휴대폰 Layout
    private EditText etReceiptFirstNum; //현금영수증 휴대폰
    private EditText etReceiptMiddleNum; //현금영수증 휴대폰
    private EditText etReceiptLastNum; //현금영수증 휴대폰

    private LinearLayout llReceiptRegiCode; //현금영수증 주민번호 Layout
    private EditText etReceiptRegiCode1; //현금영수증 주민번호
    private EditText etReceiptRegiCode2; //현금영수증 주민번호

    private TextView txtReceiptBusinessNo; //현금영수증 사업자번호

    private TextView txtTaxInvoiceBusinessNo; //세금계산서 사업자번호
    private EditText etTaxInvoiceEmail; //세금계산서 이메일

    //충전요청 기본 값
    private final String STR_EMAIL = "mail_id"; //email
    private final String STR_BUSINESS_NO = "biz_no"; //사업자 번호
    private final String STR_ACCOUNT_HOLDER = "sys_host"; //예금주
    private final String STR_BANK = "sys_bank"; //입금은행
    private final String STR_ACCOUNT = "sys_account"; //입금계좌
    private final String STR_MIN_CHARGE = "sys_minamnt"; //충전요청 최소 금액
    private final String STR_TEL = "biz_tel"; //대표번호

    //충전 요청
    private final String STR_CHARGE_PAYER = "chrg_payer"; //입금자 명
    private final String STR_CHARGE_TEL = "chrg_tel"; //전화번호
    private final String STR_CHARGE_MEMO = "chrg_txt"; //기타메모
    private final String STR_CHARGE_REQUEST_MONEY = "chrg_reqamnt"; //요청금액
    private final String STR_CHARGE_VAT = "chrg_vat"; //부가세
    private final String STR_CHARGE_INPUT_MOMEY = "chrg_payamnt"; //입금금액

    private final String STR_CHARGE_RECEIPT = "chrg_receipt"; //현금영수증, 세금계산서 신청 유/무(0: 신청안함, 1:신청)
    private final String STR_CHARGE_CASH_TAX = "chrg_cashtax"; // 0: 현금영수증, 1: 세금계산서
    private final String STR_CHARGE_HOST_TYPE = "chrg_hosttype"; //0: 개인, 1: 사업자
    private final String STR_CHARGE_NM = "chrg_nm"; //이름 - 현금영수증(개인) 신청 시
    private final String STR_CHARGE_HP = "chrg_hp"; //핸드폰 (xxx-xxxx-xxxx) - 현금영수증(개인) 신청 시
    private final String STR_CHARGE_REGI_CODE = "chrg_regcode"; //주민번호 (xxxxxx-xxxxxxx) - 현금영수증(개인) 신청 시
    private final String STR_CHARGE_EMAIL = "chrg_email"; //이메일 주소 - 세금계산서 신청 시

    private final int INDEX_2 = 2;
    private final int INDEX_3 = 3;
    private final int INDEX_4 = 4;
    private final int INDEX_5 = 5;
    private final int INDEX_6 = 6;
    private final int INDEX_7 = 7;
    private final int INDEX_8 = 8;
    private final int INDEX_9 = 9;
    private final int INDEX_10 = 10;
    private final int INDEX_11 = 11;
    private final int INDEX_12 = 12;
    private final int INDEX_13 = 13;

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;

//    private String STR_MODE_CHARGES_AMOUNT = "AMOUNT"; //충전금
    private String STR_MODE_CHARGES_ACCOUNT = "Account"; //충전 요청 기본 정보
    private String STR_MODE_CHARGES_CHARGE = "Charge"; //충전 요청
    private String strMode = STR_MODE_CHARGES_ACCOUNT; //충전 기본 정보 요청인지 충전 요청인지 모드

    private final String STR_0 = "0";
    private final String STR_1 = "1";

    private String strIsReceipt = STR_0; //현금영수정, 세금계산서 신청 유/무 (0: 신청안함, 1: 신청함)
    private String strKindReceipt = ""; //현금영수증 or 세금계산서 (0: 현금영수증, 1: 세금계산서)
    private String strKindUser = ""; //개인 or 사업자 (0: 개인, 1: 사업자)

    private final int REQUEST_REQUEST_AMOUNT_ERR = 999;
    private final int REQUEST_REQUEST_CHARGE = 888;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(strMode.equals(STR_MODE_CHARGES_CHARGE)){
                        Intent intent = new Intent(ChargeActivity.this, DlgBtnActivity.class);
                        intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_request_amount_success), txtAccountHolder.getText(), txtDepositBank.getText(), txtDepositAccount.getText(), txtInputAmount.getText()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, REQUEST_REQUEST_CHARGE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_charge_activitiy);
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_charge_request));

        etRemitter = (EditText) findViewById(R.id.et_remitter);
        etFirstNum = (EditText) findViewById(R.id.et_first_num);
        etMiddleNum = (EditText) findViewById(R.id.et_middle_num);
        etLastNum = (EditText) findViewById(R.id.et_last_num);
        etMemoEtc = (EditText) findViewById(R.id.et_memo_etc);
        etChargeRequestAmount = (EditText) findViewById(R.id.et_charge_request_amount);

        txtEmail = (TextView) findViewById(R.id.txt_remitter_email);
        txtBusinessNo = (TextView) findViewById(R.id.txt_businessman_registered_num);
        txtAccountHolder = (TextView) findViewById(R.id.txt_account_holder);
        txtDepositBank = (TextView) findViewById(R.id.txt_deposit_bank);
        txtDepositAccount = (TextView) findViewById(R.id.txt_deposit_account);

        chkReceiptTaxInvoice = (CheckBox) findViewById(R.id.chk_receipt_tax_invoice);
        chkReceiptTaxInvoice.setOnCheckedChangeListener(this);

        llReceiptTaxInvoice = (LinearLayout) findViewById(R.id.ll_receipt_tax_invoice);
        rgReceiptCash = (RadioGroup) findViewById(R.id.rg_receipt_cash);

        rbReceiptCash = (RadioButton) findViewById(R.id.rb_receipt); //현금영수증
        rbTaxInvoice = (RadioButton) findViewById(R.id.rb_tax_invoice); //세금계산서
        rbIndividual = (RadioButton) findViewById(R.id.rb_individual); //개인
        rbBusinessman = (RadioButton) findViewById(R.id.rb_businessman); //사업자

        rbReceiptCash.setOnCheckedChangeListener(this);
        rbTaxInvoice.setOnCheckedChangeListener(this);
        rbIndividual.setOnCheckedChangeListener(this);
        rbBusinessman.setOnCheckedChangeListener(this);

        llTaxInvoice = (LinearLayout) findViewById(R.id.ll_tax_invoice);

        etRequestAmount = (EditText) findViewById(R.id.et_charge_request_amount); //충전 요청 금액
        txtTax = (TextView) findViewById(R.id.txt_charge_request_vat); //부가세
        txtInputAmount = (TextView) findViewById(R.id.txt_input_amount); //총 입금 금액
        etRequestAmount.addTextChangedListener(mTextWatcher);

        etReceiptName = (EditText) findViewById(R.id.et_receipt_name); //현금영수증 이름
        txtUserInfo = (TextView) findViewById(R.id.txt_user_info); //핸드폰 주민번호
        txtUserInfo.setOnClickListener(this);

        llReceiptPhoneNum = (LinearLayout) findViewById(R.id.ll_receipt_phone_num); //현금영수증 휴대폰 layout
        etReceiptFirstNum = (EditText) findViewById(R.id.et_receipt_first_num); //현금영수증 휴대폰
        etReceiptMiddleNum = (EditText) findViewById(R.id.et_receipt_middle_num); //현금영수증 휴대폰
        etReceiptLastNum = (EditText) findViewById(R.id.et_receipt_last_num); //현금영수증 휴대폰

        llReceiptRegiCode = (LinearLayout) findViewById(R.id.ll_regi_code); //현금영수증 주빈번호 layout
        etReceiptRegiCode1 = (EditText) findViewById(R.id.et_regi_code1); //현금영수증 주빈번호
        etReceiptRegiCode2 = (EditText) findViewById(R.id.et_regi_code2); //현금영수증 주빈번호

        txtReceiptBusinessNo = (TextView) findViewById(R.id.txt_receipt_businessman_registered); //현금영수증 사업자 번호

        txtTaxInvoiceBusinessNo = (TextView) findViewById(R.id.txt_tax_invoice_businessman_registered); //세금계산서 사업자 번호
        etTaxInvoiceEmail = (EditText) findViewById(R.id.et_tax_invoice_email); //세금계산서 이메일
        ((Button) findViewById(R.id.btn_charge_request)).setOnClickListener(this);

        etRequestAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(chkReceiptTaxInvoice.isChecked()){
                        if(rbReceiptCash.isChecked()){
                            etRequestAmount.setNextFocusDownId(R.id.et_receipt_name);
                        }else if(rbTaxInvoice.isChecked()){
                            etRequestAmount.setNextFocusDownId(R.id.et_tax_invoice_email);
                        }
                    }else{
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etRequestAmount.getApplicationWindowToken(), 0);
                    }
                }
            }
        });

        setAdvertiserInfo(STR_MODE_CHARGES_ACCOUNT);

        WebView wvInfo1 = (WebView) findViewById(R.id.wv_info_msg1);
        WebView wvInfo2 = (WebView) findViewById(R.id.wv_info_msg2);
        wvInfo1.getSettings().setJavaScriptEnabled(true);
        wvInfo1.setBackgroundColor(0);

        wvInfo2.getSettings().setJavaScriptEnabled(true);
        wvInfo2.setBackgroundColor(0);

        wvInfo1.loadUrl(getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_link_charge_info1));
        wvInfo2.loadUrl(getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_link_charge_info2));
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


    private String strCommaResult = "";
    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(strCommaResult)){     // StackOverflow를 막기위해,
                strCommaResult = StaticDataInfo.makeStringComma(s.toString().replaceAll(",", ""));   // 에딧텍스트의 값을 변환하여, result에 저장.
                etRequestAmount.setText(strCommaResult);    // 결과 텍스트 셋팅.
                etRequestAmount.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.

                if(etRequestAmount.getText().toString().equals("")) {
                    txtTax.setText("");
                    txtInputAmount.setText("");
                }else{
                    long mRequestAmount = Long.parseLong(etRequestAmount.getText().toString().replaceAll(",", ""));
                    txtTax.setText(StaticDataInfo.makeStringComma(String.valueOf(mRequestAmount * 10 / 100)));
                    txtInputAmount.setText(StaticDataInfo.makeStringComma(String.valueOf(mRequestAmount + Long.parseLong(txtTax.getText().toString().replaceAll(",", "")))));
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int buttonId = buttonView.getId();
        if (buttonId == R.id.chk_receipt_tax_invoice) {
            if (isChecked) {
                strIsReceipt = STR_1;
                llReceiptTaxInvoice.setVisibility(View.VISIBLE);
                rbReceiptCash.setChecked(true);
                rgReceiptCash.setVisibility(View.VISIBLE);
                rbIndividual.setChecked(true);
            } else {
                strIsReceipt = STR_0;
                llReceiptTaxInvoice.setVisibility(View.GONE);
            }
        } else if (buttonId == R.id.rb_receipt) {
            if (isChecked) {
                strKindReceipt = STR_0;
                rgReceiptCash.setVisibility(View.VISIBLE);
                rbIndividual.setChecked(true);
                llTaxInvoice.setVisibility(View.GONE);
            } else {
                rgReceiptCash.setVisibility(View.GONE);
            }
        } else if (buttonId == R.id.rb_tax_invoice) {
            if (isChecked) {
                strKindReceipt = STR_1;
                rgReceiptCash.setVisibility(View.GONE);
                llTaxInvoice.setVisibility(View.VISIBLE);
            } else {
                llTaxInvoice.setVisibility(View.GONE);
            }
        } else if (buttonId == R.id.rb_individual) {
            if (isChecked) {
                strKindUser = STR_0;
                etReceiptName.setFocusable(true);
                etReceiptName.setFocusableInTouchMode(true);

                txtUserInfo.setClickable(true);

                etReceiptRegiCode1.setFocusable(true);
                etReceiptRegiCode2.setFocusable(true);
                etReceiptFirstNum.setFocusable(true);
                etReceiptMiddleNum.setFocusable(true);
                etReceiptLastNum.setFocusable(true);

                etReceiptRegiCode1.setFocusableInTouchMode(true);
                etReceiptRegiCode2.setFocusableInTouchMode(true);
                etReceiptFirstNum.setFocusableInTouchMode(true);
                etReceiptMiddleNum.setFocusableInTouchMode(true);
                etReceiptLastNum.setFocusableInTouchMode(true);
            }
        } else if (buttonId == R.id.rb_businessman) {
            if (isChecked) {
                strKindUser = STR_1;
                etReceiptName.setFocusable(false);

                txtUserInfo.setClickable(false);

                etReceiptRegiCode1.setFocusable(false);
                etReceiptRegiCode2.setFocusable(false);
                etReceiptFirstNum.setFocusable(false);
                etReceiptMiddleNum.setFocusable(false);
                etReceiptLastNum.setFocusable(false);

                etReceiptRegiCode1.setFocusableInTouchMode(false);
                etReceiptRegiCode2.setFocusableInTouchMode(false);
                etReceiptFirstNum.setFocusableInTouchMode(false);
                etReceiptMiddleNum.setFocusableInTouchMode(false);
                etReceiptLastNum.setFocusableInTouchMode(false);
            }
        }
    }

    /**
     * 충전요청(광고주) 기본 정보 or 충전요청
     * @param mode 충전요청 기본 정보 or 충전요청
     */
    public void setAdvertiserInfo(String mode) {
        strMode = mode;
        String url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_charges_account_page);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if(mode.equals(STR_MODE_CHARGES_CHARGE)){
            url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_charges_charge_page);
        }

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);

        if(mode.equals(STR_MODE_CHARGES_CHARGE)){
            String strTel = etFirstNum.getText() + "-" + etMiddleNum.getText() + "-" + etLastNum.getText();
            k_param.put(INDEX_2, etRemitter.getText().toString());
            k_param.put(INDEX_3, strTel);
            k_param.put(INDEX_4, etMemoEtc.getText().toString());
            if(etChargeRequestAmount.getText().toString().contains(",")) {
                k_param.put(INDEX_5, etChargeRequestAmount.getText().toString().replaceAll(",", ""));
            }else{
                k_param.put(INDEX_5, etChargeRequestAmount.getText().toString());
            }
            if(txtTax.getText().toString().contains(",")) {
                k_param.put(INDEX_6, txtTax.getText().toString().replaceAll(",", ""));
            }else{
                k_param.put(INDEX_6, txtTax.getText().toString());
            }
            if(txtInputAmount.getText().toString().contains(",")) {
                k_param.put(INDEX_7, txtInputAmount.getText().toString().replaceAll(",", ""));
            }else{
                k_param.put(INDEX_7, txtInputAmount.getText().toString());
            }
            k_param.put(INDEX_8, strIsReceipt);

            if(strIsReceipt.equals(STR_1)){ //신청 유/무
                k_param.put(INDEX_9, strKindReceipt);

                if(strKindReceipt.equals(STR_0)) {
                    k_param.put(INDEX_10, strKindUser);
                    if(strKindUser.equals(STR_0)) {
                        k_param.put(INDEX_11, etReceiptName.getText().toString());
                        if (isPhone) {
                            k_param.put(INDEX_12, etReceiptFirstNum.getText() + "-" + etReceiptMiddleNum.getText() + "-" + etReceiptLastNum.getText());
                        } else {
                            k_param.put(INDEX_12, etReceiptFirstNum.getText() + "-" + etReceiptMiddleNum.getText() + "-" + etReceiptLastNum.getText());
                        }
                    }
                }else if(strKindReceipt.equals(STR_1)) {
                    k_param.put(INDEX_10, etTaxInvoiceEmail.getText().toString());
                }
            }
        }

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new DataTask().execute(strTask);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_charge_request) {
            inputInfoChk();
        } else if (viewId == R.id.txt_user_info) {
            if (!isPhone) {
                txtUserInfo.setText(getResources().getString(R.string.str_phone_num));
                llReceiptPhoneNum.setVisibility(View.VISIBLE);
                llReceiptRegiCode.setVisibility(View.GONE);
                etReceiptRegiCode1.setText("");
                etReceiptRegiCode2.setText("");
            } else {
                txtUserInfo.setText(getResources().getString(R.string.str_registration_num));
                llReceiptPhoneNum.setVisibility(View.GONE);
                llReceiptRegiCode.setVisibility(View.VISIBLE);
                etReceiptFirstNum.setText("");
                etReceiptMiddleNum.setText("");
                etReceiptLastNum.setText("");
            }
            isPhone = !isPhone;
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

                if(strMode.equals(STR_MODE_CHARGES_CHARGE)) {
                    listParams.add(new BasicNameValuePair(STR_CHARGE_PAYER, params[INDEX_2]));
                    listParams.add(new BasicNameValuePair(STR_CHARGE_TEL, params[INDEX_3]));
                    listParams.add(new BasicNameValuePair(STR_CHARGE_MEMO, params[INDEX_4]));

                    listParams.add(new BasicNameValuePair(STR_CHARGE_REQUEST_MONEY, params[INDEX_5]));
                    listParams.add(new BasicNameValuePair(STR_CHARGE_VAT, params[INDEX_6]));
                    listParams.add(new BasicNameValuePair(STR_CHARGE_INPUT_MOMEY, params[INDEX_7]));
                    listParams.add(new BasicNameValuePair(STR_CHARGE_RECEIPT, params[INDEX_8]));

                    if(strIsReceipt.equals(STR_1)){ //신청 유/무
                        listParams.add(new BasicNameValuePair(STR_CHARGE_CASH_TAX, params[INDEX_9]));
                        if(strKindReceipt.equals(STR_0)) {
                            listParams.add(new BasicNameValuePair(STR_CHARGE_HOST_TYPE, params[INDEX_10]));
                            if(strKindUser.equals(STR_0)) {
                                listParams.add(new BasicNameValuePair(STR_CHARGE_NM, params[INDEX_11]));
                                if (isPhone) {
                                    listParams.add(new BasicNameValuePair(STR_CHARGE_HP, params[INDEX_12]));
                                } else {
                                    listParams.add(new BasicNameValuePair(STR_CHARGE_REGI_CODE, params[INDEX_12]));
                                }
                            }
                        }else if(strKindReceipt.equals(STR_1)) {
                            listParams.add(new BasicNameValuePair(STR_CHARGE_EMAIL, params[INDEX_10]));
                        }
                    }
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

            if(!strMode.equals(STR_MODE_CHARGES_CHARGE)) {
                if (result.startsWith(StaticDataInfo.TAG_LIST)){
                        displayAddcountInfo(result);
                } else {
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
                }
            }else{
                if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
                }else{
                    handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
                }
            }
        }
    }

    /**
     * 충전요청 기본 정보 d/p
     */
    public void displayAddcountInfo(String result){
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
                        if (parser.getName().equals(STR_EMAIL)) {
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_BUSINESS_NO)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_ACCOUNT_HOLDER)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_BANK)) {
                            k_data_num = PARSER_NUM_3;
                        } else if (parser.getName().equals(STR_ACCOUNT)) {
                            k_data_num = PARSER_NUM_4;
                        } else if (parser.getName().equals(STR_MIN_CHARGE)) {
                            k_data_num = PARSER_NUM_5;
                        } else if (parser.getName().equals(STR_TEL)) {
                            k_data_num = PARSER_NUM_6;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    txtEmail.setText(parser.getText());
                                    break;
                                case PARSER_NUM_1:
                                    txtBusinessNo.setText(parser.getText());

                                    txtReceiptBusinessNo.setText(parser.getText());
                                    txtTaxInvoiceBusinessNo.setText(parser.getText());
                                    break;
                                case PARSER_NUM_2:
                                    txtAccountHolder.setText(parser.getText());
                                    break;
                                case PARSER_NUM_3:
                                    txtDepositBank.setText(parser.getText());
                                    break;
                                case PARSER_NUM_4:
                                    txtDepositAccount.setText(parser.getText());
                                    break;
                                case PARSER_NUM_5:
                                    strMinCharge = parser.getText();
                                    etChargeRequestAmount.setHint(StaticDataInfo.makeStringComma(strMinCharge)+getResources().getString(R.string.str_up));
                                    etChargeRequestAmount.setHintTextColor(getResources().getColor(R.color.color_hint_txt));
                                    break;
                                case PARSER_NUM_6:
                                    String strTel[] = new String[3];
                                    strTel = PhoneNumberUtils.formatNumber(parser.getText()).split("-");

                                    etFirstNum.setText(strTel[0]);
                                    etMiddleNum.setText(strTel[1]);
                                    etLastNum.setText(strTel[2]);
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 충전 요청 시 입력 정보 체크
     */
    public void inputInfoChk(){
        if(etRemitter.getText().toString().trim().equals("")){
            Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_remitter_empty), Toast.LENGTH_SHORT).show();
            etRemitter.requestFocus();
            etRemitter.setSelection(etRemitter.length());
            return;
        }else if(etFirstNum.getText().toString().trim().equals("") || etMiddleNum.getText().toString().trim().equals("") || etLastNum.getText().toString().trim().equals("")){
            Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_tel_empty), Toast.LENGTH_SHORT).show();
            etFirstNum.requestFocus();
            etFirstNum.setSelection(etFirstNum.length());
            return;
        }else if(etChargeRequestAmount.getText().toString().trim().equals("")){
            Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_request_amount_empty), Toast.LENGTH_SHORT).show();
            etChargeRequestAmount.requestFocus();
            etChargeRequestAmount.setSelection(etChargeRequestAmount.length());
            return;
        }else if(!strMinCharge.equals("") && !etChargeRequestAmount.getText().toString().trim().equals("")){
            long mMinAmount = Long.valueOf(strMinCharge.replaceAll(",", ""));
            long mInputAmount = Long.valueOf(etChargeRequestAmount.getText().toString().replaceAll(",", ""));

            if(mInputAmount<mMinAmount){
                Intent intent = new Intent(ChargeActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_request_amount_err), StaticDataInfo.makeStringComma(strMinCharge)));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_REQUEST_AMOUNT_ERR);
                return;
            }
        }

        if(chkReceiptTaxInvoice.isChecked()){ //현금영수증 or 세금계산서
            if(rbReceiptCash.isChecked()){ //현금영수증
                if(rbIndividual.isChecked()) { //개인
                    if (etReceiptName.getText().toString().trim().equals("")) {
                        Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_input_name), Toast.LENGTH_SHORT).show();
                        etReceiptName.requestFocus();
                        etReceiptName.setSelection(etReceiptName.length());
                        return;
                    }else if(isPhone && (etReceiptFirstNum.getText().toString().trim().equals("") || etReceiptMiddleNum.getText().toString().trim().equals("") || etReceiptLastNum.getText().toString().trim().equals(""))) {
                        Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show();
                        etReceiptFirstNum.requestFocus();
                        etReceiptFirstNum.setSelection(etReceiptFirstNum.length());
                        return;
                    }
                    else if(!isPhone){
                        String strRegiCode = etReceiptRegiCode1.getText().toString() + "-" + etReceiptRegiCode2.getText().toString();
                        String regex = "^(\\d{2})[0|1](\\d)[0|1|2|3](\\d)[\\-||\\s]?([1|2|3|4]\\d{6})$";
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(strRegiCode);
                        if (!m.matches()) {
                            Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_input_registration_number), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            }else if(rbTaxInvoice.isChecked()){ //세금계산서
                if(etTaxInvoiceEmail.getText().toString().trim().equals("")){
                    Toast.makeText(ChargeActivity.this, getResources().getString(R.string.str_id_hint), Toast.LENGTH_SHORT).show();
                    etTaxInvoiceEmail.requestFocus();
                    etTaxInvoiceEmail.setSelection(etTaxInvoiceEmail.length());
                    return;
                }
            }
        }

        setAdvertiserInfo(STR_MODE_CHARGES_CHARGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_REQUEST_AMOUNT_ERR){
                etChargeRequestAmount.requestFocus();
                etChargeRequestAmount.setSelection(etChargeRequestAmount.length());
            } else if(requestCode == REQUEST_REQUEST_CHARGE){
                finish();
            }
        }
    }
}
