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

    private JSONObject generateRequestParameters(String appId, int clientVerion, String clientInfo, String deviceType, String osType, String osVersion, String method, String timestamp, String encryptData, String bizContent, String sign) {
        JSONObject jsonObject = new JSONObject();
        if (!TextUtils.isEmpty(appId)) {
            jsonObject.put(BaseRequestParameterKey.APP_ID, appId);
        }
        if (clientVerion != -1) {
            jsonObject.put(BaseRequestParameterKey.CLIENT_VERION, clientVerion);
        }
        if (!TextUtils.isEmpty(clientInfo)) {
            jsonObject.put(BaseRequestParameterKey.CLIENT_INFO, clientInfo);
        }
        if (!TextUtils.isEmpty(deviceType)) {
            jsonObject.put(BaseRequestParameterKey.DEVICE_TYPE, deviceType);
        }
        if (!TextUtils.isEmpty(osType)) {
            jsonObject.put(BaseRequestParameterKey.OS_TYPE, osType);
        }
        if (!TextUtils.isEmpty(osVersion)) {
            jsonObject.put(BaseRequestParameterKey.OS_VERSION, osVersion);
        }
        if (!TextUtils.isEmpty(method)) {
            jsonObject.put(BaseRequestParameterKey.METHOD, method);
        }
        if (!TextUtils.isEmpty(timestamp)) {
            jsonObject.put(BaseRequestParameterKey.TIME_STAMP, timestamp);
        }
        if (!TextUtils.isEmpty(encryptData)) {
            jsonObject.put(BaseRequestParameterKey.ENCRYPT_DATA, encryptData);
        }
        if (!TextUtils.isEmpty(bizContent)) {
            jsonObject.put(BaseRequestParameterKey.BIZ_CONTENT, bizContent);
        }
        if (!TextUtils.isEmpty(sign)) {
            jsonObject.put(BaseRequestParameterKey.SIGN, sign);
        }
        return jsonObject;
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
