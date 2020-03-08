package io.josh.akkastarter.interactions

import akka.actor.typed.{ActorRef, ActorSystem}
import io.josh.akkastarter.interactions.Printer.PrintMe

object Main {
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[Printer.PrintMe] = ActorSystem(Printer(), "printer")
    val printer: ActorRef[Printer.PrintMe] = system

    printer ! PrintMe("hello")
    printer ! PrintMe("WOW")
  }
}
