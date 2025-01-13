package com.cashcuk.advertiser.charge.chargelist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.ad.adlist.SwipeDismissListViewTouchListener;
import com.cashcuk.advertiser.GetChargeAmount;

import java.util.ArrayList;

/**
 * 사용충전금 내역
 */
public class ChargeUseListActivity extends Activity {
    private ListView lvChargeList;
    private LinearLayout llListEmpty; //내역이 존재 하지 않을 때 d/p

    private ArrayList<ChargeListDataInfo> arrChargeInfo = new ArrayList<ChargeListDataInfo>();
    private TextView txtMyCharge;
    private LinearLayout llProgress;

    private int mPageNo = 1;
    private ChargeListAdapter adapter=null;

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
                    Toast.makeText(ChargeUseListActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if(msg.arg1== GetChargeAmount.MSG_ARG){
                        txtMyCharge.setText((String)msg.obj);
                    } else if((ArrayList<ChargeListDataInfo>)msg.obj!=null && ((ArrayList<ChargeListDataInfo>) msg.obj).size()>0) {
                        lvChargeList.setVisibility(View.VISIBLE);
                        llListEmpty.setVisibility(View.GONE);

                        if(mPageNo==1){
                            arrChargeInfo = new ArrayList<ChargeListDataInfo>();
                        }

                        arrChargeInfo.addAll((ArrayList<ChargeListDataInfo>) msg.obj);
                        if(mPageNo==1) {
                            adapter = new ChargeListAdapter(ChargeUseListActivity.this, arrChargeInfo);
                            lvChargeList.setAdapter(adapter);
                            lvChargeList.setOnScrollListener(mListScroll);
                        }else{
                            if(adapter!=null) adapter.notifyDataSetChanged();
                        }

                        mPageNo++;
                    }
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                    lvChargeList.setVisibility(View.GONE);
                    llListEmpty.setVisibility(View.VISIBLE);
                    break;
            }

            if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_charge_list_activity);
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
                if (llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
                new RequestChargeList(ChargeUseListActivity.this, StaticDataInfo.MODE_POINT_LIST_USE, STR_REQUEST_LIST_CNT, handler, mPageNo);
            }
        });

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_use_charge));

        llProgress = (LinearLayout)findViewById(R.id.ll_progress_circle);
        Intent intent = getIntent();

        View vMyCharge = (View)findViewById(R.id.layout_charge);
        txtMyCharge = (TextView) vMyCharge.findViewById(R.id.txt_title_charge);

        lvChargeList = (ListView) findViewById(R.id.lv_charge);
        llListEmpty = (LinearLayout) findViewById(R.id.ll_list_empty);

        new RequestChargeList(ChargeUseListActivity.this, StaticDataInfo.MODE_POINT_LIST_USE, STR_REQUEST_LIST_CNT, handler, mPageNo);
        new GetChargeAmount(ChargeUseListActivity.this, handler);
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
                lvChargeList.setScrollContainer(false);
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
        new RequestChargeList(ChargeUseListActivity.this, StaticDataInfo.MODE_POINT_LIST_USE, STR_REQUEST_LIST_CNT, handler, mPageNo);
    }
}
