package com.cashcuk.main;

import android.app.Activity;
import android.content.SharedPreferences;
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
 * 이벤트 activity
 */
public class EventWebViewActivity extends Activity {
    private WebView wvEvent;
    private LinearLayout llProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_webview);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_event));

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        wvEvent = (WebView) findViewById(R.id.wv_event);

        wvEvent.getSettings().setJavaScriptEnabled(true);
        wvEvent.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wvEvent.getSettings().setSupportMultipleWindows(false);
        wvEvent.getSettings().setUseWideViewPort(true);
        wvEvent.getSettings().setLoadWithOverviewMode(true);
        wvEvent.getSettings().setBuiltInZoomControls(false);
        wvEvent.getSettings().setSupportZoom(false);
        wvEvent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (llProgress != null && llProgress.isShown())
                            llProgress.setVisibility(View.GONE);
                    }
                }, 500);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (llProgress != null && !llProgress.isShown())
                    llProgress.setVisibility(View.VISIBLE);
            }
        });
        wvEvent.setWebChromeClient(new WebChromeClient());

        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        String strEmail = prefs.getString("LogIn_ID", "");

        wvEvent.loadUrl(getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_link_event)+strEmail);

        MainTitleBar mainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = (ImageButton) mainTitleBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wvEvent.reload();
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

    @Override
    public void onBackPressed() {
        if(wvEvent.canGoBack()){
            wvEvent.goBack();
        }else {
            super.onBackPressed();
        }
    }
}
