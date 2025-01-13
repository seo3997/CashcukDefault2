package com.cashcuk.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;

import java.util.ArrayList;

/**
 * 광고 가이드, 캐릭터 가이드
 */
public class GuideActivity extends Activity {
    /** ViewFilpper 안에서 터치된 X축의 좌표 */
    private int m_nPreTouchPosX = 0;

    private LinearLayout llPageImg;
    private ImageView ivPage;
    private  ArrayList<Integer> mImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        CheckLoginService.mActivityList.add(this);

        ViewPager vpGuideImg = (ViewPager) findViewById(R.id.guide_pager);

        Intent intent = getIntent();
        mImg = new ArrayList<Integer>();
        mImg = intent.getIntegerArrayListExtra("GuideAD");

        GuideAdapter mAdapter = new GuideAdapter(getLayoutInflater(), mImg);
        vpGuideImg.setAdapter(mAdapter);
        vpGuideImg.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i=0; i<mImg.size(); i++){
                    if(i==position) {
                        ((ImageView)findViewById(position)).setImageDrawable(getResources().getDrawable(R.drawable.one_press));
                    }else{
                        ((ImageView)findViewById(i)).setImageDrawable(getResources().getDrawable(R.drawable.one));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        llPageImg = (LinearLayout) findViewById(R.id.ll_guide_page_img);
        float scale = getResources().getDisplayMetrics().density;
        final int padding = (int) (5*scale);

        for(int i=0; i<mImg.size(); i++){
            ivPage = new ImageView(this);
            if(i==0){
                ivPage.setImageDrawable(getResources().getDrawable(R.drawable.one_press));
            }else {
                ivPage.setImageDrawable(getResources().getDrawable(R.drawable.one));
            }
            ivPage.setId(i);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = padding;

            llPageImg.addView(ivPage, params);
        }

    }

    public class GuideAdapter extends PagerAdapter {
        private LayoutInflater inflater;
        private ArrayList<Integer> mImgId;

        public GuideAdapter(LayoutInflater inflater, ArrayList<Integer> imgId) {
            //전달 받은 LayoutInflater를 멤버변수로 전달
            this.inflater=inflater;
            mImgId = imgId;
        }

        @Override
        public int getCount() {
            return mImgId.size();
        }

        //instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        //ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
        //쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
        //첫번째 파라미터 : ViewPager
        //두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view=null;

            //새로운 View 객체를 Layoutinflater를 이용해서 생성
            //만들어질 View의 설계는 res폴더>>layout폴더>>viewpater_childview.xml 레이아웃 파일 사용
            view= inflater.inflate(R.layout.tutorial_child, null);

            //만들어진 View안에 있는 ImageView 객체 참조
            //위에서 inflated 되어 만들어진 view로부터 findViewById()를 해야 하는 것에 주의.
            ImageView img= (ImageView)view.findViewById(R.id.img_viewpager_childimage);

            Button btnClose= (Button)view.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            //ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
            //현재 position에 해당하는 이미지를 setting
            img.setBackgroundDrawable(getResources().getDrawable(mImgId.get(position)));

            //ViewPager에 만들어 낸 View 추가
            container.addView(view);

            //Image가 세팅된 View를 리턴
            return view;
        }

        //화면에 보이지 않은 View는파쾨를 해서 메모리를 관리함.
        //첫번째 파라미터 : ViewPager
        //두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
        //세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //ViewPager에서 보이지 않는 View는 제거
            //세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
            container.removeView((View)object);
        }
    }
}
