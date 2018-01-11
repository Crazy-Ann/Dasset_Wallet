package com.dasset.wallet.core.ecc;

import com.dasset.wallet.components.constant.Regex;

public final class Constant {

    public final static class FilePath {
        public static final String KEYSTORE = "keystore";
        public static final String PASSWORD = "password";
        public static final String KEYSTORE_CACHE = com.dasset.wallet.components.constant.Constant.FilePath.CACHE + Regex.LEFT_SLASH.getRegext() + KEYSTORE;
    }

    public final static class AddressType {
        public static final int BTC = 0;
        public static final int ETH = 1;
        public static final int HYC = 2;
    }
}
