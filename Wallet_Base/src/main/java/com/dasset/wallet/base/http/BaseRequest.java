package com.dasset.wallet.base.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.constant.BaseRequestParameterKey;
import com.dasset.wallet.components.http.request.RequestParameter;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.SecurityUtil;

public class BaseRequest {

    protected BaseRequest() {
        // cannot be instantiated
    }

    protected JSONObject generateRequestParameters(String appId, int clientVerion, String clientInfo, String deviceType, String osType, String osVersion, String method, String timestamp, String bizContent) {
        JSONObject jsonObject = new JSONObject();
        if (!TextUtils.isEmpty(appId)) {
            jsonObject.put(BaseRequestParameterKey.APP_ID, appId);
        }
        if (clientVerion > 0) {
            jsonObject.put(BaseRequestParameterKey.CLIENT_VERION, String.valueOf(clientVerion));
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
        if (!TextUtils.isEmpty(bizContent)) {
            jsonObject.put(BaseRequestParameterKey.BIZ_CONTENT, bizContent);
        }
        LogUtil.getInstance().print(String.format("jsonObject:%s", jsonObject));
        return jsonObject;
    }

    protected RequestParameter formatParameters(JSONObject jsonObject, boolean isJson, boolean isEncrypt) {
        RequestParameter requestParameter = new RequestParameter();
        requestParameter.setJsonType(isJson);
        String encryptData = SecurityUtil.encryptAES(jsonObject.toString(), BaseApplication.getInstance().getEncryptKey(), isEncrypt, 3);
        if (!TextUtils.isEmpty(encryptData)) {
            jsonObject = JSONObject.parseObject(encryptData);
            for (String key : jsonObject.keySet()) {
                String value = jsonObject.getString(key);
                if (!TextUtils.isEmpty(value)) {
                    requestParameter.addFormDataParameter(key, value);
                    LogUtil.getInstance().print(String.format("key:%s, value:%s", key, value));
                }
            }
            return requestParameter;
        } else {
            return null;
        }
    }
}
