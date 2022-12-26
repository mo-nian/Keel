package io.github.sinri.keel.web.http.receptionist;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.web.http.ApiMeta;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @param <R> class of a subclass of KeelWebReceptionist
 * @since 2.9.2
 * @since 3.0.0 TEST PASSED
 */
public class KeelWebReceptionistKit<R extends KeelWebReceptionist> {
    private final Router router;
    private final Class<R> classOfReceptionist;
    private final List<PlatformHandler> platformHandlers = new ArrayList<>();

    private final List<SecurityPolicyHandler> securityPolicyHandlers = new ArrayList<>();
    private final List<ProtocolUpgradeHandler> protocolUpgradeHandlers = new ArrayList<>();
    private final List<MultiTenantHandler> multiTenantHandlers = new ArrayList<>();
    /**
     * Tells who the user is
     */
    private final List<AuthenticationHandler> authenticationHandlers = new ArrayList<>();
    private final List<InputTrustHandler> inputTrustHandlers = new ArrayList<>();
    /**
     * Tells what the user is allowed to do
     */
    private final List<AuthorizationHandler> authorizationHandlers = new ArrayList<>();
    private final List<Handler<RoutingContext>> userHandlers = new ArrayList<>();
    private String uploadDirectory = BodyHandler.DEFAULT_UPLOADS_DIRECTORY;
    private String virtualHost = null;
    /**
     * @since 2.9.2
     */
    private Handler<RoutingContext> failureHandler = null;
    private final KeelEventLogger logger;

    public KeelWebReceptionistKit(Class<R> classOfReceptionist, Router router) {
        this.classOfReceptionist = classOfReceptionist;
        this.router = router;
        this.logger = KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName());
    }

    public void loadPackage(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends R>> allClasses = reflections.getSubTypesOf(classOfReceptionist);

        try {
            allClasses.forEach(this::loadClass);
        } catch (Exception e) {
            logger.exception(e, getClass().getName() + "::loadPackage THROWS");
        }
    }

    public void loadClass(Class<? extends R> c) {
        ApiMeta apiMeta = KeelHelpers.reflectionHelper().getAnnotationOfClass(c, ApiMeta.class);
        if (apiMeta == null) return;

        logger.debug(getClass().getName() + " Loading " + c.getName());

        Constructor<? extends R> receptionistConstructor;
        try {
            receptionistConstructor = c.getConstructor(RoutingContext.class);
        } catch (NoSuchMethodException e) {
            logger.exception(e, "HANDLER REFLECTION EXCEPTION");
            return;
        }

        Route route = router.route(apiMeta.routePath());

        if (apiMeta.allowMethods() != null) {
            for (var methodName : apiMeta.allowMethods()) {
                route.method(HttpMethod.valueOf(methodName));
            }
        }

        if (apiMeta.virtualHost() != null && !apiMeta.virtualHost().equals("")) {
            route.virtualHost(apiMeta.virtualHost());
        } else if (this.virtualHost != null && !Objects.equals("", this.virtualHost)) {
            route.virtualHost(this.virtualHost);
        }

        // === HANDLERS WEIGHT IN ORDER ===
        // PLATFORM
        route.handler(new KeelPlatformHandler());
        if (apiMeta.timeout() > 0) {
            // PlatformHandler
            route.handler(TimeoutHandler.create(apiMeta.timeout(), apiMeta.statusCodeForTimeout()));
        }
        route.handler(ResponseTimeHandler.create());
        this.platformHandlers.forEach(route::handler);

        //    SECURITY_POLICY,
        // SecurityPolicyHandler
        // CorsHandler: Cross Origin Resource Sharing
        this.securityPolicyHandlers.forEach(route::handler);

        //    PROTOCOL_UPGRADE,
        protocolUpgradeHandlers.forEach(route::handler);
        //    BODY,
        if (apiMeta.requestBodyNeeded()) {
            route.handler(BodyHandler.create(uploadDirectory));
        }
        //    MULTI_TENANT,
        multiTenantHandlers.forEach(route::handler);
        //    AUTHENTICATION,
        authenticationHandlers.forEach(route::handler);
        //    INPUT_TRUST,
        inputTrustHandlers.forEach(route::handler);
        //    AUTHORIZATION,
        authorizationHandlers.forEach(route::handler);
        //    USER
        userHandlers.forEach(route::handler);

        // finally!
        route.handler(routingContext -> {
            try {
                R receptionist = receptionistConstructor.newInstance(routingContext);
                //receptionist.setApiMeta(apiMeta);
                receptionist.handle();
            } catch (Throwable e) {
                routingContext.fail(e);
            }
        });

        // failure handler since 2.9.2
        if (failureHandler != null) {
            route.failureHandler(failureHandler);
        }
    }

    /**
     * @since 2.9.2
     */
    public KeelWebReceptionistKit<R> setFailureHandler(Handler<RoutingContext> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }


    public KeelWebReceptionistKit<R> addPlatformHandler(PlatformHandler handler) {
        this.platformHandlers.add(handler);
        return this;
    }

    public KeelWebReceptionistKit<R> addSecurityPolicyHandler(SecurityPolicyHandler handler) {
        this.securityPolicyHandlers.add(handler);
        return this;
    }

    public KeelWebReceptionistKit<R> addProtocolUpgradeHandler(ProtocolUpgradeHandler handler) {
        this.protocolUpgradeHandlers.add(handler);
        return this;
    }

    public KeelWebReceptionistKit<R> addMultiTenantHandler(MultiTenantHandler handler) {
        this.multiTenantHandlers.add(handler);
        return this;
    }

    /**
     * 追加一个认证校验器
     */
    public KeelWebReceptionistKit<R> addAuthenticationHandler(AuthenticationHandler handler) {
        this.authenticationHandlers.add(handler);
        return this;
    }

    public KeelWebReceptionistKit<R> addInputTrustHandler(InputTrustHandler handler) {
        this.inputTrustHandlers.add(handler);
        return this;
    }

    /**
     * 追加一个授权校验器
     */
    public KeelWebReceptionistKit<R> addAuthorizationHandler(AuthorizationHandler handler) {
        this.authorizationHandlers.add(handler);
        return this;
    }

    public KeelWebReceptionistKit<R> addUserHandler(Handler<RoutingContext> handler) {
        this.userHandlers.add(handler);
        return this;
    }

    public KeelWebReceptionistKit<R> setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
        return this;
    }

    /**
     * @since 2.9
     */
    public KeelWebReceptionistKit<R> setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

}
