package com.cashcuk.advertiser.sendpush;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.dialog.DlgDatePicker;
import com.cashcuk.membership.txtlistdata.TxtPushTargetInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 광고 대상 - 2. 발송대상 (세부 설정)
 */
public class ADTargetChkLinear2 extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater inflater;

    private String pushTargetSetInfo;
    private TxtPushTargetInfo returnTargetInfo;

    // 년월일, 시분 저장소
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private SimpleDateFormat todayFormatter;

    public static final int REQUEST_DATE = 2222;
    public static final int REQUEST_TIME = 4444;

    public ADTargetChkLinear2(Context context) {
        super(context);
        mContext = context;
        init();
    }

    /**
     * 생성자
     * @param context
     * @param attrs
     */
    public ADTargetChkLinear2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private TextView txtTotalCost;
    /**
     * layout 구성
     */
    public void init() {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.advertiser_push_target_chk, this, true);

        ((LinearLayout) findViewById(R.id.ll_push_date)).setOnClickListener(this);
        txtDate = (TextView) findViewById(R.id.txt_push_date);
        txtTime = (TextView) findViewById(R.id.txt_push_time);
        ((LinearLayout) findViewById(R.id.ll_push_state)).setVisibility(View.GONE);
        ((EditText)findViewById(R.id.et_push_send_cnt)).addTextChangedListener(textWatcher);

        txtTotalCost = (TextView)findViewById(R.id.txt_push_send_total_cost);

        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        timeFormatter = new SimpleDateFormat("HH");
        todayFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        WebView wbInfo = (WebView) findViewById(R.id.wv_info_msg);
        wbInfo.getSettings().setJavaScriptEnabled(true);
        wbInfo.setBackgroundColor(0);
        wbInfo.loadUrl(getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_link_push_info));

        makePush();
    }

    public void makePush(){
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        setDate(REQUEST_DATE, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * 날짜 표시
     */
    public void setDate(int mMode, int mYear, int mMonth, int mDay, int mHour) {
        calendar.set(mYear, mMonth, mDay);
        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        Date date = calendar.getTime();
        String dateResult = dateFormatter.format(date);
        String timeResult = timeFormatter.format(date);
        String[] strDate = dateResult.split("-");

        if (mMode == REQUEST_DATE) {
            txtDate.setText(dateResult);
            txtTime.setText(timeResult);
        }


        if (mMode == REQUEST_TIME) {
            chkDateTime(mMode, dateResult, timeResult);
        }
    }

    /**
     * 날짜, 시간 비교
     *
     * @param mode       비교할 mode
     * @param dateResult 날짜
     * @param timeResult 시간
     */
    public void chkDateTime(int mode, String dateResult, String timeResult) {
        if (mode == REQUEST_TIME) {
            try {
                // 현재 시간을 msec으로 구한다.
                long now = System.currentTimeMillis();
                // 현재 시간을 저장 한다.
                Date today = new Date(now);
                today = todayFormatter.parse(todayFormatter.format(today));

                int mCompare = today.compareTo(calendar.getTime());
                if (mCompare >= 0) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.str_date_err), Toast.LENGTH_SHORT).show();
                } else {
                txtDate.setText(dateResult);
                    txtTime.setText(timeResult);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return 날짜
     */
    public String getDate(){
        return txtDate.getText().toString();
    }

    /**
     * @return 시간
     */
    public String getTime(){
        return txtTime.getText().toString();
    }

    public void setPage(String pushTargetSetInfo, TxtPushTargetInfo returnTargetInfo) {
        this.pushTargetSetInfo = pushTargetSetInfo;
        this.returnTargetInfo = returnTargetInfo;

        ((TextView)findViewById(R.id.txt_change_money)).setText(StaticDataInfo.makeStringComma(returnTargetInfo.getStrAmnt()));// 충전금
        ((TextView)findViewById(R.id.txt_push_target_cnt)).setText(StaticDataInfo.makeStringComma(returnTargetInfo.getStrTargetNum()));// 대상자 수
        ((TextView)findViewById(R.id.txt_push_send_amount)).setText(StaticDataInfo.makeStringComma(returnTargetInfo.getStrUnitAmnt()));// 발송 단가
    }

    TextWatcher textWatcher = new TextWatcher() {
        private boolean mSelfChange = false;    // 리스너 무한루프 방지
        @Override
        public void afterTextChanged(Editable editable) {
            String s = editable.toString();
            if (s.equals("") || s == null) {
                txtTotalCost.setText("");
                return;
            }
            if (mSelfChange) return;

            mSelfChange = true;
            long editValue = Long.parseLong(editable.toString().replace(",", ""));
            long targetCount = Long.parseLong(returnTargetInfo.getStrTargetNum().replace(",", ""));
            long unitAmnt = Long.parseLong(returnTargetInfo.getStrUnitAmnt().replace(",", ""));
            long amnt = Long.parseLong(returnTargetInfo.getStrAmnt().replace(",", ""));

            // 1보다 작은 수는 올 수 없음
            if (editValue < 1) return;

            // 발송건수는 대상자 수보다 클 수 없음
            if (editValue > targetCount) {
                editValue = targetCount;
            }

            // 총 발송 금액이 충전금보다 커질 수 없음
            if ((editValue*unitAmnt) > amnt) {
                long canTargetCount = amnt/unitAmnt;

                if (canTargetCount > targetCount) {
                    canTargetCount = targetCount;
                }
                editValue = canTargetCount;
            }

            EditText editText = (EditText)findViewById(R.id.et_push_send_cnt);
            editText.setText(""+editValue);
            editText.setSelection(editText.getText().length());
            txtTotalCost.setText(StaticDataInfo.makeStringComma("" + (editValue * unitAmnt)));
            setData(""+editValue);
            mSelfChange = false;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (mSelfChange) return;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (mSelfChange) return;
        }
    };

    private ADTargetChkLinear2.OnGetData mGetData;

    private TextView txtDate; //요청 날짜
    private TextView txtTime; //요청 시간
    @Override
    public void onClick(View v) {
        String[] strData;
        Intent intent = new Intent(mContext, DlgDatePicker.class);
        strData = txtDate.getText().toString().split("-");
        intent.putExtra("Year", Integer.parseInt(strData[0]));
        intent.putExtra("Month", Integer.parseInt(strData[1]));
        intent.putExtra("Day", Integer.parseInt(strData[2]));

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ((Activity) mContext).startActivityForResult(intent, REQUEST_DATE);
    }

    // 이벤트 인터페이스를 정의
    public interface OnGetData {
        public void onGetData(String pushSendCnt);
    }
    public void getOnData(ADTargetChkLinear2.OnGetData getSendData)
    {
        mGetData = getSendData;
    }


    /**
     * 세부이미지 추가 사항에 대한 list 구성 (이미지 전달 data 필요)
     */
    public void setData(String pushSendCntData){
        mGetData.onGetData(pushSendCntData);
    }
}
