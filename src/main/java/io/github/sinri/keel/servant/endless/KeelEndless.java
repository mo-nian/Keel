package io.github.sinri.keel.servant.endless;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;

import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.keel;

/**
 * 任务定期触发器，隔一段时间调用任务供应商获取任务执行。
 * 隔一段时间无条件跑一次。
 * Timer Triggered
 * - START
 * - Supplier.get()
 * - Set Next Timer
 * - END
 * 使用deploy开启，使用undeploy撤销。
 * 仅用于单节点模式。
 *
 * @since 2.7
 * @since 3.0.0 use io.github.sinri.keel.facade.async.KeelAsyncKit#endless(io.vertx.core.Handler) instead.
 */
@Deprecated(since = "3.0.0")
public class KeelEndless extends KeelVerticleBase {
    private final long restMS;
    private final Supplier<Future<Void>> supplier;

    private Future<Void> routine() {
        // since 2.8 防止 inner exception 爆破
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    /**
     * @param restMS   干完一组事情后休息的时间长度，单位为 千分之一秒
     * @param supplier 所谓的干完一组事情
     */
    public KeelEndless(long restMS, Supplier<Future<Void>> supplier) {
        this.restMS = restMS;
        this.supplier = supplier;
        this.setLogger(KeelEventLogger.silentLogger());
    }

    /**
     * @since 2.8 如果alive显示false，则不再策划下一波触发
     */
    private void routineWrapper() {
        keel.getVertx().setTimer(
                restMS,
                currentTimerID -> routine().onComplete(done -> routineWrapper())
        );
    }

    @Override
    public void start() {
        routineWrapper();
    }
}
