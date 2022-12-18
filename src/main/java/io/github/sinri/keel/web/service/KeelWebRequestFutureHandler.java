package io.github.sinri.keel.web.service;

import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.9
 */
public abstract class KeelWebRequestFutureHandler extends KeelWebRequestHandler {


    public KeelWebRequestFutureHandler(Keel keel) {
        super(keel);
    }

    abstract protected Future<Object> handleRequestForFuture(RoutingContext routingContext);

    @Override
    public final void handleRequest(RoutingContext routingContext) {
        Future.succeededFuture()
                .compose(v -> handleRequestForFuture(routingContext))
                .andThen(ar -> {
                    if (ar.failed()) {
                        this.respondOnFailure(routingContext, ar.cause());
                    } else {
                        this.respondOnSuccess(routingContext, ar.result());
                    }
                });
    }
}
