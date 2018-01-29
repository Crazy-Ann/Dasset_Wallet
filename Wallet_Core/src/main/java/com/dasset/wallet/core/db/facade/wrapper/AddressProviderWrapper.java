package com.dasset.wallet.core.db.facade.wrapper;

import com.dasset.wallet.core.Address;
import com.dasset.wallet.core.contant.db.AddressesColumns;
import com.dasset.wallet.core.contant.db.AliasColumns;
import com.dasset.wallet.core.contant.db.EnterpriseHDAccountColumns;
import com.dasset.wallet.core.contant.db.HDAccountColumns;
import com.dasset.wallet.core.contant.db.HDMAddressesColumns;
import com.dasset.wallet.core.contant.db.HDMBIdColumns;
import com.dasset.wallet.core.contant.db.HDSeedsColumns;
import com.dasset.wallet.core.contant.db.PasswordSeedColumns;
import com.dasset.wallet.core.contant.db.VanityAddressColumns;
import com.dasset.wallet.core.crypto.EncryptedData;
import com.dasset.wallet.core.crypto.PasswordSeed;
import com.dasset.wallet.core.db.facade.IAddressProvider;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.exception.AddressFormatException;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.Utils;
import com.dasset.wallet.core.wallet.hd.HDMAddress;
import com.dasset.wallet.core.wallet.hd.HDMBId;
import com.dasset.wallet.core.wallet.hd.HDMKeychain;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public abstract class AddressProviderWrapper extends ProviderWrapper implements IAddressProvider {

    @Override
    public boolean changePassword(CharSequence oldPassword, CharSequence newPassword) {
        IDb readDb = this.getReadableDatabase();
        final HashMap<String, String> addressPrivateKeyHashMap = Maps.newHashMap();
        String sql = "select address,encrypt_private_key,pub_key,is_xrandom from addresses where encrypt_private_key is not null";
        this.execQueryLoop(readDb, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(ICursor iCursor) {
                String address = iCursor.getString(0);
                String encryptPrivateKey = iCursor.getString(1);
                boolean isCompress = true;
                try {
                    byte[] pubKey = Base58.decode(iCursor.getString(2));
                    isCompress = pubKey.length == 33;
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
                int isXRandom = iCursor.getInt(3);
                addressPrivateKeyHashMap.put(address, new EncryptedData(encryptPrivateKey).toEncryptedStringForQRCode(isCompress, isXRandom == 1));
                return null;
            }
        });

        final String[] hdmEncryptPassword = {null};
        sql = "select encrypt_bither_password from hdm_bid limit 1";
        this.execQueryOneRecord(readDb, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                hdmEncryptPassword[0] = iCursor.getString(0);
                return null;
            }
        });

        final HashMap<Integer, String> encryptMenmonicSeedHashMap = Maps.newHashMap();
        final HashMap<Integer, String> encryptHDSeedHashMap = Maps.newHashMap();
        final HashMap<Integer, String> singularModeBackupHashMap = Maps.newHashMap();
        sql = "select hd_seed_id,encrypt_seed,encrypt_hd_seed,singular_mode_backup from hd_seeds where encrypt_seed!='RECOVER'";
        this.execQueryLoop(readDb, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                Integer hdSeedId = iCursor.getInt(0);
                String encryptSeed = iCursor.getString(1);
                if (!iCursor.isNull(2)) {
                    String encryptHDSeed = iCursor.getString(2);
                    encryptHDSeedHashMap.put(hdSeedId, encryptHDSeed);
                }
                if (!iCursor.isNull(3)) {
                    String singularModeBackup = iCursor.getString(3);
                    singularModeBackupHashMap.put(hdSeedId, singularModeBackup);
                }
                encryptMenmonicSeedHashMap.put(hdSeedId, encryptSeed);
                return null;
            }
        });

        final HashMap<Integer, String> hdEncryptSeedHashMap = Maps.newHashMap();
        final HashMap<Integer, String> hdEncryptMnemonicSeedHashMap = Maps.newHashMap();
        sql = "select hd_account_id,encrypt_seed,encrypt_mnemonic_seed from hd_account where encrypt_mnemonic_seed is not null";
        this.execQueryLoop(readDb, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountColumns.HD_ACCOUNT_ID);
                Integer hdAccountId = 0;
                if (idColumn != -1) {
                    hdAccountId = iCursor.getInt(idColumn);
                }
                idColumn = iCursor.getColumnIndex(HDAccountColumns.ENCRYPT_SEED);
                if (idColumn != -1) {
                    String encryptSeed = iCursor.getString(idColumn);
                    hdEncryptSeedHashMap.put(hdAccountId, encryptSeed);
                }
                idColumn = iCursor.getColumnIndex(HDAccountColumns.ENCRYPT_MNMONIC_SEED);
                if (idColumn != -1) {
                    String encryptHDSeed = iCursor.getString(idColumn);
                    hdEncryptMnemonicSeedHashMap.put(hdAccountId, encryptHDSeed);
                }
                return null;
            }
        });

        final HashMap<Integer, String> enterpriseHDEncryptSeedHashMap = Maps.newHashMap();
        final HashMap<Integer, String> enterpriseHDEncryptMnemonicSeedHashMap = Maps.newHashMap();
        sql = "select hd_account_id,encrypt_seed,encrypt_mnemonic_seed from enterprise_hd_account";
        this.execQueryLoop(readDb, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(EnterpriseHDAccountColumns.HD_ACCOUNT_ID);
                Integer hdAccountId = 0;
                if (idColumn != -1) {
                    hdAccountId = iCursor.getInt(idColumn);
                }
                idColumn = iCursor.getColumnIndex(EnterpriseHDAccountColumns.ENCRYPT_SEED);
                if (idColumn != -1) {
                    String encryptSeed = iCursor.getString(idColumn);
                    if (!Utils.isEmpty(encryptSeed)) {
                        enterpriseHDEncryptSeedHashMap.put(hdAccountId, encryptSeed);
                    }
                }
                idColumn = iCursor.getColumnIndex(EnterpriseHDAccountColumns.ENCRYPT_MNEMONIC_SEED);
                if (idColumn != -1) {
                    String encryptHDMnemonicSeed = iCursor.getString(idColumn);
                    if (Utils.isEmpty(encryptHDMnemonicSeed)) {
                        enterpriseHDEncryptMnemonicSeedHashMap.put(hdAccountId, encryptHDMnemonicSeed);
                    }
                }
                return null;
            }
        });

        final PasswordSeed[] passwordSeeds = {null};
        sql = "select password_seed from password_seed limit 1";
        this.execQueryLoop(readDb, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                passwordSeeds[0] = new PasswordSeed(iCursor.getString(0));
                return null;
            }
        });

        for (Map.Entry<String, String> entry : addressPrivateKeyHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePasswordKeepFlag(entry.getValue(), oldPassword, newPassword));
        }
        if (hdmEncryptPassword[0] != null) {
            hdmEncryptPassword[0] = EncryptedData.changePassword(hdmEncryptPassword[0], oldPassword, newPassword);
        }
        for (Map.Entry<Integer, String> entry : encryptMenmonicSeedHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePassword(entry.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> entry : encryptHDSeedHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePassword(entry.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> entry : hdEncryptSeedHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePassword(entry.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> entry : hdEncryptMnemonicSeedHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePassword(entry.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> entry : enterpriseHDEncryptSeedHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePassword(entry.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> entry : enterpriseHDEncryptMnemonicSeedHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePassword(entry.getValue(), oldPassword, newPassword));
        }
        for (Map.Entry<Integer, String> entry : singularModeBackupHashMap.entrySet()) {
            entry.setValue(EncryptedData.changePassword(entry.getValue(), oldPassword, newPassword));
        }
        if (passwordSeeds[0] != null) {
            return passwordSeeds[0].changePassword(oldPassword, newPassword);
        }

        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        sql = "update addresses set encrypt_private_key=? where address=?";
        for (Map.Entry<String, String> entry : addressPrivateKeyHashMap.entrySet()) {
            this.execUpdate(writeDb, sql, new String[]{entry.getValue(), entry.getKey()});
        }
        sql = "update hdm_bid set encrypt_bither_password=?";
        if (hdmEncryptPassword[0] != null) {
            this.execUpdate(writeDb, sql, new String[]{hdmEncryptPassword[0]});
        }
        String sql1 = "update hd_seeds set encrypt_seed=? ";
        String sql2 = " where hd_seed_id=?";
        for (Map.Entry<Integer, String> entry : encryptMenmonicSeedHashMap.entrySet()) {
            ArrayList<String> params = Lists.newArrayList();
            params.add(entry.getValue());
            sql = sql1;
            if (encryptHDSeedHashMap.containsKey(entry.getKey())) {
                sql += ",encrypt_hd_seed=?";
                params.add(encryptHDSeedHashMap.get(entry.getKey()));
            }
            if (singularModeBackupHashMap.containsKey(entry.getKey())) {
                sql += ",singular_mode_backup=?";
                params.add(singularModeBackupHashMap.get(entry.getKey()));
            }
            sql += sql2;
            params.add(Integer.toString(entry.getKey()));
            this.execUpdate(writeDb, sql, params.toArray(new String[params.size()]));
        }

        sql1 = "update hd_account set encrypt_seed=? ";
        sql2 = " where hd_account_id=?";
        for (Map.Entry<Integer, String> entry : hdEncryptSeedHashMap.entrySet()) {
            ArrayList<String> params = Lists.newArrayList();
            sql = sql1;
            params.add(entry.getValue());
            if (hdEncryptMnemonicSeedHashMap.containsKey(entry.getKey())) {
                sql += ",encrypt_mnemonic_seed=?";
                params.add(hdEncryptMnemonicSeedHashMap.get(entry.getKey()));
            }
            sql += sql2;
            params.add(Integer.toString(entry.getKey()));
            this.execUpdate(writeDb, sql, params.toArray(new String[params.size()]));
        }

        sql1 = "update enterprise_hd_account set encrypt_seed=? ";
        sql2 = " where hd_account_id=?";
        for (Map.Entry<Integer, String> entry : enterpriseHDEncryptSeedHashMap.entrySet()) {
            ArrayList<String> params = Lists.newArrayList();
            sql = sql1;
            params.add(entry.getValue());
            if (enterpriseHDEncryptMnemonicSeedHashMap.containsKey(entry.getKey())) {
                sql += ",encrypt_mnemonic_seed=?";
                params.add(enterpriseHDEncryptMnemonicSeedHashMap.get(entry.getKey()));
            }
            sql += sql2;
            params.add(Integer.toString(entry.getKey()));
            this.execUpdate(writeDb, sql, params.toArray(new String[params.size()]));
        }

        sql = "update password_seed set password_seed=?";
        if (passwordSeeds[0] != null) {
            this.execUpdate(writeDb, sql, new String[]{passwordSeeds[0].toPasswordSeedString()});
        }
        writeDb.endTransaction();
        return true;
    }

    @Override
    public PasswordSeed getPasswordSeed() {
        final PasswordSeed[] passwordSeeds = {null};
        String sql = "select password_seed from password_seed limit 1";
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                passwordSeeds[0] = applyPasswordSeed(iCursor);
                return null;
            }
        });
        return passwordSeeds[0];
    }

    public boolean hasPasswordSeed(IDb db) {
        String sql = "select count(0) cnt from password_seed where password_seed is not null";
        final int[] count = {0};
        this.execQueryOneRecord(db, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("cnt");
                if (idColumn != -1) {
                    count[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        return count[0] > 0;
    }

    @Override
    public boolean hasPasswordSeed() {
        return this.hasPasswordSeed(this.getReadableDatabase());
    }

    public void addPasswordSeed(IDb db, PasswordSeed passwordSeed) {
        if (!Utils.isEmpty(passwordSeed.toPasswordSeedString())) {
            String sql = "insert into password_seed (password_seed) values (?)";
            this.execUpdate(db, sql, new String[]{passwordSeed.toPasswordSeedString()});
        }
    }

    @Override
    public List<Integer> getHDSeeds() {
        final List<Integer> hdSeedIds = Lists.newArrayList();
        String sql = "select hd_seed_id from hd_seeds";
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDSeedsColumns.HD_SEED_ID);
                if (idColumn != -1) {
                    hdSeedIds.add(iCursor.getInt(idColumn));
                }
                return null;
            }
        });
        return hdSeedIds;
    }

    @Override
    public String getEncryptMnemonicSeed(int hdSeedId) {
        final String[] encryptSeed = {null};
        String sql = "select encrypt_seed from hd_seeds where hd_seed_id=?";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                encryptSeed[0] = iCursor.getString(0);
                return null;
            }
        });
        return encryptSeed[0];
    }

    @Override
    public String getEncryptHDSeed(int hdSeedId) {
        final String[] encryptHDSeed = {null};
        String sql = "select encrypt_hd_seed from hd_seeds where hd_seed_id=?";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                encryptHDSeed[0] = iCursor.getString(0);
                return null;
            }
        });
        return encryptHDSeed[0];
    }


    @Override
    public void updateEncryptedMnmonicSeed(int hdSeedId, String encryptMnmonicSeed) {
        String sql = "update hd_seeds set encrypt_hd_seed=? where hd_seed_id=?";
        this.execUpdate(sql, new String[]{encryptMnmonicSeed, Integer.toString(hdSeedId)});
    }


    @Override
    public boolean isHDSeedFromXRandom(int hdSeedId) {
        String sql = "select is_xrandom from hd_seeds where hd_seed_id=?";
        final boolean[] isXRandom = {false};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("is_xrandom");
                if (idColumn != -1) {
                    isXRandom[0] = iCursor.getInt(idColumn) == 1;
                }
                return null;
            }
        });
        return isXRandom[0];
    }


    @Override
    public String getHDMFristAddress(int hdSeedId) {
        String sql = "select hdm_address from hd_seeds where hd_seed_id=?";
        final String[] address = {null};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDSeedsColumns.HDM_ADDRESS);
                if (idColumn != -1) {
                    address[0] = iCursor.getString(idColumn);
                }
                return null;
            }
        });
        return address[0];
    }

    @Override
    public String getSingularModeBackup(int hdSeedId) {
        String sql = "select singular_mode_backup from hd_seeds where hd_seed_id=?";
        final String[] singularModeBackup = {null};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                singularModeBackup[0] = iCursor.getString(0);
                return null;
            }
        });
        return singularModeBackup[0];
    }

    @Override
    public void setSingularModeBackup(int hdSeedId, String singularModeBackup) {
        String sql = "update hd_seeds set singular_mode_backup=? where hd_seed_id=?";
        this.execUpdate(sql, new String[]{singularModeBackup, Integer.toString(hdSeedId)});
    }

    @Override
    public int addHDKey(String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom, String addressOfPS) {
        IDb db = this.getWritableDatabase();
        db.beginTransaction();
        int seedId = this.insertHDKeyToDb(db, encryptedMnemonicSeed, encryptHdSeed, firstAddress, isXrandom);
        if (!hasPasswordSeed(db) && !Utils.isEmpty(addressOfPS)) {
            this.addPasswordSeed(db, new PasswordSeed(addressOfPS, encryptedMnemonicSeed));
        }
        db.endTransaction();
        return seedId;
    }

    protected abstract int insertHDKeyToDb(IDb db, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom);

    @Override
    public int addEnterpriseHDKey(String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom, String addressOfPS) {
        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        int seedId = this.insertEnterpriseHDKeyToDb(writeDb, encryptedMnemonicSeed, encryptHdSeed, firstAddress, isXrandom);
        if (!hasPasswordSeed(writeDb) && !Utils.isEmpty(addressOfPS)) {
            addPasswordSeed(writeDb, new PasswordSeed(addressOfPS, encryptedMnemonicSeed));
        }
        writeDb.endTransaction();
        return seedId;
    }

    protected abstract int insertEnterpriseHDKeyToDb(IDb db, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom);

    @Override
    public HDMBId getHDMBId() {
        String sql = "select hdm_bid,encrypt_bither_password from hdm_bid";
        HDMBId hdmbId = null;
        final String[] address = {null};
        final String[] encryptPassword = {null};
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDMBIdColumns.HDM_BID);
                if (idColumn != -1) {
                    address[0] = iCursor.getString(idColumn);
                }
                idColumn = iCursor.getColumnIndex(HDMBIdColumns.ENCRYPT_BITHER_PASSWORD);
                if (idColumn != -1) {
                    encryptPassword[0] = iCursor.getString(idColumn);
                }
                return null;
            }
        });

        if (!Utils.isEmpty(address[0]) && !Utils.isEmpty(encryptPassword[0])) {
            hdmbId = new HDMBId(address[0], encryptPassword[0]);
        }
        return hdmbId;
    }


    @Override
    public void addAndUpdateHDMBId(HDMBId hdmBid, String addressOfPS) {
        String sql = "select count(0) from hdm_bid";
        final boolean[] isExist = {true};
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                isExist[0] = iCursor.getInt(0) > 0;
                return null;
            }
        });
        if (!isExist[0]) {
            String encryptedPasswordString = hdmBid.getEncryptedBitherPasswordString();
            IDb writeDb = this.getWritableDatabase();
            sql = "insert into hdm_bid(hdm_bid,encrypt_bither_password) values(?,?)";
            writeDb.beginTransaction();
            this.execUpdate(writeDb, sql, new String[]{hdmBid.getAddress(), encryptedPasswordString});
            if (!hasPasswordSeed(writeDb) && !Utils.isEmpty(addressOfPS)) {
                addPasswordSeed(writeDb, new PasswordSeed(addressOfPS, encryptedPasswordString));
            }
            writeDb.endTransaction();
        } else {
            String encryptedPasswordString = hdmBid.getEncryptedBitherPasswordString();
            IDb writeDb = this.getWritableDatabase();
            sql = "update hdm_bid set encrypt_bither_password=? where hdm_bid=?";
            writeDb.beginTransaction();
            ;
            this.execUpdate(writeDb, sql, new String[]{encryptedPasswordString, hdmBid.getAddress()});
            if (!hasPasswordSeed(writeDb) && !Utils.isEmpty(addressOfPS)) {
                addPasswordSeed(writeDb, new PasswordSeed(addressOfPS, encryptedPasswordString));
            }
            writeDb.endTransaction();
        }
    }

    @Override
    public List<HDMAddress> getHDMAddressInUse(HDMKeychain keychain) {
        String sql = "select hd_seed_index,pub_key_hot,pub_key_cold,pub_key_remote,address,is_synced " +
                " from hdm_addresses " +
                " where hd_seed_id=? and address is not null order by hd_seed_index";
        final List<HDMAddress> addresses = Lists.newArrayList();
        final HDMKeychain hdmKeychain = keychain;
        this.execQueryLoop(sql, new String[]{Integer.toString(keychain.getHdSeedId())}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                HDMAddress hdmAddress = applyHDMAddress(iCursor, hdmKeychain);
                if (hdmAddress != null) {
                    addresses.add(hdmAddress);
                }
                return null;
            }
        });
        return addresses;
    }


    @Override
    public void prepareHDMAddresses(int hdSeedId, List<HDMAddress.Pubs> pubsList) {
        String sql = "select count(0) from hdm_addresses where hd_seed_id=? and hd_seed_index=?";
        final boolean[] isExist = {false};
        for (HDMAddress.Pubs pubs : pubsList) {
            this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId), Integer.toString(pubs.index)}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    isExist[0] |= iCursor.getInt(0) > 0;
                    return null;
                }
            });
            if (!isExist[0]) {
                break;
            }
        }
        if (!isExist[0]) {
            IDb writeDb = this.getWritableDatabase();
            writeDb.beginTransaction();
            for (int i = 0; i < pubsList.size(); i++) {
                HDMAddress.Pubs pubs = pubsList.get(i);
                this.insertHDMAddressToDb(writeDb, null, hdSeedId, pubs.index, pubs.hot, pubs.cold, null, false);
            }
            writeDb.endTransaction();
        }
    }

    protected abstract void insertHDMAddressToDb(IDb db, String address, int hdSeedId, int index, byte[] pubKeysHot, byte[] pubKeysCold, byte[] pubKeysRemote, boolean isSynced);

    @Override
    public List<HDMAddress.Pubs> getUncompletedHDMAddressPubs(int hdSeedId, int count) {
        String sql = "select * from hdm_addresses where hd_seed_id=? and pub_key_remote is null limit ? ";
        final List<HDMAddress.Pubs> pubs = Lists.newArrayList();
        this.execQueryLoop(sql, new String[]{Integer.toString(hdSeedId), Integer.toString(count)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                pubs.add(applyPubs(iCursor));
                return null;
            }
        });
        return pubs;
    }

    @Override
    public int maxHDMAddressPubIndex(int hdSeedId) {
        String sql = "select ifnull(max(hd_seed_index),-1)  hd_seed_index from hdm_addresses where hd_seed_id=?  ";
        final int[] maxIndex = {-1};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDMAddressesColumns.HD_SEED_INDEX);
                if (idColumn != -1) {
                    maxIndex[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        return maxIndex[0];
    }

    @Override
    public int uncompletedHDMAddressCount(int hdSeedId) {
        String sql = "select count(0) cnt from hdm_addresses where hd_seed_id=?  and pub_key_remote is null ";
        final int[] count = {0};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("cnt");
                if (idColumn != -1) {
                    count[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        return count[0];
    }

    @Override
    public void setHDMPubsRemote(int hdSeedId, int index, byte[] remote) {
        String sql = "select count(0) from hdm_addresses " +
                "where hd_seed_id=? and hd_seed_index=? and pub_key_remote is null";
        final boolean[] isExist = {true};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId), Integer.toString(index)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                isExist[0] = iCursor.getInt(0) > 0;
                return null;
            }
        });
        if (isExist[0]) {
            sql = "update hdm_addresses set pub_key_remote=? where hd_seed_id=? and hd_seed_index=?";
            this.execUpdate(sql, new String[]{Base58.encode(remote), Integer.toString(hdSeedId), Integer.toString(index)});
        }
    }

    @Override
    public void completeHDMAddresses(int hdSeedId, List<HDMAddress> addresses) {
        String sql = "select count(0) from hdm_addresses " +
                "where hd_seed_id=? and hd_seed_index=? and pub_key_remote is null";
        final boolean[] isExist = {true};
        for (HDMAddress address : addresses) {
            this.execQueryOneRecord(sql, new String[]{Integer.toString(hdSeedId), Integer.toString(address.getIndex())}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    isExist[0] &= iCursor.getInt(0) > 0;
                    return null;
                }
            });
            if (!isExist[0]) {
                break;
            }
        }
        if (isExist[0]) {
            IDb writeDb = this.getWritableDatabase();
            writeDb.beginTransaction();
            sql = "update hdm_addresses set pub_key_remote=?,address=? where hd_seed_id=? and hd_seed_index=?";
            for (int i = 0; i < addresses.size(); i++) {
                HDMAddress hdmAddress = addresses.get(i);
                this.execUpdate(writeDb, sql, new String[]{Base58.encode(hdmAddress.getPubRemote()), hdmAddress.getAddress(), Integer.toString(hdSeedId), Integer.toString(hdmAddress.getIndex())});
            }
            writeDb.endTransaction();
        }
    }

    @Override
    public void recoverHDMAddresses(int hdSeedId, List<HDMAddress> addresses) {
        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        for (int i = 0; i < addresses.size(); i++) {
            HDMAddress hdmAddress = addresses.get(i);
            this.insertHDMAddressToDb(writeDb, hdmAddress.getAddress(), hdSeedId, hdmAddress.getIndex(), hdmAddress.getPubHot(), hdmAddress.getPubCold(), hdmAddress.getPubRemote(), false);
        }
        writeDb.endTransaction();
    }

    @Override
    public void syncComplete(int hdSeedId, int hdSeedIndex) {
        String sql = "update hdm_addresses set is_synced=? where hd_seed_id=? and hd_seed_index=?";
        this.execUpdate(sql, new String[]{"1", Integer.toString(hdSeedId), Integer.toString(hdSeedIndex)});
    }

    //normal
    @Override
    public List<Address> getAddresses() {
        String sql = "select address,encrypt_private_key,pub_key,is_xrandom,is_trash,is_synced,sort_time " +
                "from addresses  order by sort_time desc";
        final List<Address> addresses = Lists.newArrayList();
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                try {
                    Address address = applyAddressCursor(iCursor);
                    if (address != null) {
                        addresses.add(address);
                    }
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return addresses;
    }

    @Override
    public String getEncryptPrivateKey(String address) {
        String sql = "select encrypt_private_key from addresses where address=?";
        final String[] encryptPrivateKey = {null};
        this.execQueryOneRecord(sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(AddressesColumns.ENCRYPT_PRIVATE_KEY);
                if (idColumn != -1) {
                    encryptPrivateKey[0] = iCursor.getString(idColumn);
                }
                return null;
            }
        });
        return encryptPrivateKey[0];
    }

    @Override
    public void addAddress(Address address) {
        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        this.insertAddressToDb(writeDb, address);
        if (address.hasPrivateKey()) {
            if (!hasPasswordSeed(writeDb)) {
                PasswordSeed passwordSeed = new PasswordSeed(address.getAddress(), address.getFullEncryptPrivKeyOfDb());
                addPasswordSeed(writeDb, passwordSeed);
            }
        }
        writeDb.endTransaction();
    }

    protected abstract void insertAddressToDb(IDb db, Address address);

    @Override
    public void removeWatchOnlyAddress(Address address) {
        String sql = "delete from addresses where address=? and encrypt_private_key is null";
        this.execUpdate(sql, new String[]{address.getAddress()});
    }


    @Override
    public void trashPrivateKeyAddress(Address address) {
        String sql = "update addresses set is_trash=? where address=?";
        this.execUpdate(sql, new String[]{"1", address.getAddress()});
    }

    @Override
    public void restorePrivateKeyAddress(Address address) {
        String sql = "update addresses set is_trash=?,sort_time=?,is_synced=? where address=?";
        this.execUpdate(sql, new String[]{"0", Long.toString(address.getSortTime()), "0", address.getAddress()});
    }

    @Override
    public void updateSyncComplete(Address address) {
        String sql = "update addresses set is_synced=? where address=?";
        this.execUpdate(sql, new String[]{address.isSyncComplete() ? "1" : "0", address.getAddress()});
    }

    @Override
    public void updatePrivateKey(String address, String encryptPriv) {
        String sql = "update addresses set encrypt_private_key=? where address=?";
        this.execUpdate(sql, new String[]{encryptPriv, address});
    }

    @Override
    public Map<String, String> getAliases() {
        String sql = "select * from aliases";
        final Map<String, String> aliases = Maps.newHashMap();
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(AliasColumns.ADDRESS);
                String address = null;
                String alias = null;
                if (idColumn > -1) {
                    address = iCursor.getString(idColumn);
                }
                idColumn = iCursor.getColumnIndex(AliasColumns.ALIAS);
                if (idColumn > -1) {
                    alias = iCursor.getString(idColumn);
                }
                aliases.put(address, alias);
                return null;
            }
        });
        return aliases;
    }

    @Override
    public void updateAlias(String address, @Nullable String alias) {
        if (alias == null) {
            String sql = "delete from aliases where address=?";
            this.execUpdate(sql, new String[]{address});
        } else {
            String sql = "insert or replace into aliases(address,alias) values(?,?)";
            this.execUpdate(sql, new String[]{address, alias});
        }
    }

    @Override
    public Map<String, Integer> getVanitylens() {
        String sql = "select * from vanity_address";
        final Map<String, Integer> vanityLenMap = Maps.newHashMap();
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(VanityAddressColumns.ADDRESS);
                String address = null;
                int alias = Address.VANITY_LEN_NO_EXSITS;
                if (idColumn > -1) {
                    address = iCursor.getString(idColumn);
                }
                idColumn = iCursor.getColumnIndex(VanityAddressColumns.VANITY_LEN);
                if (idColumn > -1) {
                    alias = iCursor.getInt(idColumn);
                }
                vanityLenMap.put(address, alias);
                return null;
            }
        });
        return vanityLenMap;
    }

    @Override
    public void updateVaitylen(String address, int vanitylen) {
        if (vanitylen == Address.VANITY_LEN_NO_EXSITS) {
            String sql = "delete from vanity_address where address=?";
            this.execUpdate(sql, new String[]{address});
        } else {
            String sql = "insert or replace into vanity_address(address,vanity_len) values(?,?)";
            this.execUpdate(sql, new String[]{address, Integer.toString(vanitylen)});
        }
    }

    private HDMAddress applyHDMAddress(ICursor iCursor, HDMKeychain hdmKeychain) {
        HDMAddress hdmAddress;
        String address = null;
        boolean isSynced = false;
        int idColumn = iCursor.getColumnIndex(HDMAddressesColumns.ADDRESS);
        if (idColumn != -1) {
            address = iCursor.getString(idColumn);
        }
        idColumn = iCursor.getColumnIndex(HDMAddressesColumns.IS_SYNCED);
        if (idColumn != -1) {
            isSynced = iCursor.getInt(idColumn) == 1;
        }
        hdmAddress = new HDMAddress(applyPubs(iCursor), address, isSynced, hdmKeychain);
        return hdmAddress;
    }

    public PasswordSeed applyPasswordSeed(ICursor iCursor) {
        int idColumn = iCursor.getColumnIndex(PasswordSeedColumns.PASSWORD_SEED);
        String passwordSeed = null;
        if (idColumn != -1) {
            passwordSeed = iCursor.getString(idColumn);
        }
        if (Utils.isEmpty(passwordSeed)) {
            return null;
        }
        return new PasswordSeed(passwordSeed);
    }

    private HDMAddress.Pubs applyPubs(ICursor iCursor) {
        int hdSeedIndex = 0;
        byte[] hot = null;
        byte[] cold = null;
        byte[] remote = null;
        int idColumn = iCursor.getColumnIndex(HDMAddressesColumns.HD_SEED_INDEX);
        if (idColumn != -1) {
            hdSeedIndex = iCursor.getInt(idColumn);
        }
        idColumn = iCursor.getColumnIndex(HDMAddressesColumns.PUB_KEY_HOT);
        if (idColumn != -1 && !iCursor.isNull(idColumn)) {
            try {
                hot = Base58.decode(iCursor.getString(idColumn));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(HDMAddressesColumns.PUB_KEY_COLD);
        if (idColumn != -1 && !iCursor.isNull(idColumn)) {
            try {
                cold = Base58.decode(iCursor.getString(idColumn));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(HDMAddressesColumns.PUB_KEY_REMOTE);
        if (idColumn != -1 && !iCursor.isNull(idColumn)) {
            try {
                remote = Base58.decode(iCursor.getString(idColumn));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        return new HDMAddress.Pubs(hot, cold, remote, hdSeedIndex);
    }

    private Address applyAddressCursor(ICursor iCursor) throws AddressFormatException {
        int idColumn = iCursor.getColumnIndex(AddressesColumns.ADDRESS);
        String addressStr = null;
        String encryptPrivateKey = null;
        byte[] pubKey = null;
        boolean isXRandom = false;
        boolean isSynced = false;
        boolean isTrash = false;
        long sortTime = 0;
        if (idColumn != -1) {
            addressStr = iCursor.getString(idColumn);
            if (!Utils.validBicoinAddress(addressStr)) {
                return null;
            }
        }
        idColumn = iCursor.getColumnIndex(AddressesColumns.ENCRYPT_PRIVATE_KEY);
        if (idColumn != -1) {
            encryptPrivateKey = iCursor.getString(idColumn);
        }
        idColumn = iCursor.getColumnIndex(AddressesColumns.PUB_KEY);
        if (idColumn != -1) {
            pubKey = Base58.decode(iCursor.getString(idColumn));
        }
        idColumn = iCursor.getColumnIndex(AddressesColumns.IS_XRANDOM);
        if (idColumn != -1) {
            isXRandom = iCursor.getInt(idColumn) == 1;
        }
        idColumn = iCursor.getColumnIndex(AddressesColumns.IS_SYNCED);
        if (idColumn != -1) {
            isSynced = iCursor.getInt(idColumn) == 1;
        }
        idColumn = iCursor.getColumnIndex(AddressesColumns.IS_TRASH);
        if (idColumn != -1) {
            isTrash = iCursor.getInt(idColumn) == 1;
        }
        idColumn = iCursor.getColumnIndex(AddressesColumns.SORT_TIME);
        if (idColumn != -1) {
            sortTime = iCursor.getLong(idColumn);
        }
        return new Address(addressStr, pubKey, sortTime, isSynced, isXRandom, isTrash, encryptPrivateKey);
    }
}
