package io.github.sinri.keel.logger.event.logger;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelSilentEventLogCenter;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class KeelSilentEventLogger extends KeelFreeEventLogger {
    private final static KeelSilentEventLogger instance = new KeelSilentEventLogger();

    public static KeelSilentEventLogger getInstance() {
        return instance;
    }

    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return KeelSilentEventLogCenter::getInstance;
    }

    @Override
    public String getPresetTopic() {
        return null;
    }

    @Override
    public void log(@NotNull Handler<KeelEventLog> eventLogHandler) {
        // keep silent
    }
}
