package com.dasset.wallet.core.crypto.bip44;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.util.ArrayList;
import java.util.List;

/**
 * HDWallet.java : BIP44 wallet
 */
public class HDWallet {

    private byte[] seed;
    private String passphrase;
    private List<String> wordList;
    private DeterministicKey deterministicKey;
    private DeterministicKey rootDeterministicKey;
    private ArrayList<HDAccount> hdAccounts;
    private String path;
    private NetworkParameters networkParameters;

    /**
     * Constructor for wallet.
     *
     * @param mnemonicCode mnemonic code object
     * @param seed         seed for this wallet
     * @param passphrase   optional BIP39 passphrase
     * @param nbAccounts   number of hdAccounts to create
     */
    public HDWallet(MnemonicCode mnemonicCode, NetworkParameters networkParameters, byte[] seed, String passphrase, int nbAccounts) throws MnemonicException.MnemonicLengthException {
        this.networkParameters = networkParameters;
        this.seed = seed;
        this.passphrase = passphrase;
        this.wordList = mnemonicCode.toMnemonic(seed);
        this.deterministicKey = HDKeyDerivation.createMasterPrivateKey(MnemonicCode.toSeed(wordList, this.passphrase));
        DeterministicKey deterministicKey = HDKeyDerivation.deriveChildKey(this.deterministicKey, 44 | ChildNumber.HARDENED_BIT);
        this.rootDeterministicKey = HDKeyDerivation.deriveChildKey(deterministicKey, ChildNumber.HARDENED_BIT);
        this.hdAccounts = Lists.newArrayList();
        for (int i = 0; i < nbAccounts; i++) {
            hdAccounts.add(new HDAccount(networkParameters, rootDeterministicKey, i));
        }
        this.path = deterministicKey.getPathAsString();
    }

    /**
     * Constructor for watch-only wallet initialized from submitted XPUB(s).
     *
     * @param xpubs arrayList of XPUB strings
     */
    public HDWallet(NetworkParameters networkParameters, ArrayList<String> xpubs) throws AddressFormatException {
        this.networkParameters = networkParameters;
        this.hdAccounts = Lists.newArrayList();
        int i = 0;
        for (String xpub : xpubs) {
            hdAccounts.add(new HDAccount(networkParameters, xpub, i));
            i++;
        }
    }

    /**
     * Return wallet seed as byte array.
     *
     * @return byte[]
     */
    public byte[] getSeed() {
        return seed;
    }

    /**
     * Return wallet seed as hex string.
     *
     * @return String
     */
    public String getSeedHex() {
        return new String(Hex.encodeHex(seed));
    }

    /**
     * Return wallet BIP39 mnemonic as string containing space separated words.
     *
     * @return String
     */
    @Deprecated
    public String getMnemonicOld() {
        return Joiner.on(" ").join(wordList);
    }

    public List<String> getMnemonic() {
        return wordList;
    }

    /**
     * Return wallet BIP39 passphrase.
     *
     * @return String
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Return hdAccounts for this wallet.
     *
     * @return List<HDAccount>
     */
    public List<HDAccount> getHdAccounts() {
        return hdAccounts;
    }

    /**
     * Return account for submitted account id.
     *
     * @return HDAccount
     */
    public HDAccount getAccount(int accountId) {
        return hdAccounts.get(accountId);
    }

    /**
     * Add new account.
     */
    public HDAccount addAccount() {
        HDAccount account = new HDAccount(networkParameters, rootDeterministicKey, hdAccounts.size());
        hdAccounts.add(account);

        return account;
    }

    /**
     * Return BIP44 path for this wallet (m / purpose').
     *
     * @return String
     */
    public String getPath() {
        return path;
    }


    public DeterministicKey getMasterKey() {
        return deterministicKey;
    }

}