package com.dasset.wallet.core.random.listener;

import com.dasset.wallet.core.random.IUEntropySource;

public interface UEntropyCollectorListener {

    void onUEntropySourceError(Exception e, IUEntropySource source);
}
