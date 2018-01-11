package com.dasset.wallet.net;


import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.http.BaseRequest;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.http.request.RequestParameter;
import com.dasset.wallet.components.utils.DeviceUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WalletRequest extends BaseRequest {

    private static WalletRequest walletRequest;

    private WalletRequest() {
        // cannot be instantiated
    }

    public static synchronized WalletRequest getInstance() {
        if (walletRequest == null) {
            walletRequest = new WalletRequest();
        }
        return walletRequest;
    }

    public static void releaseInstance() {
        if (walletRequest != null) {
            walletRequest = null;
        }
    }

    public RequestParameter generateRequestParameters(String method, String bizContent, boolean isJson, boolean isEncrypt) {
        return formatParameters(generateRequestParameters(DeviceUtil.getInstance().getDeviceId(BaseApplication.getInstance())
                , BuildConfig.VERSION_CODE
                , DeviceUtil.getInstance().getDeviceInfo(BaseApplication.getInstance(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, false)
                , Regex.ANDROID.getRegext()
                , Regex.ANDROID.getRegext()
                , DeviceUtil.getInstance().getSystemVersion()
                , method
                , new SimpleDateFormat(Regex.DATE.getRegext(), Locale.getDefault()).format(new Date(System.currentTimeMillis()))
                , bizContent), isJson, isEncrypt);
    }
}
