package com.cashcuk.character.setreceive;

import android.app.Activity;
import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cashcuk.R;

import java.util.ArrayList;

/**
 * 수신 대상 설정 adapter
 */
public class ContactsAdapter extends ArrayAdapter<ContactsInfo> {
    private int resId;
    private ArrayList<ContactsInfo> arrContacts;
    private LayoutInflater Inflater;
    private Context mContext;
    private ViewHolder vh;

    private SparseArray<View> views = new SparseArray<View>();

    public ContactsAdapter(Context context, int resource, ArrayList<ContactsInfo> objects) {
        super(context, resource, objects);
        mContext = context;
        resId = resource;
        arrContacts = (ArrayList<ContactsInfo>) objects;
        Inflater = (LayoutInflater) ((Activity) context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static final int CHK_NOTIFY = 123;
    public void itemChk(int index){
        ViewGroup ro = (ViewGroup) views.get(index);
        CheckBox chkSet = (CheckBox) ro.findViewById(R.id.chk_set_receive);
        chkSet.setChecked(!chkSet.isChecked());

        arrContacts.get(index).setIsChk(chkSet.isChecked());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v==null){
            vh = new ViewHolder();
            v = Inflater.inflate(R.layout.character_set_receive_item, null);
            vh.txtName = (TextView) v.findViewById(R.id.txt_name);
            vh.txtPhoneNum = (TextView) v.findViewById(R.id.txt_phone_num);
            vh.chkSetReceive = (CheckBox) v.findViewById(R.id.chk_set_receive);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        if(arrContacts.get(position) != null){
            vh.txtName.setText(arrContacts.get(position).getStrName());
            vh.txtPhoneNum.setText(PhoneNumberUtils.formatNumber(arrContacts.get(position).getStrPhoneNum()));
        }

        if(arrContacts.get(position).getIsChk()){
            vh.chkSetReceive.setChecked(true);
        }else{
            vh.chkSetReceive.setChecked(false);
        }

        views.put(position, v);
        return v;
    }
    class ViewHolder{
        public TextView txtName;
        public TextView txtPhoneNum;

        public CheckBox chkSetReceive;
    }
}
