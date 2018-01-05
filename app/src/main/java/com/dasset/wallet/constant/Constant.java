package com.dasset.wallet.constant;

import android.Manifest;

public final class Constant {

    private Constant() { }

    public static final String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE
            , Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            , Manifest.permission.RECORD_AUDIO
            , Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.CAMERA
            , Manifest.permission.READ_SMS
            , Manifest.permission.READ_LOGS
            , Manifest.permission.CALL_PHONE
    };

    public final static class RequestCode {
        public static final int DIALOG_PROMPT_SET_PERMISSION = 0x1000;
        public static final int PREMISSION_SETTING = 0x1001;
        public static final int DIALOG_PROMPT_SET_NET_WORK = 0x1002;
        public static final int NET_WORK_SETTING = 0x1003;
        public static final int DIALOG_PROMPT_QUIT = 0x1004;
        public static final int DIALOG_PROGRESS_WALLET = 0x1005;
        public static final int INSTALL_APK = 0x1006;
        public static final int DIALOG_PROMPT_REQUEST_DATA_ERROR = 0x1007;
        public static final int DIALOG_PROGRESS_CREATE_ACCOUNT = 0x1008;
        public static final int DIALOG_PROMPT_CREATE_ACCOUNT = 0x1009;
        public static final int DIALOG_PROMPT_CREATE_ACCOUNT_ERROR = 0x1010;
        public static final int DIALOG_PROMPT_EXPORT_ACCOUNT = 0x1011;
        public static final int DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS = 0x1012;
        public static final int DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED = 0x1013;
        public static final int DIALOG_PROMPT_IMPORT_ACCOUNT = 0x1012;
        public static final int CREATE_ACCOUNT = 0x2000;
    }

    public final static class ResultCode {
        public static final int CREATE_ACCOUNT = 0x3001;

    }

    public final static class Configuration {
        public static final String CONFIGURATION = "CONFIGURATION";
        public static final String KEY1 = "SplashActivity";
    }

    public final static class BundleKey {
        public static final String ENCRYPT_KEY = "encrypt_key";
        public static final String WALLET_ACCOUNT = "wallet_account";
    }

    public class Cache {
        public static final String ROOT = "/mergepay";
        public static final String CACHE_ROOT = ROOT + "/cache";
        public static final String PAGE_DATA_CACHE_PATH = CACHE_ROOT + "/page";
        public static final String PAGE_IMAGE_CACHE_PATH = PAGE_DATA_CACHE_PATH + "/image";
        public static final String SERVICE_DATA_CACHE_PATH = CACHE_ROOT + "/service";
        public static final String SERVICE_IMAGE_CACHE_PATH = SERVICE_DATA_CACHE_PATH + "/icon";
        public static final String TAB_DATA_CACHE_PATH = CACHE_ROOT + "/tab";
        public static final String TAB_IMAGE_CACHE_PATH = TAB_DATA_CACHE_PATH + "/icon";
    }

    public static class CustomProgressBarStatus {
        public static final int STATE_DEFAULT = 101;
        public static final int STATE_DOWNLOADING = 102;
        public static final int STATE_DOWNLOADED = 103;
    }

    public static class RecycleView {
        public static final int ADD_ACCOUNT = 0x7000;

        public static final int DATA_EMPTY = 0x7001;
        public static final int DATA_LOADING = 0x7002;
        public static final int DATA_NONE = 0x7003;
        public static final int DATA_ERROR = 0x7004;
        public static final int FOOTER_VIEW_ID = 0x7005;
        public static final int DATA_SIZE = 10;
    }

    public static class StateCode {
        public static final int TEST_SUCCESS = 0x8001;
        public static final int TEST_FAILED = 0x8002;
        public static final int GENERATE_ECKEYPAIR_SUCCESS = 0x8003;
        public static final int GENERATE_ECKEYPAIR_FAILED = 0x8004;
        public static final int EXPORT_ACCOUNT_SUCCESS = 0x8005;
        public static final int EXPORT_ACCOUNT_FAILED = 0x8006;
        public static final int DELETE_ACCOUNT_SUCCESS = 0x8007;
        public static final int DELETE_ACCOUNT_FAILED = 0x8008;
    }
}
