package com.cashcuk.advertiser.refund;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.advertiser.GetChargeAmount;
import com.cashcuk.common.BankList;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgChkPwdActivity;
import com.cashcuk.membership.txtlistdata.TxtListAdapter;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;

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
import java.util.HashMap;
import java.util.List;

/**
 * 환불요청
 */
public class RefundRequestActivity extends Activity implements View.OnClickListener {
    private LinearLayout llProgress;

    private TextView txtChargeMoney; //충전금
    private EditText etAccountHolder; //예금주
    private TextView txtInputBankNm; //입금은행
    private EditText etInputAccount; //입금 계좌
    private EditText etRefundRequestAmount; //요청 금액
    private EditText etRefundReason; //환불사유

    private String selBankNmIdx;
    private ArrayList<TxtListDataInfo> arrBankNm = new ArrayList<TxtListDataInfo>();
    private final int REQUEST_CHK_PWD = 999;
    private final int REQUEST_REFUND_SUCCESS = 888;

    private final String STR_MODE_BANK = "Bank"; //은행
    private final String STR_MODE_CHARGE_AMOUNT = "ChargeAmount"; //충전금
    private final String STR_MODE_RETURN_REQUEST = "return"; //환불요청
    private String strMode = STR_MODE_BANK;

    private final String STR_ACCOUNT_HOLDER = "ret_host"; //예금주
    private final String STR_RETURN_BANK = "ret_bank"; //입금은행
    private final String STR_RETURN_ACCOUNT = "ret_account"; //입금계좌
    private final String STR_RETURN_AMOUNT = "ret_amnt"; //요청금액
    private final String STR_RETURN_CUZ = "ret_bcoz"; //환불사유

    private final int SEND_ACCOUNT_HOLDER = 2;
    private final int SEND_RETURN_BANK = 3;
    private final int SEND_RETURN_ACCOUNT = 4;
    private final int SEND_RETURN_AMOUNT = 5;
    private final int SEND_RETURN_CUZ = 6;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(RefundRequestActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(strMode.equals(STR_MODE_BANK)) {
                        if (arrBankNm == null) {
                            arrBankNm = new ArrayList<TxtListDataInfo>();
                        }
                        if (msg.obj != null) {
                            arrBankNm.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                        }

                        OpenDialog();

                        strMode = STR_MODE_CHARGE_AMOUNT;
                        new GetChargeAmount(RefundRequestActivity.this, handler);
                        return;
                    }else if(strMode.equals(STR_MODE_CHARGE_AMOUNT)){
                        strMode = "";
                        txtChargeMoney.setText(StaticDataInfo.makeStringComma((String)msg.obj));
                    }else if(strMode.equals(STR_MODE_RETURN_REQUEST)){
                        strMode = "";

                        Intent intent = new Intent(RefundRequestActivity.this, DlgBtnActivity.class);
                        intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_refund_money_success), StaticDataInfo.makeStringComma(etRefundRequestAmount.getText().toString())));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, REQUEST_REFUND_SUCCESS);
                    }
                    break;
                default:
                    Toast.makeText(RefundRequestActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            }, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_refund_request);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_refund_request));
        txtChargeMoney = (TextView) findViewById(R.id.txt_title_charge_money);
        etAccountHolder = (EditText) findViewById(R.id.et_account_holder);
        txtInputBankNm = (TextView) findViewById(R.id.txt_bank_nm);
        txtInputBankNm.setOnClickListener(this);
        etInputAccount = (EditText) findViewById(R.id.et_deposit_account);
        etRefundRequestAmount = (EditText) findViewById(R.id.et_input_amount);
        etRefundRequestAmount.addTextChangedListener(mTextWatcher);
        etRefundReason = (EditText) findViewById(R.id.et_refund_reason);

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        strMode = STR_MODE_BANK;
        new BankList(this, handler);
        ((Button) findViewById(R.id.btn_refund_request)).setOnClickListener(this);

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        WebView wbInfo = (WebView) findViewById(R.id.wv_info_msg);
        wbInfo.getSettings().setJavaScriptEnabled(true);
        wbInfo.setBackgroundColor(0);
        wbInfo.loadUrl(getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_link_payback_info));
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
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    private String strCommaResult="";
    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(strCommaResult)){     // StackOverflow를 막기위해,
                strCommaResult = StaticDataInfo.makeStringComma(s.toString().replaceAll(",", ""));   // 에딧텍스트의 값을 변환하여, result에 저장.
                etRefundRequestAmount.setText(strCommaResult);    // 결과 텍스트 셋팅.
                etRefundRequestAmount.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * 입력 항목 check
     */
    public void chkInputData(){
        if(etAccountHolder.getText().toString().trim().equals("")){
            Toast.makeText(RefundRequestActivity.this, getResources().getString(R.string.str_account_holder_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }else if(txtInputBankNm.getText().toString().trim().equals("")){
            Toast.makeText(RefundRequestActivity.this, getResources().getString(R.string.str_point_use_sel_bank_empty), Toast.LENGTH_SHORT).show();
            return;
        }else if(etInputAccount.getText().toString().trim().equals("")){
            Toast.makeText(RefundRequestActivity.this, getResources().getString(R.string.str_account_num_empty), Toast.LENGTH_SHORT).show();
            return;
        }else if(etRefundRequestAmount.getText().toString().trim().equals("") || etRefundRequestAmount.getText().toString().trim().equals("0") ){
            Toast.makeText(RefundRequestActivity.this, getResources().getString(R.string.str_account_input_amount_empty), Toast.LENGTH_SHORT).show();
            return;
        }else if(etRefundReason.getText().toString().trim().equals("")){
            Toast.makeText(RefundRequestActivity.this, getResources().getString(R.string.str_refund_reason_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        long lChargeMoney = Long.parseLong(txtChargeMoney.getText().toString().replaceAll(",","")); //충전금
        long lRequestAmount = Long.parseLong(etRefundRequestAmount.getText().toString().replaceAll(",","")); //환불 요청 금액
        if(lRequestAmount>lChargeMoney){
            Intent intent = new Intent(RefundRequestActivity.this, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_refund_money_err));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else {
            Intent intent = new Intent(RefundRequestActivity.this, DlgChkPwdActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_CHK_PWD);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_refund_request) {
            chkInputData();
        } else if (viewId == R.id.txt_bank_nm) {
            if (mDialog != null && !mDialog.isShowing()) mDialog.show();
        }
    }

    private Dialog mDialog;
    public void OpenDialog() {
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();

        mDialog = new Dialog(RefundRequestActivity.this);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.dlg_txt_list);

        ListView lvBankList = (ListView) mDialog.findViewById(R.id.lv_txt);
        TextView txtTitle = (TextView) mDialog.findViewById(R.id.txt_dlg_title);
        txtTitle.setText(getResources().getString(R.string.str_point_use_sel_bank));

        if(arrBankNm!=null) {
            lvBankList.setAdapter(new TxtListAdapter(this, arrBankNm));
        }

        lvBankList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selBankNmIdx = arrBankNm.get(position).getStrIdx();
                txtInputBankNm.setText(arrBankNm.get(position).getStrMsg());
                if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHK_PWD) {
                setDataInfo();
            }else if(requestCode == REQUEST_REFUND_SUCCESS){
                finish();
            }
        }
    }

    /**
     * 서버로 전송하는 값
     */
    public void setDataInfo(){
        strMode = STR_MODE_RETURN_REQUEST;
        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_charges_return_page);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_ACCOUNT_HOLDER, etAccountHolder.getText().toString());
        k_param.put(SEND_RETURN_BANK, txtInputBankNm.getText().toString());
        k_param.put(SEND_RETURN_ACCOUNT, etInputAccount.getText().toString());
        k_param.put(SEND_RETURN_AMOUNT, etRefundRequestAmount.getText().toString().replace(",", ""));
        k_param.put(SEND_RETURN_CUZ, etRefundReason.getText().toString());

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
                listParams.add(new BasicNameValuePair(STR_ACCOUNT_HOLDER, params[SEND_ACCOUNT_HOLDER]));
                listParams.add(new BasicNameValuePair(STR_RETURN_BANK, params[SEND_RETURN_BANK]));
                listParams.add(new BasicNameValuePair(STR_RETURN_ACCOUNT, params[SEND_RETURN_ACCOUNT]));
                listParams.add(new BasicNameValuePair(STR_RETURN_AMOUNT, params[SEND_RETURN_AMOUNT]));
                listParams.add(new BasicNameValuePair(STR_RETURN_CUZ, params[SEND_RETURN_CUZ]));

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
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }
}
