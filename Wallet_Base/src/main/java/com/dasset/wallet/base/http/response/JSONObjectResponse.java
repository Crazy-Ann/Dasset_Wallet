package com.dasset.wallet.base.http.response;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.base.constant.ResponseCode;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.http.response.HttpResponse;

public abstract class JSONObjectResponse extends HttpResponse<JSONObject> {

    private BaseEntity baseEntity;

    public JSONObjectResponse() {
        super();
        type = JSONObject.class;
        this.baseEntity = new BaseEntity();
    }

    @Override
    public void onSuccess(JSONObject object) {
        baseEntity.parse(object);
        switch (baseEntity.getErrorCode()) {
            case ResponseCode.ErrorCode.SUCCESS:
                onResponseSuccess(object);
                break;
            default:
                onResponseFailed(object);
                break;
        }
    }

    public abstract void onParseData(JSONObject object);

    public abstract void onResponseSuccess(JSONObject object);

    public abstract void onResponseFailed(JSONObject object);

    public abstract void onResponseFailed(String code, String message);

    public abstract void onResponseFailed(String code, String message, JSONObject object);
}
