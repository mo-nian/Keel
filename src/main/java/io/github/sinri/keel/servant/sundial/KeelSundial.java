package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.shareddata.Counter;

import java.util.*;

/**
 * 日晷 (CRON TABLE)
 * 基于CRON的定时任务实现。
 *
 * @since 2.7 rename from KeelServantTimer
 * @since 2.9.3 建议使用KeelWatchman系列实现
 */
public class KeelSundial {
    private final KeelEventLogger logger;
    private final Map<String, KeelSundialWorker> registeredWorkers = new HashMap<>();
    private Long byMinuteTimerID = null;
    private final Keel keel;

    public KeelSundial(Keel keel, KeelEventLogger logger) {
        this.keel = keel;
        this.logger = logger;

        // by default starts
        start();
    }

    public KeelEventLogger getLogger() {
        return logger;
    }

    /**
     * @return 当前注册中的worker meta列表
     * @since 2.8
     */
    public List<KeelSundialWorkerMeta> getRegisteredWorkerList() {
        List<KeelSundialWorkerMeta> list = new ArrayList<>();
        registeredWorkers.keySet().forEach(key -> {
            list.add(registeredWorkers.get(key).getMeta());
        });
        return list;
    }

    public KeelSundial registerWorker(KeelSundialWorker worker) {
        registeredWorkers.put(worker.getName(), worker);
        getLogger().info(eventLog -> {
            eventLog.message("registered worker to sundial")
                    .put("worker", worker.getMeta().toJsonObject());
        });
        return this;
    }

    @Deprecated(since = "2.8")
    public KeelSundial registerWorkers(Collection<KeelSundialWorker> workers) {
        workers.forEach(this::registerWorker);
        return this;
    }

    public KeelSundial unregisterWorker(String workerName) {
        if (workerName != null) {
            registeredWorkers.remove(workerName);
            getLogger().info("unregister worker from sundial: " + workerName);
        }
        return this;
    }

    /**
     * @since 2.8
     */
    public KeelSundial unregisterAllWorkers() {
        registeredWorkers.clear();
        getLogger().info("unregister all workers from sundial");
        return this;
    }

    /**
     * @since 2.8
     */
    public void start() {
        if (byMinuteTimerID != null) {
            throw new RuntimeException("stop the existed first!");
        }
        byMinuteTimerID = keel.setPeriodic(60 * 1000, id -> {
            Calendar calendar = Calendar.getInstance();

            registeredWorkers.keySet().forEach(workerName -> {
                try {
                    KeelSundialWorker worker = registeredWorkers.get(workerName);

                    if (!worker.shouldRunNow(calendar)) {
                        return;
                    }

                    int parallelLimit = worker.getParallelLimit();
                    if (parallelLimit > 0) {
                        keel.sharedData().getCounter(
                                "KeelSundial-" + workerName,
                                counterAsyncResult -> {
                                    if (counterAsyncResult.failed()) {
                                        this.logger.exception(
                                                counterAsyncResult.cause(),
                                                "getWorkerParallelCounter for " + workerName + " failed"
                                        );
                                        return;
                                    }

                                    Counter counter = counterAsyncResult.result();

                                    counter.get(currentAsyncResult -> {
                                        if (currentAsyncResult.failed()) {
                                            this.logger.exception(
                                                    currentAsyncResult.cause(),
                                                    "Counter for " + workerName + " get current value failed"
                                            );
                                            return;
                                        }

                                        Long current = currentAsyncResult.result();

                                        if (current >= parallelLimit) {
                                            getLogger().warning("Worker Name " + workerName + " current parallel " + current + " >= limit " + parallelLimit);
                                            return;
                                        }

                                        counter.incrementAndGet(x -> {
                                            getLogger().info("Worker Name " + workerName + " to work");
                                            worker.work(calendar, workAsyncResult -> {
                                                counter.decrementAndGet();
                                            });
                                        });
                                    });
                                }
                        );
                    } else {
                        worker.work(calendar);
                    }
                } catch (Throwable throwable) {
                    getLogger().exception(throwable, "KeelSundial Periodic Inside Exception");
                }
            });
        });
    }

    /**
     * If the timer ID (`byMinuteTimerID`) existed, cancel the related timer.
     * Note, the workers already running would not be affected.
     */
    public void stop() {
        if (byMinuteTimerID != null) {
            boolean done = keel.cancelTimer(byMinuteTimerID);
            if (done) {
                getLogger().info("stopped timer " + byMinuteTimerID);
            } else {
                getLogger().warning("failed to stop timer " + byMinuteTimerID);
            }
            byMinuteTimerID = null;
        }
    }
}
