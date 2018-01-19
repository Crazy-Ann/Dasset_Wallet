package com.dasset.wallet.core.wallet.hd.crypto;

// SubKeystore - stores data in its backing but prefixes all Ids with its own subId
// Use it to store unrelated HD-Accounts in one backing
public class SecureSubKeyValueStore extends SecureKeyValueStore {

    private final int subId;

    public SecureSubKeyValueStore(SecureKeyValueStoreBacking backing, RandomSource randomSource, int subId) {
        super(backing, randomSource);
        this.subId = subId;
    }

    @Override
    protected synchronized byte[] getValue(byte[] realId) {
        return secureKeyValueStoreBacking.getValue(realId, subId);
    }

    @Override
    protected synchronized void setValue(byte[] realId, byte[] value) {
        secureKeyValueStoreBacking.setValue(realId, subId, value);
    }

    public int getSubId() {
        return subId;
    }

    public void deleteAllData() {
        secureKeyValueStoreBacking.deleteSubStorageId(subId);
    }
}
