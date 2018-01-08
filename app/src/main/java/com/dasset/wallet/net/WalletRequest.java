package com.dasset.wallet.net;


import com.dasset.wallet.base.http.BaseRequest;

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
}
