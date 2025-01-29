package com.cashcuk.common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;

public class MultipartDataTask {

    public interface DataTaskCallback {
        void onPreExecute();

        void onPostExecute(String result);

        void onError(Exception e);
    }

    private final String url;
    private final Map<String, String> params;
    private final DataTaskCallback callback;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<File> contentDetail;
    private final File contentTitle;
    private final File contentThumbnail;
    private final Boolean isChangeTitleIlmg;
    private final String strTitleImgPath;
    private final String STR_AD_TITLE_IMG = "ad_titleimg";
    private final String STR_AD_THUMBNAIL = "ad_thumbnail";
    private final String STR_AD_DETAIL_IMG = "ad_dtlimg";

    public MultipartDataTask(String url,
                             Map<String, String> params,
                             File contentTitle,
                             File contentThumbnail,
                             Boolean isChangeTitleIlmg,
                             String strTitleImgPath,
                             ArrayList<File> contentDetail,
                             DataTaskCallback callback
                             ) {
        this.url = url;
        this.params = params;
        this.contentTitle = contentTitle;
        this.contentThumbnail = contentThumbnail;
        this.strTitleImgPath = strTitleImgPath;
        this.isChangeTitleIlmg = isChangeTitleIlmg;
        this.contentDetail = contentDetail;
        this.callback = callback;
    }

    public void execute() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onPreExecute();
                }
            }
        });

        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return doInBackground();
            }
        });

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = future.get();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onPostExecute(result);
                            }
                        }
                    });
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onError(e);
                            }
                        }
                    });
                    Log.e("MultipartDataTask", "Error in doInBackground", e);
                }
            }
        });
    }

    private String doInBackground() throws Exception {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("OkHttp", message); // OkHttp 태그로 로그 출력
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 로그 레벨 설정 (BODY로 설정하면 요청/응답 본문까지 출력)
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);


        String sValue = "";
        // Map에서 데이터를 가져와서 MultipartBody에 추가
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sValue = "";
                if (entry.getValue() != null)  sValue = entry.getValue();
                builder.addFormDataPart(entry.getKey(), sValue);
            }
        }

        // 이미지 관련 File 추가
        MediaType mediaType = MediaType.parse("image/*");
        if (isChangeTitleIlmg) {
            if (contentTitle != null) {
                builder.addFormDataPart(STR_AD_TITLE_IMG, contentTitle.getName(),
                        RequestBody.Companion.create(fileToByteArray(contentTitle), mediaType));
            }
            if (contentThumbnail != null) {
                builder.addFormDataPart(STR_AD_THUMBNAIL, contentThumbnail.getName(),
                        RequestBody.Companion.create(fileToByteArray(contentThumbnail), mediaType));
            }
        } else {
            if (strTitleImgPath != null) {
                builder.addFormDataPart(STR_AD_TITLE_IMG, strTitleImgPath);
            }
        }
        if (contentDetail != null) {
            for (int i = 0; i < contentDetail.size(); i++) {
                File detailFile = contentDetail.get(i);
                String partName = STR_AD_DETAIL_IMG + (i + 1);
                if(i == 0) partName = "ad_dtlimg1";
                else if(i == 1) partName = "ad_dtlimg2";
                else if(i == 2) partName = "ad_dtlimg3";
                builder.addFormDataPart(partName, detailFile.getName(),
                        RequestBody.Companion.create(fileToByteArray(detailFile), mediaType));
            }
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.string();
            } else {
                return "";
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private byte[] fileToByteArray(File file) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        fileInputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}