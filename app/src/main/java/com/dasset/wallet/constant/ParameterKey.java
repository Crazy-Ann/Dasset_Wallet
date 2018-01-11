package com.dasset.wallet.constant;

/**
 * Created by yjt on 2017/12/12.
 */

public class ParameterKey {

    private ParameterKey() { }

    public static class Menu {
        public static final String Menu = "menu";
        public static final String NAME = "name";
    }

    public static class GetVersion {
        public static final String CLIENT_VERSION = "client_ver";
        public static final String VERSION_NAME = "ver_name";
        public static final String DOWNLOAD_URL = "download_url";
        public static final String LOWEST_CLIENT_VERSION = "lowest_client_ver";
        public static final String APK = "apk";
        public static final String UPDATE_MESSAGE = "update_msg";
        public static final String SERVICE_URL = "service_url";
        public static final String PAGE_SRC_SIGN = "page_src_sign";
        public static final String PAGES_LIST = "pages_list";
        public static final String NAME = "name";
        public static final String INDEX = "index";
        public static final String ICON_URL = "icon_url";
        public static final String TYPE = "type";
        public static final String ACTION = "action";
        public static final String EXTENDED_PARAMETER = "ext_param";
    }

    public static class CreateAccount {
        public static final String TRADE_PASSWORD = "tradepwd";
        public static final String LOCK_PASSWORD = "lockpwd";
        public static final String PUBLIC_KEY = "pubkey";
        public static final String SIGN = "sign";
    }
}
