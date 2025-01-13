package com.cashcuk.pointlist;

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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.GetChargePoint;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 내 계좌로 입금
 */
public class PointInputAccountActivity extends Activity implements View.OnClickListener{
    private final int DIALOG_BANK_LIST = 2; //은행명 list 팝업

    private LinearLayout llProgress;
    private TextView txtMyPoint;
    private TextView txtBankNm;
    private String selBankNmIdx;
    private EditText etAccountHolderNm; //예금주 명
    private EditText etAccountNum; //계좌번호
    private EditText etInputAmount; //입금급액

    private ArrayList<TxtListDataInfo> arrBankNm = new ArrayList<TxtListDataInfo>();

    private final int SEND_GUBUN = 2; //내 포인트 요청인지 신청 요청인지 구분
    // 포인트 사용
    private final int SEND_BANK_IDX = 3;
    private final int SEND_ACCOUNT_NUM = 4;
    private final int SEND_DEPOSITOR = 5;
    private final int SEND_AMOUNT = 6;

    private final String STR_USER_IDX = "idx";
    private final String STR_GUBUN = "pagegubun";
    private final String STR_BANK_IDX = "auBank";
    private final String STR_ACCOUNT_NUM = "auAccnum";
    private final String STR_DEPOSITOR = "auDepositor";
    private final String STR_AMOUNT = "auAmount";

    private final String STR_REQUEST_INPUT_POINT = "A";

    private String strGubun = "";
    private String strBankNm; //은행명
    private String strAccountHolderNm; //예금주 명
    private String strAccountNum; //계좌번호
    private String strInputAmount; //입금금액
    private String strDI;
    private String strCI;

    private String strUsePoint="";
    private final String MODE_USE_POINT = "UsePoint";

    private final int REQUEST_CHK_PWD = 999;
    private final int REQUEST_INPUT_POINT = 666;

    private String sMyPoint = "0";

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_NO_USER:
                    Intent intent = new Intent(PointInputAccountActivity.this, DlgBtnActivity.class);
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_no_user));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(PointInputAccountActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1 == StaticDataInfo.RESULT_CODE_MY_POINT) {
                        txtMyPoint.setText(StaticDataInfo.makeStringComma((String) msg.obj));
                    }else if(strUsePoint.equals(MODE_USE_POINT)){
                        strUsePoint = "";
                        String strMsg = getResources().getString(R.string.str_point_use_sel_bank)+": "+ txtBankNm.getText() + "\n" +
                                getResources().getString(R.string.str_account_holder_nm)+": "+ etAccountHolderNm.getText() + "\n" +
                                getResources().getString(R.string.str_account_num)+": "+ etAccountNum.getText() + "\n" +
                                getResources().getString(R.string.str_account_input_amount)+": "+ etInputAmount.getText() + " "+ getResources().getString(R.string.str_won) +"\n\n" +
                                getResources().getString(R.string.str_account_input_request_succese);

                        Intent intent1 = new Intent(PointInputAccountActivity.this, DlgBtnActivity.class);
                        intent1.putExtra("BtnDlgMsg", strMsg);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent1, REQUEST_INPUT_POINT);
                    }else {
                        if (arrBankNm == null) {
                            arrBankNm = new ArrayList<TxtListDataInfo>();
                        }
                        if (msg.obj != null) {
                            arrBankNm.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                        }
                    }
                    break;
                default:
                    Toast.makeText(PointInputAccountActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_point_input_account);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        Intent intent = getIntent();
        if(intent!=null) {
            sMyPoint = StaticDataInfo.makeStringComma(intent.getStringExtra("MyPoint"));
        }
        View vTitleBar = (View) findViewById(R.id.layout_my_point);
        txtMyPoint = (TextView) vTitleBar.findViewById(R.id.txt_title_my_point);

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_point_use_input_my_account));
        txtBankNm = (TextView) findViewById(R.id.txt_bank_nm);
        txtBankNm.setOnClickListener(this);

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);

        Button btnPointUse = (Button) findViewById(R.id.btn_point_use);
        btnPointUse.setOnClickListener(this);

        etAccountHolderNm = (EditText) findViewById(R.id.et_account_holder_nm);
        etAccountNum = (EditText) findViewById(R.id.et_account_num);
        etInputAmount = (EditText) findViewById(R.id.et_input_amount);
        etInputAmount.addTextChangedListener(mTextWatcher);

        new BankList(PointInputAccountActivity.this, handler);

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
                strGubun = "";
                new GetChargePoint(PointInputAccountActivity.this, handler);
                new BankList(PointInputAccountActivity.this, handler);

                txtBankNm.setText("");
                etAccountHolderNm.setText("");
                etAccountNum.setText("");
                etInputAmount.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        new GetChargePoint(PointInputAccountActivity.this, handler);
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


    private String strCommaResult="";
    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(strCommaResult)){     // StackOverflow를 막기위해,
                strCommaResult = StaticDataInfo.makeStringComma(s.toString().replaceAll(",", ""));   // 에딧텍스트의 값을 변환하여, result에 저장.
                etInputAmount.setText(strCommaResult);    // 결과 텍스트 셋팅.
                etInputAmount.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * 입금신청 버튼 클릭 시 data 입력 체크 및 진행
     */
    public void ChkInputData(){
        strBankNm = txtBankNm.getText().toString();
        strAccountHolderNm = etAccountHolderNm.getText().toString();
        strAccountNum = etAccountNum.getText().toString();
        strInputAmount = etInputAmount.getText().toString();

        if(strBankNm.trim().equals("")){
            Toast.makeText(PointInputAccountActivity.this, getResources().getString(R.string.str_point_use_sel_bank_empty), Toast.LENGTH_SHORT).show();
            return;
        }else if(strAccountHolderNm.trim().equals("")){
            Toast.makeText(PointInputAccountActivity.this, getResources().getString(R.string.str_account_holder_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }else if(strAccountNum.trim().equals("")){
            Toast.makeText(PointInputAccountActivity.this, getResources().getString(R.string.str_account_num_empty), Toast.LENGTH_SHORT).show();
            return;
        }else if(strInputAmount.trim().equals("")){
            Toast.makeText(PointInputAccountActivity.this, getResources().getString(R.string.str_account_input_amount_empty), Toast.LENGTH_SHORT).show();
            return;
        }else{
            float mInputAmount = 0;
            if(strInputAmount.contains(",")) {
                mInputAmount =Long.parseLong(strInputAmount.replace(",", ""));
            }else{
                mInputAmount = Long.parseLong(strInputAmount);
            }

            if(mInputAmount>50000){
                Intent intent = new Intent(PointInputAccountActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_input_money_max));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            }else if (!strInputAmount.trim().contains(",")) {
                Intent intent = new Intent(PointInputAccountActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_input_money_err));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            } else if (strInputAmount.trim().contains(",") && !(strInputAmount.substring(strInputAmount.length() - 3, strInputAmount.length()).equals("000"))) {
                Intent intent = new Intent(PointInputAccountActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_input_money_err));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            } else if (mInputAmount > Float.parseFloat(txtMyPoint.getText().toString().contains(",") ? txtMyPoint.getText().toString().replace(",", "") : txtMyPoint.getText().toString())) {
                Intent intent = new Intent(PointInputAccountActivity.this, DlgBtnActivity.class);
                intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_input_money_err1));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            } else {
                SharedPreferences prefs = getSharedPreferences("SaveDefaultSetInfo", MODE_PRIVATE);
                String strChangeMinMoney = prefs.getString("ChangeMinMoney", "");
                if (strChangeMinMoney == null || strChangeMinMoney.equals("")) {
                    strChangeMinMoney = "20000";
                }

                if (mInputAmount < Integer.parseInt(strChangeMinMoney)) {
                    Intent intent = new Intent(PointInputAccountActivity.this, DlgBtnActivity.class);
                    intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_input_money_request_err), StaticDataInfo.makeStringComma(strChangeMinMoney)));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return;
                }
            }
            Intent intent = new Intent(PointInputAccountActivity.this, DlgChkPwdActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_CHK_PWD);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();

        int viewId = v.getId();
        if (viewId == R.id.txt_bank_nm) {
            //은행 선택
            OpenDialog(DIALOG_BANK_LIST);
        } else if (viewId == R.id.btn_point_use) {
            //입금 신청
            ChkInputData();
        } else if (viewId == R.id.btn_chk_pwd_cancel) {
            //비밀번호 확인 취소
            if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDialog!=null && mDialog.isShowing()) mDialog.dismiss();
        recycleView(findViewById(R.id.fl_bg));
    }

    /**
     * 팝업창 width full로 설정
     * @param dlg
     * @return WindowManager.LayoutParams
     */
    public WindowManager.LayoutParams DialogWidth(Dialog dlg){
        WindowManager.LayoutParams lpWindow = dlg.getWindow().getAttributes();
        lpWindow.width = WindowManager.LayoutParams.MATCH_PARENT;
        lpWindow.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lpWindow.gravity = Gravity.CENTER;

        return lpWindow;
    }

    /**
     * 비밀번호 확인 후 true 일 때 조건 체크 및 서버 연결
     */
    public void ChkAgree(){

        final String url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_charges_cash);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_GUBUN, strGubun);
        if(strGubun.equals(STR_REQUEST_INPUT_POINT)) {
            k_param.put(SEND_BANK_IDX, selBankNmIdx);
            k_param.put(SEND_ACCOUNT_NUM, strAccountNum);
            k_param.put(SEND_DEPOSITOR, strAccountHolderNm);
            k_param.put(SEND_AMOUNT, strInputAmount.replaceAll(",", ""));
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
                listParams.add(new BasicNameValuePair(STR_GUBUN, params[SEND_GUBUN]));

                if(strGubun.equals(STR_REQUEST_INPUT_POINT)) {
                    listParams.add(new BasicNameValuePair(STR_BANK_IDX, params[SEND_BANK_IDX]));
                    listParams.add(new BasicNameValuePair(STR_ACCOUNT_NUM, params[SEND_ACCOUNT_NUM]));
                    listParams.add(new BasicNameValuePair(STR_DEPOSITOR, params[SEND_DEPOSITOR]));
                    listParams.add(new BasicNameValuePair(STR_AMOUNT, params[SEND_AMOUNT]));
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
            Message msg = new Message();
            if(result.equals("") || (result.startsWith("<") && !result.startsWith(StaticDataInfo.TAG_LIST))){
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
                handler.sendMessage(msg);
                return;
            }

            if(result.startsWith(StaticDataInfo.TAG_LIST)){
                getMyPoint(result);
            }else {
                if (result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))) {
                    strUsePoint = MODE_USE_POINT;
                    msg.what = StaticDataInfo.RESULT_CODE_200;
                } else {
                    msg.what = StaticDataInfo.RESULT_CODE_ERR;
                }
                handler.sendMessage(msg);
            }
        }
    }

    private final String STR_MY_POINT = "point_my"; //내 포인트

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;

    public void getMyPoint(String result){
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

                        if (parser.getName().equals(STR_MY_POINT)) {
                            k_data_num = PARSER_NUM_0;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    if(parser.getText().equals("")) {
                                        txtMyPoint.setText(sMyPoint);
                                    }else {
                                        txtMyPoint.setText(StaticDataInfo.makeStringComma(parser.getText()));
                                    }
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

    private Dialog mDialog;
    public void OpenDialog(int dlgMode) {
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            mDialog = new Dialog(PointInputAccountActivity.this);

            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        switch (dlgMode){
            case DIALOG_BANK_LIST: //은행 명
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
                        txtBankNm.setText(arrBankNm.get(position).getStrMsg());
                        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
                    }
                });
                break;
        }

            mDialog.getWindow().setAttributes(DialogWidth(mDialog));
            if (!mDialog.isShowing()) mDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CHK_PWD){
                strGubun = STR_REQUEST_INPUT_POINT;
                ChkAgree();
            }else if(requestCode == REQUEST_INPUT_POINT){
                finish();
            }
        }
    }
}
