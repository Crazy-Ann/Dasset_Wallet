package com.dasset.wallet.net.task;

import android.os.AsyncTask;

import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.http.CustomHttpClient;
import com.dasset.wallet.components.utils.ApplicationUtil;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.net.task.listener.OnDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Long, Boolean> {

//    private NotificationManager notificationManager;
//    private NotificationCompat.Builder builder;

    private OnDownloadListener onDownloadListener;
    private String url;
    private File file;
    private long time;

    public DownloadTask(String url, File file, OnDownloadListener onDownloadListener) {
        this.url = url;
        this.file = file;
        this.onDownloadListener = onDownloadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        time = System.currentTimeMillis();
        if (onDownloadListener != null) {
            onDownloadListener.onDownloadStart();
        }
//        notificationManager = (NotificationManager) BaseApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
//        builder = new NotificationCompat.Builder(BaseApplication.getInstance());
//        builder.setSmallIcon(R.mipmap.icon_logo);
//        builder.setContentTitle("版本更新");
//        builder.setContentText("准备下载...");
//        builder.setContentIntent(PendingIntent.getActivity(BaseApplication.getInstance(), 0, new Intent(), 0));
//        notificationManager.notify(Constant.NOTIFICATION_ID, builder.build());
    }

    @Override
    protected Boolean doInBackground(String... params) {
        while (true) {
            if (isCancelled()) {
                break;
            }
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                Response response = CustomHttpClient.getInstance().getHttpClientBuilder().build().newCall(new Request.Builder().url(url).build()).execute();
                long total = response.body().contentLength();
                byte[] buffer = new byte[2048];
                int length;
                inputStream = response.body().byteStream();
                long sum = 0;
                IOUtil.getInstance().forceMkdir(file.getParentFile());
                fileOutputStream = new FileOutputStream(file);
                while ((length = inputStream.read(buffer)) != -1) {
                    sum += length;
                    fileOutputStream.write(buffer, 0, length);
                    if (onDownloadListener != null) {
                        publishProgress(sum, total);
                    }
                }
                fileOutputStream.flush();
                if (total == file.length()) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        LogUtil.getInstance().print("onProgressUpdate");
        if (onDownloadListener != null && values != null && values.length >= 2) {
            long data = values[0];
            long dataLength = values[1];
            long totalTime = (System.currentTimeMillis() - time) / 1000;
            if (totalTime == 0) {
                totalTime += 1;
            }
            int progress = (int) (data * 100.0f / dataLength);
            onDownloadListener.onDownloadProgress(progress, data / totalTime);
//            builder.setContentText("当前进度：" + progress + Regex.PERCENT.getRegext());
//            notificationManager.notify(Constant.NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        LogUtil.getInstance().print("onPostExecute");
        if (result) {
            ApplicationUtil.getInstance().chmod(Regex.PERMISSION.getRegext(), file.getAbsolutePath());
//            builder.setContentIntent(PendingIntent.getActivity(BaseApplication.getInstance()
//                    , 0
//                    , new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(Regex.FILE_URI.getRegext() + file.getAbsolutePath()), Regex.FILE_TYPE.getRegext())
//                    , PendingIntent.FLAG_UPDATE_CURRENT));
//            builder.setContentText("下载完成,点击安装...");
//            builder.setDefaults(Notification.DEFAULT_ALL);
//            notificationManager.notify(Constant.NOTIFICATION_ID, builder.build());
            if (onDownloadListener != null) {
                onDownloadListener.onDownloadSuccess();
            }
        } else {
//            builder.setContentText("下载失败,稍后请重试...");
//            notificationManager.notify(Constant.NOTIFICATION_ID, builder.build());
            if (onDownloadListener != null) {
                onDownloadListener.onDownloadFailed();
            }
        }
    }
}
