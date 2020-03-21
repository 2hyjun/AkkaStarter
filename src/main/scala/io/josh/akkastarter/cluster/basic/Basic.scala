package io.josh.akkastarter.cluster.basic

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.typed.{Cluster, Join, Leave, Subscribe}

object Basic {
  def apply(): Behavior[MemberEvent] = Behaviors.setup[MemberEvent] { context =>
    Behaviors.same
  }
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[MemberEvent](Basic(), "basic")
    val cluster = Cluster(system)

    cluster.subscriptions ! Subscribe(system, classOf[MemberEvent])
  }
}
