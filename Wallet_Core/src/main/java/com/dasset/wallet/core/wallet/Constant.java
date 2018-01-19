package com.dasset.wallet.core.wallet;

import com.dasset.wallet.components.constant.Regex;

public final class Constant {

    public final static String BASE58 = "base58";
    public final static String BASE64 = "base64";
    public final static String BIP38 = "bip38";
    public final static String HEX = "hex";
    public final static String MINI = "mini";
    public final static String WIF_COMPRESSED = "wif_c";
    public final static String WIF_UNCOMPRESSED = "wif_u";

    public final static String SHA_256 = "SHA-256";
    public final static String UTF_8 = "UTF-8";

    public final static String FILE_URANDOM = "/dev/urandom";

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
