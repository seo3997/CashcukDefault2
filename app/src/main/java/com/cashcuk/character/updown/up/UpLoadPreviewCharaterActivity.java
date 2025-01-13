package com.cashcuk.character.updown.up;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.TitleBar;
import com.cashcuk.character.CharacterInfo;
import com.cashcuk.common.ImageLoader;

/**
 * 캐릭터 공유 미리보기
 */
public class UpLoadPreviewCharaterActivity extends Activity implements View.OnClickListener {
    private LinearLayout llProgress;
    private WindowManager windowManager;
    private WebView wvADImg;

    private CharacterInfo mCharInfo=null;
    private final int REQUEST_UPLOAD_OK = 321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_charater);
        CheckLoginService.mActivityList.add(this);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_preview));

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int height = (int) (display.getHeight() * 0.53); //Display 사이즈의 53%

        View vPreview = (View) findViewById(R.id.view_preview);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        vPreview.setLayoutParams(params);

        ((Button) findViewById(R.id.btn_preview_cancel)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_preview_ok)).setText(getResources().getString(R.string.str_ok));
        ((Button) findViewById(R.id.btn_preview_ok)).setOnClickListener(this);

        Intent intent = getIntent();
        if(intent!=null){
            mCharInfo = (CharacterInfo) intent.getSerializableExtra("CharInfo");
        }

        ((ImageButton)vPreview.findViewById(R.id.ib_close)).setClickable(false);

        if(mCharInfo!=null) {
            if(mCharInfo.getStrTxt()!=null && !mCharInfo.getStrTxt().equals("")) ((TextView) vPreview.findViewById(R.id.txt_send_msg)).setText(mCharInfo.getStrTxt());

            String strImgUrl = mCharInfo.getStrImgUrl().replace("\\", "//");
            ImageView ivADImg = (ImageView) vPreview.findViewById(R.id.iv_ad_img);

            final ProgressBar pbADImg = (ProgressBar) vPreview.findViewById(R.id.pb_ad_img);
            if (pbADImg != null && !pbADImg.isShown()) pbADImg.setVisibility(View.VISIBLE);
            ImageLoader.loadImage(this, strImgUrl, ivADImg, pbADImg);

        }
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
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_preview_cancel) {
            finish();
        } else if (viewId == R.id.btn_preview_ok) {
            Intent intent = new Intent(UpLoadPreviewCharaterActivity.this, UpLoadCharacterInputInfoActivity.class);
            intent.putExtra("CharInfo", mCharInfo);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_UPLOAD_OK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_UPLOAD_OK){
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }
}
