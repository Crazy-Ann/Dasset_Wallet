package com.dasset.wallet.ui.activity.contract;

import android.content.Context;
import android.net.Uri;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface MainContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void setAddAccountView();

        void setMenuView();

        void showImportAccountPromptDialog();

        void loadAccountData();

        void showAddressQRCodePromptDialog(byte[] data, String prompt);
    }

    interface Presenter extends BasePresenter {

        void importAccount(String path, String password);

        String generatorPathFromUri(Uri uri);

        String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs);

        void generateAddresQRCode(int position);

        void save();

        void share();
    }
}
