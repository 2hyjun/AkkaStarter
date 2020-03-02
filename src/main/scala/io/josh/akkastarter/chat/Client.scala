package io.josh.akkastarter.chat

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Client {
  import ChatRoom._

  def apply(id: Int, currentSession: Option[ActorRef[SessionCommand]] = Option.empty): Behavior[ClientEvent] =
    Behaviors.logMessages {
      Behaviors.setup { context =>
        Behaviors.receiveMessage {
          case SessionGranted(handle) =>
            handle ! PostMessage("Hello World!", context.self)
            Client(id, Option(handle))
          case MessagePosted(screenName, message) =>
            context.log.info(s"message has been posted by '${screenName}'\n ${"\t" * id}${message}")
            Behaviors.same
          case ClientFollowed(client) =>
            currentSession.get!ChatRoom.JoinClient(client)
            Behaviors.same
        }
      }
    }

}
