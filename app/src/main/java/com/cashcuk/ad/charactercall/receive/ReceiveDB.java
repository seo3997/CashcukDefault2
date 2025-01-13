package com.cashcuk.ad.charactercall.receive;

import android.provider.BaseColumns;

/**
 * 수신 측 DB
 */
public class ReceiveDB {
    public static final String HP_NUM = "hp_number";
    public static final String HP_EMAIL = "hp_email";
    public static final String CHAR_IMG_LOCAL_PATH = "local_path";

    public static final String CHAR_IMG_IDX = "char_idx";
//    public static final String CHAR_IMG_URL = "char_url";
    public static final String CHAR_SEND_MSG = "char_msg";
    public static final String CHAR_TITLE_TXT = "sys_char_msg";

    public static abstract class CreateDB implements BaseColumns {
        public static final String _TABLENAME = "table_receive";
        public static final String _CREATE =
                "create table " + _TABLENAME + "("
                        + _ID + " integer primary key autoincrement, "
                        + HP_NUM + " text not null , "
                        + HP_EMAIL + " text , "
                        + CHAR_IMG_LOCAL_PATH + " text , "
                        + CHAR_IMG_IDX + " text , "
                        + CHAR_SEND_MSG + " text , "
                        + CHAR_TITLE_TXT + " text);";
    }
}
