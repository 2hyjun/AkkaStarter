package io.josh.akkastarter.chat

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.remote.WireFormats.TimeUnit
import akka.util.Timeout
import io.josh.akkastarter.chat
import io.josh.akkastarter.chat.ChatRoom.{ClientEvent, ClientFollowed, GetSession, RoomCommand, SessionGranted}

import scala.concurrent.duration._
import scala.util.Try



object Main {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>

      implicit val timeout: Timeout = 3.seconds

      val chatRoom = context.spawn(ChatRoom(), "ChatRoom")
      val client1 = context.spawn(Client(4), "Client1")
      val client2 = context.spawn(Client(8), "Client2")

      context.child("")

      context.watch(client1)
      context.watch(client2)

      chatRoom ! GetSession("screen1", client1)
      Thread.sleep(1000)
      client1 ! ClientFollowed(client2)

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }

  def main(args: Array[String]): Unit = {
    ActorSystem(Main(), "ChatRoomExample")
  }
}
