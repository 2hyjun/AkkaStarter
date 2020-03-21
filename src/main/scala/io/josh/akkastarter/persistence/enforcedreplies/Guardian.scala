package io.josh.akkastarter.persistence.enforcedreplies

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.{ Config, ConfigFactory }

object Guardian {
  def apply(): Behavior[NotUsed] = Behaviors.setup { context =>
    implicit val system: ActorSystem[Nothing] = context.system
    Account.init()
    Behaviors.empty
  }

  def initConfig(port: Int = 24895): Config = ConfigFactory.parseString(s"""
       akka.remote.artery.canonical.port = $port
       """).withFallback(ConfigFactory.load())

  def main(args: Array[String]): Unit = {
    val config = initConfig()

    ActorSystem[NotUsed](Guardian(), config.getString("app.name"), config)
  }
}
