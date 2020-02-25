package org.bolay.duplicati.monitoring.verticle

import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.impl.Utils

import io.vertx.kotlin.coroutines.CoroutineVerticle
import java.net.URLDecoder


class MonitoringVerticle : CoroutineVerticle() {
    override suspend fun start() {

        var server = vertx.createHttpServer()
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create());
        router.route("/").handler { routingContext ->
            println("root called")
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(rootHandler())
        }
        router.route(HttpMethod.POST, "/duplicati/:terminal/:user/:backup").handler { ctx ->

            println("duplicati called")
            val headers = ctx.request().headers()
            val params = ctx.request().params()
//            val body = ctx.getBodyAsString()
            val body = ctx.getBodyAsJson()
            body.put("terminal", ctx.request().getParam("terminal"))
            body.put("user", ctx.request().getParam("user"))
            body.put("backup", ctx.request().getParam("backup"))
            println("request header: " + headers)
            println("request params: " + params)
            println("body: " + body.encodePrettily())

            ctx.response().end()
        }

        server.requestHandler(router).listen(8080)
        println("Duplicati Monitoring Verticle verticle startet!")

    }

    override suspend fun stop() {
        super.stop()
    }

    fun rootHandler(): String {
        return "<h1>Hello from my first Vert.x 3 application</h1>"
    }

    fun duplicatiMonitoring() {

    }
}