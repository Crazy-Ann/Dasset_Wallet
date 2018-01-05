package com.dasset.wallet.constant;

/**
 * Created by yjt on 2017/12/12.
 */

public class ParameterKey {

    private ParameterKey() { }

    public static class CreateAccount {
        public static final String TRADE_PASSWORD = "tradepwd";
        public static final String LOCK_PASSWORD = "lockpwd";
        public static final String PUBLIC_KEY = "pubkey";
        public static final String SIGN = "sign";
    }
}
