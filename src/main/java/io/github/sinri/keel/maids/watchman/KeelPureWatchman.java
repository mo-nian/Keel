package io.github.sinri.keel.maids.watchman;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.ThreadingModel;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * It might be used like KeelEndless in standalone mode with Promise and clustered lock.
 *
 * @since 2.9.3
 */
public class KeelPureWatchman extends KeelWatchmanImpl {

    private final Options options;

    protected KeelPureWatchman(String watchmanName, Options options) {
        super(watchmanName);
        this.options = options;
    }

    public static Future<String> deploy(String watchmanName, Handler<Options> optionsHandler) {
        Options options = new Options();
        optionsHandler.handle(options);
        KeelPureWatchman keelPureWatchman = new KeelPureWatchman(watchmanName, options);
        return Keel.getVertx().deployVerticle(keelPureWatchman, new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER)
        );
    }

    public KeelWatchmanEventHandler regularHandler() {
        return options.getHandler();
    }

    @Override
    public long interval() {
        return options.getInterval();
    }

    /**
     * @since 3.2.0
     */
    @Override
    protected KeelEventLogger buildEventLogger() {
        return KeelIssueRecordCenter.silentCenter().generateEventLogger(getClass().getName());
    }

    public static class Options {
        private KeelWatchmanEventHandler handler;
        private long interval = 60_000L;

        public Options() {
            this.handler = event -> {
            };
        }

        public KeelWatchmanEventHandler getHandler() {
            return handler;
        }

        public Options setHandler(KeelWatchmanEventHandler handler) {
            this.handler = handler;
            return this;
        }

        public long getInterval() {
            return interval;
        }

        public Options setInterval(long interval) {
            this.interval = interval;
            return this;
        }

    }
}
