package com.dasset.wallet.net.response;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.base.http.response.JSONObjectResponse;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.model.Version;

import okhttp3.Headers;
import okhttp3.Response;

public class GetVersionResponse extends JSONObjectResponse {

    public Version version;

    public GetVersionResponse() {
        super();
        this.version = new Version();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onProgress(int progress, long speed, boolean isDone) {
        LogUtil.getInstance().print("GetVersionResponse's progress:" + progress + ",speed:" + speed + ",isDone:" + isDone);
    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onResponse(Response httpResponse, String response, Headers headers) {

    }

    @Override
    public void onResponse(String response, Headers headers) {

    }

    @Override
    public void onSuccess(Headers headers, JSONObject jsonObject) {

    }

    @Override
    public void onFailed(int code, String message) {

    }

    @Override
    public void onParseData(JSONObject object) {
        version.parse(object);
    }

    @Override
    public void onResponseSuccess(JSONObject object) {
        onParseData(object);
    }

    @Override
    public void onResponseFailed(JSONObject object) {
        onParseData(object);
    }

    @Override
    public void onResponseFailed(String code, String message) {

    }

    @Override
    public void onResponseFailed(String code, String message, JSONObject object) {
        
    }
}
