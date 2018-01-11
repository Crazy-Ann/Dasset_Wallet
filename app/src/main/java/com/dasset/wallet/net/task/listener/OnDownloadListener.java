package com.dasset.wallet.net.task.listener;

public interface OnDownloadListener {

    void onDownloadStart();

    void onDownloadProgress(float progress, long speed);

    void onDownloadFailed();

    void onDownloadSuccess();
}
