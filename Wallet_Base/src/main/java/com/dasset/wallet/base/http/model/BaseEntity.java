package com.dasset.wallet.base.http.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.base.BuildConfig;
import com.dasset.wallet.base.constant.BaseResponseParameterKey;
import com.dasset.wallet.base.http.model.cache.listener.implement.CacheableImplement;

public class BaseEntity extends CacheableImplement implements Parcelable {

    private String errorCode;
    private String errorMessage;
    private String returnCode;
    private String returnMessage;
    private String sign;

    public BaseEntity() { }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public String getSign() {
        return sign;
    }

    public BaseEntity parse(JSONObject object) {
        if (object != null) {
            errorCode = object.getString(BaseResponseParameterKey.ERROR_CODE);
            errorMessage = object.getString(BaseResponseParameterKey.ERROR_MESSAGE);
            returnCode = object.getString(BaseResponseParameterKey.RETURN_CODE);
            returnMessage = object.getString(BaseResponseParameterKey.RETURN_MESSAGE);
            sign = object.getString(BaseResponseParameterKey.SIGN);
        }
        return this;
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "BaseEntity{" +
                    "errorCode='" + errorCode + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", returnCode='" + returnCode + '\'' +
                    ", returnMessage='" + returnMessage + '\'' +
                    ", sign='" + sign +
                    '}';
        } else {
            return super.toString();
        }
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.errorCode);
        dest.writeString(this.errorMessage);
        dest.writeString(this.returnCode);
        dest.writeString(this.returnMessage);
        dest.writeString(this.sign);
    }

    protected BaseEntity(Parcel in) {
        this.errorCode = in.readString();
        this.errorMessage = in.readString();
        this.returnCode = in.readString();
        this.returnMessage = in.readString();
        this.sign = in.readString();
    }

}
