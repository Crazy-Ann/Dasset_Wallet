package com.dasset.wallet.base.sticky.model;

import com.dasset.wallet.base.BuildConfig;

public class Footer {

    private int code;
    private Object tag;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "Footer{" +
                    "code=" + code +
                    ", tag=" + tag +
                    '}';
        } else {
            return super.toString();
        }
    }
}
