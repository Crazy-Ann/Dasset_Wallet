package com.dasset.wallet.ui.activity.contract;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

import java.io.File;

public interface MainContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void setAddAccountView();

        void showImportAccountPromptDialog();

        void loadAccountData();
    }

    interface Presenter extends BasePresenter {

        void importAccount(Intent data);

        String generatorPathFromUri(Uri uri);

        String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs);

    }
}
