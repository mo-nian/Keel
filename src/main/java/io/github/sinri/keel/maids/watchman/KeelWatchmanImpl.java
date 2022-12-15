package io.github.sinri.keel.maids.watchman;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.shareddata.Lock;

/**
 * @since 2.9.3
 */
abstract class KeelWatchmanImpl extends AbstractVerticle implements KeelWatchman {
    private final String watchmanName;
    private KeelLogger logger;
    private MessageConsumer<Long> consumer;

    public KeelWatchmanImpl(String watchmanName) {
        this.logger = KeelLogger.silentLogger();//Keel.outputLogger(hourglassName);
        this.watchmanName = watchmanName;
    }

    @Override
    public String watchmanName() {
        return this.watchmanName;
    }

    @Override
    public KeelLogger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(KeelLogger logger) {
        this.logger = logger;
    }

    protected String eventBusAddress() {
        return this.getClass().getName() + ":" + watchmanName();
    }

    @Override
    public void start() {
        this.consumer = Keel.vertx().eventBus().consumer(eventBusAddress());
        this.consumer.handler(this::consumeHandleMassage);
        this.consumer.exceptionHandler(throwable -> getLogger()
                .exception(watchmanName() + " ERROR", throwable));

        try {
            // @since 2.9.3 强行拟合HH:MM:SS.000-200
            long x = 1000 - System.currentTimeMillis() % 1_000;
            if (x < 800) {
                Thread.sleep(x);
            }
        } catch (Exception ignore) {
            // 拟合不了拉倒
        }
        Keel.vertx().setPeriodic(
                interval(),
                timerID -> Keel.vertx().eventBus()
                        .send(eventBusAddress(), System.currentTimeMillis()));
    }

    protected void consumeHandleMassage(Message<Long> message) {
        Long timestamp = message.body();
        getLogger().debug(watchmanName() + " TRIGGERED FOR " + timestamp);

        long x = timestamp / interval();
        Keel.vertx().sharedData().getLockWithTimeout(eventBusAddress() + "@" + x, Math.min(3_000L, interval() - 1), lockAR -> {
            if (lockAR.failed()) {
                getLogger().warning("LOCK ACQUIRE FAILED FOR " + timestamp + " i.e. " + x);
            } else {
                Lock lock = lockAR.result();
                getLogger().info("LOCK ACQUIRED FOR " + timestamp + " i.e. " + x);
                regularHandler().handle(timestamp);
                Keel.vertx().setTimer(interval(), timerID -> {
                    lock.release();
                    getLogger().info("LOCK RELEASED FOR " + timestamp + " i.e. " + x);
                });
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        consumer.unregister();
    }
}
