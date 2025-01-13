package com.cashcuk.character.updown.up;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.character.CharacterInfo;
import com.cashcuk.common.CommCode;
import com.cashcuk.common.CommCodeAdapter;
import com.cashcuk.dialog.DlgBtnActivity;
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
 * 캐릭터 공유 정보 입력
 */
public class UpLoadCharacterInputInfoActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private RelativeLayout rlCategory;
    private TextView txtCategory;
    private EditText etName;
    private EditText etSaleName;
    private TextView txtSaleID; //공유인 ID
    private String strCategoryIdx="";
    private LinearLayout llProgress;
    private CharacterInfo mCharInfo=null;

    private final int PARSER_NUM_1 = 1;
    private ArrayList<TxtListDataInfo> arrCharCategory;

    private final String STR_CATEGORY_IDX="cat_idx";
    private final String STR_CHAR_NAME="sha_nm";
    private final String STR_CHAR_SALE_NAME="sha_biznm";
    private final String STR_CHAR_SALE_ID="sha_sid";
    private final String STR_CHAR_IDX="char_idx";

    private final int SEND_CATEGORY_IDX = 2;
    private final int SEND_CHAR_NAME = 3;
    private final int SEND_CHAR_SALE_NAME = 4;
    private final int SEND_CHAR_IDX = 5;

    private final int REQUEST_UPLOAD_SUCCESS = 999;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(UpLoadCharacterInputInfoActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1==REQUEST_UPLOAD_SUCCESS){
                        Intent intent = new Intent(UpLoadCharacterInputInfoActivity.this, DlgBtnActivity.class);
                        intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_sale_ok));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, REQUEST_UPLOAD_SUCCESS);
                    }else {
                        arrCharCategory = (ArrayList<TxtListDataInfo>) msg.obj;
                        for (int i = 0; i < arrCharCategory.size(); i++) {
                            if (arrCharCategory.get(i).getStrIdx().startsWith(StaticDataInfo.STRING_N)) {
                                arrCharCategory.remove(i);
                            }
                        }

                        dlgCategory();
                    }
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
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
        setContentView(R.layout.activity_upload_char_input_info);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_upload_info));
        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        Intent intent = getIntent();
        if(intent!=null){
            mCharInfo = (CharacterInfo) intent.getSerializableExtra("CharInfo");
        }

        new CommCode(UpLoadCharacterInputInfoActivity.this, StaticDataInfo.COMMON_CODE_TYPE_CH, PARSER_NUM_1, "", handler);
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        rlCategory = (RelativeLayout) findViewById(R.id.rl_category);
        txtCategory = (TextView) findViewById(R.id.txt_category);
        etName = (EditText) findViewById(R.id.et_input_name); //명칭
        etSaleName = (EditText) findViewById(R.id.et_input_sale_name);
        txtSaleID = (TextView) findViewById(R.id.txt_input_sale_id);
        rlCategory.setOnClickListener(this);

        ((Button) findViewById(R.id.btn_upload)).setOnClickListener(this);
        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        txtSaleID.setText(prefs.getString("LogIn_ID", "").substring(0, 2));
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


    private Dialog mDlg;
    private ListView lvDlgMsg;
    private Button btnCancel;
    public void dlgCategory(){
        mDlg = new Dialog(this);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDlg.setContentView(R.layout.dlg_list_title);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        lvDlgMsg = (ListView) mDlg.findViewById(R.id.lv_dlg);
        btnCancel = (Button) mDlg.findViewById(R.id.btn1);
        btnCancel.setOnTouchListener(this);
        ((LinearLayout) mDlg.findViewById(R.id.ll1)).setOnTouchListener(this);

        CommCodeAdapter dlgAdapter = new CommCodeAdapter(UpLoadCharacterInputInfoActivity.this, R.layout.dlg_list_item, arrCharCategory);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                strCategoryIdx = arrCharCategory.get(position).getStrIdx();
                txtCategory.setText(arrCharCategory.get(position).getStrMsg());
                mDlg.dismiss();
            }
        });
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.ll1 || v.getId() == R.id.btn1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                btnCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if (event.getAction() == MotionEvent.ACTION_UP) {
                btnCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.rl_category) {
            if (mDlg != null && !mDlg.isShowing()) mDlg.show();
        } else if (viewId == R.id.btn_upload) {
            inputDataChk();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    public void inputDataChk(){
        if(txtCategory.getText().toString().trim().equals("")){
            Toast.makeText(UpLoadCharacterInputInfoActivity.this, getResources().getString(R.string.str_category_empty), Toast.LENGTH_SHORT).show();
        }else if (etName.getText().toString().trim().equals("")) {
            Toast.makeText(UpLoadCharacterInputInfoActivity.this, getResources().getString(R.string.str_title_empty), Toast.LENGTH_SHORT).show();
        }else if(etSaleName.getText().toString().trim().equals("")) {
            Toast.makeText(UpLoadCharacterInputInfoActivity.this, getResources().getString(R.string.str_sale_name_empty), Toast.LENGTH_SHORT).show();
        }else{
            DataRequest();
        }
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(){
        final String url=getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_characteres_sha);
        SharedPreferences pref = getSharedPreferences("TokenInfo", MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        k_param.put(SEND_CATEGORY_IDX, strCategoryIdx);
        k_param.put(SEND_CHAR_NAME, etName.getText().toString());
        k_param.put(SEND_CHAR_SALE_NAME, etSaleName.getText().toString());
        k_param.put(SEND_CHAR_IDX, mCharInfo.getStrIdx());

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
                listParams.add(new BasicNameValuePair(STR_CATEGORY_IDX, params[SEND_CATEGORY_IDX]));
                listParams.add(new BasicNameValuePair(STR_CHAR_NAME, params[SEND_CHAR_NAME]));
                listParams.add(new BasicNameValuePair(STR_CHAR_SALE_NAME, params[SEND_CHAR_SALE_NAME]));
                listParams.add(new BasicNameValuePair(STR_CHAR_IDX, params[SEND_CHAR_IDX]));

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
                Message msg = new Message();
                msg.arg1 = REQUEST_UPLOAD_SUCCESS;
                msg.what = StaticDataInfo.RESULT_CODE_200;
                handler.sendMessage(msg);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_UPLOAD_SUCCESS){
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
