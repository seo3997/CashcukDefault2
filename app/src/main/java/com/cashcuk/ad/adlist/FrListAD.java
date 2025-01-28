package com.cashcuk.ad.adlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.cashcuk.MainActivity;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.ad.detailview.ADDetailViewActivity;
import com.cashcuk.common.CommCode;
import com.cashcuk.common.CommCodeAdapter;
import com.cashcuk.common.CommonDataTask;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * 광고보기 list
 */
public class FrListAD extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private Activity mActivity;
    private RelativeLayout rlCategory1; //1차 분류 layout
    private RelativeLayout rlCategory2; //2차 분류 layout
    private RelativeLayout rlCategory3; //3차 분류 layout
    private LinearLayout llLowCategory; //2차 분류 & 3차 분류 layout
    private TextView txtCategory1; //1차 분류(대분류)
    private TextView txtCategory2; //2차분류
    private TextView txtCategory3; //3차분류
    private ListView lvAd; //광고 list
    private LinearLayout llADEmpty; //광고 없을 시 d/p
    private ImageView ivSearch; //검색 이미지

    private final int DIALOG_MODE_1 = 0; //1차분류 dlg
    private final int DIALOG_MODE_2 = 1; //2차분류 dlg
    private final int DIALOG_MODE_3 = 2; //3차분류 dlg

    //광고 list [[
    private final int SEND_PAGE_NO = 2;
    private final int SEND_AD_CODE = 3; //적립하기 or 관심광고
    private final int SEND_SCH_STEP1 = 4;
    private final int SEND_SCH_STEP2 = 5;
    private final int SEND_SCH_STEP3 = 6;

    private final String STR_PAGE_NO = "pageno";
    private final String STR_AD_CODE = "ad_code";
    private final String STR_SCH_STEP1 = "sch_step1";
    private final String STR_SCH_STEP2 = "sch_step2";
    private final String STR_SCH_STEP3 = "sch_step3";
    private final String STR_USER_IDX = "idx";

    private final String STR_AD_CODE_AC = "ac";

    //서버 리턴 값
    private final String STR_AD_IDX = "ad_idx"; //광고 idx
    private final String STR_AD_MAIN_IMG_URL = "ad_mainurl"; //광고메인 이미지 url
    private final String STR_AD_NM = "ad_nm"; //광고명
    private final String STR_AD_POINT = "ad_point"; //광고 클릭 시 적립포인트
    private final String STR_AD_CONTENT = "ad_content"; //이용방법 및 주의사항
    private final String STR_AD_ETC = "ad_etc"; //이용방법 및 주의사항
    private final String STR_AD_DETAIL = "ad_brief"; //광고 설명
    private final String STR_AD_GRADE = "ad_rating"; //평점
    private final String STR_AD_EVENT = "ad_event"; //이벤트
    private final String STR_AD_IS_READ = "ad_is_read"; // 광고 확인 여부 0: 확인안함 1: 확인

    private int mPageNo = 1; //요청 페이지
    //광고 list ]]

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;
    private final int PARSER_NUM_7 = 7;
    private final int PARSER_NUM_8 = 8;
    private final int PARSER_NUM_9 = 9;

    private String strDlgChildTitle1=""; //1차 선택 된 text
    private String strDlgChildTitle2=""; //2차 선택 된 text
    private String strGroupIdx=""; //1차 분류 idx
    private String strChildIdx1 = ""; //2차 분류 idx
    private String strChildIdx2 = ""; //3차 분류 idx
    private int mCodeStep; //step num
    private String strDataType = "";

    private ArrayList<TxtListDataInfo> arrData1; //1차 분류 data
    private ArrayList<TxtListDataInfo> arrData2; //2차 분류 data
    private ArrayList<TxtListDataInfo> arrData3; //3차 분류 data
    private TxtListDataInfo mStepData;

    private ListADInfo mADAddTemp = new ListADInfo();
    private ListADAdapter lvAdapter = null;

    private LinearLayout llProgress;

    private boolean isMyADAdd = true;

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.\
    private boolean isListUpdate = false;

    private boolean isSelSiGun = false;
    private boolean isFirst = true;

    private final int FR_PAGE = 0;
    private int mFrPage=-1;

    private  String mSiDo;
    private  String mGuDong;
    private int iarrData1index=0;
    private int iarrData2index=0;
    private int iarrData3index=0;

    private FrameLayout layoutBG;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mLockListView = false;
            switch (msg.what) {
                case StaticDataInfo.ADVER_INTEREST:
                    if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
                    if(msg.arg1== StaticDataInfo.RESULT_CODE_200) {
                        if(msg.arg2 == StaticDataInfo.TRUE) {
                            Toast.makeText(mActivity, getResources().getString(R.string.str_my_ad_add_succese), Toast.LENGTH_SHORT).show();
                        }else if(msg.arg2 == StaticDataInfo.FALSE) {
                            Toast.makeText(mActivity, getResources().getString(R.string.str_my_ad_minus_succese), Toast.LENGTH_SHORT).show();
                        }
                        if (lvAdapter != null) lvAdapter.notifyDataSetChanged();
                    }else {
                        mADAddTemp = new ListADInfo();
                        mADAddTemp = (ListADInfo) msg.obj;

//                        Intent intent = new Intent(mActivity, DlgBtnActivity.class);
//                        intent.putExtra("DlgMode", "Two");
//                        if(mADAddTemp.getStrMyADYN().equals(StaticDataInfo.STRING_Y)){
//                            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_my_ad_add_chk));
//                            isMyADAdd = true;
//                        }else{
//                            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_my_ad_minus_chk));
//                            isMyADAdd = false;
//                        }
//                        startActivityForResult(intent, REQUEST_CODE);
                    }
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(isListUpdate){
                        isListUpdate=false;
                    }else if(msg.arg1 == StaticDataInfo.RESULT_NO_DATA) {
                        if (arrListADInfo != null) arrListADInfo.clear();

//                        if(mLockListView) {
                        lvAd.setVisibility(View.GONE);
                        llADEmpty.setVisibility(View.VISIBLE);
//                        }

                        if (mPageNo>1){
                            mLockListView=true;
                        }

                    }

                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    if(!isFirst) {
                    }else{
                        isFirst = false;
                        lvAd.setVisibility(View.GONE);
                        llADEmpty.setVisibility(View.VISIBLE);
                    }
                    break;
                case StaticDataInfo.RESULT_NO_SIGUN:
                    isSelSiGun = true;
                    if(arrData3!=null) arrData3.clear();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if (msg.arg1 == PARSER_NUM_1 && ((ArrayList<TxtListDataInfo>) msg.obj).size()>0) {
                        arrData1 = new ArrayList<TxtListDataInfo>();
                        arrData1.add(mStepData);
                        arrData1.addAll(chkNoBuyCharCategory((ArrayList<TxtListDataInfo>) msg.obj));

                        //여기서 지역을 설정하자  2017-08-24 테스트 임
                        if(!mSiDo.equals("")) {                                                   //지역이 있다면 지역검색
                            iarrData1index = 1;
                            strDlgChildTitle1 = arrData1.get(iarrData1index).getStrMsg();
                            strGroupIdx = arrData1.get(iarrData1index).getStrIdx();
                            txtCategory1.setText(strDlgChildTitle1);
                            llLowCategory.setVisibility(View.VISIBLE);
                            ivSearch.setVisibility(View.VISIBLE);
                            new CommCode(mActivity, StaticDataInfo.COMMON_CODE_TYPE_AD, 2, strGroupIdx, handler);
                        }
                    } else if (msg.arg1 == PARSER_NUM_2 && ((ArrayList<TxtListDataInfo>) msg.obj).size()>0) {
                        arrData2 = new ArrayList<TxtListDataInfo>();
                        arrData2.addAll(chkNoBuyCharCategory((ArrayList<TxtListDataInfo>) msg.obj));

                        //여기서 지역을 설정하자  2017-08-24 테스트 임
                        if(!mSiDo.equals("")) {                                                   //지역이 있다면 지역검색

                            //GPS가 수신되지만 자기가 설정한 지역이 나온다면  GPS로 수신되 지역으로 바꾸자 메인에서 주소를 늦게 받는다면 여기서 다시 바구자
                            //GPS가 수신되었고 메인의 mGpsAddress1 "" 이 아니데  프레그먼트에서 전달 받은 주소가 다르 다면 지역을 재설정 하자
                            //리스트가 늦어진다면 getData() 에 아래의 로직을 넣어야함

                            String lmGpsAddress1=((MainActivity)mActivity).mGpsAddress1;
                            String lmGpsAddress2=((MainActivity)mActivity).mGpsAddress2;
                            boolean lGPSOn = ((MainActivity)mActivity).mGPSOn;
                            Log.d("temp","**********************lGPSOn["+lGPSOn+"]lmGpsAddress1["+lmGpsAddress1+"]lmGpsAddress2["+lmGpsAddress2+"*************************");
                            Log.d("temp","**********************lGPSOn["+lGPSOn+"]mSiDo["+mSiDo+"]mGuDong["+mGuDong+"*************************");

                            if( lGPSOn==true && !lmGpsAddress1.equals("")) {
                                Log.d("temp","**********************lGPSOn==true && !lmGpsAddress1.equals*************************");
                                    if(!mSiDo.equals(lmGpsAddress1)){
                                        Log.d("temp","**********************!mSiDo.equals(lmGpsAddress1)*************************");
                                        mSiDo=lmGpsAddress1;
                                        mGuDong=lmGpsAddress2;
                                    }
                            }
                            Log.d("temp","**********************lGPSOn["+lGPSOn+"]mSiDo["+mSiDo+"]mGuDong["+mGuDong+"*************************");

                            iarrData2index = findAreaIndex(arrData2, mSiDo);
                            Log.d("temp", "****************iarrData2index[" + iarrData2index + "]");

                            //strDlgChildTitle2 = arrData2.get(0).getStrMsg();
                            //strChildIdx1 = arrData2.get(0).getStrIdx();
                            strDlgChildTitle2 = arrData2.get(iarrData2index).getStrMsg();
                            strChildIdx1 = arrData2.get(iarrData2index).getStrIdx();
                            new CommCode(mActivity, StaticDataInfo.COMMON_CODE_TYPE_AD, 3, strChildIdx1, handler);
                            txtCategory2.setText(strDlgChildTitle2);
                        }

                    } else if (msg.arg1 == PARSER_NUM_3 && (((ArrayList<TxtListDataInfo>) msg.obj).size()>0) && !txtCategory2.getText().toString().equals("")) {
                        arrData3 = new ArrayList<TxtListDataInfo>();
                        arrData3.addAll(chkNoBuyCharCategory((ArrayList<TxtListDataInfo>) msg.obj));

                        //여기서 지역을 설정하자  2017-08-24 테스트 임
                        if(!mSiDo.equals("")) {                                                     //지역이 있다면 지역검색
                            iarrData3index = findAreaIndex(arrData3, mGuDong);

                            Log.d("temp", "****************iarrData3index[" + iarrData3index + "]");
                            //txtCategory3.setText(arrData3.get(7).getStrMsg());
                            //strChildIdx2 = arrData3.get(7).getStrIdx();
                            txtCategory3.setText(arrData3.get(iarrData3index).getStrMsg());
                            strChildIdx2 = arrData3.get(iarrData3index).getStrIdx();

                            //검색을 요청하자
                            if(mPageNo==1)  getADList();
                        }
                    } else if (arrListADInfo != null && arrListADInfo.size() > 0) {
                        lvAd.setVisibility(View.VISIBLE);
                        llADEmpty.setVisibility(View.GONE);
                        if (mPageNo == 1) {
                            lvAdapter = new ListADAdapter(mActivity, arrListADInfo, "", handler);
                            lvAd.setAdapter(lvAdapter);
                            lvAd.setOnItemClickListener(mItemClick);
                            lvAd.setOnScrollListener(mListScroll);
                        } else {
                            if (lvAdapter != null) lvAdapter.notifyDataSetChanged();
                        }
                        mPageNo++;
                    }
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

    private  ArrayList<TxtListDataInfo> chkNoBuyCharCategory(ArrayList<TxtListDataInfo> arrData){
        ArrayList<TxtListDataInfo> tmp = new ArrayList<TxtListDataInfo>();
        for(int i = 0; i<arrData.size(); i++){
            if(!arrData.get(i).getStrIdx().startsWith(StaticDataInfo.STRING_N)) tmp.add(arrData.get(i));
        }

        return tmp;
    }

    private int findAreaIndex(ArrayList<TxtListDataInfo> pArrDate,String pAddr){
        int iReturn=0;
        if(pArrDate==null || pAddr.equals("")){
            return iReturn;
        }

        int i=0;
        for(TxtListDataInfo s: pArrDate){
            /*
            if(s.getStrMsg().indexOf(pAddr)>=0){
                return iReturn=i;
            }
            */
            if(s.getStrMsg().equals(pAddr)){                                                        //인천 남구,남동구로 나와서 교체함
                return iReturn=i;
            }

            i++;
        }
        if(iReturn<0) iReturn=0;

        return iReturn;
    }



    public static FrListAD newInstance(int mPage,String mAddr1,String mAddr2){
        FrListAD fragment = new FrListAD();
        Bundle args =  new Bundle();
        args.putInt("Page", mPage);
        args.putString("Addr1", mAddr1);
        args.putString("Addr2", mAddr2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            mFrPage     = getArguments().getInt("Page");
            mSiDo       = getArguments().getString("Addr1");
            mGuDong     = getArguments().getString("Addr2");
        }

        if(mSiDo==null || mSiDo.equals("null")){
            mSiDo="";
        }
        if(mGuDong==null || mGuDong.equals("null")){
            mGuDong="";
        }

        Log.d("temp","***************FrListAD mSiDo["+mSiDo+"]****************");
        Log.d("temp","***************FrListAD mAddrSi["+mGuDong+"]****************");
    }

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
    public void onStart() {
        super.onStart();
        if(mFrPage==FR_PAGE) {
            if (isRestart) {
                mPageNo = 1;
                getData();

                isRestart = false;
            }
        }else{
            isRestart = false;
        }
    }

    private boolean isRestart = true;
    @Override
    public void onPause() {
        super.onPause();

        isRestart = false;
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(lvAdapter!=null) lvAdapter.notifyDataSetChanged();
    }

    public void getData(){
        mStepData = new TxtListDataInfo();
        mStepData.setStrIdx("0");
        mStepData.setStrMsg(getResources().getString(R.string.str_sel_ad_send_default_info));
        strDataType = StaticDataInfo.COMMON_CODE_TYPE_AD;
        new CommCode(mActivity, strDataType, PARSER_NUM_1, "", handler);

        strDlgChildTitle1 = mStepData.getStrMsg();
        strGroupIdx = mStepData.getStrIdx();
        txtCategory1.setText(strDlgChildTitle1);
        llLowCategory.setVisibility(View.GONE);
        ivSearch.setVisibility(View.GONE);
        strChildIdx2="";

        mPageNo = 1;


        if(mSiDo.equals("")) {                                                                    //지역이 없다면 전체로 먼저 조회한다. 지역이 있다면 지역코드 가져와서 광고리스트를 조회한다.
            getADList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_list_ad, null);

        layoutBG = (FrameLayout) view.findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        if(isRestart) {
            llProgress = (LinearLayout) view.findViewById(R.id.ll_progress_circle);
            llADEmpty = (LinearLayout) view.findViewById(R.id.ll_ad_empty);
            lvAd = (ListView) view.findViewById(R.id.lv_ad);
            ((LinearLayout) view.findViewById(R.id.ll_search)).setOnClickListener(this);
            rlCategory1 = (RelativeLayout) view.findViewById(R.id.rl_category1);
            rlCategory2 = (RelativeLayout) view.findViewById(R.id.rl_category2);
            rlCategory3 = (RelativeLayout) view.findViewById(R.id.rl_category3);
            llLowCategory = (LinearLayout) view.findViewById(R.id.ll_low_category);
            txtCategory1 = (TextView) view.findViewById(R.id.txt_category1);
            txtCategory2 = (TextView) view.findViewById(R.id.txt_category2);
            txtCategory3 = (TextView) view.findViewById(R.id.txt_category3);
            rlCategory1.setOnClickListener(this);
            rlCategory2.setOnClickListener(this);
            rlCategory3.setOnClickListener(this);
            ivSearch = (ImageView) view.findViewById(R.id.iv_search);
        }
        return view;
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


    private int mListPosition = -1;
    public AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ADListClickDlg(arrListADInfo.get(position));
            mListPosition = position;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public OnScrollListener mListScroll = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            Log.d("temp","************mLockListView["+mLockListView+"]************");
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && !mLockListView) {
                if(lastitemVisibleFlag){
                    isListUpdate = true;
                    getADList();
                }else if(firstitemVisibleFlag){
                    mPageNo = 1;
                    getADList();
                }
            }

            if(touchListener!=null && !mLockListView){
                lvAd.setScrollContainer(false);
                touchListener.setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount) >= totalItemCount;
            firstitemVisibleFlag = (firstVisibleItem==0) && view.getChildAt(0)!=null && view.getChildAt(0).getTop()==0;
        }
    };

    private Dialog mDlgADCareMsg;
    private Button btnCareCancel;
    private Button btnCareOk;
    public void ADListClickDlg(ListADInfo adInfo){
        mDlgADCareMsg = new Dialog(mActivity);
        mDlgADCareMsg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlgADCareMsg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDlgADCareMsg.setContentView(R.layout.dlg_ad_list_click);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlgADCareMsg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlgADCareMsg.getWindow().setAttributes((WindowManager.LayoutParams) params);
        ImageView ivAD = (ImageView) mDlgADCareMsg.findViewById(R.id.iv_ad);

        Glide
                .with(mActivity)
                .load(adInfo.getStrTitleImgUrl().replace("\\", "//"))
                .centerCrop()
                .placeholder(R.drawable.image_none)
                .into(ivAD);
        ((TextView) mDlgADCareMsg.findViewById(R.id.txt_ad_nm)).setText(adInfo.getStrName());
        TextView txtInfo = (TextView) mDlgADCareMsg.findViewById(R.id.txt_ad_info);
        if (adInfo.getStrDetail().equals("")) {
            txtInfo.setVisibility(View.GONE);
        }else{
            txtInfo.setText(adInfo.getStrDetail());
        }
        ((TextView) mDlgADCareMsg.findViewById(R.id.txt_point)).setText(StaticDataInfo.makeStringComma(adInfo.getStrPoint()));
        if(adInfo==null || adInfo.getStrGrade().equals("")) {
            adInfo.setStrGrade("0.0");
        }
        ((TextView) mDlgADCareMsg.findViewById(R.id.txt_grade)).setText(mActivity.getResources().getString(R.string.str_ad_average) + " " + adInfo.getStrGrade());
        String strNewLine = "\n";

        LinearLayout llMsgDiv = (LinearLayout) mDlgADCareMsg.findViewById(R.id.ll_msg_divider);
        TextView txtMsg = (TextView) mDlgADCareMsg.findViewById(R.id.txt_case_msg);
        if(adInfo.getStrContents().equals("") && adInfo.getStrEtc().equals("")){
            txtMsg.setVisibility(View.GONE);
            llMsgDiv.setVisibility(View.VISIBLE);
        }else{
            llMsgDiv.setVisibility(View.GONE);
            if(adInfo.getStrContents().equals("") || adInfo.getStrEtc().equals("")){
                txtMsg.setText(adInfo.getStrContents()+adInfo.getStrEtc());
            }else {
                txtMsg.setText(adInfo.getStrContents() + "\n" + adInfo.getStrEtc());
            }
        }

        btnCareCancel = (Button) mDlgADCareMsg.findViewById(R.id.btn_care_cancel);
        btnCareOk = (Button) mDlgADCareMsg.findViewById(R.id.btn_care_ok);
        ((LinearLayout) mDlgADCareMsg.findViewById(R.id.ll_care_ok)).setOnTouchListener(this);
        ((LinearLayout) mDlgADCareMsg.findViewById(R.id.ll_care_cancel)).setOnTouchListener(this);
        btnCareCancel.setOnTouchListener(this);
        btnCareOk.setOnTouchListener(this);

        mDlgADCareMsg.show();
    }

    private Dialog mDlg;
    /**
     * 1차분류, 2차분류, 3차분류 선택 popup
     * @param mMode
     */
    public void OpenDlg(final int mMode){
        mDlg = new Dialog(mActivity);
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

        switch(mMode){
            case DIALOG_MODE_1: //1차 분류
                if(arrData1!=null && arrData1.size()>0) {
                    txtDlgTitle.setVisibility(View.GONE);
                    ((LinearLayout) mDlg.findViewById(R.id.ll_dlg_title_devider)).setVisibility(View.GONE);
                    commAdapter = new CommCodeAdapter(mActivity, R.layout.list_txt_item, arrData1);
                }
                break;
            case DIALOG_MODE_2: //2차 분류
                if(arrData2!=null && arrData2.size()>0) {
                    txtDlgTitle.setText(txtCategory1.getText().toString());
                    commAdapter = new CommCodeAdapter(mActivity, R.layout.list_txt_item, arrData2);
                }else{
                    Toast.makeText(mActivity, getResources().getString(R.string.str_select_empty), Toast.LENGTH_SHORT).show();
                }
                break;
            case DIALOG_MODE_3: //3차 분류
                if(arrData3!=null && arrData3.size()>0) {
                    txtDlgTitle.setText(txtCategory2.getText().toString());
                    commAdapter = new CommCodeAdapter(mActivity, R.layout.list_txt_item, arrData3);
                } else {
                    isSelSiGun = true;
                    Toast.makeText(mActivity, getResources().getString(R.string.str_select_empty), Toast.LENGTH_SHORT).show();
                }
                break;
        }

        if(commAdapter!=null){
            lvText.setAdapter(commAdapter);
            mDlg.show();
        }

        lvText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mMode) {
                    case DIALOG_MODE_1:
                        strDlgChildTitle1 = arrData1.get(position).getStrMsg();
                        strGroupIdx = arrData1.get(position).getStrIdx();
                        txtCategory1.setText(strDlgChildTitle1);
                        if(arrData2!=null) arrData2.clear();
                        if(arrData3!=null) arrData3.clear();

                        txtCategory2.setText("");
                        txtCategory3.setText("");
                        strChildIdx1 = "";
                        strChildIdx2 = "";

                        if (strDlgChildTitle1.equals(getResources().getString(R.string.str_sel_ad_send_default_info))) {
                            llLowCategory.setVisibility(View.GONE);
                            ivSearch.setVisibility(View.GONE);
                        } else {
                            llLowCategory.setVisibility(View.VISIBLE);
                            ivSearch.setVisibility(View.VISIBLE);
                            new CommCode(mActivity, StaticDataInfo.COMMON_CODE_TYPE_AD, 2, strGroupIdx, handler);
                        }
                        break;
                    case DIALOG_MODE_2:
                        strDlgChildTitle2 = arrData2.get(position).getStrMsg();
                        strChildIdx1 = arrData2.get(position).getStrIdx();
                        new CommCode(mActivity, StaticDataInfo.COMMON_CODE_TYPE_AD, 3, strChildIdx1, handler);
                        txtCategory2.setText(strDlgChildTitle2);
                        txtCategory3.setText("");
                        strChildIdx2="";
                        isSelSiGun = false;
                        break;
                    case DIALOG_MODE_3:
                        txtCategory3.setText(arrData3.get(position).getStrMsg());
                        strChildIdx2 = arrData3.get(position).getStrIdx();
                        break;
                }
                mDlg.dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recycleView(layoutBG);
        if (mDlg != null && mDlg.isShowing()) mDlg.dismiss();
        if (mDlgADCareMsg != null && mDlgADCareMsg.isShowing()) mDlgADCareMsg.dismiss();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_category1) {
            OpenDlg(DIALOG_MODE_1);
        } else if (v.getId() == R.id.rl_category2) {
            OpenDlg(DIALOG_MODE_2);
        } else if (v.getId() == R.id.rl_category3) {
            OpenDlg(DIALOG_MODE_3);
        } else if (v.getId() == R.id.ll_search) {
            mPageNo = 1;
            getADList();
        }
    }

    public void getADList(){
        mLockListView = true;
        String url      = "";
        String token    = "";

        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        token = pref.getString(mActivity.getResources().getString(R.string.str_token), "");
        HashMap<String, String> k_param = new HashMap<String, String>();

        if(txtCategory1.getText().toString().equals("지역")){                                               //지역 선택시
            if(txtCategory2.getText().toString().equals("") || txtCategory3.getText().toString().equals("")){
                Toast.makeText(mActivity, getResources().getString(R.string.str_select_area), Toast.LENGTH_SHORT).show();
                return;
            }

            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_areaadlist);
            k_param = new HashMap<String, String>();
            k_param.put(mActivity.getResources().getString(R.string.str_token), token);
            k_param.put(STR_PAGE_NO, String.valueOf(mPageNo));
            k_param.put(STR_SCH_STEP1, strGroupIdx);
            k_param.put(STR_SCH_STEP2, txtCategory2.getText().toString());
            if(!strChildIdx2.equals("")){
                k_param.put(STR_SCH_STEP3, txtCategory3.getText().toString());
            }
            k_param.put(STR_AD_CODE, STR_AD_CODE_AC);
        }else{
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise);
            k_param = new HashMap<String, String>();
            k_param.put(mActivity.getResources().getString(R.string.str_token), token);
            k_param.put(STR_PAGE_NO, String.valueOf(mPageNo));
            k_param.put(STR_SCH_STEP1, strGroupIdx);
            k_param.put(STR_SCH_STEP2, strChildIdx1);
            if(!strChildIdx2.equals("")){
                k_param.put(STR_SCH_STEP3, txtCategory3.getText().toString());
            }
            k_param.put(STR_AD_CODE, STR_AD_CODE_AC);
        }


        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }


        CommonDataTask.DataTaskCallback callback = new CommonDataTask.DataTaskCallback() {
            @Override
            public void onPreExecute() {
                // 네트워크 요청 시작 전에 UI 업데이트 (예: 프로그레스바 표시)
                Log.d("CommonDataTask", "onPreExecute");
                if (llProgress != null && !llProgress.isShown()) {
                    llProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPostExecute(String result) {
                // 네트워크 요청 완료 후 결과 처리
                Log.d("CommonDataTask", "onPostExecute: " + result);
                ResultADList(result);
            }

            @Override
            public void onError(Exception e) {
                // 네트워크 요청 중 에러 발생 시 처리
                Log.e("CommonDataTask", "onError", e);
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        };

        CommonDataTask task = new CommonDataTask(url, k_param, callback);
        task.execute();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId()==R.id.ll_care_cancel || v.getId()==R.id.btn_care_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCareCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) if(mDlgADCareMsg!=null && mDlgADCareMsg.isShowing()) mDlgADCareMsg.dismiss();
            return true;
        }else if(v.getId()==R.id.ll_care_ok || v.getId()==R.id.btn_care_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCareOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                if(mListPosition!=-1 && arrListADInfo!=null){
                    Intent intent = new Intent(mActivity, ADDetailViewActivity.class);
                    intent.putExtra("AD_IDX", arrListADInfo.get(mListPosition).getStrIdx());
                    intent.putExtra("AD_KIND", getResources().getString(R.string.str_user_en));
                    startActivity(intent);

                    if(!arrListADInfo.get(mListPosition).getStrIsRead().equals(String.valueOf(StaticDataInfo.TRUE))) {
                        arrListADInfo.get(mListPosition).setStrIsRead(String.valueOf(StaticDataInfo.TRUE));
                    }
                }

                if(mDlgADCareMsg!=null && mDlgADCareMsg.isShowing()) mDlgADCareMsg.dismiss();
            }
            return true;
        }

        return false;
    }

    private ListADInfo mListADInfo;
    private ArrayList<ListADInfo> arrListADInfo;

    public void ResultADList(String result){
        //result.endsWith("out") : 타임아웃
        if(result.startsWith(StaticDataInfo.TAG_LIST)){
            try {

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(result));

                int eventType = parser.getEventType();
                int k_data_num = 0;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.END_TAG:
                            if (parser.getName().equalsIgnoreCase(StaticDataInfo.TAG_ITEM) && arrListADInfo != null && mListADInfo != null) {
                                arrListADInfo.add(mListADInfo);
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            if(mPageNo==1){
                                arrListADInfo = new ArrayList<ListADInfo>();
                                arrListADInfo.clear();
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                                mListADInfo = new ListADInfo();
                            }

                            if (parser.getName().equals(STR_AD_IDX)) {
                                k_data_num = PARSER_NUM_0;
                            } else if (parser.getName().equals(STR_AD_NM)) {
                                k_data_num = PARSER_NUM_1;
                            } else if (parser.getName().equals(STR_AD_MAIN_IMG_URL)) {
                                k_data_num = PARSER_NUM_2;
                            } else if (parser.getName().equals(STR_AD_GRADE)) {
                                k_data_num = PARSER_NUM_3;
                            } else if (parser.getName().equals(STR_AD_POINT)) {
                                k_data_num = PARSER_NUM_4;
                            } else if (parser.getName().equals(STR_AD_ETC)) {
                                k_data_num = PARSER_NUM_5;
                            } else if (parser.getName().equals(STR_AD_EVENT)) {
                                k_data_num = PARSER_NUM_6;
                            } else if (parser.getName().equals(STR_AD_DETAIL)) {
                                k_data_num = PARSER_NUM_7;
                            } else if (parser.getName().equals(STR_AD_CONTENT)) {
                                k_data_num = PARSER_NUM_8;
                            } else if (parser.getName().equals(STR_AD_IS_READ)) {
                                k_data_num = PARSER_NUM_9;
                            } else {
                                k_data_num = DEFAULT_NUM;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (k_data_num > DEFAULT_NUM) {
                                switch (k_data_num) {
                                    case PARSER_NUM_0:
                                        mListADInfo.setStrIdx(parser.getText());
                                        break;
                                    case PARSER_NUM_1:
                                        mListADInfo.setStrName(parser.getText());
                                        break;
                                    case PARSER_NUM_2:
                                        mListADInfo.setStrTitleImgUrl(parser.getText());
                                        break;
                                    case PARSER_NUM_3:
                                        if(parser.getText()!=null && parser.getText().equals("0")){
                                            mListADInfo.setStrGrade("0.0");
                                        }else {
                                            mListADInfo.setStrGrade(parser.getText());
                                        }
                                        break;
                                    case PARSER_NUM_4:
                                        mListADInfo.setStrPoint(parser.getText());
                                        break;
                                    case PARSER_NUM_5:
                                        mListADInfo.setStrEtc(parser.getText());
                                        break;
                                    case PARSER_NUM_6:
                                        mListADInfo.setStrEventYN(parser.getText());
                                        break;
                                    case PARSER_NUM_7:
                                        mListADInfo.setStrDetail(parser.getText());
                                        break;
                                    case PARSER_NUM_8:
                                        mListADInfo.setStrContents(parser.getText());
                                        break;
                                    case PARSER_NUM_9:
                                        mListADInfo.setStrIsRead(parser.getText());
                                        break;
                                }
                                k_data_num = DEFAULT_NUM;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(result.equals(String.valueOf(StaticDataInfo.RESULT_NO_DATA))){
            Message msg = new Message();
            msg.what = StaticDataInfo.RESULT_NO_DATA;
            msg.arg1 = StaticDataInfo.RESULT_NO_DATA;
            handler.sendMessage(msg);
        }else{
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }
}
