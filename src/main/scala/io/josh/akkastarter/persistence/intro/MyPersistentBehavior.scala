package io.josh.akkastarter.persistence.intro

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior

class MyPersistentBehavior {
  sealed trait Command
  sealed trait Event
  final case class State()

  /**
   * [commandHandler] defines how to handle command by producing [Effects] e.g. persisting events, stopping the persistent actor.
   * [eventHandler] returns the new state given the current state when a event has been persisted
   */

  def apply(): Behavior[Command] =

    EventSourcedBehavior[Command, Event, State] (
      persistenceId = PersistenceId.ofUniqueId("abc"),
      emptyState = State(),
      commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event & return an next state"),
    )


}
