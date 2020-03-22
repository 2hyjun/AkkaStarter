package io.josh.akkastarter.persistence.enforcedreplies

import akka.{ actor, Done }
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.{ Failure, Success }
import scala.concurrent.duration._

class HttpServer(routes: Route, port: Int, system: ActorSystem[_]) {
  import akka.actor.typed.scaladsl.adapter._
  implicit val classicSystem: actor.ActorSystem = system.toClassic
  private val shutdown = CoordinatedShutdown(classicSystem)

  import system.executionContext

  def start(): Unit = {
    Http().bindAndHandle(routes, "0.0.0.0", port).onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Http server online at http://{}:{}/", address.getHostString, address.getPort)
        shutdown.addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "http-graceful-terminate") { () =>
          binding.terminate(10.seconds).map { _ =>
            system.log
              .info("Http server http://{}:{}/ graceful shutdown completed", address.getHostString, address.getPort)
            Done
          }
        }
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminate system", ex)
        system.terminate
    }
  }
}
