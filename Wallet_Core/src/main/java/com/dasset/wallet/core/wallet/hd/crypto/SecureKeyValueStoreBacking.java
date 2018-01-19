package com.dasset.wallet.core.wallet.hd.crypto;

public interface SecureKeyValueStoreBacking {
    /**
     * Get the plaintext value of a specified id.
     *
     * @param id The ID to get the value for
     *
     * @return The plaintext value associated with the ID or null if no plaintext value was associated
     */
    byte[] getValue(byte[] id);

    byte[] getValue(byte[] id, int subId);

    /**
     * Store the plaintext value for a given ID.
     * <p/>
     * If another plaintext value is stored under the same ID  it is overwritten
     *
     * @param id             The id to store a value under
     * @param plaintextValue The value to store
     */
    void setValue(byte[] id, byte[] plaintextValue);

    int getMaxSubId();

    void setValue(byte[] key, int subId, byte[] value);

    /**
     * Delete the plaintext value for a given ID.
     *
     * @param id the ID of the value to delete
     */
    void deleteValue(byte[] id);


    /**
     * Deletes all associated entries for this subId
     *
     * @param subId the subId to delete
     */
    void deleteSubStorageId(int subId);
}
