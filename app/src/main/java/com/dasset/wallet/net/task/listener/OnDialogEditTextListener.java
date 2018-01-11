package com.dasset.wallet.net.task.listener;

import android.os.Bundle;

import com.dasset.wallet.base.dialog.listener.OnDialogNegativeListener;

public interface OnDialogEditTextListener extends OnDialogNegativeListener {

    void onPositiveButtonClicked(int requestCode, Bundle bundle);

}
