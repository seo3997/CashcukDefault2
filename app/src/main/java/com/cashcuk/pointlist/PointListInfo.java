package com.cashcuk.pointlist;

/**
 * Point List data info
 */
public class PointListInfo {
    private String strAccruePoint=""; //누적포인트
    private String strUsePoint=""; //사용포인트

    public String getStrAccruePoint() {
        return strAccruePoint;
    }

    public void setStrAccruePoint(String strAccruePoint) {
        this.strAccruePoint = strAccruePoint;
    }

    public String getStrUsePoint() {
        return strUsePoint;
    }

    public void setStrUsePoint(String strUsePoint) {
        this.strUsePoint = strUsePoint;
    }
}
