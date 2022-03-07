package com.microsoft.azure.sdk.iot.e2e;

import io.swagger.server.api.MainApiVerticle;
import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        MainApiVerticle myVerticle = new MainApiVerticle();

        vertx.deployVerticle(myVerticle, res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
            } else {
                System.out.println("Deployment failed!");
            }
        });
    }
}
