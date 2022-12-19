package io.github.sinri.keel.web.http.handler;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * 这个类是个针对某一类RoutingContext的handler，而不是针对某一个RoutingContext的handler！！！
 *
 * @since 2.9
 * @since 2.9.2 Remove Property `RoutingContext`.
 * @since 3.0.0 TEST PASSED
 */
abstract public class KeelWebRequestHandler implements KeelHttpHandler {

    private final Keel keel;
    private final KeelEventLogger logger;

    public KeelWebRequestHandler(Keel keel) {
        this.keel = keel;
        this.logger = createLogger();
    }

    public Keel getKeel() {
        return keel;
    }

    abstract protected KeelEventLogger createLogger();

    public KeelEventLogger getLogger() {
        return logger;
    }

    protected void respondOnSuccess(RoutingContext routingContext, Object data) {
        JsonObject resp = new JsonObject()
                .put("code", "OK")
                .put("data", data);
        logger.info(eventLog -> eventLog
                .message("RESPOND SUCCESS")
                .put("response", resp));

        try {
            routingContext.json(resp);
        } catch (Throwable throwable) {
            logger.exception(throwable, eventLog -> eventLog
                    .message("RoutingContext has been dealt by others")
                    .put("response", new JsonObject()
                            .put("code", routingContext.response().getStatusCode())
                            .put("message", routingContext.response().getStatusMessage())
                            .put("ended", routingContext.response().ended())
                            .put("closed", routingContext.response().closed()))
            );
        }
    }

    protected void respondOnFailure(RoutingContext routingContext, Throwable throwable) {
        var x = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        String error = keel.stringHelper().renderThrowableChain(throwable);
        x.put("throwable", error);
        logger.error(eventLog -> eventLog
                .message("RESPOND FAILURE")
                .put("response", x)
        );
        try {
            routingContext.json(x);
        } catch (Throwable throwable2) {
            logger.exception(throwable2, eventLog -> eventLog
                    .message("RoutingContext has been dealt by others")
                    .put("response", new JsonObject()
                            .put("code", routingContext.response().getStatusCode())
                            .put("message", routingContext.response().getStatusMessage())
                            .put("ended", routingContext.response().ended())
                            .put("closed", routingContext.response().closed())
                    )
            );
        }
    }

    public final void handle(RoutingContext routingContext) {
        handleRequest(routingContext);
    }

    abstract protected void handleRequest(RoutingContext routingContext);
}
