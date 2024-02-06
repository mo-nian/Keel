package io.github.sinri.keel.logger.issue;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;

/**
 * @param <T> The class defines a kind of issue, with fixed properties.
 * @since 3.1.9 Technical Preview
 */
@TechnicalPreview(since = "3.1.9")
public class KeelIssueLogger<T extends KeelIssueLog> {
    private final KeelEventLogger eventLogger;

    public KeelIssueLogger(KeelEventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public void issue(T issueLog) {
        this.eventLogger.log(issueLog::toEventLog);
    }
}
