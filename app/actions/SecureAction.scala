package actions

import models.SessionModel
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc._
import utils.ConsoleMessage.logMessage
import utils.JwtUtil.{
  createToken,
  extractPayloadFromExpiredToken,
  issueJwtCookie,
  validateToken
}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecureAction @Inject() (
    val parser: BodyParsers.Default,
    config: play.api.Configuration,
    sessionModel: SessionModel
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[Request, AnyContent]
    with ActionFilter[Request] {

  override protected def filter[A](
      request: Request[A]
  ): Future[Option[Result]] = {
    request.cookies.get("sessionToken") match {
      case Some(cookie) =>
        validateToken(cookie.value) match {
          case Some(claim) =>
            val jsonClaim = Json.parse(claim.content)
            (jsonClaim \ "userId").asOpt[Int] match {
              case Some(value) =>
                Future.successful(
                  None
                ) // Authentication successful, continue request
              case None =>
                // UserId field missing, the token is invalid
                Future.successful(Some(Redirect("/login")))
            }

          case None =>
            val tokenPayloadOpt = extractPayloadFromExpiredToken(cookie.value)
            tokenPayloadOpt match {
              case Some((userIdStr, username)) =>
                val userId = userIdStr.toInt
                sessionModel.compareSessionToken(userId, cookie.value).flatMap {
                  tokenMatches =>
                    if (tokenMatches) {
                      logMessage(
                        "Redis Lookup was successful,refreshing the token"
                      )
                      val newJwtToken = createToken(userId, username)
                      sessionModel.updateLastJwtIssued(userId, newJwtToken)
                      val newJwtCookie = issueJwtCookie(config, newJwtToken)
                      // This makes it so the request goes through while still setting a new JWT Cookie
                      Future
                        .successful(None)
                        .map(_ =>
                          Some(Redirect(request.uri).withCookies(newJwtCookie))
                        )
                    } else {
                      logMessage("TOken didn't matched with the one on redis")
                      Future.successful(Some(Redirect("/login")))
                    }
                }
              case None =>
                logMessage("Invalid or expired token.")
                Future.successful(Some(Redirect("/login")))
            }
        }

      case None =>
        logMessage("No session token found.")
        Future.successful(Some(Redirect("/login")))
    }
  }
}
