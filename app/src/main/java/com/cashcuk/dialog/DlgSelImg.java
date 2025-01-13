package com.cashcuk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.cashcuk.FileManager;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static androidx.core.content.FileProvider.getUriForFile;

/**
 * 사진촬영, 앨범 선택 할 수 있는 팝업
 */
public class DlgSelImg extends Dialog implements View.OnTouchListener {
    private Context mContext;
    private Button btnCancel;
    private ArrayList<String> arrString = new ArrayList<String>();
    private boolean isImg = false;
    private boolean isChar = false;
    public static Uri mImageUri;

    private String strFilePath=""; //폴더 경로
    private String strFileName=""; //파일 name
    private String imageFilePath;

    public static final int CROP_FROM_CAMERA = 222;
    public static final int PICK_FROM_GALLERY = 333;
    public static final int PICK_FROM_CAMERA = 444;
    public static final int CROP_FROM_GALLERY = 555;

    private OnDismissListener mDismiss;

    public static final String STR_SECERT_FOLDER_NAME = "CashcukTemp";
    public static final String STR_DIR = Environment.getExternalStorageDirectory()+"/."+STR_SECERT_FOLDER_NAME;

    public DlgSelImg(Context context) {
        super(context);
        mContext = context;
    }


    public DlgSelImg(Context context, boolean isImg, boolean isChar) {
        super(context);
        mContext = context;
        this.isImg = isImg;
        this.isChar = isChar;
    }

    public void DeleteDir(String path)
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        if(file.exists()) {
            if(childFileList.length>0) {
                for (File childFile : childFileList) {
                    if (childFile.isDirectory()) {
                        DeleteDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
                    } else {
                        childFile.delete();    //하위 파일삭제
                    }
                }
            }
            file.delete();    //root 삭제
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_list_title);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ((TextView) findViewById(R.id.txt_dlg_title)).setText(mContext.getResources().getString(R.string.str_img_title));
        ((LinearLayout) findViewById(R.id.ll1)).setOnTouchListener(this);
        btnCancel = (Button) findViewById(R.id.btn1);
        btnCancel.setOnTouchListener(this);
        ListView lvDlgMsg = (ListView) findViewById(R.id.lv_dlg);

        arrString.add(mContext.getResources().getString(R.string.str_camera));
        arrString.add(mContext.getResources().getString(R.string.str_album));
        if(isImg) arrString.add(mContext.getResources().getString(R.string.str_img_del));

        DlgListAdapter dlgAdapter = new DlgListAdapter(mContext, arrString);
        lvDlgMsg.setAdapter(dlgAdapter);
        lvDlgMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (arrString.get(position).equals(mContext.getResources().getString(R.string.str_camera))) {
                    // 카메라 호출
                    intent = new Intent("android.media.action.IMAGE_CAPTURE");

                    //임시 저장경로
                    String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    mImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ((Activity) mContext).startActivityForResult(intent, PICK_FROM_CAMERA);
                } else if (arrString.get(position).equals(mContext.getResources().getString(R.string.str_album))) {
                   try{
                       storeCropImage();

                       intent = new Intent(Intent.ACTION_PICK);
                       intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                       /*
                       intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                       intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                       mContext.grantUriPermission(mContext.getPackageName(), getPhotoUri() , Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        */
                       intent.putExtra(MediaStore.EXTRA_OUTPUT, FileManager.get(mContext).photoUri);

                       ((Activity)mContext).startActivityForResult(intent, PICK_FROM_GALLERY);

                    } catch (Exception e) {
                        // Toast.makeText(getApplicationContext(), R.string.imageException, Toast.LENGTH_LONG).show();
                        Log.d("temp", "ACTION_PICK["+e+"]");
                        Toast toast = Toast.makeText(mContext, "This device doesn't support the crop action!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }



            } else if (arrString.get(position).equals(mContext.getResources().getString(R.string.str_img_del))){
                    if(mDismiss!=null) mDismiss.onDismiss(DlgSelImg.this);
                }
                dismiss();
            }
        });
    }

    public boolean getDelImg(){
        return true;
    }

    public String getFilePath(){
        return strFilePath;
    }

    public String getFileName(){
        return strFileName;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public void setFilePathName(String filePath, String fileName){
        strFilePath = filePath;
        strFileName = fileName;
    }

    /**
     * crop 이미지 저장
     * @param isSecret 숨김 폴더 여부
     * @param folder 폴더 name
     * @return
     */
    public Uri storeCropImage(boolean isSecret, String folder) {

        // crop된 이미지를 저장하기 위한 파일 경로
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String fileName = timeStamp;
        //String fileName = "cash";
        Log.d("temp","fileName["+fileName+"]");

        try {
            //Log.d("temp","mContext.getFilesDir()["+mContext.getFilesDir()+"]");
            File imagePath = Environment.getExternalStorageDirectory();
            if(!imagePath.exists()){
                imagePath.mkdirs();
            }

            File newFile = new File(imagePath, fileName+".jpg");

            if(newFile!=null) {
                FileManager.get(mContext).photoUri = getUriForFile(mContext, "com.cashcuk.fileprovider", newFile);
               imageFilePath=newFile.getAbsolutePath();

                strFilePath=imagePath.getAbsolutePath(); //폴더 경로
                strFileName="/"+fileName; //파일 name

                Log.d("temp", "imagePath[" + newFile.getAbsolutePath() + "]");
                Log.d("temp", "photoUri[" + FileManager.get(mContext).photoUri.toString() + "]");
                Log.d("temp", "newFile.getAbsolutePath()[" + newFile.getAbsolutePath() + "]");
            }


        } catch (Exception e) {
            Log.d("temp","error["+e+"]");
            e.printStackTrace();
        }
        Log.d("temp","photoUri["+FileManager.get(mContext).photoUri.toString()+"]");

        return FileManager.get(mContext).photoUri;
    }
    /**
     * crop 이미지 저장
     * @return
     */
    public void storeCropImage() {

        // crop된 이미지를 저장하기 위한 파일 경로
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String fileName = timeStamp;
        //String fileName = "cash";
        Log.d("temp","fileName["+fileName+"]");

        try {
            //Log.d("temp","mContext.getFilesDir()["+mContext.getFilesDir()+"]");
            File imagePath = Environment.getExternalStorageDirectory();
            if(!imagePath.exists()){
                imagePath.mkdirs();
            }

            File newFile = new File(imagePath, fileName+".jpg");

            if(newFile!=null) {
                FileManager.get(mContext).photoUri = getUriForFile(mContext, "com.cashcuk.fileprovider", newFile);
                Log.d("temp", "imagePath[" + imagePath.getAbsolutePath() + "]");
                Log.d("temp", "photoUri[" + FileManager.get(mContext).photoUri.toString() + "]");
                Log.d("temp", "newFile.getAbsolutePath()[" + newFile.getAbsolutePath() + "]");
            }


        } catch (Exception e) {
            Log.d("temp","error["+e+"]");
            e.printStackTrace();
        }
        Log.d("temp","photoUri["+FileManager.get(mContext).photoUri.toString()+"]");

    }

    /**
     * 갤러리를 갱신
     * kitkat부터는 아래와 같이 써야함.
     * @param photoPath
     */
    public void galleryAddPic(String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll1 || v.getId() == R.id.btn1){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCancel.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                btnCancel.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.dlg_chk));
                dismiss();
            }
            return true;
        }

        return false;
    }

    public void setonDismissListener(OnDismissListener listener){
        mDismiss = listener;
    }
}
