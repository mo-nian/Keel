package io.github.sinri.keel.test.lab.blocking;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.legacy.KeelEventLogger;
import io.github.sinri.keel.logger.event.legacy.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.*;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * 可以多线程运行。
 */
public class BlockingVerticlePlanB {
    private static Future<Void> executeBlocking(Handler<Promise<Void>> blockCode) {
        KeelIssueRecorder<KeelEventLog> issueRecorder = KeelIssueRecordCenter.outputCenter().generateRoutineIssueRecorder("Sample");
        Promise<Void> promise = Promise.promise();
        KeelVerticleBase<KeelEventLog> verticle = new KeelVerticleBase<>() {
            @Override
            public void start() throws Exception {
                this.setRoutineIssueRecorder(issueRecorder);

                getRoutineIssueRecorder().info(r -> r.message("in verticle " + deploymentID()));
                blockCode.handle(promise);

                promise.future()
                        .onComplete(ar -> {
                            this.undeployMe();
                        });
            }
        };
        return verticle.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .compose(deploymentId -> {
                    issueRecorder.info(r -> r.message("deployed: " + deploymentId));
                    return promise.future();
                });
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions())
                .compose(done -> {
                    KeelEventLogger loggerInEventLoopContext = KeelOutputEventLogCenter.getInstance().createLogger("Sample");

                    loggerInEventLoopContext.info(log -> log
                            .message("init")
                            .context(c -> c
                                    .put("thread_id", Thread.currentThread().getId())
                            )
                    );

                    return Future.all(
                                    blockPiece(loggerInEventLoopContext),
                                    blockPiece(loggerInEventLoopContext)
                            )
                            .compose(compositeFuture -> {
                                loggerInEventLoopContext.info("FIN");
                                return Future.succeededFuture();
                            });
                })

                .onFailure(throwable -> {
                    throwable.printStackTrace();
                })
                .eventually(() -> {
                    return Keel.close();
                });
    }

    private static Future<Void> blockPiece(KeelEventLogger loggerInEventLoopContext) {
        return executeBlocking(new Handler<Promise<Void>>() {
            @Override
            public void handle(Promise<Void> event) {
                loggerInEventLoopContext.info(log -> log
                        .message("blockPiece::handle before block")
                        .context(c -> c
                                .put("thread_id", Thread.currentThread().getId())
                        )
                );
                block(event);
            }
        });
    }

    private static void block(Promise<Void> promise) {
        KeelEventLogger loggerInBlockingContext = KeelOutputEventLogCenter.getInstance().createLogger("Sample");
        loggerInBlockingContext.info(log -> log.message("START")
                .context(c -> c.put("thread_id", Thread.currentThread().getId())));
        try {
            Thread.sleep(30_000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        loggerInBlockingContext.info(log -> log.message("END")
                .context(c -> c.put("thread_id", Thread.currentThread().getId())));
        promise.complete();
    }
}
