package com.cashcuk.character.updown;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

import java.util.ArrayList;

/**
 * 캐릭턱 구매 adapter
 */
public class CharacterAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ViewHolder vh;
    private ArrayList<CharacterUpDownInfo> arrDataInfo;
    private String strMode; //다운or공유

    public CharacterAdapter(Context context, ArrayList<CharacterUpDownInfo> arrInfo, String mode){
        mContext = context;
        arrDataInfo = arrInfo;
        strMode = mode;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrDataInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return arrDataInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v==null){
            vh = new ViewHolder();
            v = mInflater.inflate(R.layout.buy_character_item, null);
            vh.ivBuy = (ImageView) v.findViewById(R.id.iv_buy);
            vh.txtTitle = (TextView) v.findViewById(R.id.txt_title);
            vh.txtSaleName = (TextView) v.findViewById(R.id.txt_sale_name);
            vh.txtSaleID = (TextView) v.findViewById(R.id.txt_sale_id);
            vh.ivDownState = (ImageView) v.findViewById(R.id.iv_down_state);
            vh.rlPoint = (RelativeLayout) v.findViewById(R.id.rl_point);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        String strThumbNail = arrDataInfo.get(position).getStrCharThumbnail();

        Glide
                .with(mContext)
                .load(strThumbNail)
                .centerCrop()
                .placeholder(R.drawable.image_none)
                .into(vh.ivBuy);

        vh.txtTitle.setText(arrDataInfo.get(position).getStrCharName());
        vh.txtSaleName.setText(arrDataInfo.get(position).getStrCompayName());
        vh.txtSaleID.setText(arrDataInfo.get(position).getStrSellerId());

        if(strMode.equals(mContext.getResources().getString(R.string.str_char_download))) {
            vh.rlPoint.setVisibility(View.VISIBLE);
            if (arrDataInfo.get(position).getStrDownState() != null && arrDataInfo.get(position).getStrDownState().equals(String.valueOf(StaticDataInfo.TRUE))) {
                vh.ivDownState.setVisibility(View.VISIBLE);
            } else {
                vh.ivDownState.setVisibility(View.GONE);
            }
        }else{
            vh.rlPoint.setVisibility(View.GONE);
            vh.ivDownState.setVisibility(View.GONE);
        }

        return v;
    }

    public class ViewHolder{
        private ImageView ivBuy;
        private TextView txtTitle; //명칭
        private TextView txtSaleName; //판매자명
        private TextView txtSaleID; //판매자 ID
        private ImageView ivDownState; //다운 상태
        private RelativeLayout rlPoint; //다운 시 포인트
    }
}
