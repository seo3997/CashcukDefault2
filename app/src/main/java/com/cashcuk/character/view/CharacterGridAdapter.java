package com.cashcuk.character.view;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cashcuk.R;
import com.cashcuk.character.CharacterInfo;

import java.util.ArrayList;

/**
 * 나의캐릭터 (카테고리별) adapter
 */
public class CharacterGridAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<CharacterInfo> arrChar;
    private LayoutInflater inflater;
    private ViewHolder vh;
//    private Handler mHandler;

    public static final int CHK_NOTIFY = 123;
//    private ArrayList<CharacterInfo> arrChkChar = new ArrayList<CharacterInfo>();

    public CharacterGridAdapter(Context context, ArrayList<CharacterInfo> arrChar){
        mContext = context;
        this.arrChar = arrChar;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrChar.size();
    }

    @Override
    public Object getItem(int position) {
        return arrChar.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private boolean isDel = false;
    private int selectedIndex = -1;
    /**
     * 삭제 모드 체크
     * @param isDel
     */
    public void modeState(boolean isDel){
        this.isDel = isDel;
    }


    private SparseArray<View> views = new SparseArray<View>();
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        String strImgPath = arrChar.get(position).getStrMiddleImgUrl().replace("\\", "//");

        if (v == null) {
            vh = new ViewHolder();
            v = inflater.inflate(R.layout.character_gv_item, null);
            vh.ivChar = (ImageView) v.findViewById(R.id.iv_character);
            vh.chkDel = (CheckBox) v.findViewById(R.id.chk_del_char);

            v.setTag(vh);
        } else {
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }
        Glide
                .with(mContext)
                .load(strImgPath)
                .centerCrop()
                .placeholder(R.drawable.image_none)
                .into(vh.ivChar);

        vh.chkDel.setFocusable(false);
        vh.chkDel.setFocusableInTouchMode(false);
        vh.chkDel.setClickable(false);
        vh.chkDel.setChecked(false);
        if(isDel){
            vh.chkDel.setVisibility(View.VISIBLE);
        }else{
            vh.chkDel.setVisibility(View.GONE);
        }

        views.put(position, v);
        return v;
    }

    public class ViewHolder{
        private ImageView ivChar;
        private CheckBox chkDel;
    }
}
