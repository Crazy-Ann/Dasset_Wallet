package com.dasset.wallet.constant;


public class Method {

    private Method() { }

    public static final String CREATE_ACCOUNT = "/action/create_account";
    public static final String REGISTER_ASSET = "/action/register_asset";
    public static final String ISSUE_ASSET = "/action/issue_asset";
    public static final String TRANSFER_ASSETS = "/action/transfer_assets";

    public final static String GET_VERSION = "digitalasset.client.get.version";
    public final static String REGISTER_ACCOUNT = "digitalasset.client.register.account";
    public final static String QUERY_ACCOUNT = "digitalasset.client.query.account";
    public final static String TRANSFER_ASSET = "digitalasset.client.transfer.asset";
}
