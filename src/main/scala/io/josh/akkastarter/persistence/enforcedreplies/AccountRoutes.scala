package io.josh.akkastarter.persistence.enforcedreplies

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route

object AccountRoutes {
  import akka.http.scaladsl.server.Directives._

  def apply(account: AccountApplication)(implicit  system: ActorSystem[_]): Route = {
    concat(
      pathPrefix("account") {
        account.route()
      }
    )
  }
}
