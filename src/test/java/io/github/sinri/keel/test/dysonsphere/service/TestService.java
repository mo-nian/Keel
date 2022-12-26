package io.github.sinri.keel.test.dysonsphere.service;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.web.http.ApiMeta;
import io.github.sinri.keel.web.http.handler.KeelWebRequestFutureHandler;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@ApiMeta(routePath = "/service/test", allowMethods = {"GET", "POST"})
public class TestService extends KeelWebRequestFutureHandler {

    @Override
    protected Future<Object> handleRequestForFuture(RoutingContext routingContext) {
        JsonObject jsonObject = new JsonObject().put("path", routingContext.request().path());
        return Future.succeededFuture(jsonObject);
    }

    @Override
    protected KeelEventLogger createLogger() {
        return KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName());
    }

}
