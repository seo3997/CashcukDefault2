package com.cashcuk.membership.txtlistdata;

/**
 * 광고 PUSH Target Data Info
 */
public class TxtPushTargetInfo {
    private String strAmnt = ""; //충전금
    private String strTargetNum = ""; // 대상자 수
    private String strUnitAmnt = ""; //발송단가

    public String getStrAmnt() {
        return strAmnt;
    }

    public void setStrAmnt(String amnt) {
        this.strAmnt = amnt;
    }

    public String getStrTargetNum() {
        return strTargetNum;
    }

    public void setStrTargetNum(String strTargetNum) {
        this.strTargetNum = strTargetNum;
    }

    public String getStrUnitAmnt() {
        return strUnitAmnt;
    }

    public void setStrUnitAmnt(String strUnitAmnt) {
        this.strUnitAmnt = strUnitAmnt;
    }
}
