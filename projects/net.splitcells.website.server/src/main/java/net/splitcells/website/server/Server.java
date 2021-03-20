package net.splitcells.website.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.web.Router;
import net.splitcells.website.server.renderer.RenderingResult;

import java.util.function.Function;

public class Server {
    public void serveToHttpAt(int port, boolean useHttps, Function<String, RenderingResult> renderer) {
        {
            System.setProperty("vertx.disableFileCaching", "true");
            System.setProperty("log4j.rootLogger", "DEBUG, stdout");
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new AbstractVerticle() {
                @Override
                public void start() {
                    // TODO Errors are not logged.
                    final var webServerOptions = new HttpServerOptions()//
                            .setLogActivity(true)//
                            .setSsl(useHttps)//
                            .setKeyCertOptions(new PfxOptions().setPath("src/main/resources.private/keystore.p12").setPassword("password"))//
                            .setTrustOptions(new PfxOptions().setPath("src/main/resources.private/keystore.p12").setPassword("password"))//
                            .setPort(port);//
                    final var router = Router.router(vertx);
                    router.route("/favicon.ico").handler(a -> {
                    });
                    router.route("/*").handler(routingContext -> {
                        HttpServerResponse response = routingContext.response();
                        vertx.<byte[]>executeBlocking((promise) -> {
                            final String requestPath;
                            if ("".equals(routingContext.request().path()) || "/".equals(routingContext.request().path())) {
                                requestPath = "index.html";
                            } else {
                                requestPath = routingContext.request().path();
                            }
                            final var result = renderer.apply(requestPath);
                            response.putHeader("content-type", result.getFormat());
                            promise.complete(result.getContent());
                        }, (result) -> {
                            if (result.failed()) {
                                result.cause().printStackTrace();
                                response.setStatusCode(500);
                                response.end();
                            } else {
                                response.end(Buffer.buffer().appendBytes(result.result()));
                            }
                        });
                    });
                    router.errorHandler(500, e -> {
                        e.failure().printStackTrace();
                    });
                    final var server = vertx.createHttpServer(webServerOptions);//
                    server.requestHandler(router);//
                    server.listen();
                }
            });
        }
    }

    public void serveAsAuthenticatedHttpsAt(int port, Function<String, RenderingResult> renderer) {
        {
            System.setProperty("vertx.disableFileCaching", "true");
            System.setProperty("log4j.rootLogger", "DEBUG, stdout");
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new AbstractVerticle() {
                @Override
                public void start() {
                    // TODO Errors are not logged.
                    final var webServerOptions = new HttpServerOptions()//
                            .setSsl(true)//
                            .setKeyCertOptions(new PfxOptions().setPath("src/main/resources.private/keystore.p12").setPassword("password"))//
                            .setTrustOptions(new PfxOptions().setPath("src/main/resources.private/keystore.p12").setPassword("password"))//
                            .setClientAuth(ClientAuth.REQUIRED)//
                            .setLogActivity(true)//
                            .setPort(port);//
                    final var router = Router.router(vertx);
                    router.route("/favicon.ico").handler(a -> {
                    });
                    router.route("/*").handler(routingContext -> {
                        HttpServerResponse response = routingContext.response();
                        vertx.<byte[]>executeBlocking((promise) -> {
                            final String requestPath;
                            if ("".equals(routingContext.request().path()) || "/".equals(routingContext.request().path())) {
                                requestPath = "index.html";
                            } else {
                                requestPath = routingContext.request().path();
                            }
                            final var result = renderer.apply(requestPath);
                            response.putHeader("content-type", result.getFormat());
                            promise.complete(result.getContent());
                        }, (result) -> {
                            if (result.failed()) {
                                result.cause().printStackTrace();
                                response.setStatusCode(500);
                                response.end();
                            } else {
                                response.end(Buffer.buffer().appendBytes(result.result()));
                            }
                        });
                    });
                    router.errorHandler(500, e -> {
                        e.failure().printStackTrace();
                    });
                    final var server = vertx.createHttpServer(webServerOptions);//
                    server.requestHandler(router);//
                    server.listen();
                }
            });
        }
    }
}