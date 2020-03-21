package io.josh.akkastarter.persistence.enforcedreplies

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import io.josh.akkastarter.CborSerializable

object Account {
  val name = "Account"
  val EntityKey : EntityTypeKey[Command[OperationResult]] = EntityTypeKey[Command[OperationResult]](name)
  sealed trait Command[Reply <: CommandReply] extends CborSerializable {
    def replyTo: ActorRef[Reply]
  }

  final case class Withdraw(amount: BigDecimal, replyTo: ActorRef[OperationResult]) extends Command[OperationResult]
  final case class CloseAccount(replyTo: ActorRef[OperationResult]) extends Command[OperationResult]

  sealed trait State
  final case class OpenedAccount(balance: BigDecimal) extends State {
    def canWithdraw(balance: BigDecimal): Boolean = this.balance > balance
  }

  final case object ClosedAccount extends State

  sealed trait CommandReply extends CborSerializable
  sealed trait OperationResult extends CommandReply

  case object Confirmed extends OperationResult
  final case class Rejected(reason: String) extends OperationResult

  sealed trait Event
  case object AccountClosed extends Event
  final case class Withdrawn(amount: BigDecimal) extends Event

  val commandHandler: (State, Command[OperationResult]) => ReplyEffect[Event, State] = { (state, command) =>
    state match {
      case ClosedAccount => Effect.unhandled.thenNoReply
      case openedAccount: OpenedAccount => {
        command match {
          case cmd: CloseAccount => closeAccount(cmd)
          case cmd: Withdraw     => withdraw(openedAccount, cmd)
        }
      }
    }
  }

  private def closeAccount(command: CloseAccount): ReplyEffect[Event, State] = {
    val event = AccountClosed
    Effect.persist(event).thenReply(command.replyTo)(_ => Confirmed)
  }

  private def withdraw(account: OpenedAccount, command: Withdraw): ReplyEffect[Event, State] = {
    if (account.canWithdraw(command.amount)) {
      val event = Withdrawn(command.amount)
      Effect.persist(event).thenReply(command.replyTo)(_ => Confirmed)
    } else {
      Effect.reply(command.replyTo)(
        Rejected(s"Insufficient balance ${account.balance} requested amount: ${command.amount}"))
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    state match {
      case ClosedAccount =>
        throw new IllegalStateException(s"unexpected event [$event] in state [$state]")
      case acc: OpenedAccount =>
        event match {
          case AccountClosed => ClosedAccount
          case Withdrawn(amount) => OpenedAccount(acc.balance - amount)
        }
    }
  }

  def init()(implicit system: ActorSystem[_]): Unit = {
    ClusterSharding(system).init(Entity(EntityKey) { entityContext =>
      Account(entityContext.entityId, PersistenceId(EntityKey.name, entityContext.entityId))
    })
  }

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command[OperationResult]] = {
    Behaviors.setup { context =>
      context.log.info(s"${entityId}, $persistenceId created")
      EventSourcedBehavior.withEnforcedReplies(
        persistenceId,
        OpenedAccount(BigDecimal(0)),
        commandHandler,
        eventHandler
      )
    }
  }
}
