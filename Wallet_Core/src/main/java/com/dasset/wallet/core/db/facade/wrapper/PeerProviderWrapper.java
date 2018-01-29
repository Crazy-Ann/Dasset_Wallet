/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasset.wallet.core.db.facade.wrapper;

import com.dasset.wallet.core.Peer;
import com.dasset.wallet.core.contant.db.PeersColumns;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.BaseProvider;
import com.dasset.wallet.core.db.facade.IPeerProvider;
import com.dasset.wallet.core.utils.Utils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.Nullable;

public abstract class PeerProviderWrapper extends ProviderWrapper implements IPeerProvider {

    public List<Peer> getAllPeers() {
        final List<Peer> peers = Lists.newArrayList();
        String sql = "select * from peers";
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(ICursor iCursor) {
                Peer peer = applyCursor(iCursor);
                if (peer != null) {
                    peers.add(peer);
                }
                return null;
            }
        });
        return peers;
    }

    @Override
    public void addPeers(List<Peer> peers) {
        List<Peer> tempPeers = Lists.newArrayList();
        List<Peer> allPeers = getAllPeers();
        for (Peer peerItem : peers) {
            if (!allPeers.contains(peerItem) && !tempPeers.contains(peerItem)) {
                tempPeers.add(peerItem);
            }
        }
        if (tempPeers.size() > 0) {
            String sql = "insert into peers(peer_address,peer_port,peer_services,peer_timestamp,peer_connected_cnt) values(?,?,?,?,?)";
            IDb writeDb = this.getWritableDatabase();
            writeDb.beginTransaction();
            for (Peer peer : tempPeers) {
                this.execUpdate(writeDb, sql, new String[]{
                        Long.toString(Utils.parseLongFromAddress(peer.getPeerAddress()))
                        , Integer.toString(peer.getPeerPort())
                        , Long.toString(peer.getPeerServices())
                        , Integer.toString(peer.getPeerTimestamp())
                        , Integer.toString(peer.getPeerConnectedCnt())});
            }
            writeDb.endTransaction();
        }
    }

    @Override
    public void removePeer(InetAddress address) {
        String sql = "delete from peers where peer_address=?";
        this.execUpdate(sql, new String[]{Long.toString(Utils.parseLongFromAddress(address))});
    }

    public void conncetFail(InetAddress address) {
        long addressLong = Utils.parseLongFromAddress(address);
        String sql = "select count(0) cnt from peers where peer_address=? and peer_connected_cnt=0";
        final int[] cnt = {0};
        this.execQueryOneRecord(sql, new String[]{Long.toString(addressLong)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("cnt");
                if (idColumn != -1) {
                    cnt[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        if (cnt[0] == 0) {
            sql = "update peers set peer_connected_cnt=peer_connected_cnt+1 where peer_address=?";
            this.execUpdate(sql, new String[]{Long.toString(addressLong)});
        } else {
            sql = "update peers set peer_connected_cnt=2 where peer_address=?";
            this.execUpdate(sql, new String[]{Long.toString(addressLong)});
        }
    }

    @Override
    public void connectSucceed(InetAddress address) {
        String sql = "update peers set peer_connected_cnt=?,peer_timestamp=? where peer_address=?";
        this.execUpdate(sql, new String[]{"1", Long.toString(System.currentTimeMillis()), Long.toString(Utils.parseLongFromAddress(address))});
    }

    @Override
    public List<Peer> getPeersWithLimit(int limit) {
        String sql = "select * from peers order by peer_address limit ?";
        final List<Peer> peerItemList = Lists.newArrayList();
        this.execQueryLoop(sql, new String[]{Integer.toString(limit)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Peer peer = applyCursor(iCursor);
                if (peer != null) {
                    peerItemList.add(peer);
                }
                return null;
            }
        });
        return peerItemList;
    }

    public void clearIPV6() {
        String sql = "delete from peers where peer_address>? or peer_address<? or peer_address=0";
        this.execUpdate(sql, new String[]{Integer.toString(Integer.MAX_VALUE), Integer.toString(Integer.MIN_VALUE)});
    }

    @Override
    public void cleanPeers() {
        int maxPeerSaveCnt = 12;
        String disconnectingPeerCntSql = "select count(0) cnt from peers where peer_connected_cnt<>1";
        final int[] disconnectingPeerCnt = {0};
        this.execQueryOneRecord(disconnectingPeerCntSql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("cnt");
                if (idColumn != -1) {
                    disconnectingPeerCnt[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });

        if (disconnectingPeerCnt[0] > maxPeerSaveCnt) {
            String sql = "select peer_timestamp from peers where peer_connected_cnt<>1 " +
                    " limit 1 offset ?";
            final long[] timestamp = {0};
            this.execQueryOneRecord(sql, new String[]{Integer.toString(maxPeerSaveCnt)}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    int idColumn = iCursor.getColumnIndex(PeersColumns.PEER_TIMESTAMP);
                    if (idColumn != -1) {
                        timestamp[0] = iCursor.getLong(idColumn);
                    }
                    return null;
                }
            });
            if (timestamp[0] > 0) {
                sql = "delete from peers where peer_connected_cnt<>1 and peer_timestamp<=?";
                this.execUpdate(sql, new String[]{Long.toString(timestamp[0])});
            }
        }
    }

    private void deleteUnknowHost(long address) {
        String sql = "delete from peers where peer_address=?";
        this.execUpdate(sql, new String[]{Long.toString(address)});
    }

    private Peer applyCursor(ICursor iCursor) {
        InetAddress address = null;
        int idColumn = iCursor.getColumnIndex(PeersColumns.PEER_ADDRESS);
        if (idColumn != -1) {
            long addressLong = iCursor.getLong(idColumn);
            try {
                if (addressLong >= Integer.MIN_VALUE && addressLong <= Integer.MAX_VALUE) {
                    address = Utils.parseAddressFromLong(iCursor.getLong(idColumn));
                } else {
                    clearIPV6();
                }
            } catch (UnknownHostException e) {
                deleteUnknowHost(addressLong);
                e.printStackTrace();
                return null;
            }
        }
        Peer peer = new Peer(address);
        idColumn = iCursor.getColumnIndex(PeersColumns.PEER_CONNECTED_CNT);
        if (idColumn != -1) {
            peer.setPeerConnectedCnt(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(PeersColumns.PEER_PORT);
        if (idColumn != -1) {
            peer.setPeerPort(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(PeersColumns.PEER_SERVICES);
        if (idColumn != -1) {
            peer.setPeerServices(iCursor.getLong(idColumn));
        }
        idColumn = iCursor.getColumnIndex(PeersColumns.PEER_TIMESTAMP);
        if (idColumn != -1) {
            peer.setPeerTimestamp(iCursor.getInt(idColumn));
        }
        return peer;
    }

    @Override
    public void recreate() {
        IDb writeDb = this.getWritableDatabase();
        writeDb.beginTransaction();
        this.execUpdate(writeDb, "drop table peers", null);
        this.execUpdate(writeDb, BaseProvider.CREATE_PEER_SQL, null);
        writeDb.endTransaction();
    }
}
