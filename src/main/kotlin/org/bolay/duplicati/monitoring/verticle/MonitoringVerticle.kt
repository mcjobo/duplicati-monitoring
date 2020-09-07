package org.bolay.duplicati.monitoring.verticle

import io.netty.handler.codec.mqtt.MqttQoS
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.mqtt.MqttClient
import io.vertx.mqtt.messages.MqttConnAckMessage


class MonitoringVerticle : CoroutineVerticle() {
    var mqttHost = System.getProperty("MQTT_HOST")

    val mqttClient = MqttClient.create(Vertx.vertx())
    override suspend fun start() {
        mqttClient.connect(1883, mqttHost) { connection ->
            if (connection.succeeded()) {
                println("connection to mqtt succeded")
            }
        }

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
            duplicatiMonitoring(body)
            ctx.response().end()
        }
        var listenPort = System.getProperty("LISTEN_PORT")
        var port = (listenPort as String).toInt()
        server.requestHandler(router).listen(port)
        println("Duplicati Monitoring Verticle verticle startet!")
        println("listening on port: " + port)

    }

    override suspend fun stop() {
        super.stop()
    }

    fun rootHandler(): String {
        return "<h1>Hello from my first Vert.x 3 application</h1>"
    }

    fun duplicatiMonitoring(result: JsonObject) {


        val baseTopic = "/backup/duplicati/monitoring/${result.getString("terminal")}/"
        val succesfulString = result.getJsonObject("Data").getString("ParsedResult")

        mqttClient.publish(baseTopic + "lastResult", Buffer.buffer(succesfulString), MqttQoS.EXACTLY_ONCE, false, false)
        mqttClient.publish(baseTopic + "lastDate", Buffer.buffer(result.getJsonObject("Data").getString("EndTime")), MqttQoS.EXACTLY_ONCE, false, false)
        if(succesfulString == "Success"){
            mqttClient.publish(baseTopic + "lastSuccesfullDate", Buffer.buffer(result.getJsonObject("Data").getString("EndTime")), MqttQoS.EXACTLY_ONCE, false, false)
            mqttClient.publish(baseTopic + "lastSuccesfullNumberFiles", Buffer.buffer(result.getJsonObject("Data").getInteger("ExaminedFiles").toString()), MqttQoS.EXACTLY_ONCE, false, false)
            mqttClient.publish(baseTopic + "lastSuccesfullFileSize", Buffer.buffer(result.getJsonObject("Data").getInteger("SizeOfExaminedFiles").toString()), MqttQoS.EXACTLY_ONCE, false, false)
        }
    }
}