package actions

import models.SessionModel
import play.api.mvc._
import utils.JwtUtil.validateToken
import utils.ConsoleMessage.logMessage
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedAction @Inject() (
    val parser: BodyParsers.Default,
    sessionModel: SessionModel
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[Request, AnyContent]
    with ActionFilter[Request] {

  override protected def filter[A](
      request: Request[A]
  ): Future[Option[Result]] = {
    request.cookies.get("session") match {
      case Some(cookie) =>
        validateToken(cookie.value) match {
          case Some(claim) =>
            // Parse the claim as JSON
            val jsonClaim = Json.parse(claim.content)
            (jsonClaim \ "userId").asOpt[String] match {
              case Some(userId) =>
                Future.successful(
                  Some(Results.Redirect(s"/${userId}"))
                )
              case None =>
                logMessage("Invalid token: userId missing.")
                Future.successful(Some(Results.Redirect("/login")))
            }

          case None =>
            logMessage("Invalid JWT token.")
            Future.successful(Some(Results.Redirect("/login")))
        }

      case None =>
        logMessage("No session token found.")
        Future.successful(Some(Results.Redirect("/login")))
    }
  }
}
