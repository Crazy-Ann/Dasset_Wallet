package com.dasset.wallet.core.utils;

import com.dasset.wallet.core.Block;
import com.dasset.wallet.core.BlockChain;
import com.dasset.wallet.core.api.BlockChainDownloadSpvApi;
import com.dasset.wallet.core.api.BlockChainGetLatestBlockApi;
import com.dasset.wallet.core.api.DownloadSpvApi;
import com.dasset.wallet.core.contant.AbstractApp;
import com.dasset.wallet.core.contant.BitherjSettings;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockUtil {

    private static final Logger log = LoggerFactory.getLogger(BlockUtil.class);

    private static final String VER = "ver";
    private static final String PREV_BLOCK = "prev_block";
    private static final String MRKL_ROOT = "mrkl_root";
    private static final String TIME = "time";
    private static final String BITS = "bits";
    private static final String NONCE = "nonce";
    private static final String BLOCK_NO = "block_no";
    private static final String HEIGHT = "height";

    public  static Block getLatestBlockHeight(JSONObject jsonObject)
            throws Exception {
        int latestHeight = jsonObject.getInt("height");
        int height = 0;
        if (latestHeight % 2016 !=0){
            height = latestHeight - (latestHeight%2016);
        }else {
            height = latestHeight;
        }
        BlockChainDownloadSpvApi blockChainDownloadSpvApi = new BlockChainDownloadSpvApi(height);
        blockChainDownloadSpvApi.handleHttpGet();
        Block block = null;
        block = blockChainDownloadSpvApi.getResult();
        return block;
    }
    public static Block formatStoreBlockFromBlockChainInfo(JSONObject jsonObject)
        throws JSONException{
        long ver = jsonObject.getLong(VER);
        int height = jsonObject.getInt(HEIGHT);
        String prevBlock = jsonObject.getString(PREV_BLOCK);
        String mrklRoot = jsonObject.getString(MRKL_ROOT);
        int time = jsonObject.getInt(TIME);
        long difficultyTarget = jsonObject.getLong(BITS);
        long nonce = jsonObject.getLong(NONCE);

        return BlockUtil.getStoredBlock(ver, prevBlock, mrklRoot, time,
                difficultyTarget, nonce, height);

    }

    public static Block formatStoredBlock(JSONObject jsonObject)
            throws JSONException {
        long ver = jsonObject.getLong(VER);
        int height = jsonObject.getInt(BLOCK_NO);
        String prevBlock = jsonObject.getString(PREV_BLOCK);
        String mrklRoot = jsonObject.getString(MRKL_ROOT);
        int time = jsonObject.getInt(TIME);
        long difficultyTarget = jsonObject.getLong(BITS);
        long nonce = jsonObject.getLong(NONCE);

        return BlockUtil.getStoredBlock(ver, prevBlock, mrklRoot, time,
                difficultyTarget, nonce, height);
    }

    public static Block formatStoredBlock(JSONObject jsonObject, int hegih)
            throws JSONException {
        long ver = jsonObject.getLong(VER);
        String prevBlock = jsonObject.getString(PREV_BLOCK);
        String mrklRoot = jsonObject.getString(MRKL_ROOT);
        int time = jsonObject.getInt(TIME);
        long difficultyTarget = jsonObject.getLong(BITS);
        long nonce = jsonObject.getLong(NONCE);

        return BlockUtil.getStoredBlock(ver, prevBlock, mrklRoot, time,
                difficultyTarget, nonce, hegih);

    }

    public static Block getStoredBlock(long ver, String prevBlock,
                                       String mrklRoot, int time, long difficultyTarget, long nonce,
                                       int hegiht) {
        Block b = new Block(ver,
                prevBlock, mrklRoot, time,
                difficultyTarget, nonce, hegiht);
        return b;
    }

    public synchronized static Block dowloadSpvBlock() throws Exception {
        if (AbstractApp.bitherjSetting.getDownloadSpvFinish()) {
            return null;
        }
        Block block = null;
        try {
            DownloadSpvApi downloadSpvApi = new DownloadSpvApi();
            downloadSpvApi.handleHttpGet();
            block = downloadSpvApi.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (block == null) {
                BlockChainGetLatestBlockApi blockChainGetLatestBlockApi = new
                        BlockChainGetLatestBlockApi();
                blockChainGetLatestBlockApi.handleHttpGet();
                block = blockChainGetLatestBlockApi.getResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AbstractApp.notificationService.sendBroadcastGetSpvBlockComplete(false);
            throw e;
        }
        if (block.getBlockNo() % BitherjSettings.INTERVAL == 0) {
            BlockChain.getInstance().addSPVBlock(block);
            AbstractApp.bitherjSetting.setDownloadSpvFinish(true);
            AbstractApp.notificationService.sendBroadcastGetSpvBlockComplete(true);
        } else {
            log.debug("spv", "service is not vaild");
            AbstractApp.notificationService.sendBroadcastGetSpvBlockComplete(false);
            return null;
        }
        return block;
    }


}
