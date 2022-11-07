package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.List;

public class Cluster {

    public static Future<Void> startCluster() {
        return Keel.initializeClusteredVertx(
                "h1",
                List.of(
                        "172.20.12.66", "172.20.12.170"
                ),
                new VertxOptions()
        );
    }
}