package io.swagger.server.api;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.phiz71.vertx.swagger.router.OperationIdServiceIdResolver;
import com.github.phiz71.vertx.swagger.router.SwaggerRouter;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


@SuppressWarnings("CommentedOutCode") // According to the comment we need to leave the snippet
public class MainApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(MainApiVerticle.class);

    protected Router router;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        router = Router.router(vertx);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Json.mapper.registerModule(new JavaTimeModule());
        FileSystem vertxFileSystem = vertx.fileSystem();
        vertxFileSystem.readFile("swagger.json", readFile -> {
            if (readFile.succeeded()) {
                Swagger swagger = new SwaggerParser().parse(readFile.result().toString(StandardCharsets.UTF_8));
                Router swaggerRouter = SwaggerRouter.swaggerRouter(router, swagger, vertx.eventBus(), new OperationIdServiceIdResolver(), new Function<RoutingContext, DeliveryOptions>() {
                    @Override
                    public DeliveryOptions apply(RoutingContext t) {
                        return new DeliveryOptions().setSendTimeout(90000);
                    }
                });
                deployVerticles(startFuture);

                vertx.createHttpServer()
                    .requestHandler(swaggerRouter::accept)
                    .listen(8080);
                startFuture.complete();
            } else {
            	startFuture.fail(readFile.cause());
            }
        });
    }

    public void deployVerticles(Future<Void> startFuture) {

        vertx.deployVerticle("io.swagger.server.api.verticle.DeviceApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("DeviceApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("DeviceApiVerticle : Deployment failed");
            }
        });

        /*
        //
        // We will most likely never implement this verticle because it doesn't add value.
        // however, it still needs to exist in the swagger (so we can use the node implementation),
        // so it ends up in the generated code.  It should probably be removed from the swagger, but
        // that's a bigger change.
        //
        vertx.deployVerticle("io.swagger.server.api.verticle.EventhubApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("EventhubApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("EventhubApiVerticle : Deployment failed");
            }
        });
        */

        vertx.deployVerticle("io.swagger.server.api.verticle.ModuleApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("ModuleApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("ModuleApiVerticle : Deployment failed");
            }
        });

        vertx.deployVerticle("io.swagger.server.api.verticle.RegistryApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("RegistryApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("RegistryApiVerticle : Deployment failed");
            }
        });

        vertx.deployVerticle("io.swagger.server.api.verticle.ServiceApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("ServiceApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("ServiceApiVerticle : Deployment failed");
            }
        });

        vertx.deployVerticle("io.swagger.server.api.verticle.WrapperApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("WrapperApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("WrapperApiVerticle : Deployment failed");
            }
        });


    }
}