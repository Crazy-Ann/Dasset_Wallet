package com.dasset.wallet.net;

public final class NotNullValidator {

    private static NotNullValidator notNullValidator;

    private NotNullValidator() {
    }

    public static synchronized NotNullValidator getInstance() {
        if (notNullValidator == null) {
            notNullValidator = new NotNullValidator();
        }
        return notNullValidator;
    }

    public static void releaseInstance() {
        if (notNullValidator != null) {
            notNullValidator = null;
        }
    }

    /*----------------------------  版本信息开始  -----------------------------------*/

}
