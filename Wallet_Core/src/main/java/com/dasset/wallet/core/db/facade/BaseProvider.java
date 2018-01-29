package com.dasset.wallet.core.db.facade;

public abstract class BaseProvider {

    public static final String CREATE_PEER_SQL = "create table if not exists peers (peer_address integer primary key" +
            ", peer_port integer not null, peer_services integer not null, peer_timestamp integer not null, peer_connected_cnt integer not null);";
    
    public static final String CREATE_OUTS_SQL = "create table if not exists outs (tx_hash text not null" +
            ", out_sn integer not null, out_script text not null, out_value integer not null, out_status integer not null, out_address text, hd_account_id integer, primary key (tx_hash, out_sn));";

    public static final String CREATE_INS_SQL = "create table if not exists ins (tx_hash text not null" +
            ", in_sn integer not null, prev_tx_hash text, prev_out_sn integer, in_signature text, in_sequence integer, primary key (tx_hash, in_sn));";

    public static final String CREATE_ADDRESSTXS_SQL = "create table if not exists addresses_txs (address text not null, tx_hash text not null, primary key (address, tx_hash));";

    public static final String CREATE_TXS_SQL = "create table if not exists txs (tx_hash text primary key, tx_ver integer, tx_locktime integer, tx_time integer, block_no integer, source integer);";

    public static final String CREATE_BLOCKS_SQL = "create table if not exists blocks (block_no integer not null" +
            ", block_hash text not null primary key, block_root text not null, block_ver integer not null, block_bits integer not null, block_nonce integer not null, block_time integer not null, block_prev text, is_main integer not null);";

    public static final String CREATE_PASSWORD_SEED_SQL = "create table if not exists password_seed (password_seed text not null primary key);";

    public static final String CREATE_ADDRESSES_SQL = "create table if not exists addresses (address text not null primary key" +
            ", encrypt_private_key text, pub_key text not null, is_xrandom integer not null, is_trash integer not null, is_synced integer not null, sort_time integer not null);";

    public static final String CREATE_HD_SEEDS_SQL = "create table if not exists hd_seeds (hd_seed_id integer not null primary key autoincrement" +
            ", encrypt_seed text not null, encrypt_hd_seed text, hdm_address text not null, is_xrandom integer not null, singular_mode_backup text);";

    public static final String CREATE_HDM_ADDRESSES_SQL = "create table if not exists hdm_addresses (hd_seed_id integer not null" +
            ", hd_seed_index integer not null, pub_key_hot text not null, pub_key_cold text not null, pub_key_remote text, address text, is_synced integer not null, primary key (hd_seed_id, hd_seed_index));";

    public static final String CREATE_HDM_BID_SQL = "create table if not exists hdm_bid (hdm_bid text not null primary key, encrypt_bither_password text not null);";

    public static final String CREATE_ALIASES_SQL = "create table if not exists aliases (address text not null primary key, alias text not null);";

    public static final String CREATE_VANITY_ADDRESS_SQL = "create table if not exists vanity_address (address text not null primary key, vanity_len integer );";

    public static final String CREATE_BLOCK_NO_INDEX = "create index idx_blocks_block_no on blocks (block_no);";

    public static final String CREATE_BLOCK_PREV_INDEX = "create index idx_blocks_block_prev on blocks (block_prev);";
    
    // new index
    public static final String CREATE_OUT_ADDRESS_INDEX = "create index idx_out_out_address on outs (out_address);";

    public static final String CREATE_OUT_HD_ACCOUNT_ID_INDEX = "create index idx_out_hd_account_id on outs (hd_account_id);";

    public static final String CREATE_TX_BLOCK_NO_INDEX = "create index idx_tx_block_no on txs (block_no);";

    public static final String CREATE_IN_PREV_TX_HASH_INDEX = "create index idx_in_prev_tx_hash on ins (prev_tx_hash);";
    
    //hd account
    public static final String CREATE_HD_ACCOUNT = "create table if not exists hd_account (hd_account_id integer not null primary key autoincrement" +
            ", encrypt_seed text, encrypt_mnemonic_seed text, hd_address text not null, external_pub text not null, internal_pub text not null, is_xrandom integer not null);";

    public static final String CREATE_HD_ACCOUNT_ADDRESSES = "create table if not exists hd_account_addresses (hd_account_id integer not null" +
            ", path_type integer not null, address_index integer not null, is_issued integer not null, address text not null, pub text not null, is_synced integer not null, primary key (address));";
    
    // hd Account index
    public static final String CREATE_HD_ACCOUNT_ADDRESS_INDEX = "create index idx_hd_address_address on hd_account_addresses (address);";

    public static final String CREATE_HD_ACCOUNT_ACCOUNT_ID_AND_PATH_TYPE_INDEX = "create index idx_hd_address_account_id_path on hd_account_addresses (hd_account_id, path_type);";
    
    //add hd_accont_id for outs
    public static final String ADD_HD_ACCOUNT_ID_FOR_OUTS = "alter table outs add column hd_account_id integer;";
    
    //enterprise hdm
    public static final String CREATE_ENTERPRISE_HD_ACCOUNT = "create table if not exists enterprise_hd_account (hd_account_id integer not null primary key autoincrement" +
            ", encrypt_seed text not null, encrypt_mnemonic_seed text, hd_address text not null, is_xrandom integer not null);";

    public static final String CREATE_MULTI_SIGN_SET = "create table if not exists enterprise_multi_sign_set (multi_sign_id integer not null primary key autoincrement, multi_sign_n integer not null, multi_sign_m integer not null);";

    public static final String CREATE_ENTERPRISE_HDM_ADDRESSES_SQL = "create table if not exists enterprise_hdm_addresses (hdm_index integer not null" +
            ", address text not null, pub_key_0 text, pub_key_1 text, pub_key_2 text, pub_key_3 text, pub_key_4 text, pub_key_5 text, pub_key_6 text, pub_key_7 text, pub_key_8 text, pub_key_9 text, is_synced integer, primary key (hdm_index));";

    public static IBlockProvider iBlockProvider;
    public static IPeerProvider iPeerProvider;
    public static ITxProvider iTxProvider;
    public static IAddressProvider iAddressProvider;
    public static IHDAccountAddressProvider iHDAccountAddressProvider;
    public static IHDAccountProvider iHDAccountProvider;
    public static IEnterpriseHDMProvider iEnterpriseHDMProvider;
    public static IDesktopAddressProvider iDesktopAddressProvider;
    public static IDesktopTxProvider iDesktopTxProvider;

    public void intialize() {
        iBlockProvider = intializeBlockProvider();
        iPeerProvider = intializePeerProvider();
        iTxProvider = intializeTxProvider();
        iAddressProvider = intializeAddressProvider();
        iHDAccountAddressProvider = intializeHDAccountAddressProvider();
        iHDAccountProvider = intializeHDAccountProvider();
        iEnterpriseHDMProvider = intializeEnterpriseHDMProvider();
        iDesktopAddressProvider = intializeEnDesktopAddressProvider();
        iDesktopTxProvider = intializeDesktopTxProvider();
    }

    public abstract IBlockProvider intializeBlockProvider();

    public abstract IPeerProvider intializePeerProvider();

    public abstract ITxProvider intializeTxProvider();

    public abstract IAddressProvider intializeAddressProvider();

    public abstract IHDAccountAddressProvider intializeHDAccountAddressProvider();

    public abstract IHDAccountProvider intializeHDAccountProvider();

    public abstract IEnterpriseHDMProvider intializeEnterpriseHDMProvider();

    public abstract IDesktopAddressProvider intializeEnDesktopAddressProvider();

    public abstract IDesktopTxProvider intializeDesktopTxProvider();

}
