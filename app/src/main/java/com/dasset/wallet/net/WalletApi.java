package com.dasset.wallet.net;


import com.dasset.wallet.base.BuildConfig;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.utils.ApplicationUtil;

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
            return BuildConfig.SERVICE_URL_212;
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

//    public void editAccount(final Context context, final BaseView view, String tradepwd, String lockpwd, BigInteger privateKey, byte[] publicKey, String note, final ApiResponse apiResponse) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
//        LogUtil.getInstance().print("editAccount");
//        if (NetworkUtil.getInstance().isInternetConnecting(context)) {
//            JSONObject object = new JSONObject();
//            object.put(ParameterKey.CreateAccount.TRADE_PASSWORD, tradepwd);
//            object.put(ParameterKey.CreateAccount.LOCK_PASSWORD, lockpwd);
//            object.put(ParameterKey.CreateAccount.PUBLIC_KEY, Hex.toHexString(publicKey));
//            object.put(ParameterKey.CreateAccount.SIGN, Hex.toHexString(ECSignatureFactory.getInstance().generateSignature(privateKey, publicKey, object.toString(), false, true)));
//            RequestParameter parameter = WalletRequest.getInstance().generateRequestParameters(AddressFactory.generatorAddress(publicKey, com.dasset.wallet.core.ecc.Constant.AddressType.HYC), note, object.toString());
//            if (parameter != null) {
//                HttpRequest.getInstance().doPost(context, getServerUrl() + Method.CREATE_ACCOUNT, parameter, new CreateAccountResponse() {
//
//                    @Override
//                    public void onStart() {
//                        super.onStart();
//                        LogUtil.getInstance().print("资产帐户建立开始");
//                        if (!view.isActivityFinish()) {
//                            view.showLoadingPromptDialog(R.string.create_account, Constant.RequestCode.DIALOG_PROGRESS_CREATE_ACCOUNT);
//                        }
//                    }
//
//                    @Override
//                    public void onResponseSuccess(JSONObject object) {
//                        super.onResponseSuccess(object);
//                        LogUtil.getInstance().print("资产帐户建立成功:" + editAccount.toString());
//                        if (!view.isActivityFinish()) {
//                            if (editAccount != null) {
//                                apiResponse.success(editAccount);
//                            } else {
//                                view.showPromptDialog(R.string.dialog_prompt_create_account_error, true, true, Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT_ERROR);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onResponseFailed(String code, String message) {
//                        super.onResponseFailed(code, message);
//                        LogUtil.getInstance().print("资产帐户建立失败,code:" + code + ",message:" + message);
//                        if (!view.isActivityFinish()) {
//                            view.showPromptDialog(message, true, true, Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT_ERROR);
//                            apiResponse.failed(null, code, message);
//                        }
//                    }
//
//                    @Override
//                    public void onResponseFailed(String code, String message, JSONObject object) {
//                        super.onResponseFailed(code, message, object);
//                        LogUtil.getInstance().print("资产帐户建立失败,code:" + code + ",message:" + message);
//                    }
//
//                    @Override
//                    public void onEnd() {
//                        super.onEnd();
//                        LogUtil.getInstance().print("资产帐户建立结束");
//                        if (!view.isActivityFinish()) {
//                            view.hideLoadingPromptDialog();
//                        }
//                    }
//
//                    @Override
//                    public void onFailed(int code, String message) {
//                        super.onFailed(code, message);
//                        LogUtil.getInstance().print("资产帐户建立失败,code:" + code + ",message:" + message);
//                        if (!view.isActivityFinish()) {
//                            view.showPromptDialog(message, true, true, Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT_ERROR);
//                        }
//                    }
//                });
//            } else {
//                if (!view.isActivityFinish()) {
//                    view.showPromptDialog(R.string.request_data_error, true, true, Constant.RequestCode.DIALOG_PROMPT_REQUEST_DATA_ERROR);
//                }
//            }
//        } else {
//            if (!view.isActivityFinish()) {
//                view.showNetWorkPromptDialog();
//            }
//        }
//    }

}
