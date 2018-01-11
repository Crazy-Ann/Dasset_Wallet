package com.dasset.wallet.net;


import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.R;
import com.dasset.wallet.base.BuildConfig;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.constant.BaseResponseParameterKey;
import com.dasset.wallet.base.constant.ResponseCode;
import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.components.http.request.HttpRequest;
import com.dasset.wallet.components.http.request.RequestParameter;
import com.dasset.wallet.components.utils.ApplicationUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.NetworkUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.constant.Method;
import com.dasset.wallet.constant.ParameterKey;
import com.dasset.wallet.net.listener.ApiResponse;
import com.dasset.wallet.net.response.GetVersionResponse;

public class WalletApi {

    private static WalletApi walletApi;
    private int retryCount;

    private WalletApi() {
        // cannot be instantiated
    }

    public static synchronized WalletApi getInstance() {
        if (walletApi == null) {
            walletApi = new WalletApi();
        }
        return walletApi;
    }

    private String getServerUrl() {
        if (BuildConfig.DEBUG) {
            return BuildConfig.SERVICE_URL_166;
        } else {
            switch (ApplicationUtil.getInstance().getMetaData(BaseApplication.getInstance(), com.dasset.wallet.components.constant.Constant.Channel.SERVER).toString()) {
                case com.dasset.wallet.components.constant.Constant.Channel.SERVER_95:
                    return BuildConfig.SERVICE_URL_95;
                case com.dasset.wallet.components.constant.Constant.Channel.SERVER_104:
                    return BuildConfig.SERVICE_URL_104;
                case com.dasset.wallet.components.constant.Constant.Channel.SERVER_105:
                    return BuildConfig.SERVICE_URL_105;
                default:
                    return BuildConfig.SERVICE_URL_105;
            }
        }
    }

    private void handleResponseFailed(BaseView view, int promptCode, JSONObject object) {
        if (object.containsKey(BaseResponseParameterKey.ERROR_CODE)) {
            switch (object.getString(BaseResponseParameterKey.ERROR_CODE)) {
                case ResponseCode.ErrorCode.VERSION_ERROR:
                    if (object.containsKey(BaseResponseParameterKey.ERROR_MESSAGE)) {
                        view.showPromptDialog(object.getString(BaseResponseParameterKey.ERROR_MESSAGE), false, false, promptCode);
                    } else if (object.containsKey(BaseResponseParameterKey.RETURN_MESSAGE)) {
                        view.showPromptDialog(object.getString(BaseResponseParameterKey.RETURN_MESSAGE), true, false, promptCode);
                    } else {
                        view.showPromptDialog("未知错误", true, false, promptCode);
                    }
                    break;
                default:
                    if (object.containsKey(BaseResponseParameterKey.RETURN_MESSAGE)) {
                        view.showPromptDialog(object.getString(BaseResponseParameterKey.RETURN_MESSAGE), true, false, promptCode);
                    } else {
                        view.showPromptDialog("未知错误", true, false, promptCode);
                    }
                    break;
            }
        } else {
            if (object.containsKey(BaseResponseParameterKey.RETURN_MESSAGE)) {
                view.showPromptDialog(object.getString(BaseResponseParameterKey.RETURN_MESSAGE), false, false, promptCode);
            } else {
                view.showPromptDialog("未知错误", true, false, promptCode);
            }
        }
    }

    public void getVersion(final Context context, final BaseView view, final String pageSrcSign, final ApiResponse apiResponse) {
        if (NetworkUtil.getInstance().isInternetConnecting(context)) {
            JSONObject object = new JSONObject();
            object.put(ParameterKey.GetVersion.PAGE_SRC_SIGN, pageSrcSign);
            RequestParameter requestParameter = WalletRequest.getInstance().generateRequestParameters(Method.GET_VERSION, object.toString(), true, false);
            if (requestParameter != null) {
                HttpRequest.getInstance().doPost(context, getServerUrl(), requestParameter, new GetVersionResponse() {

                    @Override
                    public void onStart() {
                        super.onStart();
                        LogUtil.getInstance().print("获取版本信息开始");
                        view.showLoadingPromptDialog(R.string.dialog_prompt_get_version, Constant.RequestCode.DIALOG_PROMPT_GET_VERSION);
                    }

                    @Override
                    public void onResponseSuccess(JSONObject object) {
                        super.onResponseSuccess(object);
                        LogUtil.getInstance().print("获取版本信息成功:" + version.toString());
                        if (version != null) {
                            if (TextUtils.isEmpty(version.getApk())) {
                                if (retryCount < Constant.RETRY_TIME) {
                                    getVersion(context, view, pageSrcSign, apiResponse);
                                } else {
                                    view.showPromptDialog(R.string.dialog_prompt_get_version_error, true, false, Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_ERROR);
                                }
                                retryCount++;
                            } else {
                                apiResponse.success(version);
                            }
                        } else {
                            view.showPromptDialog(R.string.dialog_prompt_get_version_error, true, false, Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_ERROR);
                        }
                    }

                    @Override
                    public void onResponseFailed(JSONObject object) {
                        super.onResponseFailed(object);
                        LogUtil.getInstance().print("资产帐户建立失败:" + object.toString());
                        handleResponseFailed(view, Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT_ERROR, object);
                        apiResponse.failed(version);
                    }

                    @Override
                    public void onResponseFailed(String code, String message) {
                        super.onResponseFailed(code, message);
                    }

                    @Override
                    public void onResponseFailed(String code, String message, JSONObject object) {
                        super.onResponseFailed(code, message, object);
                    }

                    @Override
                    public void onEnd() {
                        super.onEnd();
                        LogUtil.getInstance().print("资产帐户建立结束");
                        view.hideLoadingPromptDialog();
                    }

                });
            } else {
                view.showPromptDialog(R.string.request_data_error, true, false, Constant.RequestCode.DIALOG_PROMPT_REQUEST_DATA_ERROR);
            }
        } else {
            view.showNetWorkPromptDialog();
        }
    }

}
