package com.cashcuk.setting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.TitleBar;

/**
 * 고객센터
 */
public class CustomerServiceActivity extends Activity implements View.OnTouchListener {
    private TextView txtInquiryEmail;
    private TextView txtInquiryTel;
    private ImageButton ibInquiryEmail;
    private ImageButton ibInquiryTel;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service);
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        ((TitleBar)findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_setting_customer_service));
        ((TextView) findViewById(R.id.txt_customer_service_time)).setText(Html.fromHtml(getResources().getString(R.string.str_customer_service_info2)));

        txtInquiryEmail = (TextView) findViewById(R.id.txt_inquiry_email);
        txtInquiryTel = (TextView) findViewById(R.id.txt_inquiry_tel);
        txtInquiryEmail.setOnTouchListener(this);
        txtInquiryTel.setOnTouchListener(this);

        ibInquiryEmail = (ImageButton) findViewById(R.id.ib_inquiry_email);
        ibInquiryTel = (ImageButton) findViewById(R.id.ib_inquiry_tel);
        ibInquiryEmail.setOnTouchListener(this);
        ibInquiryTel.setOnTouchListener(this);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Intent intent = null;
        if(v.getId() == R.id.txt_inquiry_email || v.getId() == R.id.ib_inquiry_email){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibInquiryEmail.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibInquiryEmail.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + txtInquiryEmail.getText().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        }else if(v.getId() == R.id.txt_inquiry_tel || v.getId() == R.id.ib_inquiry_tel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) ibInquiryTel.setImageDrawable(getResources().getDrawable(R.drawable.next_bt_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                ibInquiryTel.setImageDrawable(getResources().getDrawable(R.drawable.next_bt));
                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + txtInquiryTel.getText().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        }
        return false;
    }
}
