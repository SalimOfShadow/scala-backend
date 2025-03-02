package actions

import models.SessionModel
import play.api.mvc._
import utils.JwtUtil.{
  createToken,
  extractPayloadFromExpiredToken,
  issueJwtCookie,
  validateToken
}
import utils.ConsoleMessage.logMessage
import play.api.libs.json.Json
import play.api.mvc.Results.{Ok, Redirect}

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
              case Some(userId) =>
                Future.successful(
                  Some(
                    Ok(s"Successfully validate JWT for user ${userId.toString}")
                  )
                ) // Authentication successful, continue request
              case None =>
                logMessage("Invalid token: userId missing.")
                Future.successful(Some(Redirect("/login")))
            }

          case None =>
            val tokenPayloadOpt = extractPayloadFromExpiredToken(cookie.value)
            logMessage(cookie.value)
            logMessage(tokenPayloadOpt)
            tokenPayloadOpt match {
              case Some((userIdStr, username)) =>
                val userId = userIdStr.toInt

                sessionModel.compareSessionToken(userId, cookie.value).flatMap {
                  tokenMatches =>
                    if (tokenMatches) {
                      logMessage("TOken did matched with the one on redis")
                      val newJwtToken = createToken(userId, username)
                      sessionModel.updateLastJwtIssued(userId, newJwtToken)
                      val newJwtCookie = issueJwtCookie(config, newJwtToken)
                      Future.successful(
                        Some(Ok("Token refreshed").withCookies(newJwtCookie))
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
