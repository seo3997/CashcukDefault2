package com.cashcuk.advertiser.sendpush;

import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Administrator on 2017-01-12.
 */
public class ViewPageAdapter extends PagerAdapter {
    private LayoutInflater inflater;

    public ViewPageAdapter(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
