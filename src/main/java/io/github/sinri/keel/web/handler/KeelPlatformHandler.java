package io.github.sinri.keel.web.handler;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.shareddata.Counter;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.PlatformHandler;

import java.util.*;

/**
 * @since 2.9.2
 */
public class KeelPlatformHandler implements PlatformHandler {
    public final static String KEEL_REQUEST_ID = "KEEL_REQUEST_ID"; // -> String
    public final static String KEEL_REQUEST_START_TIME = "KEEL_REQUEST_START_TIME"; // -> long * 0.001 second
    public final static String KEEL_REQUEST_CLIENT_IP_CHAIN = "KEEL_REQUEST_CLIENT_IP_CHAIN"; // -> List<String of IP>

    public final static String KEEL_PRIVILEGE_SET_IN_API_META = "KEEL_PRIVILEGE_SET_IN_API_META";// -> Set<String of Privilege>
    private final Set<String> privilegeSetForAuthorization;

    public KeelPlatformHandler(String[] privilegeSetForAuthorization) {
        this.privilegeSetForAuthorization = new HashSet<>();
        if (privilegeSetForAuthorization != null) {
            this.privilegeSetForAuthorization.addAll(Arrays.asList(privilegeSetForAuthorization));
        }
    }

    @Override
    public void handle(RoutingContext routingContext) {
        // BEFORE ASYNC PAUSE
        routingContext.request().pause();
        // START !
        Keel.getVertx().sharedData()
                .getCounter("KeelPlatformHandler-RequestID-Counter")
                .compose(Counter::incrementAndGet)
                .recover(throwable -> {
                    return Future.succeededFuture(new Random().nextLong() * -1);
                })
                .compose(id -> {
                    routingContext.put(KEEL_REQUEST_ID, Keel.helpers().net().getLocalHostAddress() + "[" + id + "]" + UUID.randomUUID());

                    routingContext.put(KEEL_REQUEST_START_TIME, System.currentTimeMillis());
                    routingContext.put(KEEL_REQUEST_CLIENT_IP_CHAIN, Keel.helpers().net().parseWebClientIPChain(routingContext));

                    return Future.succeededFuture();
                })
                .andThen(v -> {
                    // RESUME
                    routingContext.request().resume();
                    // NEXT !
                    routingContext.next();
                });
    }
}
