package com.cashcuk.common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommonDataTask {

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

    public CommonDataTask(String url, Map<String, String> params, DataTaskCallback callback) {
        this.url = url;
        this.params = params;
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
                    Log.e("CommonDataTask", "Error in doInBackground", e);
                }
            }
        });
    }

    private String doInBackground() throws Exception {
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = formBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        } catch (IOException e) {
            throw e;
        }
    }
}