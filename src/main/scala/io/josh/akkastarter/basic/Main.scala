package io.josh.akkastarter.basic

import akka.actor.typed.ActorSystem
import org.slf4j.LoggerFactory

object Main {
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  def main(args: Array[String]): Unit = {
    logger.info("Start!")

    val system: ActorSystem[HelloWorldMain.SayHello] = ActorSystem(
      // system guardian
      HelloWorldMain(),
      "ActorSystem"
    )

    system ! HelloWorldMain.SayHello(name = "World")
  }
}
