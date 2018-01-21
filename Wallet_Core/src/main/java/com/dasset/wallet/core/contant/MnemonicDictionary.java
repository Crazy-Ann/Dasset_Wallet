package com.dasset.wallet.core.contant;

import com.google.common.collect.Lists;

import java.util.ArrayList;

public enum MnemonicDictionary {

    US("en_us"), ES("es_es"), FR("fr_fr"), JP("jp_jp"), CN("zh_cn"), TW("zh_tw"), US_FLAG("%"), CN_FLAG("%1%"), TW_FLAG("%2%");

    private String language;

    MnemonicDictionary(String language) {
        this.language = language;

    }

    public String getLanguage() {
        return language;
    }

    public String getHDQRCodeFlag() {
        switch (this) {
            case US:
                return US_FLAG.getLanguage();
            case CN:
                return CN_FLAG.getLanguage();
            case TW:
                return TW_FLAG.getLanguage();
            default:
                return US_FLAG.getLanguage();
        }
    }

    public String getSupportedLanguage() {
        switch (this) {
            case US:
                return US.getLanguage();
            case ES:
                return ES.getLanguage();
            case FR:
                return FR.getLanguage();
            case JP:
                return JP.getLanguage();
            case CN:
                return CN.getLanguage();
            case TW:
                return TW.getLanguage();
            default:
                return US.getLanguage();
        }
    }

    public static MnemonicDictionary getSupportedLanguage(String language) {
        for (MnemonicDictionary mnemonicDictionary : getSupportedLanguages()) {
            if (mnemonicDictionary.getSupportedLanguage().equals(language)) {
                return mnemonicDictionary;
            }
        }
        return US;
    }

    public static ArrayList<MnemonicDictionary> getSupportedLanguages() {
        ArrayList<MnemonicDictionary> mnemonicDictionaries = Lists.newArrayList();
        mnemonicDictionaries.add(US);
        mnemonicDictionaries.add(ES);
        mnemonicDictionaries.add(FR);
        mnemonicDictionaries.add(JP);
        mnemonicDictionaries.add(CN);
        mnemonicDictionaries.add(TW);
        return mnemonicDictionaries;
    }

    public static MnemonicDictionary getMnemonicWordListForHdSeed(String data) {
        if (getHDQRCodeFlagLength(data, MnemonicDictionary.CN) > 0) {
            return MnemonicDictionary.CN;
        }
        if (getHDQRCodeFlagLength(data, MnemonicDictionary.TW) > 0) {
            return MnemonicDictionary.TW;
        }
        if (getHDQRCodeFlagLength(data, MnemonicDictionary.US) > 0) {
            return MnemonicDictionary.US;
        }
        return null;
    }

    public static int getHDQRCodeFlagLength(String data, MnemonicDictionary mnemonicDictionary) {
        String flag = mnemonicDictionary.getHDQRCodeFlag();
        if (data.length() < flag.length()) {
            return 0;
        }
        return flag.equals(data.substring(0, flag.length())) ? flag.length() : 0;
    }
}

