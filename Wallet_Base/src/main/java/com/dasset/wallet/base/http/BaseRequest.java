package com.dasset.wallet.base.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.base.constant.BaseRequestParameterKey;
import com.dasset.wallet.components.http.request.RequestParameter;
import com.dasset.wallet.components.utils.LogUtil;

public class BaseRequest {

    protected BaseRequest() {
        // cannot be instantiated
    }

    private JSONObject generateRequestParameters(String address, String timeStamp, String note, String clientInfo, String bizContent) {
        JSONObject jsonObject = new JSONObject();
        if (!TextUtils.isEmpty(address)) {
            jsonObject.put(BaseRequestParameterKey.ADDRESS, address);
        }
        if (!TextUtils.isEmpty(timeStamp)) {
            jsonObject.put(BaseRequestParameterKey.TIME_STAMP, timeStamp);
        }
        if (!TextUtils.isEmpty(note)) {
            jsonObject.put(BaseRequestParameterKey.NOTE, note);
        }
        if (!TextUtils.isEmpty(clientInfo)) {
            jsonObject.put(BaseRequestParameterKey.CLIENT_INFO, clientInfo);
        }
        if (!TextUtils.isEmpty(bizContent)) {
            jsonObject.put(BaseRequestParameterKey.BIZ_CONTENT, bizContent);
        }
        return jsonObject;
    }

    protected RequestParameter generateRequestParameters(String address, String timeStamp, String note, String clientInfo, String bizContent, boolean isJson) {
        return formatParameters(generateRequestParameters(address, timeStamp, note, clientInfo, bizContent), isJson);
    }

    private RequestParameter formatParameters(JSONObject jsonObject, boolean isJson) {
        RequestParameter parameter = new RequestParameter();
        parameter.setJsonType(isJson);
        LogUtil.getInstance().print("签名加密前：" + jsonObject.toString());
        if (!TextUtils.isEmpty(jsonObject.toString())) {
            jsonObject = JSONObject.parseObject(jsonObject.toString());
            for (String key : jsonObject.keySet()) {
                String value = jsonObject.getString(key);
                if (!TextUtils.isEmpty(value)) {
                    parameter.addFormDataParameter(key, value);
                    LogUtil.getInstance().print("key:" + key + ",value:" + value);
                }
            }
            return parameter;
        } else {
            return null;
        }
    }
}
