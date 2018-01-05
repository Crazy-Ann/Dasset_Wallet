package com.dasset.wallet.model.validation;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.dasset.wallet.R;
import com.dasset.wallet.components.utils.ToastUtil;
import com.dasset.wallet.components.validation.ValidationExecutor;


public class AccountNameValidation extends ValidationExecutor {

    @Override
    public boolean doValidate(Context context, String text) {
        if (TextUtils.isEmpty(text)) {
            ToastUtil.getInstance().showToast(context, context.getString(R.string.prompt_account_name), Toast.LENGTH_SHORT);
            return false;
        } else {
            return true;
        }
    }
}
