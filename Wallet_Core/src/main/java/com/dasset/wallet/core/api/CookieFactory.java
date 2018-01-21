package com.dasset.wallet.core.api;

import com.dasset.wallet.core.contant.AbstractApp;

import org.apache.http.client.CookieStore;

public class CookieFactory {

    private static boolean isRunning = false;
    //private static final Logger log = LoggerFactory.getLogger(CookieFactory.class);

    private CookieFactory() {

    }

    public synchronized static boolean initCookie() {
        boolean success = true;
        isRunning = true;
        CookieStore cookieStore = AbstractApp.bitherjSetting.getCookieStore();
        if (cookieStore.getCookies() == null
                || cookieStore.getCookies().size() == 0) {
            try {
                GetCookieApi getCookieApi = new GetCookieApi();
                getCookieApi.handleHttpPost();
                // log.debug("getCookieApi");
            } catch (Exception e) {
                success = false;
                e.printStackTrace();
            }
        }
        isRunning = false;
        return success;

    }

    public static boolean isRunning() {
        return isRunning;
    }

}
