package com.dasset.wallet.base.http.response;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.base.constant.ResponseCode;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.http.response.HttpResponse;

import okhttp3.Headers;
import okhttp3.Response;

public abstract class JSONObjectResponse extends HttpResponse<JSONObject> {

    private BaseEntity baseEntity;

    public JSONObjectResponse() {
        super();
        type = JSONObject.class;
        this.baseEntity = new BaseEntity();
    }

    @Override
    public void onResponse(String response, Headers headers) {

    }

    @Override
    public void onResponse(Response httpResponse, String response, Headers headers) {

    }

    @Override
    public void onSuccess(Headers headers, JSONObject jsonObject) {

    }

    @Override
    public void onSuccess(JSONObject object) {
        baseEntity.parse(object);
        switch (baseEntity.getErrorCode()) {
            case ResponseCode.ErrorCode.SUCCESS:
                onResponseSuccess(object);
                break;
            default:
                onResponseFailed(-1, null, object);
                break;
        }
    }

    @Override
    public void onFailed(int code, String message) {
        onResponseFailed(code, message, null);
    }

    public abstract void onParseData(JSONObject object);

    public abstract void onResponseSuccess(JSONObject object);

    public abstract void onResponseFailed(int code, String message, JSONObject object);
}
