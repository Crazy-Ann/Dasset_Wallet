package com.dasset.wallet.core.db.facade.wrapper;

import com.dasset.wallet.core.Block;
import com.dasset.wallet.core.contant.BitherjSettings;
import com.dasset.wallet.core.contant.db.BlocksColumns;
import com.dasset.wallet.core.db.facade.IBlockProvider;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.exception.AddressFormatException;
import com.dasset.wallet.core.utils.Base58;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public abstract class BlockProviderWrapper extends ProviderWrapper implements IBlockProvider {

    @Override
    public List<Block> getAllBlocks() {
        final List<Block> blocks = Lists.newArrayList();
        String sql = "select * from blocks order by block_no desc";
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(ICursor iCursor) {
                blocks.add(applyCursor(iCursor));
                return null;
            }
        });
        return blocks;
    }

    @Override
    public List<Block> getLimitBlocks(int limit) {
        final List<Block> blockItems = Lists.newArrayList();
        String sql = "select * from blocks order by block_no desc limit ?";
        this.execQueryLoop(sql, new String[]{Integer.toString(limit)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                blockItems.add(applyCursor(iCursor));
                return null;
            }
        });
        return blockItems;
    }

    @Override
    public List<Block> getBlocksFrom(int blockNo) {
        final List<Block> blocks = new ArrayList<Block>();
        String sql = "select * from blocks where block_no>? order by block_no desc";
        this.execQueryLoop(sql, new String[]{Integer.toString(blockNo)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                blocks.add(applyCursor(iCursor));
                return null;
            }
        });
        return blocks;
    }

    @Override
    public int getBlockCount() {
        String sql = "select count(*) cnt from blocks ";
        final int[] count = {0};
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
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
    public Block getLastBlock() {
        final Block[] blocks = {null};
        String sql = "select * from blocks where is_main=1 order by block_no desc limit 1";
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                blocks[0] = applyCursor(iCursor);
                return null;
            }
        });
        return blocks[0];
    }

    @Override
    public Block getLastOrphanBlock() {
        final Block[] blocks = {null};
        String sql = "select * from blocks where is_main=0 order by block_no desc limit 1";
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                blocks[0] = applyCursor(iCursor);
                return null;
            }
        });
        return blocks[0];
    }

    @Override
    public Block getBlock(byte[] blockHash) {
        final Block[] blocks = {null};
        String sql = "select * from blocks where block_hash=?";
        this.execQueryOneRecord(sql, new String[]{Base58.encode(blockHash)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                blocks[0] = applyCursor(iCursor);
                return null;
            }
        });
        return blocks[0];
    }

    @Override
    public Block getOrphanBlockByPrevHash(byte[] prevHash) {
        final Block[] blocks = {null};
        String sql = "select * from blocks where block_prev=? and is_main=0";
        this.execQueryOneRecord(sql, new String[]{Base58.encode(prevHash)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                blocks[0] = applyCursor(iCursor);
                return null;
            }
        });
        return blocks[0];
    }

    @Override
    public Block getMainChainBlock(byte[] blockHash) {
        final Block[] blocks = {null};
        String sql = "select * from blocks where block_hash=? and is_main=1";
        this.execQueryOneRecord(sql, new String[]{Base58.encode(blockHash)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                blocks[0] = applyCursor(iCursor);
                return null;
            }
        });
        return blocks[0];
    }

    @Override
    public void addBlocks(List<Block> blocks) {
        List<Block> tempBlocks = Lists.newArrayList();
        for (Block block : blocks) {
            if (!this.blockExists(block.getBlockHash())) {
                tempBlocks.add(block);
            }
        }
        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        String sql = "insert into blocks(block_no,block_hash,block_root,block_ver,block_bits,block_nonce,block_time,block_prev,is_main) values(?,?,?,?,?,?,?,?,?)";
        for (Block item : tempBlocks) {
            this.execUpdate(writeDb, sql, new String[]{
                    Integer.toString(item.getBlockNo())
                    , Base58.encode(item.getBlockHash())
                    , Base58.encode(item.getBlockRoot())
                    , Long.toString(item.getBlockVer())
                    , Long.toString(item.getBlockBits())
                    , Long.toString(item.getBlockNonce())
                    , Long.toString(item.getBlockTime())
                    , Base58.encode(item.getBlockPrev())
                    , item.isMain() ? "1" : "0"
            });
        }
        writeDb.endTransaction();
    }

    @Override
    public void addBlock(Block block) {
        if (!blockExists(block.getBlockHash())) {
            String sql = "insert into blocks(block_no,block_hash,block_root,block_ver,block_bits,block_nonce,block_time,block_prev,is_main) values(?,?,?,?,?,?,?,?,?)";
            this.execUpdate(sql, new String[]{
                    Integer.toString(block.getBlockNo())
                    , Base58.encode(block.getBlockHash())
                    , Base58.encode(block.getBlockRoot())
                    , Long.toString(block.getBlockVer())
                    , Long.toString(block.getBlockBits())
                    , Long.toString(block.getBlockNonce())
                    , Long.toString(block.getBlockTime())
                    , Base58.encode(block.getBlockPrev())
                    , block.isMain() ? "1" : "0"
            });
        }
    }

    public boolean blockExists(byte[] blockHash) {
        String sql = "select count(0) cnt from blocks where block_hash=?";
        final int[] cnt = {0};
        this.execQueryOneRecord(sql, new String[]{Base58.encode(blockHash)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                cnt[0] = iCursor.getInt(0);
                return null;
            }
        });
        return cnt[0] > 0;
    }

    @Override
    public void updateBlock(byte[] blockHash, boolean isMain) {
        String sql = "update blocks set is_main=? where block_hash=?";
        this.execUpdate(sql, new String[]{isMain ? "1" : "0", Base58.encode(blockHash)});
    }

    @Override
    public void removeBlock(byte[] blockHash) {
        String sql = "delete from blocks where block_hash=?";
        this.execUpdate(sql, new String[]{Base58.encode(blockHash)});
    }

    @Override
    public void cleanOldBlock() {
        String sql = "select count(0) cnt from blocks";
        final int[] cnt = {0};
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                cnt[0] = iCursor.getInt(0);
                return null;
            }
        });
        if (cnt[0] > 5000) {
            sql = "select max(block_no) max_block_no from blocks where is_main=1";
            final int[] maxBlockNo = {0};
            this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    maxBlockNo[0] = iCursor.getInt(0);
                    return null;
                }
            });
            sql = "delete from blocks where block_no<?";
            this.execUpdate(sql, new String[]{Integer.toString((maxBlockNo[0] - BitherjSettings.BLOCK_DIFFICULTY_INTERVAL) - maxBlockNo[0] % BitherjSettings.BLOCK_DIFFICULTY_INTERVAL)});
        }
    }

    private Block applyCursor(ICursor iCursor) {
        byte[] blockHash = null;
        long version = 1;
        byte[] prevBlock = null;
        byte[] merkleRoot = null;
        int timestamp = 0;
        long target = 0;
        long nonce = 0;
        int blockNo = 0;
        boolean isMain = false;
        int idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_BITS);
        if (idColumn != -1) {
            target = iCursor.getLong(idColumn);
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_HASH);
        if (idColumn != -1) {
            try {
                blockHash = Base58.decode(iCursor.getString(idColumn));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_NO);
        if (idColumn != -1) {
            blockNo = iCursor.getInt(idColumn);
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_NONCE);
        if (idColumn != -1) {
            nonce = iCursor.getLong(idColumn);
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_PREV);
        if (idColumn != -1) {
            try {
                prevBlock = Base58.decode(iCursor.getString(idColumn));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_ROOT);
        if (idColumn != -1) {
            try {
                merkleRoot = Base58.decode(iCursor.getString(idColumn));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_TIME);
        if (idColumn != -1) {
            timestamp = iCursor.getInt(idColumn);
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.BLOCK_VER);
        if (idColumn != -1) {
            version = iCursor.getLong(idColumn);
        }
        idColumn = iCursor.getColumnIndex(BlocksColumns.IS_MAIN);
        if (idColumn != -1) {
            isMain = iCursor.getInt(idColumn) == 1;
        }
        return new Block(blockHash, version, prevBlock, merkleRoot, timestamp, target, nonce, blockNo, isMain);
    }
}
