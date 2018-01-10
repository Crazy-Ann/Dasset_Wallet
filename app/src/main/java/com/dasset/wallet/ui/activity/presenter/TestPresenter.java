package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;
import android.os.Message;
import android.widget.Toast;

import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.components.utils.ToastUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.ecc.AddressFactory;
import com.dasset.wallet.core.ecc.ECKeyPairFactory;
import com.dasset.wallet.core.ecc.ECSignatureFactory;
import com.dasset.wallet.model.Test;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.TestActivity;
import com.dasset.wallet.ui.activity.contract.TestContract;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.SignatureException;

public class TestPresenter extends BasePresenterImplement implements TestContract.Presenter {

    private TestContract.View view;
    private TestHandler testHandler;

    private class TestHandler extends ActivityHandler<TestActivity> {

        public TestHandler(TestActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(TestActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.TEST_SUCCESS:
                        Test eccTest = (Test) message.obj;
                        LogUtil.getInstance().print(String.format("Private Key: %s", eccTest.getPrivateKey()));
                        LogUtil.getInstance().print(String.format("Public Key: %s", eccTest.getPublicKey()));
                        LogUtil.getInstance().print(String.format("Address: %s", eccTest.getAddress()));
//                        LogUtil.getInstance().print(String.format("Signature[r]: %s", eccTest.getSignatureR()));
//                        LogUtil.getInstance().print(String.format("Signature[s]: %s", eccTest.getSignatureS()));
                        LogUtil.getInstance().print(String.format("Signature: %s", eccTest.getSignature()));
                        LogUtil.getInstance().print(String.format("Signature Result: %s", eccTest.getSignatureResult()));
                        view.showTestData(eccTest);
                        break;
                    case Constant.StateCode.TEST_FAILED:
                        ToastUtil.getInstance().showToast(activity, message.obj.toString(), Toast.LENGTH_SHORT);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public TestPresenter(Context context, TestContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        testHandler = new TestHandler((TestActivity) view);
    }

    @Override
    public void getSecurityProviders() {
        if (BuildConfig.DEBUG) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    for (Provider provider : Security.getProviders()) {
                        LogUtil.getInstance().print(String.format("Provider: %s, Version: %s", provider.getName(), provider.getVersion()));
                        for (Provider.Service service : provider.getServices()) {
                            LogUtil.getInstance().print(String.format("Type: %-30s, Algorithm: %-30s", service.getType(), service.getAlgorithm()));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void test(final String data) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //        eccTestPresenter.getSecurityProviders();
                    Test eccTest = new Test();
                    ECKeyPairFactory keyPair = ECKeyPairFactory.generateECKeyPair(new BigInteger("3ed73981e3fc455a161de8fe872d34342e4b5207c8fc28e1dd35add63e92277a", 16), false);
//            Element keyPair = Element.editAccount(false);
                    eccTest.setPrivateKey(Hex.toHexString(keyPair.getPrivateKey().toByteArray()));
                    eccTest.setPublicKey(Hex.toHexString(keyPair.getPublicKey()));
                    eccTest.setAddress(AddressFactory.generatorAddress(keyPair.getPublicKey(), com.dasset.wallet.core.ecc.Constant.AddressType.HYC));
                    String signature = Hex.toHexString(ECSignatureFactory.getInstance().generateSignature(keyPair.getPrivateKey(), keyPair.getPublicKey(), data, false, false));
                    eccTest.setSignature(signature);
                    eccTest.setSignatureResult(ECSignatureFactory.getInstance().verifySignature(data, signature, keyPair.getPublicKey()));
                    testHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.TEST_SUCCESS, eccTest));
                } catch (NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IOException | SignatureException e) {
                    testHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.TEST_FAILED, e));
                    e.printStackTrace();
                }
            }
        });
    }
}
