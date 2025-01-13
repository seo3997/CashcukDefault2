package com.cashcuk.loginout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgFirstPhoneNumActivity;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * E-mail 찾기
 */
public class FrFindEmail extends Fragment implements View.OnClickListener {
    private Activity mActivity;

    private EditText etName;
    private static TextView txtEmailFirstNum;
    private EditText etEmailMiddleNum;
    private EditText etEmailLastNum;
    private TextView txtYear;
    private TextView txtMonth;
    private TextView txtDay;

    private String strName;
    private String strBirthDate;
    private String strBirthDateYear;
    private String strBirthDateMonth;
    private String strBirthDateDay;
    private String strEmailPhoneNum;

    private final int SEND_NAME = 1;
    private final int SEND_BIRTH_DATE = 2;
    private final int SEND_HP = 3;

    private final String STR_NAME = "nm";
    private final String STR_BIRTH_DATE = "birth";
    private final String STR_HP = "hp";
    private final String STRING_MAIL = "mail_id"; // email

    private static final int SET_YEAR = 0;
    private static final int SET_MONTH = 1;
    private static final int SET_DAY = 2;

    private final int REQUEST_CODE_FIRST_NUM = 999;
    private final int REQUEST_CODE_FIND_EMAIL = 777;

    private final String STR_RADIO_TYPE_USER = "U";
    private final String STR_RADIO_TYPE_ADVERTISER = "A";
    private String strUType=STR_RADIO_TYPE_USER; //일반 or 광고주

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent i = null;
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(mActivity, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    i = new Intent(mActivity, DlgBtnActivity.class);
                    i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_find_no_email));
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    i = new Intent(mActivity, DlgBtnActivity.class);
                    i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_find_email_ok) + (String) msg.obj);
                    i.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivityForResult(i, REQUEST_CODE_FIND_EMAIL);
                    return;
            }

            if (i != null) {
                startActivity(i);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(mActivity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_find_email, null);

        etName=(EditText) view.findViewById(R.id.et_name);
        txtEmailFirstNum = (TextView) view.findViewById(R.id.txt_first_num);
        etEmailMiddleNum = (EditText) view.findViewById(R.id.et_middle_num);
        etEmailLastNum = (EditText) view.findViewById(R.id.et_last_num);
        txtYear = (TextView) view.findViewById(R.id.txt_year);
        txtMonth = (TextView) view.findViewById(R.id.txt_month);
        txtDay = (TextView) view.findViewById(R.id.txt_day);
        txtEmailFirstNum.setOnClickListener(this);
        txtYear.setOnClickListener(this);
        txtMonth.setOnClickListener(this);
        txtDay.setOnClickListener(this);

        ((Button) view.findViewById(R.id.btn_find_ok)).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_find_ok) {
            ChkInputData();
        } else if (viewId == R.id.txt_first_num) {
            Intent intent = new Intent(mActivity, DlgFirstPhoneNumActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.startActivityForResult(intent, REQUEST_CODE_FIRST_NUM);
        } else if (viewId == R.id.txt_year) {
            setDate(SET_YEAR);
        } else if (viewId == R.id.txt_month) {
            setDate(SET_MONTH);
        } else if (viewId == R.id.txt_day) {
            setDate(SET_DAY);
        }
    }

    /**
     * date list에 뿌려질 data set
     */
    public void setDate(int mDateMode){
        Calendar cal = Calendar.getInstance();
        ArrayList<String> arrDate = new ArrayList<>();
        int startDate = -1;
        int endDate = -1;

        if(mDateMode == SET_YEAR){
            startDate = cal.get(Calendar.YEAR)-1;
            endDate = startDate-110;

            for(int i=startDate; i>endDate; i--){
                arrDate.add(String.valueOf(startDate--));
            }
        }else if(mDateMode == SET_MONTH){
            startDate = 1;
            endDate = 12;

            for(int i=startDate; i<=endDate; i++){
                if(String.valueOf(startDate).length()<2) {
                    arrDate.add("0"+String.valueOf(startDate++));
                }else{
                    arrDate.add(String.valueOf(startDate++));
                }
            }
        }else if(mDateMode == SET_DAY){
            if(txtMonth.getText().equals("")){
                Toast.makeText(mActivity, getResources().getString(R.string.str_sel_month_err), Toast.LENGTH_SHORT).show();
            }else {
                cal.set(Calendar.MONTH, Integer.parseInt(txtMonth.getText().toString()) - 1);

                startDate = 1;
                endDate = cal.getActualMaximum(Calendar.DATE);

                for(int i=startDate; i<=endDate; i++){
                    if(String.valueOf(startDate).length()<2) {
                        arrDate.add("0"+String.valueOf(startDate++));
                    }else{
                        arrDate.add(String.valueOf(startDate++));
                    }
                }
            }
        }

        if(arrDate!=null && arrDate.size()>0){
            openDateDlg(mDateMode, arrDate);
        }
    }

    /**
     * 생년월일 선택 dialog
     * @param mDateMode //년, 월, 일 모드
     * @param arrDate //년, 월, 일 범위
     */
    public void openDateDlg(final int mDateMode, ArrayList<String> arrDate){
        // List Adapter 생성
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1);
        adapter.addAll(arrDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDateMode == SET_YEAR) txtYear.setText(adapter.getItem(which));
                if (mDateMode == SET_MONTH) txtMonth.setText(adapter.getItem(which));
                if (mDateMode == SET_DAY) txtDay.setText(adapter.getItem(which));
            }
        });

        builder.show();
    }

    /**
     * 서버로 전송하는 값
     */
    public void ChkInputData(){
        strName = etName.getText().toString();
        strBirthDateYear = txtYear.getText().toString();
        strBirthDateMonth = txtMonth.getText().toString();
        strBirthDateDay = txtDay.getText().toString();
        strBirthDate = strBirthDateYear+"-"+strBirthDateMonth+"-"+strBirthDateDay;
        strEmailPhoneNum = txtEmailFirstNum.getText().toString()+"-"+etEmailMiddleNum.getText().toString()+"-"+etEmailLastNum.getText().toString();

        if(strName.trim().equals("")){
            Toast.makeText(mActivity, getResources().getString(R.string.str_input_name), Toast.LENGTH_SHORT).show();
            return;
        }else if(strBirthDateYear.trim().equals("") || strBirthDateMonth.trim().equals("") || strBirthDateDay.trim().equals("")){
            Toast.makeText(mActivity, getResources().getString(R.string.str_input_birthdate), Toast.LENGTH_SHORT).show();
            return;
        }else if(etEmailMiddleNum.getText().toString().trim().equals("") || etEmailLastNum.getText().toString().trim().equals("")){
            Toast.makeText(mActivity, getResources().getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show();
            return;
        }

        final String url = getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_member_mail);

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();

        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(SEND_NAME, strName);
        k_param.put(SEND_BIRTH_DATE, strBirthDate);
        k_param.put(SEND_HP, strEmailPhoneNum);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        new FindEmailTask().execute(strTask);
    }

    /**
     * 서버에 값 요청
     */
    private class FindEmailTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);
                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(STR_NAME, params[SEND_NAME]));
                listParams.add(new BasicNameValuePair(STR_BIRTH_DATE, params[SEND_BIRTH_DATE]));
                listParams.add(new BasicNameValuePair(STR_HP, params[SEND_HP]));

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
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))) {
                msg.what = StaticDataInfo.RESULT_NO_DATA;
            }else if(result.startsWith(StaticDataInfo.TAG_LIST)){
                ResultEmail(result);
                return;
            }else{
                msg.what = StaticDataInfo.RESULT_CODE_ERR;
            }

            if(handler!=null && msg!=null) {
                handler.sendMessage(msg);
            }
        }
    }

    /**
     * 메일 찾기 성공 data parsing
     * @param result
     */
    public void ResultEmail(String result) {
        final int DEFAULT_NUM = -1;
        final int PARSER_NUM_0 = 0;
        String strEmail="";

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
                        if (parser.getName().equals(STRING_MAIL)) {
                            k_data_num = PARSER_NUM_0;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;


                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    strEmail = parser.getText().toString();
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }
            Message msg = new Message();
            msg.what = StaticDataInfo.RESULT_CODE_200;
            msg.obj = strEmail;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getActivityResult(String date){
        txtEmailFirstNum.setText(date);
    }
}
