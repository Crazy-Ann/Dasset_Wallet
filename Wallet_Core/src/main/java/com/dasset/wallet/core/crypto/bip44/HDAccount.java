package com.dasset.wallet.core.crypto.bip44;

import com.google.common.collect.Lists;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;

import java.nio.ByteBuffer;
import java.util.List;

public class HDAccount {

    private DeterministicKey aKey;
    private int aID;
    private List<HDChain> hdChains;
    private String xpub;
    private String path;
    private NetworkParameters networkParameters;

    /**
     * Constructor for account.
     *
     * @param networkParameters Example MainNetParams, RegTestParams, TestNet2Params
     * @param wKey              deterministic key for this account
     * @param child             id within the wallet for this account
     */
    public HDAccount(NetworkParameters networkParameters, DeterministicKey wKey, int child) {
        this.networkParameters = networkParameters;
        this.aID = child;
        // L0PRV & STDVx: private derivation.
        int childnum = child;
        childnum |= ChildNumber.HARDENED_BIT;
        this.aKey = HDKeyDerivation.deriveChildKey(wKey, childnum);
        this.xpub = aKey.serializePubB58(networkParameters);
        this.hdChains = Lists.newArrayList();
        hdChains.add(new HDChain(networkParameters, aKey, true));
        hdChains.add(new HDChain(networkParameters, aKey, false));
        this.path = aKey.getPathAsString();
    }

    /**
     * Constructor for watch-only account.
     *
     * @param networkParameters NetworkParameters
     * @param xpub              XPUB for this account
     * @param child             id within the wallet for this account
     */
    public HDAccount(NetworkParameters networkParameters, String xpub, int child) throws AddressFormatException {
        this.networkParameters = networkParameters;
        this.aID = child;
        // assign master key to account key
        this.aKey = createMasterPubKeyFromXPub(xpub);
        this.xpub = xpub;
        this.hdChains = Lists.newArrayList();
        hdChains.add(new HDChain(networkParameters, aKey, true));
        hdChains.add(new HDChain(networkParameters, aKey, false));

    }

    /**
     * Constructor for watch-only account.
     *
     * @param networkParameters NetworkParameters
     * @param xpub              XPUB for this account
     */
    public HDAccount(NetworkParameters networkParameters, String xpub) throws AddressFormatException {
        this.networkParameters = networkParameters;
        // assign master key to account key
        this.aKey = createMasterPubKeyFromXPub(xpub);
        this.xpub = xpub;
        this.hdChains = Lists.newArrayList();
        hdChains.add(new HDChain(networkParameters, aKey, true));
        hdChains.add(new HDChain(networkParameters, aKey, false));
    }

    /**
     * Restore watch-only account deterministic public key from XPUB.
     *
     * @return DeterministicKey
     */
    private DeterministicKey createMasterPubKeyFromXPub(String xpub) throws AddressFormatException {
        boolean isTestnet = !(this.networkParameters instanceof MainNetParams);
        byte[] xpubBytes = Base58.decodeChecked(xpub);
        ByteBuffer byteBuffer = ByteBuffer.wrap(xpubBytes);
        int prefix = byteBuffer.getInt();
        if (!isTestnet && prefix != 0x0488B21E) {
            throw new AddressFormatException("invalid xpub version");
        }
        if (isTestnet && prefix != 0x043587CF) {
            throw new AddressFormatException("invalid xpub version");
        }
        byte[] chain = new byte[32];
        byte[] pub = new byte[33];
        // depth:
        byteBuffer.get();
        // parent fingerprint:
        byteBuffer.getInt();
        // child no.
        byteBuffer.getInt();
        byteBuffer.get(chain);
        byteBuffer.get(pub);
        return HDKeyDerivation.createMasterPubKeyFromBytes(pub, chain);
    }

    /**
     * Return XPUB string for this account.
     *
     * @return String
     */
    public String getXpub() {
        return xpub;
    }

    /**
     * Return xprv string for this account.
     *
     * @return String
     */
    public String getXPriv() {
        if (aKey.hasPrivKey()) {
            return aKey.serializePrivB58(networkParameters);
        } else {
            return null;
        }
    }

    /**
     * Return id for this account.
     *
     * @return int
     */
    public int getId() {
        return aID;
    }

    /**
     * Return receive chain this account.
     *
     * @return HD_Chain
     */
    public HDChain getReceive() {
        return hdChains.get(0);
    }

    /**
     * Return change chain this account.
     *
     * @return HD_Chain
     */
    public HDChain getChange() {
        return hdChains.get(1);
    }

    /**
     * Return chain for this account as indicated by index: 0 = receive, 1 = change.
     *
     * @return HD_Chain
     */
    public HDChain getChain(int idx) {
        if (idx < 0 || idx > 1) {
            return null;
        }
        return hdChains.get(idx);
    }

    /**
     * Return BIP44 path for this account (m / purpose' / coin_type' / account').
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

}