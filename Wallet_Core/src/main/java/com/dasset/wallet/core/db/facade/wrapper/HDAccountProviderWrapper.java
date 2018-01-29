package com.dasset.wallet.core.db.facade.wrapper;

import com.dasset.wallet.core.contant.db.HDAccountColumns;
import com.dasset.wallet.core.crypto.PasswordSeed;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.IHDAccountProvider;
import com.dasset.wallet.core.exception.AddressFormatException;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.Utils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;

import javax.annotation.Nullable;

public abstract class HDAccountProviderWrapper extends ProviderWrapper implements IHDAccountProvider {

    @Override
    public String getHDFirstAddress(int hdSeedId) {
        String sql = "select hd_address from hd_account where hd_account_id=?";
        final String[] address = {null};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(ICursor iCursor) {
                int idColumn = iCursor.getColumnIndex(HDAccountColumns.HD_ADDRESS);
                if (idColumn != -1) {
                    address[0] = iCursor.getString(idColumn);
                }
                return null;
            }
        });
        return address[0];
    }

    @Override
    public int addHDAccount(String encryptedMnemonicSeed, String encryptSeed, String firstAddress, boolean isXrandom, String addressOfPS, byte[] externalPublicKey, byte[] internalPublicKey) {
        if (this.isPublicKeyExist(externalPublicKey, internalPublicKey)) {
            return -1;
        }
        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        int hdAccountId = this.insertHDAccountToDb(writeDb, encryptedMnemonicSeed, encryptSeed, firstAddress, isXrandom, externalPublicKey, internalPublicKey);
        if (!this.hasPasswordSeed(writeDb) && !Utils.isEmpty(addressOfPS)) {
            this.addPasswordSeed(writeDb, new PasswordSeed(addressOfPS, encryptedMnemonicSeed));
        }
        writeDb.endTransaction();
        return hdAccountId;
    }

    protected abstract int insertHDAccountToDb(IDb db, String encryptedMnemonicSeed, String encryptSeed, String firstAddress, boolean isXrandom, byte[] externalPublicKey, byte[] internalPublicKey);

    protected abstract boolean hasPasswordSeed(IDb db);

    protected abstract void addPasswordSeed(IDb db, PasswordSeed passwordSeed);

    @Override
    public int addMonitoredHDAccount(String firstAddress, boolean isXrandom, byte[] externalPublicKey, byte[] internalPublicKey) {
        if (this.isPublicKeyExist(externalPublicKey, internalPublicKey)) {
            return -1;
        }
        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        int hdAccountId = this.insertMonitorHDAccountToDb(writeDb, firstAddress, isXrandom, externalPublicKey, internalPublicKey);
        writeDb.endTransaction();
        return hdAccountId;
    }

    protected abstract int insertMonitorHDAccountToDb(IDb db, String firstAddress, boolean isXrandom, byte[] externalPub, byte[] internalPub);

    @Override
    public boolean hasMnemonicSeed(int hdAccountId) {
        String sql = "select count(0) cnt from hd_account where encrypt_mnemonic_seed is not null and hd_account_id=?";
        final boolean[] result = {false};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("cnt");
                if (idColumn != -1) {
                    result[0] = iCursor.getInt(idColumn) > 0;
                }
                return null;
            }
        });
        return result[0];
    }

    @Override
    public byte[] getExternalPublicKey(int hdSeedId) {
        final byte[][] pub = {null};
        String sql = "select external_pub from hd_account where hd_account_id=?";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountColumns.EXTERNAL_PUB);
                if (idColumn != -1) {
                    String pubStr = iCursor.getString(idColumn);
                    try {
                        pub[0] = Base58.decode(pubStr);
                    } catch (AddressFormatException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
        return pub[0];
    }

    @Override
    public byte[] getInternalPublicKey(int hdSeedId) {
        final byte[][] publicKeys = {null};
        String sql = "select internal_pub from hd_account where hd_account_id=? ";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountColumns.INTERNAL_PUB);
                if (idColumn != -1) {
                    String publicKey = iCursor.getString(idColumn);
                    try {
                        publicKeys[0] = Base58.decode(publicKey);
                    } catch (AddressFormatException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
        return publicKeys[0];
    }


    @Override
    public String getHDAccountEncryptSeed(int hdSeedId) {
        final String[] hdAccountEncryptSeed = {null};
        String sql = "select encrypt_seed from hd_account where hd_account_id=? ";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountColumns.ENCRYPT_SEED);
                if (idColumn != -1) {
                    hdAccountEncryptSeed[0] = iCursor.getString(idColumn);
                }
                return null;
            }
        });
        return hdAccountEncryptSeed[0];
    }

    @Override
    public String getHDAccountEncryptMnemonicSeed(int hdSeedId) {
        final String[] hdAccountMnmonicEncryptSeed = {null};
        String sql = "select encrypt_mnemonic_seed from hd_account where hd_account_id=? ";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountColumns.ENCRYPT_MNMONIC_SEED);
                if (idColumn != -1) {
                    hdAccountMnmonicEncryptSeed[0] = iCursor.getString(idColumn);
                }
                return null;
            }
        });
        return hdAccountMnmonicEncryptSeed[0];
    }

    @Override
    public boolean hdAccountIsXRandom(int seedId) {
        final boolean[] result = {false};
        String sql = "select is_xrandom from hd_account where hd_account_id=?";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(seedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountColumns.IS_XRANDOM);
                if (idColumn != -1) {
                    result[0] = iCursor.getInt(idColumn) == 1;
                }
                return null;
            }
        });
        return result[0];
    }

    @Override
    public List<Integer> getHDAccountSeeds() {
        final List<Integer> hdSeedIds = Lists.newArrayList();
        String sql = "select hd_account_id from hd_account";
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                hdSeedIds.add(iCursor.getInt(0));
                return null;
            }
        });
        return hdSeedIds;
    }

    @Override
    public boolean isPublicKeyExist(byte[] externalPublicKey, byte[] internalPublicKey) {
        String sql = "select count(0) cnt from hd_account where external_pub=? or internal_pub=?";
        final boolean[] isExist = {false};
        this.execQueryOneRecord(sql, new String[]{Base58.encode(externalPublicKey), Base58.encode(internalPublicKey)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                isExist[0] = iCursor.getInt(0) > 0;
                return null;
            }
        });
        return isExist[0];
    }
}
