package com.cashcuk.loginout;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.common.FindPassword;
import com.cashcuk.dialog.DlgBtnActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 비밀버호 찾기
 */
public class FrFindPwd extends Fragment implements View.OnClickListener {
    private Activity mActivity;

    private EditText etEmail;
    private TextView txtFirstNum;
    private TextView etMiddleNum;
    private TextView etLastNum;

    private String strEmail;
    private String strPhoneNum;


    private final int REQUEST_CODE_EMAIL_PWD = 888; //비밀번호 발송
    private String mPhoneNum="";
    private String mArrPhoneNum[]= new String[3];


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
                case StaticDataInfo.RESULT_NO_USER: //가입되지 않은 이메일
                    Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_no_data), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    Log.d("temp","(String)msg.obj["+(String)msg.obj+"]");
                    String sMsg=(String)msg.obj;
                    i = new Intent(mActivity, DlgBtnActivity.class);

                    String sAlertMessage=getResources().getString(R.string.str_init_pwd,sMsg);

                    i.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
                    i.putExtra("BtnDlgMsg",sAlertMessage);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivityForResult(i, REQUEST_CODE_EMAIL_PWD);
                    break;
                default:
                    break;
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
        View view = inflater.inflate(R.layout.fr_find_pwd_new, null);

        ((Button) view.findViewById(R.id.btn_find_pwd_ok)).setOnClickListener(this);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        txtFirstNum = (TextView) view.findViewById(R.id.txt_first_num);
        etMiddleNum = (TextView) view.findViewById(R.id.et_middle_num);
        etLastNum = (TextView) view.findViewById(R.id.et_last_num);

        mPhoneNum=getArguments().getString("PhoneNum");

        mArrPhoneNum[0]="";
        mArrPhoneNum[1]="";
        mArrPhoneNum[2]="";

        Log.d("temp","***************mPhoneNum["+mPhoneNum+"]************");

        if(mPhoneNum!=null){
            mArrPhoneNum=mPhoneNum.split("-");
        }
        txtFirstNum.setText(mArrPhoneNum[0]);
        etMiddleNum.setText(mArrPhoneNum[1]);
        etLastNum.setText(mArrPhoneNum[2]);


        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_find_pwd_ok) {
            if(!ChkInputEmail()) return;
            ChkInputData();
        }
    }


    /**
     * 이메일 확인
     */
    public boolean ChkInputEmail(){
        String strEmail = etEmail.getText().toString();

        if(strEmail.trim().equals("")) {
            Toast.makeText(mActivity, getResources().getString(R.string.str_input_email_err), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if(ChkEmailType(strEmail)){
                return true;
            }
        }
        return false;
    }


    /**
     * e-mail 형식이 맞는지 체크함.
     * @param email
     */
    public boolean ChkEmailType(String email){
        if(checkEmail(email)) {
            return true;
        } else {
            Toast.makeText(mActivity, R.string.str_email_type_err, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 이메일 정규식 체크
     * @param email
     * @return true : 이메일 형식에 맞음, false : 이메일 형식에 맞지 않음
     */
    private boolean checkEmail(String email)
    {
        String mail = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(mail);
        Matcher m = p.matcher(email);

        return m.matches();
    }


    /**
     * 서버로 전송하는 값 (비밀번호 찾기)
     */
    public void ChkInputData(){
        strEmail = etEmail.getText().toString();
        strPhoneNum = txtFirstNum.getText().toString()+"-"+etMiddleNum.getText().toString()+"-"+etLastNum.getText().toString();

        if (etMiddleNum.getText().toString().trim().equals("") || etLastNum.getText().toString().trim().equals("")) {
            Toast.makeText(mActivity, getResources().getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show();
            return;
        }

        new FindPassword(mActivity, strPhoneNum,strEmail, handler);
    }



}
