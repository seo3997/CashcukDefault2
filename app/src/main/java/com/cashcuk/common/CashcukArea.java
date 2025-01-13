package com.cashcuk.common;

/**
 * Created by jun on 2017-08-29.
 */

public class CashcukArea {

    private  String mSiDo;
    private  String mGuDong;
    private  String mAdress;

    public  CashcukArea(String pPullAddress){
        this.mAdress=pPullAddress;
        SetMappingArea();
    }

    public void SetMappingArea(){

        String area1="";
        String area2="";
        String area3="";
        String[] sAddrArr=null;

        if(mAdress!="") {
            sAddrArr = mAdress.split("\\s");
        }

        int i=0;
        if(sAddrArr!=null) {
            for (String wo : sAddrArr) {
                if (i == 0) area1 = wo;
                else if (i == 1) area2 = wo;
                else  if (i == 2) area3 = wo;
                i++;
            }
        }

        this.mSiDo="";
        for(KoreaCity kcity : KoreaCity.values()){
            if(area1.equals(kcity.getCityName()))   {
                this.mSiDo=CommonUtil.getAreaGu(kcity);
                break;
            }
        }

        this.mGuDong=area2+" "+area3;                                                               // 시구를 먼저 체크한다 고양시 덕양구 or 고양시 가 존재함
        int iGudongcd=0;
        for(KoreaAddrSi kgudong : KoreaAddrSi.values()){
            if(this.mGuDong.equals(kgudong.getGuDongName()))   {
                iGudongcd=1;
                break;
            }
        }

        if(iGudongcd==0) {
            this.mGuDong = area2;
            for (KoreaGuDong kgudong : KoreaGuDong.values()) {                                      //예외적인게 존재 한다면 달성군=>달서구
                if (area2.equals(kgudong.getGuDongName())) {
                    this.mGuDong = CommonUtil.getGudong(kgudong);
                    break;
                }
            }
        }

    }


    public String getSiDo() {
        return mSiDo;
    }

    public void setSiDo(String SiDo) {
        this.mSiDo = SiDo;
    }

    public String getGuDong() {
        return mGuDong;
    }

    public void setGuDong(String GuDong) {
        this.mGuDong = mGuDong;
    }

    public String getAddress() {
        return mAdress;
    }

    public void setAddress(String Address) {
        this.mAdress = Address;
    }
}
