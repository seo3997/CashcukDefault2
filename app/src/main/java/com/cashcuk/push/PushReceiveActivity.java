package com.cashcuk.push;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.ad.detailview.ADDetailViewActivity;
import com.cashcuk.common.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * PUSH 발송에 의한 GCM 메시지 수신 시 보이는 Popup
 */

public class PushReceiveActivity extends Activity {
    private String urlStr;
    private ImageView ivPushImg;
    private Bitmap bitmap;
    private String strADIdx="";
    private String strPushIdx="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ad_push_popup_activity);
        CheckLoginService.mActivityList.add(this);

        // 화면이 잠겨있을 때 보여주기
        // 키잠금 해제하기
        // 화면 켜기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();
    }

    private void init() {
        Intent intent = getIntent();
        urlStr = intent.getStringExtra("MSG");
        ivPushImg = (ImageView) findViewById(R.id.iv_push_img);
        String strTitle = intent.getStringExtra("TITLE");
        ((TextView) findViewById(R.id.txt_title)).setText(strTitle);
        strADIdx = intent.getStringExtra("AD");
        strPushIdx = intent.getStringExtra("PUSH");
        String strADPoint = intent.getStringExtra("POINT");

        TextView txtADView = (TextView) findViewById(R.id.txt_ad_view);
        txtADView.setText(String.format(getResources().getString(R.string.str_push_ad_point), strADPoint));
        txtADView.setOnClickListener(mClick);

        ((ImageButton) findViewById(R.id.ib_close)).setOnClickListener(mClick);
        ((TextView) findViewById(R.id.txt_move_push_list)).setOnClickListener(mClick);

        final ProgressBar pbPushImg = (ProgressBar) findViewById(R.id.pb_push_img);
        if (pbPushImg != null && !pbPushImg.isShown()) pbPushImg.setVisibility(View.VISIBLE);

        ImageLoader.loadImage(this, urlStr, ivPushImg, pbPushImg);

    }

    Thread mThread = new Thread() {
        @Override
        public void run() {
            try {
                URL url = new URL(urlStr);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {

                    }
                });
            } catch (IOException ex) {

            }
        }
    };

    private View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            int viewId = v.getId();
            if (viewId == R.id.ib_close) {
                finish();
                return;
            } else if (viewId == R.id.txt_ad_view) {
                intent = new Intent(PushReceiveActivity.this, ADDetailViewActivity.class);
                if(strADIdx!=null && !strADIdx.equals("")) {
                    intent.putExtra("AD_IDX", strADIdx);
                    intent.putExtra("AD_KIND", "U");
                    intent.putExtra("PUSH_MODE", true);
                    intent.putExtra("PUSH_IDX", strPushIdx);
                }
            } else if (viewId == R.id.txt_move_push_list) {
                intent = new Intent(PushReceiveActivity.this, PushStorageActivity.class);
                intent.putExtra("PUSH_MODE", true);
            }

            if(intent!=null){
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                // notification 매니저 생성
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                // 등록된 notification 을 제거 한다.
                nm.cancel(0);

                finish();
            }
        }
    };
}
