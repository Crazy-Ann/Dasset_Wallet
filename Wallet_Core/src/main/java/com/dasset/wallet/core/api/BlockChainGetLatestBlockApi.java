package com.dasset.wallet.core.api;

import com.dasset.wallet.core.api.http.BitherUrl;
import com.dasset.wallet.core.api.http.HttpsGetResponse;
import com.dasset.wallet.core.utils.BlockUtil;
import com.dasset.wallet.core.Block;

import org.json.JSONObject;

public class BlockChainGetLatestBlockApi extends HttpsGetResponse<Block> {
    public BlockChainGetLatestBlockApi() {
        setUrl(BitherUrl.BLOCKCHAIN_INFO_GET_LASTST_BLOCK);
    }

    @Override
    public void setResult(String response) throws Exception {
        JSONObject jsonObject = new JSONObject(response);
        this.result = BlockUtil.getLatestBlockHeight(jsonObject);
    }
}
