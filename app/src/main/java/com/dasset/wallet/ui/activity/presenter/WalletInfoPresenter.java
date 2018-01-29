package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.FileProviderUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.WalletInfo;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.WalletInfoActivity;
import com.dasset.wallet.ui.activity.contract.WalletInfoContract;

import java.io.File;

public class WalletInfoPresenter extends BasePresenterImplement implements WalletInfoContract.Presenter {

    private WalletInfoContract.View view;
    private WalletInfoHandler walletInfoHandler;
    private WalletInfo walletInfo;

    public WalletInfo getWalletInfo() {
        return walletInfo;
    }

    private class WalletInfoHandler extends ActivityHandler<WalletInfoActivity> {

        public WalletInfoHandler(WalletInfoActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(WalletInfoActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.ACCOUNT_EXPORT_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS);
                        break;
                    case Constant.StateCode.ACCOUNT_EXPORT_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED);
                        break;
                    case Constant.StateCode.ACCOUNT_DELETE_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_DELETE_WALLET_SUCCESS);
                        break;
                    case Constant.StateCode.ACCOUNT_DELETE_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_DELETE_WALLET_FAILED);
                        break;
                    case Constant.StateCode.QRCODE_SAVE_SUCCESS:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SAVE_SUCCESS);
                        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(Regex.FILE_URI.getRegext() + Environment.getExternalStorageDirectory())));
                        break;
                    case Constant.StateCode.QRCODE_SAVE_FAILED:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SAVE_ERROR);
                        break;
                    case Constant.StateCode.QRCODE_SHARE_SUCCESS:
                        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(Regex.FILE_URI.getRegext() + Environment.getExternalStorageDirectory())));
                        File file = (File) message.obj;
                        if (file != null && file.exists()) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType(Regex.IMAGE_DIRECTORY_TYPE.getRegext());
                            intent.putExtra(Intent.EXTRA_STREAM, FileProviderUtil.getInstance().generateUri(activity, intent, file));
                            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                                activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.dialog_prompt_import_account_to)), Constant.RequestCode.EXPORT_QRCODE);
                            } else {
                                activity.showPromptDialog(R.string.dialog_prompt_qrcode_share_error, false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
                            }
                        } else {
                            activity.showPromptDialog(R.string.dialog_prompt_qrcode_share_error, false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
                        }
                        break;
                    case Constant.StateCode.QRCODE_SHARE_FAILED:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public WalletInfoPresenter(Context context, WalletInfoContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        walletInfoHandler = new WalletInfoHandler((WalletInfoActivity) view);
        walletInfo = BundleUtil.getInstance().getParcelableData((Activity) view, Constant.BundleKey.WALLET_INFO);
    }

    @Override
    public void deleteWallet() {
        
    }
}
