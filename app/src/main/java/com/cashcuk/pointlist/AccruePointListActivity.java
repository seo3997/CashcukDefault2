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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.cashcuk.ad.adlist.SwipeDismissListViewTouchListener;
import com.cashcuk.common.RequestPointList;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.dialog.DlgListAdapter;

import java.util.ArrayList;

/**
 * 누적포인트 내역
 */
public class AccruePointListActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private final int REQUEST_USE_POINT = 999;
    private ArrayList<PointListDataInfo> arrPointInfo = new ArrayList<PointListDataInfo>();

    private TextView txtMyPoint;
    private ListView lvPointList;
    private LinearLayout llListEmpty; //내역이 존재 하지 않을 때 d/p

    private LinearLayout llProgress;

    private int mPageNo = 1;
    private PointLIstAdapter adapter=null;

    private final String STR_REQUEST_LIST_CNT = "D";

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
                    Toast.makeText(AccruePointListActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1 == StaticDataInfo.RESULT_CODE_MY_POINT) {
                        txtMyPoint.setText(StaticDataInfo.makeStringComma((String) msg.obj));
                    }else if((ArrayList<PointListDataInfo>)msg.obj!=null && ((ArrayList<PointListDataInfo>) msg.obj).size()>0) {
                        lvPointList.setVisibility(View.VISIBLE);
                        llListEmpty.setVisibility(View.GONE);

                        if(mPageNo==1){
                            arrPointInfo = new ArrayList<PointListDataInfo>();
                        }

                        arrPointInfo.addAll((ArrayList<PointListDataInfo>) msg.obj);
                        if(mPageNo==1) {
                            adapter = new PointLIstAdapter(AccruePointListActivity.this, arrPointInfo);
                            lvPointList.setAdapter(adapter);
                            lvPointList.setOnScrollListener(mListScroll);
                        }else{
                            if(adapter!=null) adapter.notifyDataSetChanged();
                        }

                        mPageNo++;
                    }
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    if(mPageNo==1) {
                        lvPointList.setVisibility(View.GONE);
                        llListEmpty.setVisibility(View.VISIBLE);
                    }
                    break;
            }

            mLockListView = false;
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
        setContentView(R.layout.activity_point_accrue_list);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        MainTitleBar mMaintTileBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mMaintTileBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageNo = 1;
                if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
                new RequestPointList(AccruePointListActivity.this, StaticDataInfo.MODE_POINT_LIST_ACCRUE, STR_REQUEST_LIST_CNT, handler, mPageNo);
                new GetChargePoint(AccruePointListActivity.this, handler);
            }
        });

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_accrue_point));

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);
        Intent intent = getIntent();
        View vMyPoint = (View) findViewById(R.id.layout_my_point);
        txtMyPoint = (TextView) vMyPoint.findViewById(R.id.txt_title_my_point);
        ((Button) findViewById(R.id.btn_use)).setOnClickListener(this);

        lvPointList = (ListView) findViewById(R.id.lv_point_list);
        llListEmpty = (LinearLayout) findViewById(R.id.ll_list_empty);

        UsePointDlg();

        new RequestPointList(AccruePointListActivity.this, StaticDataInfo.MODE_POINT_LIST_ACCRUE, STR_REQUEST_LIST_CNT, handler, mPageNo);
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
    protected void onResume() {
        super.onResume();
        new GetChargePoint(AccruePointListActivity.this, handler);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_use) {
            //포인트 사용
            if(mDlg!=null && !mDlg.isShowing()) mDlg.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 포인트 사용하기
     */
    private Dialog mDlg;
    private ArrayList<String> arrString;
    private Button btn1;
    public void UsePointDlg(){
        mDlg = new Dialog(this);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDlg.setContentView(R.layout.dlg_list_title);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDlg.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDlg.getWindow().setAttributes((WindowManager.LayoutParams) params);

        ((TextView) mDlg.findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_point_use_dlg_title));
        ListView lvDlgMsg = (ListView) mDlg.findViewById(R.id.lv_dlg);

        arrString = new ArrayList<String>();
        arrString.add(getResources().getString(R.string.str_point_use_input_my_account)); //내 계좌로 입금
//        arrString.add(getResources().getString(R.string.str_down_character)); //캐릭터 다운

        btn1 = (Button) mDlg.findViewById(R.id.btn1);
        btn1.setOnTouchListener(this);
        ((LinearLayout) mDlg.findViewById(R.id.ll1)).setOnTouchListener(this);

        DlgListAdapter dlgAdapter = new DlgListAdapter(AccruePointListActivity.this, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(arrString.get(position).equals(getResources().getString(R.string.str_point_use_input_my_account))){ //내 계좌로 입금
                    RequestInputPoint();
//                }else if(arrString.get(position).equals(getResources().getString(R.string.str_down_character))){ //캐릭터 받기
//                    Intent intent = new Intent(AccruePointListActivity.this, DlgCharaterCategory.class);
//                    intent.putExtra(StaticDataInfo.STR_CHARATER_DOWN_OTHER, StaticDataInfo.STR_CHARATER_DOWN);
//                    startActivity(intent);
                }
                if(mDlg != null && mDlg.isShowing()) mDlg.dismiss();
            }
        });
    }

    public void RequestInputPoint(){
        SharedPreferences prefs = getSharedPreferences("SaveDefaultSetInfo", MODE_PRIVATE);
        String strChangeMinMoney = prefs.getString("ChangeMinMoney", "");
        if (strChangeMinMoney == null || strChangeMinMoney.equals("")) {
            strChangeMinMoney = "20000";
        }

        String strMyPoint = txtMyPoint.getText().toString();

        Intent intent = null;
        float mMyPoint = 0;
        if(strMyPoint.contains(",")){
            mMyPoint = Float.parseFloat(strMyPoint.replace(",", ""));
        }else{
            mMyPoint = Float.parseFloat(strMyPoint);
        }
        if(mMyPoint<Integer.parseInt(strChangeMinMoney)){
            intent = new Intent(AccruePointListActivity.this, DlgBtnActivity.class);
            intent.putExtra("DlgTitle", getResources().getString(R.string.str_setting_alrim));
            intent.putExtra("BtnDlgMsg", String.format(getResources().getString(R.string.str_input_money_request_err), StaticDataInfo.makeStringComma(strChangeMinMoney)));
        }else {
            intent = new Intent(getApplicationContext(), PointInputAccountActivity.class);
            intent.putExtra("MyPoint", strMyPoint);
            finish();
        }

        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_USE_POINT){
                finish();
            }
        }
    }

    private SwipeDismissListViewTouchListener touchListener;
    private boolean lastitemVisibleFlag = false;
    private boolean firstitemVisibleFlag = false;
    private boolean mLockListView = true; // 아이템을 추가하는 동안 중복 요청 방지위해 락을 걸어 둠. 중복요청되면 ANR 발생함.\
    private boolean isListUpdate = false;
    public AbsListView.OnScrollListener mListScroll = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !mLockListView) {
                if(lastitemVisibleFlag){
                    isListUpdate = true;
                    requestData();
                }else if(firstitemVisibleFlag){
                    mPageNo = 1;
                    requestData();
                }
            }

            if(touchListener!=null && !mLockListView){
                lvPointList.setScrollContainer(false);
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

    public void requestData(){
        mLockListView = true;
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        new RequestPointList(AccruePointListActivity.this, StaticDataInfo.MODE_POINT_LIST_ACCRUE, STR_REQUEST_LIST_CNT, handler, mPageNo);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.ll1 || v.getId() == R.id.btn1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if (event.getAction() == MotionEvent.ACTION_UP) {
                btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                if (mDlg != null && mDlg.isShowing()) mDlg.dismiss();
            }
            return true;
        }

        return false;
    }
}
