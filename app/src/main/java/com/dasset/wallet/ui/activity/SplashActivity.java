package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.ArrayMap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.ActivityUtil;
import com.dasset.wallet.components.utils.InputUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.widget.tablayout.TabLayout;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.SplashContract;
import com.dasset.wallet.ui.activity.presenter.SplashPresenter;
import com.dasset.wallet.ui.adapter.SplashAdapter;
import com.dasset.wallet.ui.dialog.PromptDialog;
import com.google.common.collect.Lists;

import java.util.List;

public class SplashActivity extends ActivityViewImplement<SplashContract.Presenter> implements SplashContract.View, View.OnClickListener, ViewPager.OnPageChangeListener {

    private SplashPresenter splashPresenter;

    private ViewPager vpSplash;
    private Button    btnEnter;
    private TabLayout tbSplash;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && ViewUtil.getInstance().getNavigationBarStatus(this) != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_splash);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            splashPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    //TODO
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    showPermissionPromptDialog();
                }
            });
        } else {
            //TODO
        }
    }

    @Override
    protected void findViewById() {
        vpSplash = ViewUtil.getInstance().findView(this, R.id.vpSplash);
        btnEnter = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnEnter, this);
        tbSplash = ViewUtil.getInstance().findView(this, R.id.tbSplash);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        splashPresenter = new SplashPresenter(this, this);
        splashPresenter.initialize();
        setBasePresenterImplement(splashPresenter);

        List<ArrayMap<String, View>> arrayMaps = Lists.newArrayList();
        ArrayMap<String, View>       arrayMap  = new ArrayMap<>();
        for (int i = 0; i < 3; i++) {
            switch (i) {
                case 0:
                    arrayMap.put("https://b-ssl.duitang.com/uploads/item/201501/02/20150102105956_ByWPa.thumb.700_0.jpeg", getLayoutInflater().inflate(R.layout.view_splash_item, null));
                    break;
                case 1:
                    arrayMap.put("https://b-ssl.duitang.com/uploads/item/201506/02/20150602182550_myGsS.thumb.700_0.jpeg", getLayoutInflater().inflate(R.layout.view_splash_item, null));
                    break;
                case 2:
                    arrayMap.put("https://b-ssl.duitang.com/uploads/item/201507/23/20150723171947_daGcQ.thumb.700_0.jpeg", getLayoutInflater().inflate(R.layout.view_splash_item, null));
                    break;
            }
            arrayMaps.add(arrayMap);
        }
        vpSplash.setAdapter(new SplashAdapter<>(this, arrayMaps));
        tbSplash.setViewPager(vpSplash);
    }

    @Override
    protected void setListener() {
        vpSplash.addOnPageChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (InputUtil.getInstance().isDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.btnEnter:
                startMainActivity();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    splashPresenter.checkPermission(new PermissionCallback() {
                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            //TODO
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.prompt_quit))
                .setPositiveButtonText(this, R.string.confirm)
                .setNegativeButtonText(this, R.string.cancel)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_QUIT)
                .showAllowingStateLoss(this);
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_NET_WORK_ERROR");
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), Constant.RequestCode.NET_WORK_SETTING);
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                startPermissionSettingActivity();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QUIT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QUIT");
                BaseApplication.getInstance().releaseInstance();
                ActivityUtil.removeAll();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_NET_WORK");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                refusePermissionSetting();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QUIT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_QUIT");
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void startMainActivity() {
        startActivity(MainActivity.class);
        onFinish("startMainActivity");
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        LogUtil.getInstance().print("position:" + position);
        LogUtil.getInstance().print("positionOffset:" + positionOffset);
        LogUtil.getInstance().print("positionOffsetPixels:" + positionOffsetPixels);
        if (position == 2) {
            ViewUtil.getInstance().setViewVisible(btnEnter);
        } else {
            ViewUtil.getInstance().setViewInvisible(btnEnter);
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
