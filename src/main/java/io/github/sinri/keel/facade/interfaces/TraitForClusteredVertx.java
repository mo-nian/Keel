package io.github.sinri.keel.facade.interfaces;

import com.hazelcast.config.*;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.List;

public interface TraitForClusteredVertx extends TraitForVertx {

    static ClusterManager createClusterManagerForSAE(
            String clusterName,
            List<String> members,
            int port, int portCount
    ) {
        TcpIpConfig tcpIpConfig = new TcpIpConfig()
                .setEnabled(true)
                .setConnectionTimeoutSeconds(1);
        members.forEach(tcpIpConfig::addMember);

        JoinConfig joinConfig = new JoinConfig()
                .setMulticastConfig(new MulticastConfig().setEnabled(false))
                .setTcpIpConfig(tcpIpConfig);

        NetworkConfig networkConfig = new NetworkConfig()
                .setJoin(joinConfig)
                .setPort(port)
                .setPortCount(portCount)
                .setPortAutoIncrement(portCount > 1)
                .setOutboundPorts(List.of(0));

        Config hazelcastConfig = ConfigUtil.loadConfig()
                .setClusterName(clusterName)
                .setNetworkConfig(networkConfig);

        return new HazelcastClusterManager(hazelcastConfig);
    }

    default String getVertxNodeNetAddress() {
        if (getClusterManager() == null) return null;
        NodeInfo nodeInfo = getClusterManager().getNodeInfo();
        return nodeInfo.host() + ":" + nodeInfo.port();
    }

    default String getVertxNodeID() {
        if (getClusterManager() == null) return null;
        return getClusterManager().getNodeId();
    }

}
