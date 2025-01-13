package com.cashcuk.ad.charactercall.receive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * 수신측 DB Open Helper.
 */
public class ReceiveDBopenHelper {
    private static final String DATABASE_NAME_RECEIVE = "ReceiveChar.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mContext;

    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(ReceiveDB.CreateDB._CREATE);
        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ReceiveDB.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public ReceiveDBopenHelper(Context context){
        this.mContext = context;
    }

    public ReceiveDBopenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mContext, DATABASE_NAME_RECEIVE, null, DATABASE_VERSION);
//        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        mDB.close();
    }

    //저장
    public void insert(String inComingNum, String inComingId, String localPath, String imgIdx, String sendMsg, String title){
        mDB = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ReceiveDB.HP_NUM, inComingNum);
        values.put(ReceiveDB.HP_EMAIL, inComingId);
        values.put(ReceiveDB.CHAR_IMG_LOCAL_PATH, localPath);
        values.put(ReceiveDB.CHAR_IMG_IDX, imgIdx);
        values.put(ReceiveDB.CHAR_SEND_MSG, sendMsg);
        values.put(ReceiveDB.CHAR_TITLE_TXT, title);

        mDB.insert(ReceiveDB.CreateDB._TABLENAME, null, values);
    }

    //수정
    public void update(String id, String inCommingNum, String localPath, String imgIdx, String sendMsg, String title) {
        mDB = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ReceiveDB.HP_NUM, inCommingNum);
        values.put(ReceiveDB.CHAR_IMG_LOCAL_PATH, localPath);
        values.put(ReceiveDB.CHAR_IMG_IDX, imgIdx);
        values.put(ReceiveDB.CHAR_SEND_MSG, sendMsg);
        values.put(ReceiveDB.CHAR_TITLE_TXT, title);
        mDB.update(ReceiveDB.CreateDB._TABLENAME, values, ReceiveDB.CreateDB._ID + "=?", new String[]{id});
    }//end update

    public void deleteAll() {
        mDB = mDBHelper.getWritableDatabase();
        String sql = "delete from "+ReceiveDB.CreateDB._TABLENAME;
        mDB.execSQL(sql);
    }

    public Cursor Search(String strWhere){
        mDB = mDBHelper.getReadableDatabase();
//        String sql = "select * from " + BellADDB.CreateDB._TABLENAME + " where " + strWhere + " order by "+ BellADDB.CreateDB._ID+" asc"+" LIMIT 2";
        String sql = "select * from " + ReceiveDB.CreateDB._TABLENAME + " where " + strWhere;
        Cursor cursor = mDB.rawQuery(sql, null);

        return cursor;
    }
}
