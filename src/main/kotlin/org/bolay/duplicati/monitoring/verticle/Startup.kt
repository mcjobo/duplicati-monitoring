package org.bolay.duplicati.monitoring.verticle

import io.vertx.core.Vertx

fun main() {
    var vertx = Vertx.vertx()
    vertx.deployVerticle(MonitoringVerticle())
}