package com.cashcuk.membership;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;

/**
 * 로그인 성공
 */
public class MembershipSuccessActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership_success);
        CheckLoginService.mActivityList.add(this);

        MainTitleBar mMainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_refresh)).setVisibility(View.GONE);
        ((ImageButton) mMainTitleBar.findViewById(R.id.ib_home)).setVisibility(View.GONE);

        ((Button) findViewById(R.id.btn_go_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
