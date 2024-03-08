package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * @param <T> The type of the certain implementation of the issue record used.
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecorder<T extends KeelIssueRecord<?>> {

    static <T extends KeelIssueRecord<?>> KeelIssueRecorder<T> build(
            @Nonnull KeelIssueRecordCenter issueRecordCenter,
            @Nonnull Supplier<T> issueRecordBuilder,
            @Nonnull String topic
    ) {
        return new KeelIssueRecorderImpl<T>(issueRecordCenter, issueRecordBuilder, topic);
    }

    @Nonnull
    KeelLogLevel getVisibleLevel();

    void setVisibleLevel(@Nonnull KeelLogLevel level);

    @Nonnull
    KeelIssueRecordCenter issueRecordCenter();

    /**
     * @return an instance of issue, to be modified for details.
     */
    @Nonnull
    Supplier<T> issueRecordBuilder();

    /**
     * @since 3.2.0
     */
    void addBypassIssueRecorder(@Nonnull KeelIssueRecorder<T> bypassIssueRecorder);

    /**
     * @since 3.2.0
     */
    @Nonnull
    List<KeelIssueRecorder<T>> getBypassIssueRecorders();

    @Nonnull
    String topic();

    @Nullable
    Handler<T> getRecordFormatter();

    void setRecordFormatter(@Nullable Handler<T> handler);

    /**
     * Record an issue (created with `issueRecordBuilder` and modified with `issueHandler`).
     * It may be handled later async, actually.
     *
     * @param issueHandler the handler to modify the base issue.
     */
    default void record(@Nonnull Handler<T> issueHandler) {
        T issue = this.issueRecordBuilder().get();
        issueHandler.handle(issue);

        Handler<T> recordFormatter = getRecordFormatter();
        if (recordFormatter != null) {
            recordFormatter.handle(issue);
        }

        if (issue.level().isEnoughSeriousAs(getVisibleLevel())) {
            this.issueRecordCenter().getAdapter().record(topic(), issue);
        }

        getBypassIssueRecorders().forEach(keelIssueRecorder -> {
            if (issue.level().isEnoughSeriousAs(keelIssueRecorder.getVisibleLevel())) {
                keelIssueRecorder.issueRecordCenter().getAdapter().record(topic(), issue);
            }
        });
    }

    default void debug(@Nonnull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.DEBUG);
        });
    }

    default void info(@Nonnull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.INFO);
        });
    }

    default void notice(@Nonnull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.NOTICE);
        });
    }

    default void warning(@Nonnull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.WARNING);
        });
    }

    default void error(@Nonnull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.ERROR);
        });
    }

    default void fatal(@Nonnull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.FATAL);
        });
    }

    default void exception(@Nonnull Throwable throwable, @Nonnull Handler<T> issueHandler) {
        error(t -> {
            t.exception(throwable);
            issueHandler.handle(t);
        });
    }

    default void exception(@Nonnull Throwable throwable, @Nonnull String message) {
        exception(throwable, t -> t.message(message));
    }

    default void exception(@Nonnull Throwable throwable) {
        exception(throwable, t -> {
        });
    }

    default void debug(@Nonnull String message) {
        debug(t -> t.message(message));
    }

    default void info(@Nonnull String message) {
        info(t -> t.message(message));
    }

    default void notice(@Nonnull String message) {
        notice(t -> t.message(message));
    }

    default void warning(@Nonnull String message) {
        warning(t -> t.message(message));
    }

    default void error(@Nonnull String message) {
        error(t -> t.message(message));
    }

    default void fatal(@Nonnull String message) {
        fatal(t -> t.message(message));
    }

}
