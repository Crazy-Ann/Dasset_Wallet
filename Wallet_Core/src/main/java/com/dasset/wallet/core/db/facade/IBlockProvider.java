package com.dasset.wallet.core.db.facade;

import com.dasset.wallet.core.Block;

import java.util.List;

public interface IBlockProvider {

    List<Block> getAllBlocks();

    List<Block> getBlocksFrom(int blockNo);

    List<Block> getLimitBlocks(int limit);

    int getBlockCount();

    Block getLastBlock();

    Block getLastOrphanBlock();

    Block getBlock(byte[] blockHash);

    Block getOrphanBlockByPrevHash(byte[] prevHash);

    Block getMainChainBlock(byte[] blockHash);

//    List<byte[]> exists(List<byte[]> blockHashes);

    void addBlocks(List<Block> blockItemList);

    void addBlock(Block item);

    void updateBlock(byte[] blockHash, boolean isMain);

    void removeBlock(byte[] blockHash);

    void cleanOldBlock();
}
