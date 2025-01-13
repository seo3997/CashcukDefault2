package com.cashcuk.advertiser.sendpush;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.advertiser.sendpush.view.PushSendViewInfo;
import com.cashcuk.common.AddAd;
import com.cashcuk.common.AddrUser;
import com.cashcuk.common.PushTargetSet;
import com.cashcuk.dialog.DlgListAdapter;
import com.cashcuk.membership.txtlistdata.TxtListAdapter;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;
import com.cashcuk.membership.txtlistdata.TxtPushTargetInfo;
import com.cashcuk.sendinfo.SendInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 광고 대상 - 대상 설정
 */
public class ADTargetSetLinear1 extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater inflater;

    private LinearLayout llCategory; // 지역 view(1, 2, 3차)
    private int mCategoryCnt = 0;    // 지역 추가 index count(== 실제 개 수 -1)

    //시도, 시군구
    private ArrayList<TxtListDataInfo> arrHangOutCitiesData = new ArrayList<TxtListDataInfo>();
    private ArrayList<TxtListDataInfo> arrHangOutTownData = new ArrayList<TxtListDataInfo>();

    //광고내역
    private ArrayList<TxtListDataInfo> arrHangOutAdsData = new ArrayList<TxtListDataInfo>();


    private final String SI_GUN_IDX = "A"; //시 or 군 선택 후 서버 전달 시 첫번째 idx는 A
    private final String STR_SI_MODE = "S";     // 시,도 선택
    private final String STR_GUN_MODE = "G";    // 시,군,구 선택
    private String selSiDoIdx="";               // 시,도 선택 idx

    private ListView lvHangOutList;             // 시도, 시군구 list
    private final int DIALOG_MODE_SI_DO = 2;
    private final int DIALOG_MODE_SI_GUN = 3;
    private final int DIALOG_MODE_SEND_INFO = 4;
    private final int DIALOG_MODE_AD_INFO = 5;

    // 광고 발송 기본 정보
    private static final int DIALOG_MODE_AD_SEND_INFO = 0;
    private static final String STR_MODE_AD_SEND_DEFAULT_INFO_TITLE = "ADSendDefaultInfoTitle"; // 광고 발송 기본정보 Title
    private int mCodeIndex = -1;            // 광고발송 기본 정보 index
    private int mCodeIndexSize = 0;         // 광고발송 기본 정보 총 개수
    private ArrayList<TxtListDataInfo> arrSendInfoTitleTemp = new ArrayList<TxtListDataInfo>();        // 기본정보 임시 배열
    private ArrayList<TxtListDataInfo> arrSendInfoSubTemp = new ArrayList<TxtListDataInfo>();        // 기본정보 임시 배열
    private ArrayList<TxtListDataInfo> arrSendInfoTitleData = new ArrayList<TxtListDataInfo>();   // 기본정보 타이틀 배열
    private ArrayList<TxtListDataInfo>[] arrSendInfoDetailData;                     // 기본정보 세부목록 배열
    private LinearLayout llADSendInfo;      // 광고발송 기본 정보
    private LinearLayout llADInfo;          // 광고

    private SparseArray<View> sendInfoViews = new SparseArray<View>();
    private View mAdInfoView = null;
    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(mContext, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;

                case StaticDataInfo.RESULT_SI_DO:
                    arrHangOutCitiesData.add(defaultInfo());
                    arrHangOutCitiesData.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                    break;
                case StaticDataInfo.RESULT_SI_GUN_GU:
                    arrHangOutTownData.clear();
                    arrHangOutTownData.add(defaultInfo());
                    arrHangOutTownData.addAll((ArrayList<TxtListDataInfo>) msg.obj);

                    if (isCategoryClick) {
                        OpenDialog(DIALOG_MODE_SI_GUN, vgCategory);
                    }
                    break;
                case StaticDataInfo.RESULT_AD_INFO:                                                 //광고리스트
                    arrHangOutAdsData.addAll((ArrayList<TxtListDataInfo>) msg.obj);

                    if (mTxtAdInfo.getStrIdx() != null || !mTxtAdInfo.getStrIdx().equals("")){
                        ((TextView) mAdInfoView.findViewById(R.id.txt_common_set_value)).setText(mTxtAdInfo.getStrMsg());
                    }

                    break;
                case StaticDataInfo.RESULT_SEND_AD_INFO:

                    if(msg.arg1<0){
                        arrSendInfoTitleTemp.clear();
                        arrSendInfoTitleTemp = (ArrayList<TxtListDataInfo>)msg.obj;
                        // 광고발송 기본정보들 TITLE 정보
                        if(arrSendInfoTitleTemp !=null && arrSendInfoTitleTemp.size()>0){
                            mCodeIndexSize = arrSendInfoTitleTemp.size();

                            arrSendInfoTitleData = new ArrayList<TxtListDataInfo>();
                            arrSendInfoTitleData.addAll(arrSendInfoTitleTemp);

                            for(int i=0; i<mCodeIndexSize; i++){
                                View sendInfoView = inflater.inflate(R.layout.low_common_set_item, null);
                                sendInfoView.setId(i);

                                // 기본정보 Title Layout
                                String titleMsg = arrSendInfoTitleData.get(i).getStrMsg();
                                String titleIdx = arrSendInfoTitleData.get(i).getStrIdx();

                                TextView txtSendInfoTitle = (TextView) sendInfoView.findViewById(R.id.txt_common_set_name);
                                txtSendInfoTitle.setText(titleMsg);

                                RelativeLayout rlADSendInfoValue = (RelativeLayout) sendInfoView.findViewById(R.id.rl_common_set_value);
                                rlADSendInfoValue.setOnClickListener(mSendInfoClick);
                                rlADSendInfoValue.setTag(i);

                                ((TextView) sendInfoView.findViewById(R.id.txt_common_set_value)).setText(R.string.str_sel_ad_send_default_info);
                                refreshSelectInfo(SelectInfo.SENDINFO, i, titleIdx, "0");

                                sendInfoView.setTag(i);
                                sendInfoViews.put(i, sendInfoView);

                                llADSendInfo.addView(sendInfoView);
                            }

                            setSendInfo();
                            new SendInfo(mContext, handler, arrSendInfoTitleData.get(mCodeIndex).getStrIdx(), mCodeIndex, "");
                        }
                    } else {
                        if (msg.obj != null && ((ArrayList<TxtListDataInfo>)msg.obj).size() > 0) {
                            arrSendInfoSubTemp.clear();
                            arrSendInfoSubTemp.add(defaultInfo());
                            arrSendInfoSubTemp.addAll((ArrayList<TxtListDataInfo>) msg.obj);

                            // 광고발송 기본정보들의 하위 메뉴들 정보
                            if (mCodeIndex < mCodeIndexSize) {
                                if (arrSendInfoDetailData == null)
                                    arrSendInfoDetailData = new ArrayList[mCodeIndexSize];
                                ArrayList<TxtListDataInfo> arrADSendDefaultInfo = new ArrayList<TxtListDataInfo>();
                                arrADSendDefaultInfo.addAll(arrSendInfoSubTemp);

                                arrSendInfoDetailData[mCodeIndex] = arrADSendDefaultInfo;
                            }

                            mCodeIndex++;

                            if (mCodeIndex < mCodeIndexSize && arrSendInfoTitleData != null && arrSendInfoTitleData.size() > 0) {
                                new SendInfo(mContext, handler, arrSendInfoTitleData.get(mCodeIndex).getStrIdx(), mCodeIndex, "");
                            } else {
                                new AddrUser(mContext, handler, STR_SI_MODE, selSiDoIdx);
                            }
                        }
                    }
                    break;
            }
        }
    };

    private Handler pushHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_200:
                    setData((TxtPushTargetInfo)msg.obj);
                    break;
            }
        }
    };


    public ADTargetSetLinear1(Context context) {
        super(context);
        mContext = context;
        Init();
    }

    /**
     * 생성자
     * @param context
     * @param attrs
     */
    public ADTargetSetLinear1(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        Init();
    }

    /**
     * layout 구성
     */
    private void Init(){
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.advertiser_ad_target_set, this, true);

        llCategory = (LinearLayout) findViewById(R.id.ll_category);
        ((Button) findViewById(R.id.btn_target_next)).setOnClickListener(mNext);
        ((Button) findViewById(R.id.btn_category_add)).setOnClickListener(this);

        arrTitleTmp.setStrIdx("0");
        arrTitleTmp.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
        arrSubTmp.setStrIdx("0");
        arrSubTmp.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
        CategoryAdd(arrTitleTmp, arrSubTmp);  // 지역 default로 1개 추가

        llADSendInfo = (LinearLayout) findViewById(R.id.ll_ad_send_info);
        llADInfo     = (LinearLayout) findViewById(R.id.ll_ad_info);

        AdAdd();                            //광고 Select 생성

        ErrorDialog();

        new SendInfo(mContext, handler, STR_MODE_AD_SEND_DEFAULT_INFO_TITLE, mCodeIndex++, StaticDataInfo.STRING_P);
        //2020.01.01 시도 나중에 가져오자 sooHyun.seo
        new AddAd(mContext, handler,"");                                                            //광고선택
    }



    /**
     * 상단 메뉴 버튼
     */
    private Dialog mErrorDlg;
    private ArrayList<String> arrString;
    private Button btn1; //취소
    private LinearLayout ll1; //취소

    public void ErrorDialog() {
        mErrorDlg = new Dialog(mContext);
        mErrorDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mErrorDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mErrorDlg.setContentView(R.layout.dlg_list_title);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mErrorDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mErrorDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ((TextView) mErrorDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_alert));
        ListView lvDlgMsg = (ListView) mErrorDlg.findViewById(R.id.lv_dlg);

        arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_ad_select));                             //광고선택


        ll1= (LinearLayout) mErrorDlg.findViewById(R.id.ll1);
        ll1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mErrorDlg!=null && mErrorDlg.isShowing()) mErrorDlg.dismiss();
            }
        });

        btn1 = (Button) mErrorDlg.findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mErrorDlg!=null && mErrorDlg.isShowing()) mErrorDlg.dismiss();
            }
        });

        DlgListAdapter dlgAdapter = new DlgListAdapter(mContext, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mErrorDlg != null && mErrorDlg.isShowing()) mErrorDlg.dismiss();
            }
        });

    }


    public TxtListDataInfo defaultInfo(){
        TxtListDataInfo txtTmp = new TxtListDataInfo();
        txtTmp.setStrIdx("0");
        txtTmp.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
        return txtTmp;
    }

    private ArrayList<TxtListDataInfo> alInfo;
    private ArrayList<TxtListDataInfo> alInfoSub;
    /**
     * 재발송, 재요청 일 경우 데이터 d/p
     * @param mInfo
     */
    public void setDataAgainRequest(PushSendViewInfo mInfo){
        mCategoryCnt = 0;
        llCategory.removeAllViews();

        TxtListDataInfo txtInfo;
        String[] arrTmp = null;
        arrTmp = mInfo.getStrAddr().split(":");
        ArrayList<TxtListDataInfo> alAddr = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alAddr.add(txtInfo);
        }

        arrTmp = null;
        arrTmp = mInfo.getStrAddrSub().split(":");
        ArrayList<TxtListDataInfo> alAddrSub = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alAddrSub.add(txtInfo);
        }
        for(int i=0; i<alAddr.size(); i++) {
            CategoryAdd(alAddr.get(i), alAddrSub.get(i));
            refreshSelectInfo(SelectInfo.ADDRESS, i, STR_SI_MODE, alAddr.get(i).getStrIdx());
            refreshSelectInfo(SelectInfo.ADDRESS, i, STR_GUN_MODE, alAddrSub.get(i).getStrIdx());
        }

       arrTmp = null;
        arrTmp = mInfo.getStrInfo().split(":");
        alInfo = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alInfo.add(txtInfo);
        }

        arrTmp = null;
        arrTmp = mInfo.getStrInfoSub().split(":");
        alInfoSub = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alInfoSub.add(txtInfo);
        }
    }

    public void setSendInfo(){
        for (int i = 0; i < arrSendInfoTitleData.size(); i++) {
            if (alInfo != null) {
                for (int j = 0; j < alInfo.size(); j++) {
                    if (arrSendInfoTitleData.get(i).getStrIdx().equals(alInfo.get(j).getStrIdx())) {
                        arrSendInfoTitleData.set(i, alInfo.get(j));

                        TextView txtInfoSub = (TextView) sendInfoViews.get(i).findViewById(R.id.txt_common_set_value);
                        txtInfoSub.setText(alInfoSub.get(j).getStrMsg());

                        refreshSelectInfo(SelectInfo.SENDINFO, j, arrSendInfoTitleData.get(i).getStrIdx(), alInfoSub.get(j).getStrIdx());
                    }
                }
            } else {
                alInfoSub = new ArrayList<TxtListDataInfo>();
                TxtListDataInfo txtSubInfo = new TxtListDataInfo();
                txtSubInfo.setStrIdx("0");
                txtSubInfo.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
                alInfoSub.add(txtSubInfo);

                TextView txtInfoSub = (TextView) sendInfoViews.get(i).findViewById(R.id.txt_common_set_value);
                txtInfoSub.setText(txtSubInfo.getStrMsg());

                refreshSelectInfo(SelectInfo.SENDINFO, i, arrSendInfoTitleData.get(i).getStrIdx(), txtSubInfo.getStrIdx());
            }
        }
    }

    TxtListDataInfo arrTitleTmp = new TxtListDataInfo();
    TxtListDataInfo arrSubTmp = new TxtListDataInfo();
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_category_add) { //카테고리 추가
            if (categoryViews != null && categoryViews.size() > 0) {
                ViewGroup ro = (ViewGroup) categoryViews.get(categoryViews.size() - 1);

                TextView txt2 = (TextView) ro.findViewById(R.id.txt_category2);
                TextView txt3 = (TextView) ro.findViewById(R.id.txt_category3);
                if (txt2 == null || txt2.getText().equals("")
                        || txt3 == null || txt3.getText().equals("")) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.str_no_addr), Toast.LENGTH_SHORT).show();
                } else {
                    arrTitleTmp.setStrIdx("0");
                    arrTitleTmp.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
                    arrSubTmp.setStrIdx("0");
                    arrSubTmp.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
                    CategoryAdd(arrTitleTmp, arrSubTmp);
                }
            } else {
                arrTitleTmp.setStrIdx("0");
                arrTitleTmp.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
                arrSubTmp.setStrIdx("0");
                arrSubTmp.setStrMsg(mContext.getResources().getString(R.string.str_sel_ad_send_default_info));
                CategoryAdd(arrTitleTmp, arrSubTmp);
            }
        }
    }

    /**
     * 지역 (2,3차 항목) 추가
     */
    private SparseArray<View> categoryViews = new SparseArray<View>();
    public void CategoryAdd(TxtListDataInfo arrTitle, TxtListDataInfo arrSub){
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.low_category_item, null);
        view.setTag(mCategoryCnt);

        RelativeLayout rlCategory2 = (RelativeLayout) view.findViewById(R.id.rl_category2);
        RelativeLayout rlCategory3 = (RelativeLayout) view.findViewById(R.id.rl_category3);
        rlCategory2.setOnClickListener(mCategoryClick);
        rlCategory3.setOnClickListener(mCategoryClick);
        rlCategory2.setTag(mCategoryCnt);
        rlCategory3.setTag(mCategoryCnt);

        TextView txtCategory2 = (TextView) view.findViewById(R.id.txt_category2);
        txtCategory2.setText(arrTitle.getStrMsg());
        txtCategory2.setTag(mCategoryCnt);

        TextView txtCategory3 = (TextView) view.findViewById(R.id.txt_category3);
        txtCategory3.setText(arrSub.getStrMsg());
        txtCategory3.setTag(mCategoryCnt);

        llCategory.addView(view);
        categoryViews.put(mCategoryCnt, view);

        mCategoryCnt++;

        refreshSelectInfo(SelectInfo.ADDRESS, (Integer) view.getTag(),
                SI_GUN_IDX,
                "0");

    }

    public void AdAdd(){
        int iViewId=0;                                                                              //동적으로 생성되는 뷰의 아이디,태그값
        View sendInfoView = inflater.inflate(R.layout.low_common_set_item, null);
        sendInfoView.setId(iViewId);

        // 기본정보 Title Layout
        String titleMsg = getResources().getString(R.string.str_ad_title);

        TextView txtSendInfoTitle = (TextView) sendInfoView.findViewById(R.id.txt_common_set_name);
        txtSendInfoTitle.setText(titleMsg);

        RelativeLayout rlADSendInfoValue = (RelativeLayout) sendInfoView.findViewById(R.id.rl_common_set_value);
        rlADSendInfoValue.setOnClickListener(mAdClick);
        rlADSendInfoValue.setTag(iViewId);

        ((TextView) sendInfoView.findViewById(R.id.txt_common_set_value)).setText(R.string.str_select);
         refreshSelectInfo(SelectInfo.ADINFO, 0,getResources().getString(R.string.str_select), "0");                                        //광고일때 4번째 항목만 사용 0전체(2,3번째 파라메터 사용하지 않음)

        sendInfoView.setTag(iViewId);
        mAdInfoView=sendInfoView;
        llADInfo.addView(sendInfoView);

    }


    private ViewGroup vgCategory = null; //선택 된 view
    private boolean isCategoryClick=false; //category 클릭 유/무
    public OnClickListener mCategoryClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int viewPos = (Integer)v.getTag();
            ViewGroup ro = (ViewGroup) categoryViews.get(viewPos);

            if(vgCategory!=ro){
                if(arrHangOutTownData != null) arrHangOutTownData.clear();
            }
            vgCategory=ro;

            int viewId = v.getId();
            if (viewId == R.id.rl_category2) {
                OpenDialog(DIALOG_MODE_SI_DO, ro);
            } else if (viewId == R.id.rl_category3) {
                if (arrHangOutTownData == null || arrHangOutTownData.size() <= 0) {
                    isCategoryClick = true;
                    isSelSiDo = true;
                    new AddrUser(mContext, handler, STR_GUN_MODE, hmSelAddrInfo.get(viewPos));
                } else {
                    OpenDialog(DIALOG_MODE_SI_GUN, ro);
                }
            }
        }
    };

    public OnClickListener mSendInfoClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mClickId = (Integer)view.getTag();
            ViewGroup ro = (ViewGroup) sendInfoViews.get(mClickId);

            if (view.getId() == R.id.rl_common_set_value) {
                OpenDialog(DIALOG_MODE_AD_SEND_INFO, ro);
            }
        }
    };

    public OnClickListener mAdClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewGroup ro = (ViewGroup) mAdInfoView;
            OpenDialog(DIALOG_MODE_AD_INFO, ro);
        }
    };

    /**
     * 카테고리에 선택 내용 d/p
     * @param mCategory 2차 or 3차
     * @param viewGroup
     */
    private HashMap<Integer, String> hmSelAddrInfo = new HashMap<Integer, String>();
    public void setCategory(int mCategory, ViewGroup viewGroup, TxtListDataInfo strItem){
        TextView txt2 = (TextView) viewGroup.findViewById(R.id.txt_category2);
        TextView txt3 = (TextView) viewGroup.findViewById(R.id.txt_category3);

        if(mCategory == DIALOG_MODE_SI_DO){
            txt2.setText(strItem.getStrMsg());
            txt3.setText(R.string.str_sel_ad_send_default_info);
            hmSelAddrInfo.put((Integer) viewGroup.getTag(), strItem.getStrIdx());
        }else if(mCategory == DIALOG_MODE_SI_GUN) {
            txt3.setText(strItem.getStrMsg());
        }
    }

    private String strTarget = "";
    //다음 버튼 클릭
    public OnClickListener mNext= new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("temp","mTxtAdInfo.getStrIdx()["+mTxtAdInfo.getStrIdx()+"]");
            if(mTxtAdInfo.getStrIdx()==null || mTxtAdInfo.getStrIdx().equals("")|| mTxtAdInfo.getStrIdx().equals("0")){                  //광고를 선택하지 않았다면 다음 버튼을 클릭 할수 없음
                if (mErrorDlg != null && !mErrorDlg.isShowing()) mErrorDlg.show();
                return;
            }

            pushTargetSetInfo = new ArrayList<ArrayList<String>>();
            if (arrOverAddressInfo != null) pushTargetSetInfo.addAll(arrOverAddressInfo);
            if (arrOverSendInfo != null) pushTargetSetInfo.addAll(arrOverSendInfo);

            if (pushTargetSetInfo == null || pushTargetSetInfo.size() == 0) return;

            String tmp = "";
            strTarget = "";
            for (int i = 0; i < pushTargetSetInfo.size(); i++) {

                tmp = pushTargetSetInfo.get(i).toString().replace(" ", "");
                tmp = tmp.replace("S","A");
                tmp = tmp.replace("G","A");
                strTarget += tmp.replace(",", ">")+",";
            }

            new PushTargetSet(getContext(), pushHandler, strTarget);
        }
    };

    private OnGetData mGetData;
    // 이벤트 인터페이스를 정의
    public interface OnGetData {
        public void onGetData(String pushTargetSetInfo, TxtPushTargetInfo pushTargetRetrunValue);
    }

    public void getOnData(OnGetData getData)
    {
        mGetData = getData;
    }

    /**
     * 카테고리에 대한 list 구성 (카테고리 전달 data 필요)
     */
    public void setData(TxtPushTargetInfo pushTargetRetrunValue) {
        mGetData.onGetData(strTarget, pushTargetRetrunValue);
    }

    private Dialog mDialog;
    private int mClickId;
    private boolean isSelSiDo =false;

    /**
     * 사는 곳, 광고 기본 발송 정보 dialog
     * @param mDlgMode
     */
    public void OpenDialog(final int mDlgMode, final ViewGroup viewGroup){
        mDialog = new Dialog(mContext);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDialog.getWindow().setAttributes((WindowManager.LayoutParams) params);
        mDialog.setContentView(R.layout.dlg_txt_list);

        lvHangOutList = (ListView) mDialog.findViewById(R.id.lv_txt);
        TextView txtTitle = (TextView) mDialog.findViewById(R.id.txt_dlg_title);

        switch(mDlgMode){
            case DIALOG_MODE_SI_DO:
                if(arrHangOutCitiesData!=null){
                    txtTitle.setText(getResources().getString(R.string.str_cities_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(mContext, arrHangOutCitiesData));

                    isSelSiDo = true;
                }
                break;
            case DIALOG_MODE_SI_GUN:
                if(arrHangOutTownData!=null && arrHangOutTownData.size()>0 && isSelSiDo){
                    txtTitle.setText(getResources().getString(R.string.str_town_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(mContext, arrHangOutTownData));

                    isSelSiDo = false;
                }else{
                    Toast.makeText(mContext  , getResources().getString(R.string.str_selector_cities), Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case DIALOG_MODE_AD_SEND_INFO:
                if(arrSendInfoDetailData != null){
                    ArrayList<TxtListDataInfo> arrADSendInfo = new ArrayList<TxtListDataInfo>();
                    ArrayList<TxtListDataInfo> arrADSendInfoTemp = new ArrayList<TxtListDataInfo>();

                    int infoIndex = (int)viewGroup.getTag();
                    txtTitle.setText(arrSendInfoTitleData.get(infoIndex).getStrMsg());
                    for(int j = 0; j< arrSendInfoDetailData[infoIndex].size(); j++) {
                        arrADSendInfoTemp.add(arrSendInfoDetailData[infoIndex].get(j));
                    }
                    arrADSendInfo.addAll(arrADSendInfoTemp);
                    lvHangOutList.setAdapter(new TxtListAdapter(mContext, arrADSendInfo));
                }
                break;
            case DIALOG_MODE_AD_INFO:
                txtTitle.setText(getResources().getString(R.string.str_seladvertis));
                lvHangOutList.setAdapter(new TxtListAdapter(mContext, arrHangOutAdsData));
                break;



        }

        lvHangOutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String msg = "";
                String titleIdx = "";
                String valueIdx = "";

                switch (mDlgMode) {
                    case DIALOG_MODE_SI_DO:
                        isCategoryClick = false;
                        new AddrUser(mContext, handler, STR_GUN_MODE, arrHangOutCitiesData.get(position).getStrIdx());
                        setCategory(DIALOG_MODE_SI_DO, viewGroup, arrHangOutCitiesData.get(position));

                        refreshSelectInfo(SelectInfo.ADDRESS, (int) viewGroup.getTag(),
                                SI_GUN_IDX,
                                arrHangOutCitiesData.get(position).getStrIdx());
                        break;
                    case DIALOG_MODE_SI_GUN:
                        isCategoryClick = false;
                        setCategory(DIALOG_MODE_SI_GUN, viewGroup, arrHangOutTownData.get(position));

                        if(!arrHangOutTownData.get(position).getStrIdx().equals("0")) {
                            refreshSelectInfo(SelectInfo.ADDRESS, (int) viewGroup.getTag(),
                                    SI_GUN_IDX,
                                    arrHangOutTownData.get(position).getStrIdx());
                        }
                        break;
                    case DIALOG_MODE_AD_SEND_INFO:
                        msg = arrSendInfoDetailData[mClickId].get(position).getStrMsg();
                        ((TextView) viewGroup.findViewById(R.id.txt_common_set_value)).setText(msg);

                        titleIdx = arrSendInfoTitleData.get(mClickId).getStrIdx();
                        valueIdx = arrSendInfoDetailData[mClickId].get(position).getStrIdx();
                        refreshSelectInfo(SelectInfo.SENDINFO, (int)viewGroup.getTag(), titleIdx, valueIdx);
                        break;
                    case DIALOG_MODE_AD_INFO:
                        msg = arrHangOutAdsData.get(position).getStrMsg();
                        ((TextView) viewGroup.findViewById(R.id.txt_common_set_value)).setText(msg);

                        valueIdx = arrHangOutAdsData.get(position).getStrIdx();
                        refreshSelectInfo(SelectInfo.ADINFO, 0, msg, valueIdx);
                        break;
                }


                if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            }
        });

        mDialog.show();
    }

    private enum SelectInfo {
        ADDRESS, SENDINFO, ADINFO;
    }

    private ArrayList arrOverAddressInfo;                           // 대상 설정에서 선택한 지역 정보
    private TxtListDataInfo  mTxtAdInfo=new TxtListDataInfo();                            // 대상 설정에서 선택한 광고 정보
    private ArrayList<ArrayList<String>> arrOverSendInfo;           // 대상 설정에서 선택한 일반 정보
    private ArrayList<ArrayList<String>> pushTargetSetInfo;         // 선택한 지역정보와 일반정보

    public TxtListDataInfo getTxtAdInfo() {
        return mTxtAdInfo;
    }

    public void setTxtAdInfo(TxtListDataInfo mTxtAdInfo) {
        this.mTxtAdInfo = mTxtAdInfo;
    }

    private void refreshSelectInfo(SelectInfo sendInfo, int index, String titleIdx, String valueIdx) {
        switch (sendInfo) {
            case ADDRESS:
                if (arrOverAddressInfo == null) {
                    arrOverAddressInfo = new ArrayList<>();
                    for (int i=0; i < mCategoryCnt; i++) {
                        arrOverAddressInfo.add(getStringArray(STR_SI_MODE, "0"));
                    }
                }

                // 새로 추가된 지역 View가 있는지 체크
                if (arrOverAddressInfo.size() != mCategoryCnt) {
                    for (int i=arrOverAddressInfo.size(); i < mCategoryCnt; i++) {
                        arrOverAddressInfo.add(getStringArray(STR_SI_MODE, "0"));
                    }
                }

                arrOverAddressInfo.set(index, getStringArray(titleIdx, valueIdx));
                break;
            case SENDINFO:
                if (arrOverSendInfo == null) {
                    arrOverSendInfo = new ArrayList<ArrayList<String>>();
                    for (int i=0; i<mCodeIndexSize; i++) {
                        arrOverSendInfo.add(getStringArray("0","0"));
                    }
                }

                arrOverSendInfo.set(index, getStringArray(titleIdx, valueIdx));
                break;
            case ADINFO:
                    mTxtAdInfo = new TxtListDataInfo();
                    mTxtAdInfo.setStrIdx(valueIdx);
                    mTxtAdInfo.setStrMsg(titleIdx);
                    setTxtAdInfo(mTxtAdInfo);
                break;
        }
    }

    /**
     * [Title Index, Value Index]
     * @param titleIdx
     * view의 Title string id
     * @param valueIdx
     * view의 Value string id
     * @return
     */
    private ArrayList<String> getStringArray(String titleIdx, String valueIdx) {
        ArrayList<String> array = new ArrayList<String>();
        array.add(0, titleIdx);
        array.add(1, valueIdx);

        return array;
    }
}
