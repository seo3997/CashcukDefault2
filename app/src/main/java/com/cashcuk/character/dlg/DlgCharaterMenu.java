package com.cashcuk.character.dlg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.character.updown.up.UploadCharacterMainActivity;
import com.cashcuk.dialog.DlgListAdapter;

import java.util.ArrayList;

/**
 * 캐릭터 메뉴 popup
 * 삭제, 구매, 판매
 */
public class DlgCharaterMenu extends Activity implements View.OnTouchListener{
    private Button btnCalcel;
    private ArrayList<String> arrString = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CheckLoginService.mActivityList.add(this);

        Intent intent = getIntent();
        String mode = "";
        if(intent != null){
            mode = intent.getStringExtra("Mode");
        }

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);
        setContentView(R.layout.dlg_list_title);
        CheckLoginService.mActivityList.add(this);

        ((TextView) findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_character));
        ListView lvDlgMsg = (ListView) findViewById(R.id.lv_dlg);

        arrString.add(getResources().getString(R.string.str_down_character));
        arrString.add(getResources().getString(R.string.str_shared_character));
        if(mode!=null && mode.equals(getResources().getString(R.string.str_char_category))) {
            arrString.add(getResources().getString(R.string.str_del_character));
        }

        btnCalcel = (Button) findViewById(R.id.btn1);
        btnCalcel.setOnTouchListener(this);
        ((LinearLayout) findViewById(R.id.ll1)).setOnTouchListener(this);

        DlgListAdapter dlgAdapter = new DlgListAdapter(DlgCharaterMenu.this, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (arrString.get(position).equals(getResources().getString(R.string.str_down_character))) { //캐릭터 다운
                    intent = new Intent(DlgCharaterMenu.this, DlgCharaterCategory.class);
                    intent.putExtra(StaticDataInfo.STR_CHARATER_DOWN_OTHER, StaticDataInfo.STR_CHARATER_DOWN);
                } else if (arrString.get(position).equals(getResources().getString(R.string.str_shared_character))) { //캐릭터 공유
                    intent = new Intent(DlgCharaterMenu.this, UploadCharacterMainActivity.class);
                } else if (arrString.get(position).equals(getResources().getString(R.string.str_del_character))) { //캐릭터 삭제
                    setResult(RESULT_OK);
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                finish();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.ll1 || v.getId() == R.id.btn1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                btnCalcel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if (event.getAction() == MotionEvent.ACTION_UP) {
                btnCalcel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk));
                finish();
            }
            return true;
        }
        return false;
    }
}
