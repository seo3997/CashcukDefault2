package com.cashcuk.setting;

import com.cashcuk.membership.txtlistdata.TxtListDataInfo;

import java.util.ArrayList;

/**
 * 계정설정 data
 */
public class AccountSetInfo {
    private String strHp="";
    private String strSex="";
    private String strBirth="";
    private String strSiDo="";
    private String strSiGunGu="";
    private String strRecommender="";

    private String strSendInfoTitle;
    private String strSendInfoSub;

    public ArrayList<TxtListDataInfo> getStrSendInfoTitle() {
        ArrayList<TxtListDataInfo> arrSendInfo= new ArrayList<TxtListDataInfo>();
        String[] strItemTmp = strSendInfoTitle.trim().split(":");
        for(int i=0; i<strItemTmp.length; i++){
            TxtListDataInfo infoTmp = new TxtListDataInfo();
            String[] strTmp = new String[2];
            strTmp = strItemTmp[i].split(">");
            infoTmp.setStrIdx(strTmp[0]);
            infoTmp.setStrMsg(strTmp[1]);
            arrSendInfo.add(infoTmp);
        }
        return arrSendInfo;
    }

    public void setStrSendInfoTitle(String strSendInfoTitle) {
        this.strSendInfoTitle = strSendInfoTitle;
    }

    public ArrayList<TxtListDataInfo> getStrSendInfoSub() {
        ArrayList<TxtListDataInfo> arrSendInfo= new ArrayList<TxtListDataInfo>();
        String[] strItemTmp = strSendInfoSub.trim().split(":");

        for(int i=0; i<strItemTmp.length; i++){
            TxtListDataInfo infoTmp = new TxtListDataInfo();
            String[] strTmp = new String[2];
            strTmp = strItemTmp[i].split(">");
            infoTmp.setStrIdx(strTmp[0]);
            infoTmp.setStrMsg(strTmp[1]);
            arrSendInfo.add(infoTmp);
        }
        return arrSendInfo;
    }

    public void setStrSendInfoSub(String strSendInfoSub) {
        this.strSendInfoSub = strSendInfoSub;
    }

    public String getStrSiGunGu() {
        return strSiGunGu;
    }

    public void setStrSiGunGu(String strSiGunGu) {
        this.strSiGunGu = strSiGunGu;
    }

    public String getStrSiDo() {
        return strSiDo;
    }

    public void setStrSiDo(String strSiDo) {
        this.strSiDo = strSiDo;
    }

    public String getStrHp() {
        return strHp;
    }

    public void setStrHp(String strHp) {
        this.strHp = strHp;
    }

    public String getStrSex() {
        return strSex;
    }

    public void setStrSex(String strSex) {
        this.strSex = strSex;
    }

    public String getStrBirth() {
        return strBirth;
    }

    public void setStrBirth(String strBirth) {
        this.strBirth = strBirth;
    }

    public String getStrRecommender() {
        return strRecommender;
    }

    public void setStrRecommender(String strRecommender) {
        this.strRecommender = strRecommender;
    }
}
