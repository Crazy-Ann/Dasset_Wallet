package com.dasset.wallet.base.constant;

public final class ResponseCode {

    public static class ErrorCode {
        public static final String SUCCESS                      = "0000";
        public static final String LOGIN_ERROR                  = "1001";
        public static final String VERSION_ERROR                = "1002";
        public static final String NEED_VERIFICATION            = "1003";
        public static final String PARAMETER_ERROR              = "1004";
        public static final String PARAMETER_INCOMPLETE         = "1005";
        public static final String SERVICE_ERROR                = "1006";
        public static final String METHOD_IMPLEMENTED           = "1007";
        public static final String SIGNATURE_VERIFICATION_ERROR = "1008";
        public static final String PASSWORD_ERROR               = "1009";
    }

    public static class ReturnCode {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAIL    = "FAIL";
    }
}
