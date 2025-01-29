package com.cashcuk.advertiser.makead;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.common.CommCode;
import com.cashcuk.common.CommCodeAdapter;
import com.cashcuk.common.GetImage;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgDatePicker;
import com.cashcuk.findaddr.FindAddressActivity;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * 광고제작 - 1. 세부정보
 */
public class MakeADDetail1 extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater inflater;

    private EditText etADName;
    private EditText etADDetail; //상세설명
    private EditText etADAmount; //광고 할 금액
    private EditText etADSavePoint; //적립포인트
    private LinearLayout llADSavePoint;
    private TextView txtADAddress; //주소
    private TextView etADHomePage; //홈페이지
    private EditText etADRecommend; //광고주 추천 ID
    private TextView txtStartDate; //시작 날짜
    private TextView txtEndDate; //종료 날짜
    private TextView txtStartTime; //시작 시간
    private TextView txtEndTime; //종료 시간
    private TextView txtAmountInfo2; //광고 중지 기준 설명 text

    private RadioButton rbHaveEvent;
    private RadioButton rbNoEvent;

    private LinearLayout llCategory; //카테고리 view(1, 2, 3차)
    private LinearLayout llLowCategory;
    private int mCategoryCnt = 0; //카테고리 추가 개수

    private boolean isSelSiGun = false;

    private final int CATEGORY_1 = 1;
    private final int CATEGORY_2 = 2;
    private final int CATEGORY_3 = 3;

    private ArrayList<TxtListDataInfo> arrData1; //1차 분류 data
    private ArrayList<TxtListDataInfo> arrData2; //2차 분류 data
    private ArrayList<TxtListDataInfo> arrData3; //3차 분류 data

    private String strCategory2 = "";

    public static final int REQUEST_CODE_ADDRESS = 1111;
    public static final int REQUEST_DATE_S = 2222;
    public static final int REQUEST_DATE_E = 3333;
    public static final int REQUEST_TIME_S = 4444;
    public static final int REQUEST_TIME_E = 5555;
    public static final int REQUEST_CHARGE_ERR = 6666;
    public static final int REQUEST_CHK_AD_AMOUNT = 1234;
    public static final int REQUEST_CHK_AD_SAVE_POINT = 5678;

    // 년월일, 시분 저장소
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private SimpleDateFormat todayFormatter;

    //입력데이터 저장 index
    public final int INDEX_AD_NAME = 0; //광고명
    public final int INDEX_AD_DETAIL = 1; //광고 상세설명
    public final int INDEX_AD_AMOUNT = 2; //광고 할 금액
    public final int INDEX_AD_SAVE_POINT = 3; //적립 할 포인트
    public final int INDEX_AD_DATE_S = 4; //광고기간
    public final int INDEX_AD_DATE_E = 5; //광고기간
    public final int INDEX_AD_MY_CHARGE = 6; //충전금
    public final int INDEX_AD_ADDRESS = 7; //주소
    public final int INDEX_AD_HOMEPAGE = 8; //홈페이지
    public final int INDEX_AD_RECOMMEND = 9; //영업광고주
    public final int INDEX_AD_EVENT = 10; //이벤트 여부
    public final int INDEX_AD_REJUDGED = 11; //재심사 여부
    public final int INDEX_AD_UP_COST = 12; //증액 금액

    private ModifyADInfo mModifyInfo;
    private boolean isRejudged = false; //재심사 여부
    public static final String STR_NOT_MODIFY = "M" ; //수정모드 아님
    private String strUpCost = ""; //증액 금액

    public final static String STR_PUT_AD_IDX = "AD_IDX";
    public final static String STR_PUT_AD_NAME = "AD_NAME";
    public final static String STR_PUT_AD_DETAIL = "AD_DETAIL";
    public final static String STR_PUT_AD_CATEGORY = "AD_CATEGORY";
    public final static String STR_PUT_AD_AMOUNT = "AD_AMOUNT";
    public final static String STR_PUT_AD_SAVE_POINT = "AD_SAVE_POINT";
    public final static String STR_PUT_AD_DATE_S = "AD_DATE_START";
    public final static String STR_PUT_AD_DATE_E = "AD_DATE_END";
    public final static String STR_PUT_AD_ADDR = "AD_ADDRESS";
    public final static String STR_PUT_AD_HOMEPAGE = "AD_HOMEPAGE";
    public final static String STR_PUT_AD_RECOMMEND = "AD_RECOMMEND";
    public final static String STR_PUT_AD_EVENT = "AD_EVENT";
    public final static String STR_PUT_AD_STATUS = "AD_STATUS";
    public final static String STR_PUT_AD_UP_COST = "AD_UP_COST";
    public final static String STR_PUT_AD_REJUDGED = "AD_REJUDGED";
    public final static String STR_PUT_AD_MY_CHARGE = "AD_MY_CHARGE";

    private InputMethodManager imm=null;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(msg.arg1 == CATEGORY_2){
                        if(isCategoryClick) Toast.makeText(mContext, getResources().getString(R.string.str_select_empty), Toast.LENGTH_SHORT).show();
                    }

                    if (arrSelCategoryChk.size()>0) {
                        arrSelCategoryChk.set((Integer)vgCategory.getTag(), true);
                    }
                    break;
                case StaticDataInfo.RESULT_NO_SIGUN:
                    isSelSiGun = true;
                    if (arrSelCategoryChk.size()>0) {
                        arrSelCategoryChk.set((Integer)vgCategory.getTag(), true);
                    }

                    if(isCategoryClick) Toast.makeText(mContext, getResources().getString(R.string.str_select_empty), Toast.LENGTH_SHORT).show();
                    if (arrData3 != null) arrData3.clear();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if (msg.arg1 == CATEGORY_1 && ((ArrayList<TxtListDataInfo>) msg.obj).size() > 0) {
                        arrData1 = new ArrayList<TxtListDataInfo>();
                        arrData1.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                    } else if (msg.arg1 == CATEGORY_2){
                            if(((ArrayList<TxtListDataInfo>) msg.obj).size() > 0) {
                                arrData2 = new ArrayList<TxtListDataInfo>();
                                arrData2.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                            }
                    } else if (msg.arg1 == CATEGORY_3){
                        if(((ArrayList<TxtListDataInfo>) msg.obj).size() > 0) {
                            arrData3 = new ArrayList<TxtListDataInfo>();
                            arrData3.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                        }
                    }

                    if(msg.arg1!=CATEGORY_1 && isCategoryClick){
                        isCategoryClick = false;
                        OpenDlg(msg.arg1, vgCategory);
                    }
                    break;

            }
        }
    };

    public MakeADDetail1(Context context) {
        super(context);
        mContext = context;
        Init();
    }

    /**
     * 생성자
     * @param context
     * @param attrs
     */
    public MakeADDetail1(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        Init();
    }

    public void modifyData(ModifyADInfo data){
        mModifyInfo = data;

        if(mModifyInfo==null) {
            makeAD();
        }else{
            modifyAD();
        }
    }

    /**
     * layout 구성
     */
    private void Init(){
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.advertiser_make_ad_detail, this, true);

        txtStartDate = (TextView) findViewById(R.id.txt_start_date);
        txtStartTime = (TextView) findViewById(R.id.txt_start_time);
        txtEndDate = (TextView) findViewById(R.id.txt_end_date);
        txtEndTime = (TextView) findViewById(R.id.txt_end_time);
        ((LinearLayout) findViewById(R.id.ll_start_date)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.ll_end_date)).setOnClickListener(this);

        etADName = (EditText) findViewById(R.id.et_input_ad_name);
        etADDetail = (EditText) findViewById(R.id.et_input_ad_detail);
        etADAmount = (EditText) findViewById(R.id.et_input_ad_amount);
        etADAmount.addTextChangedListener(mTextWatcher);
        etADAmount.setOnFocusChangeListener(mFocusChange);
        llADSavePoint = (LinearLayout) findViewById(R.id.ll_input_save_point);
        etADSavePoint = (EditText) findViewById(R.id.et_input_save_point);
        etADSavePoint.addTextChangedListener(mTextWatcher);
        etADSavePoint.setOnFocusChangeListener(mFocusChange);
        txtADAddress = (TextView) findViewById(R.id.txt_make_ad_address);
        etADHomePage = (TextView) findViewById(R.id.et_make_ad_homepage);
        etADRecommend = (EditText) findViewById(R.id.et_input_ad_recommend);
        txtStartDate = (TextView) findViewById(R.id.txt_start_date);
        txtEndDate = (TextView) findViewById(R.id.txt_end_date);
        txtStartTime = (TextView) findViewById(R.id.txt_start_time);
        txtEndTime = (TextView) findViewById(R.id.txt_end_time);
        txtAmountInfo2 = (TextView) findViewById(R.id.txt_amount_info2);

        rbHaveEvent = (RadioButton) findViewById(R.id.rb_have_event);
        rbNoEvent = (RadioButton) findViewById(R.id.rb_no_event);

        llCategory = (LinearLayout) findViewById(R.id.ll_category);
        ((Button) findViewById(R.id.btn_make_ad_detail_next)).setOnClickListener(mNextInfo);
        ((Button) findViewById(R.id.btn_add)).setOnClickListener(this);

        txtADAddress.setOnClickListener(this);
        ((Button) findViewById(R.id.btn_find_address)).setOnClickListener(this);

        new CommCode(mContext, StaticDataInfo.COMMON_CODE_TYPE_AD, CATEGORY_1, "", handler);

        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        timeFormatter = new SimpleDateFormat("HH:mm");
        todayFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void makeAD(){
        categoryAdd(null);//카테고리 default로 1개 추가
        setDate(REQUEST_DATE_S, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        calendar.add(Calendar.DAY_OF_MONTH, +6);
        setDate(REQUEST_DATE_E, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59);
    }

    private String strAmount = ""; //광고 할 금액(사용 후 남은 금액)
    private boolean isModify = false;
    public void modifyAD(){
        //광고 소비액
        LinearLayout llUseCost = (LinearLayout) findViewById(R.id.ll_use_cost);
        llUseCost.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.txt_ad_use_cost)).setText(StaticDataInfo.makeStringComma(mModifyInfo.getStrADUseCost()));

        isModify = true;
        etADName.setText(mModifyInfo.getStrADNanme());
        etADDetail.setText(mModifyInfo.getStrADDetail());
        strAmount = mModifyInfo.getStrADAmount();
        etADAmount.setText(StaticDataInfo.makeStringComma(strAmount));
        etADSavePoint.setEnabled(false);
        GetImage getImg = new GetImage(mContext);
        Bitmap bitmap  = BitmapFactory.decodeResource(getResources(), R.drawable.fixed2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            llADSavePoint.setBackground(getImg.DrawableNinePatch(bitmap));
        }else{
            llADSavePoint.setBackgroundDrawable(getImg.DrawableNinePatch(bitmap));
        }
        etADSavePoint.setText(StaticDataInfo.makeStringComma(mModifyInfo.getStrADSavePoint()));
        txtADAddress.setText(mModifyInfo.getStrADAddress());
        etADHomePage.setText(mModifyInfo.getStrADHomePage());
        etADRecommend.setText(mModifyInfo.getStrADSeller());
        String[] strDateS = mModifyInfo.getStrADDateS().split(mContext.getResources().getString(R.string.str_category_item_gubun));
        String[] strDateE = mModifyInfo.getStrADDateE().split(mContext.getResources().getString(R.string.str_category_item_gubun));
        txtStartDate.setText(strDateS[0]);
        txtEndDate.setText(strDateE[0]);
        txtStartTime.setText(strDateS[1]);
        txtEndTime.setText(strDateE[1]);

        if(!etADRecommend.getText().toString().equals("")){
            etADRecommend.setFocusable(false);
        }

        etADName.addTextChangedListener(mTextWatcher);
        etADDetail.addTextChangedListener(mTextWatcher);
        etADHomePage.addTextChangedListener(mTextWatcher);

        String strCategoryTmp = mModifyInfo.getStrADCategory();

        ArrayList<TxtListDataInfo> arrCategoryData = new ArrayList<TxtListDataInfo>();
        if (!strCategoryTmp.startsWith("0")) {

            String[] arrCategoryTmp = strCategoryTmp.split(":");
            for (int i = 0; i < arrCategoryTmp.length; i++) {
                categoryAdd(arrCategoryTmp[i]);
            }
        }

        /*
        if(mModifyInfo.getStrADEvent().equals(StaticDataInfo.STRING_Y)){
            rbHaveEvent.setChecked(true);
        }else{
            rbNoEvent.setChecked(true);
        }
         */
    }

    View view = null;
    public void showKeyboard(boolean isAmount){

        if(isAmount){
            etADAmount.requestFocus();
            etADAmount.setFocusable(true);
            etADSavePoint.clearFocus();

            view = etADAmount;
        }else{
            etADSavePoint.requestFocus();
            etADSavePoint.setFocusable(true);
            etADAmount.clearFocus();

            view = etADSavePoint;
        }

        if(view!=null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //키보드 보이게 하는 부분
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    isFocus = false;
                }
            }, 300);
        }
    }

    /**
     * 광고할 금액 chk
     */
    public void chkAmount(){
        Intent intent = new Intent(mContext, DlgBtnActivity.class);
        String strMsg="";

        String strInputAmount = etADAmount.getText().toString();
        strInputAmount = strInputAmount.replace(",", ""); //광고 할 금액
        strChargeMoney = strChargeMoney.replace(",", ""); //충전금

        if (strInputAmount.trim().equals("")
                || strInputAmount.equals("0") || (Long.parseLong(strInputAmount.replaceAll(",", "")) < Long.parseLong(strMinAmount.replaceAll(",", "")))) {
            strMsg = String.format(getResources().getString(R.string.str_make_ad_amount_reinput), StaticDataInfo.makeStringComma(strMinAmount));
        } else {
            long mInputAmount = Long.parseLong(strInputAmount); //입력한 광고 할 금액
            long mChargeMoney = Long.parseLong(strChargeMoney); //충전금

            if(!isModify){
                //광고 할 금액을 충전금보다 많이 입력했을 경우
                if (mInputAmount > mChargeMoney) {
                    strMsg = String.format(getResources().getString(R.string.str_make_ad_charge_err), StaticDataInfo.makeStringComma(strChargeMoney));
                }
            }else{
                //광고 할 금액 수정 시 남은 광고 금액보다 적은 금액 입력한 건지 chk
                if (strAmount != null && !strAmount.trim().equals("")) {
                    long adCost = Long.parseLong(strAmount.replace(",", "")); //남은 광고 금액
                    String cost = etADAmount.getText().toString().replace(",", ""); //수정한 광고 할 금액
                    if (cost != null && !cost.trim().equals("")) {
                        long adInputCost = Long.parseLong(cost); //수정한 광고 할 금액
                        if (adInputCost < adCost) {
                            strMsg = getResources().getString(R.string.str_make_ad_amount_input_err);
                        } else {
                            long upCost = adInputCost - adCost; //증액 금액
                            if (mChargeMoney < upCost) {
                                strMsg = String.format(getResources().getString(R.string.str_make_ad_charge_err), StaticDataInfo.makeStringComma(strChargeMoney));
                            }else{
                                strUpCost = String.valueOf(upCost);
                            }
                        }
                    }

                }
            }
        }

        if (!strMsg.equals("")){
            intent.putExtra("BtnDlgMsg", strMsg);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Activity) mContext).startActivityForResult(intent, REQUEST_CHK_AD_AMOUNT);
        }
    }

    /**
     * 적립포인트 chk
     */
    public void chkSavePoint(){
        Intent intent = new Intent(mContext, DlgBtnActivity.class);
        String strMsg="";

        String strInputAmount = etADAmount.getText().toString();
        String strInputSavePoint = etADSavePoint.getText().toString();
        strInputAmount = strInputAmount.replace(",", ""); //광고 할 금액
        strInputSavePoint = strInputSavePoint.replace(",", ""); //적립포인트

        if (strInputAmount.trim().equals("")
                || strInputAmount.equals("0") || (Long.parseLong(strInputAmount.replaceAll(",", "")) < Long.parseLong(strMinAmount.replaceAll(",", "")))) {
            isFocus = true;
            strMsg = String.format(getResources().getString(R.string.str_make_ad_amount_reinput), StaticDataInfo.makeStringComma(strMinAmount));
            etADSavePoint.setText("");
            intent.putExtra("BtnDlgMsg", strMsg);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Activity) mContext).startActivityForResult(intent, REQUEST_CHK_AD_AMOUNT);
            return;
        }

        if(strInputSavePoint.trim().equals("")
                || strInputSavePoint.equals("0") || (Long.parseLong(strInputSavePoint.replaceAll(",", "")) < Long.parseLong(strMinSavePoint.replaceAll(",", "")))) {
            strMsg = String.format(getResources().getString(R.string.str_make_ad_save_point_reinput), StaticDataInfo.makeStringComma(strMinSavePoint));
        } else if (!(strInputSavePoint.substring(strInputSavePoint.length() - 1, strInputSavePoint.length()).equals("0"))) {
            strMsg = getResources().getString(R.string.str_input_save_point_err);
        } else {
            long mInputAmount = Long.parseLong(strInputAmount); //입력한 광고 할 금액
            long mInputSavePoint = Long.parseLong(strInputSavePoint); //적립포인트

            if (mInputAmount < mInputSavePoint) {
                strMsg = getResources().getString(R.string.str_make_ad_save_point_err1);
            }
        }

        if (!strMsg.equals("")){
            intent.putExtra("BtnDlgMsg", strMsg);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Activity) mContext).startActivityForResult(intent, REQUEST_CHK_AD_SAVE_POINT);
        }
    }

    /**
     * 날짜 표시
     */
    public void setDate(int mMode, int mYear, int mMonth, int mDay, int mHour, int mMin) {
        calendar.set(mYear, mMonth, mDay, mHour, mMin);
        Date date = calendar.getTime();
        String dateResult = dateFormatter.format(date);
        String timeResult = timeFormatter.format(date);
        String[] strDate = dateResult.split("-");
        String[] strTime = timeResult.split(":");

        if (mMode == REQUEST_DATE_S) {
            txtStartDate.setText(dateResult);
            txtStartTime.setText(timeResult);
        } else if (mMode == REQUEST_DATE_E) {
            txtEndDate.setText(dateResult);
            txtEndTime.setText(timeResult);
        }

        if (mMode == REQUEST_TIME_S || mMode == REQUEST_TIME_E) {
            chkDateTime(mMode, dateResult, timeResult);
        }
    }

    /**
     * 광고 시작 date
     * @return 시작 날짜
     */
    public String getSDate(){
        return txtStartDate.getText().toString();
    }

    /**
     * 광고 시작 시간
     * @return 시작 시간
     */
    public String getSTime(){
        return txtStartTime.getText().toString();
    }

    /**
     * 광고 종료 date
     * @return 종료 날짜
     */
    public String getEDate(){
        return txtEndDate.getText().toString();
    }

    /**
     * 광고 종료 시간
     * @return 종료 시간
     */
    public String getETime(){
        return txtEndTime.getText().toString();
    }

    /**
     * 날짜, 시간 비교
     *
     * @param mode       비교할 mode
     * @param dateResult 날짜
     * @param timeResult 시간
     */
    public void chkDateTime(int mode, String dateResult, String timeResult) {
        if (mode == REQUEST_TIME_S) {
            try {
                // 현재 시간을 msec으로 구한다.
                long now = System.currentTimeMillis();
                // 현재 시간을 저장 한다.
                Date today = new Date(now);
                today = todayFormatter.parse(todayFormatter.format(today));

                int mCompare = today.compareTo(calendar.getTime());
                if (mCompare > 0) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.str_date_start_err), Toast.LENGTH_SHORT).show();
                } else {
                    txtStartDate.setText(dateResult);
                    txtStartTime.setText(timeResult);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (mode == REQUEST_TIME_E) {
            try {
                Date sDate = todayFormatter.parse(txtStartDate.getText().toString() + " " + txtStartTime.getText().toString());

                int mCompare = sDate.compareTo(calendar.getTime());
                if (mCompare > 0) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.str_date_end_err), Toast.LENGTH_SHORT).show();
                } else {
                    txtEndDate.setText(dateResult);
                    txtEndTime.setText(timeResult);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int mRequestCode = -1;
        String[] strData;

        int viewId = v.getId();
        if (viewId == R.id.btn_add) {
            categoryAdd(null);
        } else if (viewId == R.id.btn_find_address || viewId == R.id.txt_make_ad_address) {
            intent = new Intent(mContext, FindAddressActivity.class);
            mRequestCode = REQUEST_CODE_ADDRESS;
        } else if (viewId == R.id.ll_start_date) {
            intent = new Intent(mContext, DlgDatePicker.class);
            strData = txtStartDate.getText().toString().split("-");
            intent.putExtra("Year", Integer.parseInt(strData[0]));
            intent.putExtra("Month", Integer.parseInt(strData[1]));
            intent.putExtra("Day", Integer.parseInt(strData[2]));
            mRequestCode = REQUEST_DATE_S;
        } else if (viewId == R.id.ll_end_date) {
            intent = new Intent(mContext, DlgDatePicker.class);
            strData = txtEndDate.getText().toString().split("-");
            intent.putExtra("Year", Integer.parseInt(strData[0]));
            intent.putExtra("Month", Integer.parseInt(strData[1]));
            intent.putExtra("Day", Integer.parseInt(strData[2]));
            mRequestCode = REQUEST_DATE_E;
        }
        if (intent != null && mRequestCode != -1) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Activity) mContext).startActivityForResult(intent, mRequestCode);
        }
    }

    private SparseArray<View> views = new SparseArray<View>();
    private ArrayList<Boolean> arrSelCategoryChk = new ArrayList<Boolean>();
    private ArrayList<String[]> arrCategoryChk = new ArrayList<String[]>();
    private String[] strCategoryIndex = {"", "", ""};
    /**
     * 카테고리 view(1,2,3차 항목) 추가
     */
    public void categoryAdd(String strData) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.advertiser_make_ad_category, null);
        view.setTag(mCategoryCnt);

        RelativeLayout rlCategory1 = (RelativeLayout) view.findViewById(R.id.rl_category1);
        RelativeLayout rlCategory2 = (RelativeLayout) view.findViewById(R.id.rl_category2);
        RelativeLayout rlCategory3 = (RelativeLayout) view.findViewById(R.id.rl_category3);
        rlCategory1.setOnClickListener(mCategoryClick);
        rlCategory2.setOnClickListener(mCategoryClick);
        rlCategory3.setOnClickListener(mCategoryClick);
        rlCategory1.setTag(mCategoryCnt);
        rlCategory2.setTag(mCategoryCnt);
        rlCategory3.setTag(mCategoryCnt);

        ImageButton ibCategory1 = (ImageButton) view.findViewById(R.id.ib_category1);
        ImageButton ibCategory2 = (ImageButton) view.findViewById(R.id.ib_category2);
        ImageButton ibCategory3 = (ImageButton) view.findViewById(R.id.ib_category3);
        ibCategory1.setOnClickListener(mCategoryClick);
        ibCategory2.setOnClickListener(mCategoryClick);
        ibCategory3.setOnClickListener(mCategoryClick);
        ibCategory1.setTag(mCategoryCnt);
        ibCategory2.setTag(mCategoryCnt);
        ibCategory3.setTag(mCategoryCnt);

        Button btnDel = (Button) view.findViewById(R.id.btn_del);
        btnDel.setOnClickListener(mCategoryClick);
        btnDel.setTag(mCategoryCnt);

        llLowCategory = (LinearLayout) view.findViewById(R.id.ll_low_category);
        llLowCategory.setTag(mCategoryCnt);

        llCategory.setTag(mCategoryCnt);
        if(mCategoryCnt==0){
            btnDel.setVisibility(View.GONE);
            llCategory.addView(view);
        }else {
            float scale = getResources().getDisplayMetrics().density;
            final int topMargin = (int) (10*scale);

            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = topMargin;

            llCategory.addView(view, params);
        }

        views.put((Integer) view.getTag(), view);
        if(strData!=null && !strData.equals("")){
                TextView txtCategory1 = (TextView) view.findViewById(R.id.txt_category1);
                TextView txtCategory2 = (TextView) view.findViewById(R.id.txt_category2);
                TextView txtCategory3 = (TextView) view.findViewById(R.id.txt_category3);

                String[] strTemp = strData.split("\\?");
                String strIdx1="";
                String strIdx2="";
                String strIdx3="";
                for(int j=0; j<strTemp.length; j++) {
                    String[] tmp = strTemp[j].split(mContext.getResources().getString(R.string.str_category_item_gubun));
                    switch (j){
                        case 0:
                            if(!tmp[0].equals("\"\"")) strIdx1 = tmp[0];
                            if(!tmp[1].equals("\"\"")) txtCategory1.setText(tmp[1]);
                            break;
                        case 1:
                            if(!tmp[0].equals("\"\"")) strIdx2 = tmp[0];
                            if(!tmp[1].equals("\"\"")) txtCategory2.setText(tmp[1]);
                            break;
                        case 2:
                            if(!tmp[0].equals("\"\"")) strIdx3 = tmp[0];
                            if(!tmp[1].equals("\"\"")) txtCategory3.setText(tmp[1]);
                            break;
                    }
                }

                strCategoryIndex = new String[]{strIdx1, strIdx2, strIdx3};
            arrSelCategoryChk.add(true);
        }else {
        strCategoryIndex = new String[]{"", "", ""};
            arrSelCategoryChk.add(false);
        }
        arrCategoryChk.add(strCategoryIndex);

        mCategoryCnt++;
    }

    private int mClickCategoryId;
    private onCategoryItemClick mClick;

    // 이벤트 인터페이스를 정의
    public interface onCategoryItemClick {
        public void onCategoryClick(int mCategory, ViewGroup viewGroup);
    }

    private ViewGroup vgCategory = null; //선택 된 view
    private boolean isCategoryClick=false; //category 클릭 유/무
    public OnClickListener mCategoryClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int viewPos = (Integer) v.getTag();
            ViewGroup ro = (ViewGroup) views.get(viewPos);

            if(vgCategory!=ro){
                if (arrData2 != null) arrData2.clear();
                if (arrData3 != null) arrData3.clear();
            }
            vgCategory = ro;

            String[] arrayStrIdxTemp = new String[3];
            if(arrCategoryChk.size()>0) {
                arrayStrIdxTemp = arrCategoryChk.get((Integer)vgCategory.getTag());
            }

            int viewId = v.getId();
            if (viewId == R.id.rl_category1 || viewId == R.id.ib_category1) {
                OpenDlg(CATEGORY_1, ro);
            } else if (viewId == R.id.rl_category2 || viewId == R.id.ib_category2) {
                if (arrayStrIdxTemp[0].equals("")) {
                    Toast.makeText(mContext, getResources().getString(R.string.str_select_top), Toast.LENGTH_SHORT).show();
                } else if (arrData2 == null || arrData2.size() <= 0) {
                    isCategoryClick = true;
                    new CommCode(mContext, StaticDataInfo.COMMON_CODE_TYPE_AD, 2, arrayStrIdxTemp[0], handler);
                } else {
                    OpenDlg(CATEGORY_2, ro);
                }
            } else if (viewId == R.id.rl_category3 || viewId == R.id.ib_category3) {
                if (arrayStrIdxTemp[1].equals("")) {
                    Toast.makeText(mContext, getResources().getString(R.string.str_select_top), Toast.LENGTH_SHORT).show();
                } else if (arrData3 == null || arrData3.size() <= 0) {
                    isCategoryClick = true;
                    new CommCode(mContext, StaticDataInfo.COMMON_CODE_TYPE_AD, 3, arrayStrIdxTemp[1], handler);
                } else {
                    OpenDlg(CATEGORY_3, ro);
                }
            } else if (viewId == R.id.btn_del) {
                delCategory(ro);
            }
        }
    };

    //다음 버튼 클릭
    public OnClickListener mNextInfo = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SetInfoData();
        }
    };

    private OnGetInfoData mGetInfoData;

    // 이벤트 인터페이스를 정의
    public interface OnGetInfoData {
        public void onGetInfoData(ArrayList<String> arrData, String arrCategory);
    }

    public void getOnInfoData(OnGetInfoData getInfoData) {
        mGetInfoData = getInfoData;
    }

    /**
     * 카테고리에 대한 list 구성 (카테고리 전달 data 필요)
     */
    private boolean isSelCategory = true; //캬테고리 전체 입력 여부
    public void SetInfoData() {
        isSelCategory = true;
        imm.hideSoftInputFromWindow(etADAmount.getWindowToken(), 0);

        String strMsg = "";

        String strInputAmount = etADAmount.getText().toString();
        String strInputSavePoint = etADSavePoint.getText().toString();

        Intent intent = new Intent(mContext, DlgBtnActivity.class);
        if (etADName.getText().toString().equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_name_err);
        } else if (etADDetail.getText().toString().equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_detail_err);
        } else
            if (isSelCategory) {
            for (int i = 0; i < arrSelCategoryChk.size(); i++) {
                if (!arrSelCategoryChk.get(i)) {
                    strMsg = getResources().getString(R.string.str_make_ad_category_err);
                    intent.putExtra("BtnDlgMsg", strMsg);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                    return;
                }
            }
            isSelCategory = false;
        }else{
            isSelCategory = false;
        }

        // 광고 금액 입력 chk
        strInputAmount = strInputAmount.replace(",", ""); //입력한 광고 할 금액

            if (strInputAmount.trim().equals("")
                    || strInputAmount.equals("0") || (Long.parseLong(strInputAmount.replaceAll(",", "")) < Long.parseLong(strMinAmount.replaceAll(",", "")))) {
                strMsg = String.format(getResources().getString(R.string.str_make_ad_amount_reinput), StaticDataInfo.makeStringComma(strMinAmount));
            }else if(strInputSavePoint.trim().equals("")
                    || strInputSavePoint.equals("0") || (Long.parseLong(strInputSavePoint.replaceAll(",", "")) < Long.parseLong(strMinSavePoint.replaceAll(",", "")))){
                strMsg = String.format(getResources().getString(R.string.str_make_ad_save_point_reinput), StaticDataInfo.makeStringComma(strMinSavePoint));
            }else{
            strChargeMoney = strChargeMoney.replace(",", ""); //충전금
            strInputSavePoint = strInputSavePoint.replace(",", ""); //적립포인트

                long mInputAmount = Long.parseLong(strInputAmount); //입력한 광고 할 금액
                long mChargeMoney = Long.parseLong(strChargeMoney); //충전금
                long mInputSavePoint = Long.parseLong(strInputSavePoint); //적립포인트

                if(!isModify){
                    //광고 할 금액을 충전금보다 많이 입력했을 경우
                    if (mInputAmount > mChargeMoney) {
                        strMsg = String.format(getResources().getString(R.string.str_make_ad_charge_err), StaticDataInfo.makeStringComma(strChargeMoney));

                        intent.putExtra("BtnDlgMsg", strMsg);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent);
                        return;
                    }
                }

                //광고 할 금액 보다 적립포인트를 많이 입력했을 경우
                if (mInputAmount < mInputSavePoint) {
                    strMsg = getResources().getString(R.string.str_make_ad_save_point_err1);
                }else {

                    //광고 할 금액 수정 시 남은 광고 금액보다 적은 금액 입력한 건지 chk
                    if (strAmount != null && !strAmount.trim().equals("")) {
                        long adCost = Long.parseLong(strAmount.replace(",", "")); //남은 광고 금액
                        String cost = etADAmount.getText().toString().replace(",", ""); //수정한 광고 할 금액
                        if (cost != null && !cost.trim().equals("")) {
                            long adInputCost = Long.parseLong(cost); //수정한 광고 할 금액
                            if (adInputCost < adCost) {
                                strMsg = getResources().getString(R.string.str_make_ad_amount_input_err);
                            } else {
                                long upCost = adInputCost - adCost; //증액 금액
                                if (mChargeMoney < upCost) {
                                    strMsg = String.format(getResources().getString(R.string.str_make_ad_charge_err), StaticDataInfo.makeStringComma(strChargeMoney));
                                }else{
                                    strUpCost = String.valueOf(upCost);
                                }
                            }
                        }

                    }
                }

            }

        if (!strMsg.equals("")) {
            intent.putExtra("BtnDlgMsg", strMsg);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        } else {
            ArrayList<String> arrData = new ArrayList<String>(); //카테고리를 제외한 data
            String[] strCategory = new String[llCategory.getChildCount()];

            arrData.add(INDEX_AD_NAME, etADName.getText().toString());
            arrData.add(INDEX_AD_DETAIL, etADDetail.getText().toString());
            String strSelCategory = new String();
            for (int i = 0; i < llCategory.getChildCount(); i++) {
                String[] strTemp = arrCategoryChk.get((Integer) llCategory.getChildAt(i).getTag());
                ArrayList<String> arrCagegoryTmp = new ArrayList<String>();

                if (strTemp.length > 0) {
                    if (!strTemp[0].equals("")) {
                        if (!strTemp[2].equals("")) {
                            strSelCategory += strTemp[0] + getResources().getString(R.string.str_category_item_gubun) + strTemp[2] + ",";
                        } else if (!strTemp[1].equals("")) {
                            strSelCategory += strTemp[0] + getResources().getString(R.string.str_category_item_gubun) + strTemp[1] + ",";
                        }
                    }
                }
            }
            arrData.add(INDEX_AD_AMOUNT, etADAmount.getText().toString().replaceAll(",", ""));
            arrData.add(INDEX_AD_SAVE_POINT, etADSavePoint.getText().toString().replaceAll(",", ""));
            arrData.add(INDEX_AD_DATE_S, getSDate() + " " +getSTime());
            arrData.add(INDEX_AD_DATE_E, getEDate() + " " +getETime());
            arrData.add(INDEX_AD_MY_CHARGE,strChargeMoney);
            arrData.add(INDEX_AD_ADDRESS, txtADAddress.getText().toString());
            String strHomePage = etADHomePage.getText().toString();
            if(!strHomePage.startsWith(mContext.getResources().getString(R.string.str_ad_homepage))
                    && !strHomePage.startsWith(mContext.getResources().getString(R.string.str_ad_homepage_s))){
                strHomePage = mContext.getResources().getString(R.string.str_ad_homepage) + strHomePage;
            }
            arrData.add(INDEX_AD_HOMEPAGE, etADHomePage.getText().toString());
            arrData.add(INDEX_AD_RECOMMEND, etADRecommend.getText().toString());

            if(rbHaveEvent.isChecked()){
                arrData.add(INDEX_AD_EVENT, StaticDataInfo.STRING_Y);
            }else{
                arrData.add(INDEX_AD_EVENT, StaticDataInfo.STRING_N);
            }

            if(isModify) {
                if (isRejudged) {
                    arrData.add(INDEX_AD_REJUDGED, StaticDataInfo.STRING_Y);
                } else {
                    arrData.add(INDEX_AD_REJUDGED, StaticDataInfo.STRING_N);
                }
                arrData.add(INDEX_AD_UP_COST, strUpCost);
            }else{
                arrData.add(INDEX_AD_REJUDGED, STR_NOT_MODIFY);
            }

            mGetInfoData.onGetInfoData(arrData, strSelCategory);
        }
    }

    private String strDlgChildTitle1; //1차 선택 된 text
    private String strDlgChildTitle2; //2차 선택 된 text
    private String strGroupIdx; //1차 분류 idx
    private String strChildIdx1 = ""; //2차 분류 idx
    private String strChildIdx2 = ""; //3차 분류 idx

    private HashMap<Integer, String> hmDetailPos = new HashMap<Integer, String>();
    private Dialog mDlg;

    /**
     * 1차분류, 2차분류, 3차분류 선택 popup
     *
     * @param mMode
     */
    public void OpenDlg(final int mMode, final ViewGroup viewGroup) {
        final TextView txtCategory1 = ((TextView) viewGroup.findViewById(R.id.txt_category1));
        final TextView txtCategory2 = ((TextView) viewGroup.findViewById(R.id.txt_category2));
        final TextView txtCategory3 = ((TextView) viewGroup.findViewById(R.id.txt_category3));

        final LinearLayout llLowCategoryView = ((LinearLayout) viewGroup.findViewById(R.id.ll_low_category));

        mDlg = new Dialog(mContext);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);
        mDlg.setContentView(R.layout.dlg_txt_list);
        TextView txtDlgTitle = (TextView) mDlg.findViewById(R.id.txt_dlg_title);
        ListView lvText = (ListView) mDlg.findViewById(R.id.lv_txt);
        CommCodeAdapter commAdapter = null;

        switch (mMode) {
            case CATEGORY_1: //1차 분류
                if (arrData1 != null && arrData1.size() > 0) {
                    txtDlgTitle.setText(mContext.getResources().getString(R.string.str_category));
                    commAdapter = new CommCodeAdapter(mContext, R.layout.list_txt_item, arrData1);
                }

                if (arrData2 != null) arrData2.clear();
                if (arrData3 != null) arrData3.clear();
                break;
            case CATEGORY_2: //2차 분류
                if (arrData2 != null && arrData2.size() > 0) {
                    txtDlgTitle.setText(txtCategory1.getText().toString());
                    strCategory2 = txtDlgTitle.getText().toString();

                    commAdapter = new CommCodeAdapter(mContext, R.layout.list_txt_item, arrData2);
                }
                break;
            case CATEGORY_3: //3차 분류
                if (arrData3 != null && arrData3.size() > 0) {
                    txtDlgTitle.setText(txtCategory2.getText().toString());
                    commAdapter = new CommCodeAdapter(mContext, R.layout.list_txt_item, arrData3);
                }
                break;
        }

        if (commAdapter != null) {
            lvText.setAdapter(commAdapter);
            mDlg.show();
        }

        lvText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mMode) {
                    case CATEGORY_1:
                        if(arrData1.size()>0) {
                            strDlgChildTitle1 = arrData1.get(position).getStrMsg();
                            strGroupIdx = arrData1.get(position).getStrIdx();
                            txtCategory1.setText(strDlgChildTitle1);

                            isCategoryClick = false;
                            new CommCode(mContext, StaticDataInfo.COMMON_CODE_TYPE_AD, 2, strGroupIdx, handler);

                            txtCategory2.setText("");
                            txtCategory3.setText("");
                            strChildIdx1 = "";
                            strChildIdx2 = "";

                            llLowCategoryView.setVisibility(View.VISIBLE);

                            if (arrSelCategoryChk.size()>0) {
                                arrSelCategoryChk.set((Integer)viewGroup.getTag(), false);
                            }
                        }
                        break;
                    case CATEGORY_2:
                        if(arrData2.size()>0) {
                            strDlgChildTitle2 = arrData2.get(position).getStrMsg();
                            strChildIdx1 = arrData2.get(position).getStrIdx();

                            isCategoryClick = false;
                            new CommCode(mContext, StaticDataInfo.COMMON_CODE_TYPE_AD, 3, strChildIdx1, handler);
                            txtCategory2.setText(strDlgChildTitle2);

                            txtCategory3.setText("");
                            strChildIdx2 = "";
                            isSelSiGun = false;

                            if (arrSelCategoryChk.size()>0) {
                                arrSelCategoryChk.set((Integer)viewGroup.getTag(), false);
                            }

                            if (arrData3 != null) arrData3.clear();

                            if (arrSelCategoryChk.size()>0) {
                                arrSelCategoryChk.set((Integer)viewGroup.getTag(), false);
                            }
                        }
                        break;
                    case CATEGORY_3:
                        if(arrData3.size()>0) {
                            txtCategory3.setText(arrData3.get(position).getStrMsg());
                            strChildIdx2 = arrData3.get(position).getStrIdx();
                            isCategoryClick = false;

                            if (arrSelCategoryChk.size()>0) {
                                arrSelCategoryChk.set((Integer)viewGroup.getTag(), true);
                            }
                        }
                        break;
                }

                if(arrCategoryChk.size() > 0) {
                    strCategoryIndex = new String[]{strGroupIdx, strChildIdx1, strChildIdx2};
                    arrCategoryChk.set((Integer) viewGroup.getTag(), strCategoryIndex);
                }

                mDlg.dismiss();
            }
        });
    }

    /**
     * 카테고리 삭제
     */
    public void delCategory(ViewGroup selView) {
        llCategory.removeView(selView);
        views.remove((Integer) selView.getTag());

        if (arrSelCategoryChk.size() > 0) {
            arrSelCategoryChk.set((Integer) selView.getTag(), true);
        }
    }


    private String strCommaResult = "";
    private String strADNameTemp = "";
    private String strADDetailTemp = "";
    private String strADHomePageTemp = "";

    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            strADNameTemp = etADName.getText().toString();
            strADDetailTemp = etADDetail.getText().toString();
            strADHomePageTemp = etADHomePage.getText().toString();

            isFocus = false;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!(etADName.hasFocus() || etADDetail.hasFocus() || etADHomePage.hasFocus()) && !s.toString().equals(strCommaResult)) {     // StackOverflow를 막기위해,
                strCommaResult = StaticDataInfo.makeStringComma(s.toString().replaceAll(",", ""));   // 에딧텍스트의 값을 변환하여, result에 저장.
                if (etADAmount.hasFocus()) {
                    etADAmount.setText(strCommaResult);    // 결과 텍스트 셋팅.
                    etADAmount.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.
                } else if (etADSavePoint.hasFocus()) {
                    etADSavePoint.setText(strCommaResult);    // 결과 텍스트 셋팅.
                    etADSavePoint.setSelection(strCommaResult.length());     // 커서를 제일 끝으로 보냄.
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(etADName.hasFocus() || etADDetail.hasFocus() || etADHomePage.hasFocus()){
                if(!strADNameTemp.equals(etADName.getText().toString())
                        || !strADDetailTemp.equals(etADDetail.getText().toString())
                        || !strADHomePageTemp.equals(etADHomePage.getText().toString())) {
                    isRejudged = true;
                }
            }
        }
    };

    private String strMinAmount = "0";
    private String strMinSavePoint = "0";
    private String strChargeMoney = "0";

    public void setDetailInfo(String minAmount, String minSavePoint, String ChargeMoney) {
        if (!minAmount.equals("") && !minSavePoint.equals("") && !ChargeMoney.equals("")) {
            strMinAmount = minAmount;
            strMinSavePoint = minSavePoint;
            strChargeMoney = ChargeMoney;
            txtAmountInfo2.setText(String.format(mContext.getResources().getString(R.string.str_amount_info2), StaticDataInfo.makeStringComma(minAmount)));
            etADSavePoint.setHint(StaticDataInfo.makeStringComma(minSavePoint) + getResources().getString(R.string.str_up));
        } else if (strChargeMoney.equals("0")) {
            Intent intent = new Intent(mContext, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", mContext.getResources().getString(R.string.str_charge_err));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Activity) mContext).startActivityForResult(intent, REQUEST_CHARGE_ERR);
        }
    }

    public void setAddress(String addr) {
        txtADAddress.setText(addr);
    }

    private boolean isFocus = false;
    public View.OnFocusChangeListener mFocusChange = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(!hasFocus) {
                if (v.getId() == R.id.et_input_ad_amount && !isFocus) {
                    isFocus = true;
                    chkAmount();
                } else if (v.getId() == R.id.et_input_save_point && !isFocus) {
                    isFocus = true;
                    chkSavePoint();
                }
            }
        }
    };
}
