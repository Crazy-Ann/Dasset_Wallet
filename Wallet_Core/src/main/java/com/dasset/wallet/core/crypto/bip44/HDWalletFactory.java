package com.dasset.wallet.core.crypto.bip44;

import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.contant.Language;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.AbstractBitcoinNetParams;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * WalletFactory.java : Class for creating/restoring/reading BIP44 HD wallet
 * <p>
 * BIP44 extension of Bitcoinj
 */
public class HDWalletFactory {

    public static final String BIP39_ENGLISH_SHA256 = "ad90bf3beb7b0eb7e5acd74727dc0da96e0a280a258354e7293fb7e211ac03db";

    /**
     * Create new wallet.
     *
     * @param nbWords    number of words in menmonic
     * @param passphrase optional BIP39 passphrase
     * @param nbAccounts create this number of accounts
     *
     * @return HDWallet
     */
    public static HDWallet createWallet(AbstractBitcoinNetParams abstractBitcoinNetParams, Language language, int nbWords, String passphrase, int nbAccounts) throws IOException, MnemonicException.MnemonicLengthException {
        LogUtil.getInstance().print("Generating HDWallet!");
        HDWallet hdWallet;
        if ((nbWords % 3 != 0) || (nbWords < 12 || nbWords > 24)) {
            nbWords = 12;
        }
        // len == 16 (12 words), len == 24 (18 words), len == 32 (24 words)
        int length = (nbWords / 3) * 4;
        if (passphrase == null) {
            passphrase = Regex.NONE.getRegext();
        }
        SecureRandom secureRandom = new SecureRandom();
        byte seed[] = new byte[length];
        secureRandom.nextBytes(seed);
        InputStream inputStream = HDWalletFactory.class.getClassLoader().getResourceAsStream("wordlist/" + getLocale(language).toString() + Regex.TXT.getRegext());
        if (inputStream != null) {
            MnemonicCode mc = new MnemonicCode(inputStream, null);
            hdWallet = new HDWallet(mc, abstractBitcoinNetParams, seed, passphrase, nbAccounts);
            inputStream.close();
        } else {
            LogUtil.getInstance().print("Cannot read BIP39 word list!");
            return null;
        }
        return hdWallet;
    }

    /**
     * Restore wallet.
     *
     * @param data:      either BIP39 mnemonic or hex seed
     * @param passphrase optional BIP39 passphrase
     * @param nbAccounts create this number of accounts
     *
     * @return HDWallet
     */
    public static HDWallet restoreWallet(AbstractBitcoinNetParams abstractBitcoinNetParams, Language language, String data, String passphrase, int nbAccounts) throws AddressFormatException, IOException, DecoderException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException {
        LogUtil.getInstance().print("Restoring HDWallet from seed!");
        HDWallet hdWallet;
        if (passphrase == null) {
            passphrase = Regex.NONE.getRegext();
        }
        InputStream inputStream = HDWalletFactory.class.getClassLoader().getResourceAsStream("wordlist/" + getLocale(language).toString() + Regex.TXT.getRegext());
        if (inputStream == null) {
            throw new MnemonicException.MnemonicWordException("Cannot read BIP39 word list!");
        }
        MnemonicCode mnemonicCode = new MnemonicCode(inputStream, null);
        if (data.length() % 4 == 0 && !data.contains(Regex.SPACE.getRegext())) {
            //Hex seed
            hdWallet = new HDWallet(mnemonicCode, abstractBitcoinNetParams, Hex.decodeHex(data.toCharArray()), passphrase, nbAccounts);
        } else {
            data = data.replaceAll("[^a-z]+", Regex.SPACE.getRegext());// only use for BIP39 English
            hdWallet = new HDWallet(mnemonicCode, abstractBitcoinNetParams, mnemonicCode.toEntropy(Arrays.asList(data.trim().split("\\s+"))), passphrase, nbAccounts);
        }
        inputStream.close();
        return hdWallet;
    }

    public static HDWallet restoreWatchOnlyWallet(AbstractBitcoinNetParams abstractBitcoinNetParams, ArrayList<String> xpubs) throws AddressFormatException, IOException, DecoderException, MnemonicException.MnemonicLengthException, MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException {
        return new HDWallet(abstractBitcoinNetParams, xpubs);
    }

    private static Locale getLocale(Language language) {
        Locale locale = new Locale("en", "US");
        switch (language) {
            case US:
                locale = new Locale("en", "US");
                break;
            case ES:
                locale = new Locale("es", "ES");
                break;
            case FR:
                locale = new Locale("fr", "FR");
                break;
            case JP:
                locale = new Locale("jp", "JP");
                break;
            case CN:
                locale = new Locale("zh", "CN");
                break;
            case TW:
                locale = new Locale("zh", "TW");
                break;
        }
        return locale;
    }

}