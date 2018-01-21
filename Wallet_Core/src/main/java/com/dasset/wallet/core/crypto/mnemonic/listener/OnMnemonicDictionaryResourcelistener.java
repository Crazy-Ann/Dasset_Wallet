package com.dasset.wallet.core.crypto.mnemonic.listener;


import com.dasset.wallet.core.contant.MnemonicDictionary;

import java.io.InputStream;

public interface OnMnemonicDictionaryResourcelistener {

    InputStream onMnemonicDictionaryResource(MnemonicDictionary mnemonicDictionary);
}
