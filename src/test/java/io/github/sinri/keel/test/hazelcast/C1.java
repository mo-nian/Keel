package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;

public class C1 {
    static KeelLogger logger;

    public static void main(String[] args) {
        Cluster.startCluster()
                .compose(init -> {
                    logger = Keel.outputLogger("C1-Maxim");
                    logger.info("14001 GO");

                    MessageConsumer<Long> consumer = Keel.getVertx().eventBus().consumer("1400x");
                    consumer.handler(message -> {
                        Long body = message.body();
                        Keel.outputLogger().info("message received: " + body);
                        long reply = body + 1;
                        message.reply(reply);
                    });

                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    Keel.outputLogger().exception("!!!", throwable);
                });
    }
}
