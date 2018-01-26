package com.dasset.wallet.core.db.facade;

import com.dasset.wallet.core.Peer;

import java.net.InetAddress;
import java.util.List;

public interface IPeerProvider {

//    ArrayList<InetAddress> exists(ArrayList<InetAddress> peerAddresses);

    void addPeers(List<Peer> items);

    void removePeer(InetAddress address);

    void connectSucceed(InetAddress address);

    List<Peer> getPeersWithLimit(int limit);

    void cleanPeers();

    void recreate();
}
