package io.josh.akkastarter.ask

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.AskPattern._



object Hal {
  sealed trait Command
  case class OpenThePodBayDoorPlease(replyTo: ActorRef[Response]) extends Command
  case class Response(message: String)

  def apply(): Behaviors.Receive[Hal.Command] =
    Behaviors.receiveMessage {
      case OpenThePodBayDoorPlease(replyTo) =>
        replyTo ! Response("Sorry")
        Behaviors.same
    }
}

object Dave {
  sealed trait Command
  private case class AdaptedResponse(message: String) extends Command

  def apply(hal: ActorRef[Hal.Command]): Behavior[Dave.Command] = Behaviors.setup { context =>
    implicit val timeout: Timeout = 3.seconds

    context.ask(hal, (ref: ActorRef[Hal.Response]) => Hal.OpenThePodBayDoorPlease(ref)) {
      case Success(Hal.Response(message)) => AdaptedResponse(message)
      case Failure(_) => AdaptedResponse("Request Failed")
    }


    context.ask(hal, Hal.OpenThePodBayDoorPlease) {
      case Success(Hal.Response(message)) => AdaptedResponse(message)
      case Failure(_)                     => AdaptedResponse("Request failed")
    }

    val requestId = 1
    context.ask(hal, Hal.OpenThePodBayDoorPlease) {
      case Success(Hal.Response(message)) => AdaptedResponse(s"$requestId: $message")
      case Failure(_)                     => AdaptedResponse(s"$requestId: Request failed")
    }

    Behaviors.receiveMessage {
      case AdaptedResponse(message) =>
        context.log.info("Got response from hal: {}", message)
        Behaviors.same
    }
  }
}

object Ask {
  case class Empty()
  def apply(): Behavior[Empty] = Behaviors.setup { context =>
    val hal = context.spawn(Hal(), "hal")
    context.spawn(Dave(hal), "dave")

    Behaviors.same
  }
  def main(args: Array[String]): Unit = {
    ActorSystem(Ask(), "hal")
  }
}
