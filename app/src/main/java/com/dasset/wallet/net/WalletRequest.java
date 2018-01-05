package com.dasset.wallet.net;


import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.http.BaseRequest;
import com.dasset.wallet.components.http.request.RequestParameter;
import com.dasset.wallet.components.utils.ApplicationUtil;
import com.dasset.wallet.components.utils.DeviceUtil;

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

    public RequestParameter generateRequestParameters(String address, String timeStamp, String note, String clientInfo, String bizContent) {
        return generateRequestParameters(address, timeStamp, note, clientInfo, bizContent, true);
    }


    public RequestParameter generateRequestParameters(String address, String note, String bizContent) {
        return generateRequestParameters(address, String.valueOf(System.currentTimeMillis()), note, DeviceUtil.getInstance().getDeviceInfo(BaseApplication.getInstance(), false, ApplicationUtil.getInstance().getVersionCode(BaseApplication.getInstance())), bizContent, true);
    }
}
