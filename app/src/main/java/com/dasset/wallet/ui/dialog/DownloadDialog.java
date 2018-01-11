package com.dasset.wallet.ui.dialog;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import com.dasset.wallet.R;
import com.dasset.wallet.base.constant.Temp;
import com.dasset.wallet.base.dialog.BaseDialogFragment;
import com.dasset.wallet.base.dialog.listener.OnDialogNegativeListener;
import com.dasset.wallet.base.dialog.listener.OnDialogPositiveListener;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.widget.progressbar.DownloadProgressBar;
import com.dasset.wallet.components.widget.progressbar.listener.OnProgressUpdateListener;
import com.dasset.wallet.net.task.DownloadTask;
import com.dasset.wallet.net.task.listener.OnDialogInstallListner;
import com.dasset.wallet.net.task.listener.OnDownloadListener;
import com.dasset.wallet.ui.dialog.builder.DownloadDialogBuilder;

import java.io.File;

public class DownloadDialog extends BaseDialogFragment implements OnDownloadListener, OnProgressUpdateListener, View.OnClickListener {

    private String url;
    private File file;
    private CharSequence positive;
    private CharSequence negative;

    private DownloadTask downloadTask;
    private DownloadProgressBar dpbProgress;

    private boolean isSuccess;
    private OnDialogInstallListner onDialogInstallListner;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDialogInstallListner) {
            onDialogInstallListner = (OnDialogInstallListner) context;
        }
    }

    @Override
    protected Builder build(Builder builder) {
        CharSequence title = BundleUtil.getInstance().getCharSequenceData(getArguments(), Temp.DIALOG_TITLE.getContent());
        CharSequence prompt = BundleUtil.getInstance().getStringData(getArguments(), Temp.DIALOG_PROMPT.getContent());
        url = BundleUtil.getInstance().getStringData(getArguments(), Temp.DIALOG_DOWNLOAD_URL.getContent());
        file = BundleUtil.getInstance().getSerializableData(getArguments(), Temp.DIALOG_DOWNLOAD_FILE.getContent());
        positive = BundleUtil.getInstance().getCharSequenceData(getArguments(), Temp.DIALOG_BUTTON_POSITIVE.getContent());
        negative = BundleUtil.getInstance().getCharSequenceData(getArguments(), Temp.DIALOG_BUTTON_NEGATIVE.getContent());
        View view = builder.getLayoutInflater().inflate(R.layout.dialog_download, null);
        dpbProgress = ViewUtil.getInstance().findView(view, R.id.dpbProgress);
        dpbProgress.setOnClickListener(this);
        builder.setView(view);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(prompt)) {
            builder.setMessage(prompt);
        }
        if (!TextUtils.isEmpty(url) && file != null) {
            downloadTask = new DownloadTask(url, file, this);
            downloadTask.execute();
        } else {
            onDownloadFailed();
        }
        if (!TextUtils.isEmpty(positive)) {
            builder.setPositiveButton(positive, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (OnDialogPositiveListener listener : getDialogListeners(OnDialogPositiveListener.class)) {
                        listener.onPositiveButtonClicked(requestCode);
                    }
                }
            });
        }
        if (!TextUtils.isEmpty(negative)) {
            builder.setNegativeButton(negative, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (OnDialogNegativeListener listener : getDialogListeners(OnDialogNegativeListener.class)) {
                        listener.onNegativeButtonClicked(requestCode);
                    }
                    if (downloadTask != null) {
                        downloadTask.cancel(true);
                        dpbProgress.abortDownload();
                    }
                    dismiss();
                }
            });
        }
        return builder;
    }

    public static DownloadDialogBuilder createBuilder(FragmentManager fragmentManager) {
        return new DownloadDialogBuilder(fragmentManager, DownloadDialog.class);
    }

    @Override
    public void onDownloadStart() {
        LogUtil.getInstance().print("onDownloadStart");
        dpbProgress.onManualProgressAnimation();
        dpbProgress.setOnProgressUpdateListener(this);
        dpbProgress.setEnabled(false);
        isSuccess = false;
    }

    @Override
    public void onDownloadProgress(float progress, long speed) {
        LogUtil.getInstance().print("onDownloadProgress");
        dpbProgress.setProgress(progress);
        dpbProgress.setEnabled(false);
        isSuccess = false;
    }

    @Override
    public void onDownloadFailed() {
        LogUtil.getInstance().print("onDownloadFailed");
        dpbProgress.setEnabled(true);
        dpbProgress.onFailed();
        isSuccess = false;
    }

    @Override
    public void onDownloadSuccess() {
        LogUtil.getInstance().print("onDownloadSuccess");
        dpbProgress.setEnabled(false);
        dpbProgress.onSuccess();
        isSuccess = true;
    }

    @Override
    public void onProgressUpdate(float degree) {
        LogUtil.getInstance().print(degree);
    }

    @Override
    public void onAnimationStarted() {
        LogUtil.getInstance().print("onAnimationStarted");
        dpbProgress.setEnabled(false);
    }

    @Override
    public void onAnimationEnded() {
        LogUtil.getInstance().print("onAnimationEnded");
        dpbProgress.setEnabled(false);
        if (isSuccess) {
            dpbProgress.setEnabled(false);
            dismissAllowingStateLoss();
            onDialogInstallListner.onDialogInstall(file.getAbsolutePath());
        } else {
            if (downloadTask != null) {
                downloadTask.cancel(true);
                dpbProgress.abortDownload();
                downloadTask = null;
            }
            dpbProgress.setEnabled(true);
        }
    }

    @Override
    public void onAnimationSuccess() {
        LogUtil.getInstance().print("onAnimationSuccess");
        dpbProgress.setEnabled(false);
    }

    @Override
    public void onAnimationFailed() {
        LogUtil.getInstance().print("onAnimationFailed");
        dpbProgress.setEnabled(false);
    }

    @Override
    public void onManualProgressStarted() {
        LogUtil.getInstance().print("onManualProgressStarted");
        dpbProgress.setEnabled(false);
    }

    @Override
    public void onManualProgressEnded() {
        LogUtil.getInstance().print("onManualProgressEnded");
        dpbProgress.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dpbProgress:
                if (!TextUtils.isEmpty(url) && file != null) {
                    if (downloadTask == null) {
                        downloadTask = new DownloadTask(url, file, this);
                    }
                    downloadTask.execute();
                } else {
                    onDownloadFailed();
                }
                break;
            default:
                break;
        }
    }
}
