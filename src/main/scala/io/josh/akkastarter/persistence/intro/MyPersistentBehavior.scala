package io.josh.akkastarter.persistence.intro

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }

class MyPersistentBehavior {
  sealed trait Command
  final case class Add(data: String) extends Command
  case object Clear extends Command

  sealed trait Event
  final case class Added(data: String) extends Event
  case object Cleared extends Event

  final case class State(history: List[String] = Nil)

  /**
   * [commandHandler] defines how to handle command by producing [Effects] e.g. persisting events, stopping the persistent actor.
   * [eventHandler] returns the new state given the current state when a event has been persisted
   */
  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    // command 가 들어오면 event 저장함.
    // Add -> Added
    // Clear -> Cleared
    command match {
      case Add(data) => Effect.persist(Added(data))
      case Clear     => Effect.persist(Cleared)
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case Added(data) => state.copy((data :: state.history).take(5))
      case Cleared     => State(Nil)
    }
  }

  /**
   * `Behavior` of a persistent actor is typed [Command]
   * @return
   */
  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("abc"), // stable unique identifier for the persistent actor
      emptyState = State(Nil),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )

}
