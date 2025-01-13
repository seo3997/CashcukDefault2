package com.cashcuk.setting;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.TitleBar;

/**
 * 회사소개
 */
public class CompanyIntroductionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_introduction);
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_setting_company_introduction));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.ll_bg));
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
}
