package io.josh.akkastarter.discovery

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors
import io.josh.akkastarter.discovery.PingService.Pong



object PingService {
  val PingServiceKey = ServiceKey[Ping]("pingService")

  final case class Ping(replyTo: ActorRef[Pong.type])
  final case object Pong

  def apply(): Behavior[Ping] = {
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.register(PingServiceKey, context.self)

      Behaviors.receiveMessage {
        case Ping(replyTo) =>
          context.log.info(s"Pinged By ${replyTo}")
          replyTo ! Pong
          Behaviors.same
      }
    }
  }
}


object Pinger {
  def apply(pingService: ActorRef[PingService.Ping]): Behavior[PingService.Pong.type] = {
    Behaviors.setup { context =>
      pingService ! PingService.Ping(context.self)

      Behaviors.receiveMessage { _ =>
        context.log.info(s"${context.self} was ponged!!")
        Behaviors.stopped
      }

    }
  }
}

object PingManager {
  sealed trait Command
  case object PingAll extends Command
  private case class ListingResponse(listing: Receptionist.Listing) extends Command

  def apply() = {
    Behaviors.setup[Command] { context =>
      val listingResponseAdapter = context.messageAdapter[Receptionist.Listing](ListingResponse)

      context.spawnAnonymous(PingService())

      Behaviors.receiveMessage {
        case PingAll =>
          context.system.receptionist ! Receptionist.Find(PingService.PingServiceKey, listingResponseAdapter)
          Behaviors.same
        case ListingResponse(PingService.PingServiceKey.Listing(listings)) =>
          listings.foreach(ps => context.spawnAnonymous(Pinger(ps)))
          Behaviors.same
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem(PingManager(), "PingManager")
    system ! PingAll
  }
}


object Guardian {
  def apply(): Behavior[Nothing] = {
    Behaviors
      .setup[Receptionist.Listing] { context =>
        context.spawnAnonymous(PingService())
        context.system.receptionist ! Receptionist.Subscribe(PingService.PingServiceKey, context.self)


        Behaviors.receiveMessagePartial[Receptionist.Listing] {
          case PingService.PingServiceKey.Listing(listings) =>
            listings.foreach(ps => context.spawnAnonymous(Pinger(ps)))
            Behaviors.same
        }
      }
  }.narrow

  def main(args: Array[String]): Unit = {
    ActorSystem[Nothing](Guardian(), "Ping")
  }
}
