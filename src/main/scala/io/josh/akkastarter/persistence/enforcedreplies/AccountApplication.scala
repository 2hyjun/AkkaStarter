package io.josh.akkastarter.persistence.enforcedreplies

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.josh.akkastarter.persistence.enforcedreplies.BoardApplication.CreateRequest

import scala.concurrent.Future
import scala.concurrent.duration._

class AccountApplication()(implicit val system: ActorSystem[_]) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import akka.http.scaladsl.server.Directives._
  import io.josh.akkastarter.persistence.enforcedreplies.Account._

  implicit val timeout: Timeout = 3.seconds
  implicit val sharding: ClusterSharding = ClusterSharding(system)
  implicit val jsonEntityStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  def route(): Route =
    concat (
      post {
        create()
      }
    )

  private def create() = entity(as[CreateRequest]) { data =>
    val entityRef = sharding.entityRefFor(EntityKey, "hello~~~")
    val reply: Future[OperationResult] = entityRef.ask(Withdraw(100, _))
    onSuccess(reply) {
      case Account.Confirmed => complete(StatusCodes.OK -> "good")
      case Rejected(reason) =>complete(StatusCodes.BadRequest -> reason)
    }
  }
}

object BoardApplication {
  import spray.json.RootJsonFormat
  import io.josh.akkastarter.persistence.enforcedreplies.Account._
  import spray.json.DefaultJsonProtocol._

  final case class CreateRequest()

  implicit val createRequestFormat: RootJsonFormat[CreateRequest] = jsonFormat0(CreateRequest)
}