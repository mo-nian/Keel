package io.github.sinri.keel.web.tcp;

import io.github.sinri.keel.logger.issue.record.event.RoutineBaseIssueRecord;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.2.0
 */
public final class SocketIssueRecord extends RoutineBaseIssueRecord<SocketIssueRecord> {
    public static final String TopicTcpSocket = "TcpSocket";

    public SocketIssueRecord() {
        super(TopicTcpSocket);
    }

    @Nonnull
    @Override
    public SocketIssueRecord getImplementation() {
        return this;
    }

    public SocketIssueRecord buffer(@Nonnull Buffer buffer) {
        this.context("buffer", new JsonObject()
                .put("buffer_content", KeelHelpers.binaryHelper().encodeHexWithUpperDigits(buffer))
                .put("buffer_size", buffer.length())
        );
        return this;
    }
}
