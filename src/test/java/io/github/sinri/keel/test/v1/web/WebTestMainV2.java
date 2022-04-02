package io.github.sinri.keel.test.v1.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.test.v1.web.controller.UrlRuleController;
import io.github.sinri.keel.web.KeelHttpServer;
import io.github.sinri.keel.web.fastdocs.KeelFastDocsKit;
import io.github.sinri.keel.web.routing.KeelControllerStyleRouterKit;
import io.github.sinri.keel.web.routing.KeelUrlRuleRouterKit;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;

public class WebTestMainV2 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelHttpServer khs = new KeelHttpServer(Keel.getVertx(), new HttpServerOptions().setPort(14000), true);

        // static route
        khs.getRouter().get("/").handler(ctx -> ctx.response().end("HERE IS ROOT"));
        // automatic Controller - Method route
        KeelControllerStyleRouterKit.installToRouter(
                        khs.getRouter(),
                        "/api/",
                        "io.github.sinri.keel.test.web.controller"
                )
                .setLogger(Keel.outputLogger("KeelControllerStyleRouterKit"));

        // static content: web_root is under `resources` directory
        khs.getRouter()
                .route("/static/*")
                .handler(StaticHandler.create("web_root"));

        // KeelFastDocs
        KeelFastDocsKit.installFastDocsToRouter(
                khs.getRouter(),
                "/fastdocs/",
                "web_root/fastdocs/",
                "FastDocsSample",
                "Copyright 2022-now Sinri Edogawa",
                Keel.logger("FastDocs")
        );

        // url based
        KeelUrlRuleRouterKit.installToRouter(khs.getRouter())
                .setLogger(Keel.outputLogger("KeelUrlRuleRouterKit"))
                .registerClass(UrlRuleController.class, new ArrayList<>());

        khs.listen();
    }
}
