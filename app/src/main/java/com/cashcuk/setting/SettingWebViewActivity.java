package com.cashcuk.setting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cashcuk.CheckLoginService;
import com.cashcuk.MainTitleBar;
import com.cashcuk.R;
import com.cashcuk.TitleBar;

/**
 * 공지사항, FAQ, 서비스 이용약관, 개인정보 취급방침
 */
public class SettingWebViewActivity extends Activity {
    private LinearLayout llProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_webview_list);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        final WebView wbDisplay = (WebView) findViewById(R.id.wv_display);
        wbDisplay.setBackgroundColor(0);
        wbDisplay.getSettings().setJavaScriptEnabled(true);
        wbDisplay.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                    }
                }, 500);
            }
        });
        wbDisplay.setWebChromeClient(new WebChromeClient());

        Intent intent = new Intent(getIntent());
        String strType = intent.getStringExtra("DisplayMode");

        if (strType != null && !strType.equals("")) {
            if (strType.equals("Notice")) { //공지사항
                wbDisplay.loadUrl(getResources().getString(R.string.str_url_notice));
                ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_setting_notice));
            } else if (strType.equals("FAQ")) { //FAQ
                wbDisplay.loadUrl(getResources().getString(R.string.str_url_faq));
                ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_setting_faq));
            } else if (strType.equals("Service_Agreement")) { //서비스 이용약관
                wbDisplay.loadUrl(getResources().getString(R.string.str_url_terms1));
                ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_setting_service_agreement));
            } else if (strType.equals("PrivacyPolicy")) { //개인정보 취급방침
                wbDisplay.loadUrl(getResources().getString(R.string.str_url_terms2));
                ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_setting_privacy_policy));
            }
        }

        MainTitleBar mainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mainTitleBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wbDisplay.reload();
            }
        });
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
}
