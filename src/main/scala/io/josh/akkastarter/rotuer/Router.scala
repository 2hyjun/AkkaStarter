package io.josh.akkastarter.rotuer

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, Routers}

object Router {

}

object Worker {

  sealed trait Command

  case class DoLog(text: Int) extends Command

  val WorkerServiceKey = ServiceKey[Worker.Command]("log-worker")

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("Starting Worker")

    context.system.receptionist ! Receptionist.Register(WorkerServiceKey, context.self)

    Behaviors.receiveMessage {
      case DoLog(text) =>
        context.log.info(s"Got Message: [${text}]")
        Behaviors.same
    }
  }
}

object PoolRouter {
  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val pool = Routers.pool(4)(
      Behaviors.supervise(Worker()).onFailure[Exception](SupervisorStrategy.restart))

    val router = context.spawn(pool, "worker-pool")

    (0 to 10).foreach { n =>
      router ! Worker.DoLog(n)
    }

    Behaviors.same
  }

  def main(args: Array[String]): Unit = {
    ActorSystem[Nothing](PoolRouter(), "g")
  }
}

object GroupRouter {
  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
      context.spawnAnonymous(Worker())
      context.spawnAnonymous(Worker())
      context.spawnAnonymous(Worker())
      context.spawnAnonymous(Worker())

    val group = Routers.group(Worker.WorkerServiceKey)

    val router = context.spawn(group, "worker-group")

    (0 to 10).foreach(router ! Worker.DoLog(_))
    Behaviors.same
  }

  def main(args: Array[String]): Unit = {
    ActorSystem[Nothing](GroupRouter(), "W")
  }
}
