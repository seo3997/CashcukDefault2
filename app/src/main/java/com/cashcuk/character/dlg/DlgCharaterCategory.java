package com.cashcuk.character.dlg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.character.updown.down.DownCharacterActivity;
import com.cashcuk.character.view.CharacterListActivity;
import com.cashcuk.common.CommCode;
import com.cashcuk.common.CommCodeAdapter;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;

import java.util.ArrayList;

/**
 * 캐릭터 카테고리
 */
public class DlgCharaterCategory extends Activity implements View.OnTouchListener{
    private Button btnCancel;
    private final String STR_CATEGORY_CODE = "cat_idx";
    private final int CATEGORY_STEP = 1;
    private ArrayList<TxtListDataInfo> arrCategory;

    private ListView lvDlgMsg;
    private Intent getIntent;
    private String strDownOtherMode="";

    private LinearLayout llProgress;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                        Toast.makeText(DlgCharaterCategory.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_NO_DATA:

                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    if (msg.arg1 == CATEGORY_STEP && ((ArrayList<TxtListDataInfo>) msg.obj).size()>0) {
                        arrCategory = new ArrayList<TxtListDataInfo>();
                        arrCategory.addAll((ArrayList<TxtListDataInfo>) msg.obj);

                        setListData();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CheckLoginService.mActivityList.add(this);

        getIntent = getIntent();

        if(getIntent!=null){
            strDownOtherMode = getIntent.getStringExtra(StaticDataInfo.STR_CHARATER_DOWN_OTHER); //캐릭터 받기
            arrCategory = (ArrayList<TxtListDataInfo>) getIntent.getSerializableExtra("Category");
        }

        if(arrCategory!=null && arrCategory.size()>0) {
            setListData();
        }else {
            new CommCode(this, StaticDataInfo.COMMON_CODE_TYPE_CH, CATEGORY_STEP, "", handler);
            if (llProgress != null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 카테고리 테이터 view
     */
    public void setListData(){
        if (strDownOtherMode.equals(StaticDataInfo.STR_CHARATER_DOWN)) {
            for (int i = 0; i < arrCategory.size(); i++) {
                if (arrCategory.get(i).getStrIdx().startsWith(StaticDataInfo.STRING_N))
                    arrCategory.remove(i);
            }
        }

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);
        setContentView(R.layout.dlg_list_title);
        CheckLoginService.mActivityList.add(this);

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        ((TextView) findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_category));

        lvDlgMsg = (ListView) findViewById(R.id.lv_dlg);
        btnCancel = (Button) findViewById(R.id.btn1);
        btnCancel.setOnTouchListener(this);
        ((LinearLayout) findViewById(R.id.ll1)).setOnTouchListener(this);

        CommCodeAdapter dlgAdapter = new CommCodeAdapter(DlgCharaterCategory.this, R.layout.dlg_list_item, arrCategory);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (strDownOtherMode.equals(StaticDataInfo.STR_CHARATER_DOWN)) {
                    intent = new Intent(DlgCharaterCategory.this, DownCharacterActivity.class);
                } else if (strDownOtherMode.equals(StaticDataInfo.STR_CHARATER_OTHER)) {
                    intent = new Intent(DlgCharaterCategory.this, CharacterListActivity.class);
                }

                if(intent!=null){
                    intent.putExtra("Category", arrCategory);
                    intent.putExtra("CategoryIndex", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
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
}
