package com.dasset.wallet.core.random;

public interface IUEntropySource {

    void onResume();

    void onPause();

    UEntropyCollector.UEntropySource type();
}
