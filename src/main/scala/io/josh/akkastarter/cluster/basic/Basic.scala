package io.josh.akkastarter.cluster.basic

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.typed.{Cluster, Join, Leave, Subscribe}

object Basic {
  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    Behaviors.same
  }
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[Nothing](Basic(), "basic")
    val cluster = Cluster(system)

    cluster.subscriptions ! Subscribe(system, classOf[MemberEvent])
  }
}
