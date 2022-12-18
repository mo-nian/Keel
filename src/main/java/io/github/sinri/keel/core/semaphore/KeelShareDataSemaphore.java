package io.github.sinri.keel.core.semaphore;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Future;
import io.vertx.core.shareddata.Counter;

import java.util.function.Function;

/**
 * @since 1.3
 */
public class KeelShareDataSemaphore {
    final private String name;
    final private int permits;

    private final KeelEventLogger logger;

    private final Keel keel;

    public KeelShareDataSemaphore(Keel keel, String name, int permits) {
        this.keel = keel;
        this.name = name;
        this.permits = permits;
        this.logger = KeelEventLogger.silentLogger();
    }

    public KeelShareDataSemaphore(Keel keel, String name, int permits, KeelEventLogger logger) {
        this.keel = keel;
        this.name = name;
        this.permits = permits;
        this.logger = logger;
    }

    protected Future<Counter> getCounter() {
        return this.keel.sharedData().getCounter(name);
    }

    public Future<Boolean> isNowAvailable() {
        return getCounter().compose(Counter::get).compose(current -> Future.succeededFuture(current < permits));
    }

    public Future<Long> getAvailablePermits() {
        return getCounter().compose(Counter::get).compose(current -> Future.succeededFuture(permits - current));
    }

    /**
     * @param function the function to execute
     * @return released Future, succeed or failed
     */
    public Future<Void> tryExecute(Function<Void, Future<Void>> function) {
        return acquire().compose(acquired -> {
            if (acquired) {
                // always return succeed future
                return function.apply(null)
                        .recover(throwable -> {
                            logger.warning(getClass() + " tryExecute failed in function apply: " + throwable.getMessage());
                            return Future.succeededFuture();
                        })
                        .compose(v -> release());
            } else {
                // always return failed future
                return release().compose(v -> Future.failedFuture(getClass() + " tryExecute failed: not acquired"));
            }
        });
    }

    protected Future<Boolean> acquire() {
        return getCounter()
                .compose(Counter::incrementAndGet)
                .compose(current -> {
                    logger.debug(eventLog -> {
                        eventLog.message(getClass() + " acquire")
                                .put("current", current)
                                .put("runnable", current <= permits);
                    });
                    if (current <= permits) {
                        return Future.succeededFuture(true);
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    protected Future<Void> release() {
        return getCounter()
                .compose(Counter::decrementAndGet)
                .compose(current -> {
                    logger.debug(getClass() + " release, current " + current);
                    return Future.succeededFuture();
                });
    }
}
