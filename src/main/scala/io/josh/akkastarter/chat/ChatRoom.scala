package io.josh.akkastarter.chat

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }

object ChatRoom {

  sealed trait RoomCommand

  final case class GetSession(screenName: String, replyTo: ActorRef[ClientEvent]) extends RoomCommand

  private final case class PublishSessionMessage(screenName: String, message: String, from: ActorRef[ClientEvent]) extends RoomCommand

  sealed trait ClientEvent

  final case class SessionGranted(handle: ActorRef[SessionCommand]) extends ClientEvent

  final case class SessionDenied(reason: String) extends ClientEvent

  final case class MessagePosted(screenName: String, message: String, from: ActorRef[ClientEvent]) extends ClientEvent

  final case class ClientFollowed(client: ActorRef[ClientEvent]) extends ClientEvent

  trait SessionCommand

  final case class PostMessage(message: String, from: ActorRef[ClientEvent]) extends SessionCommand
  final case class JoinClient(client: ActorRef[ClientEvent]) extends SessionCommand
  private final case class NotifyClient(message: MessagePosted) extends SessionCommand

  def apply(): Behavior[RoomCommand] = chatRoom(List.empty)

  // The behavior that we declare here can handle both subtypes of RoomCommand
  // session Actor 로부터의 PublishSessionMessage 는 모든 연결된 클라이언트들에게 세션에 저장된 메세지를 전달한다.
  // 하지만 모든 임의의 client 들에게 PublishSessionMessage 명령을 할수있는 권한을 주고싶진 않기 때문에,
  // 우리가 생성한 내부 session actor 들의 권리를 보유한다.
  private def chatRoom(sessions: List[ActorRef[SessionCommand]]): Behavior[RoomCommand] = Behaviors.receive {
    (context, message) =>
      message match {
        // When a new GetSession command comes in
        // we add that client to the list that is in the returned behavior.
        case GetSession(screenName, client) =>
          // Then we also need to create the session’s ActorRef that will be used to post messages.
          val ses = context.spawn(
            session(context.self, screenName, List(client)),
            name = URLEncoder.encode(screenName, StandardCharsets.UTF_8.name))
          client ! SessionGranted(ses)
          chatRoom(ses :: sessions)
        case PublishSessionMessage(screenName, message, from) =>
          val notification = NotifyClient(MessagePosted(screenName, message, from))
          sessions.foreach(_ ! notification)
          Behaviors.same
      }
  }

  private def session(
      room: ActorRef[PublishSessionMessage],
      screenName: String,
      clients: List[ActorRef[ClientEvent]]): Behavior[SessionCommand] =
    Behaviors.receive { (context, msg) =>
      msg match {
        case PostMessage(message, from) =>
          // from client, publish to other via the room
          room ! PublishSessionMessage(screenName, message, from)
          Behaviors.same
        case NotifyClient(message) => {
          clients.foreach(_ ! message)
          Behaviors.same
        }
        case JoinClient(client) =>
          client ! SessionGranted(context.self)
          session(room, screenName, client :: clients)
      }
    }

}
