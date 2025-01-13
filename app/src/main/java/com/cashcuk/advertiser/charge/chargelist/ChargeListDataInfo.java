package com.cashcuk.advertiser.charge.chargelist;

/**
 * 누적 or 사용 충전금 내역 data info
 */
public class ChargeListDataInfo {
    private String strMyCharge = ""; //충전금
    private String strDate = ""; //일자
    //    private String strContent=""; //누적 충전금 내용
    private String strText = ""; //누적 내용 or 사용처
    private String strCharge = ""; //누적 or 사용 포인트
    private String strInputName = ""; //입금자명
    private String strBank = ""; //은행명
    private String strRequestState = ""; //상태 (0: 충전대기, 1: 충전완료 or  (광고등록, push요청, 환불) - 0: 대기, 1: 완료)
    private String strAccount = ""; //계좌번호
    private String strAccountHolder = ""; //예금주
    private String strChargeCode = "";   //금액 마이너스 플러스 표시

    public String getStrMyCharge() {
        return strMyCharge;
    }

    public void setStrMyCharge(String strMyCharge) {
        this.strMyCharge = strMyCharge;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }

    public String getStrDate() {
        return strDate;
    }

    public String getStrCharge() {
        return strCharge;
    }

    public void setStrCharge(String strCharge) {
        this.strCharge = strCharge;
    }

    public String getStrText() {
        return strText;
    }

    public void setStrText(String strText) {
        this.strText = strText;
    }

    public String getStrInputName() {
        return strInputName;
    }

    public void setStrInputName(String strInputName) {
        this.strInputName = strInputName;
    }

    public String getStrBank() {
        return strBank;
    }

    public void setStrBank(String strBank) {
        this.strBank = strBank;
    }

    public String getStrRequestState() {
        return strRequestState;
    }

    public void setStrRequestState(String strRequestState) {
        this.strRequestState = strRequestState;
    }

    public String getStrAccount() {
        return strAccount;
    }

    public void setStrAccount(String strAccount) {
        this.strAccount = strAccount;
    }

    public String getStrAccountHolder() {
        return strAccountHolder;
    }

    public void setStrAccountHolder(String strAccountHolder) {
        this.strAccountHolder = strAccountHolder;
    }

    public String getStrChargeCode() {
        return strChargeCode;
    }

    public void setStrChargeCode(String strChargeCode) {
        this.strChargeCode = strChargeCode;
    }
}
