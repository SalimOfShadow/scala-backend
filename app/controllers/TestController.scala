package controllers
import actions.{SecureAction}
import models.{AuthenticationModel, SessionModel}
import play.api.mvc._
import utils.ConsoleMessage.logMessage
import utils.JwtUtil._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestController @Inject() (
    cc: ControllerComponents,
    authModel: AuthenticationModel,
    sessionModel: SessionModel,
    secureAction: SecureAction,
    environment: play.api.Environment,
    configuration: play.api.Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def testConnection(): Action[AnyContent] = Action.async { implicit request =>
    authModel.getAllUsers.map { users =>
      println(s"Users: $users")
      Ok(s"Users: $users")
    } recover { case ex: Throwable =>
      println(s"An error occurred: ${ex.getMessage}")
      InternalServerError("Database connection error.")
    }
  }

// -- SESSIONS --
  def testStoreSession(): Action[AnyContent] = Action.async {
    implicit request =>
      val result =
        sessionModel.storeSession(
          12345,
          "examplusername",
          "example@email.com",
          "example_jwt"
        )
      result.map {
        case true  => Ok("Successfully stored a session on Redis")
        case false => InternalServerError("500")
      }
  }

  def testSessionRetrieval(): Action[AnyContent] = Action.async {
    implicit request =>
      val result = sessionModel.getSession(1)
      result.map {
        case Some(value: Map[String, String]) if value.nonEmpty =>
          Ok(
            s"Successfully retrieved a session on Redis - $value"
          )
        case _ =>
          InternalServerError("500")

      }
  }

  def testAllSession(): Action[AnyContent] = Action.async { implicit request =>
    val result = sessionModel.getAllSessions
    result.map {
      case Some(value) if (value.nonEmpty) =>
        Ok(
          s"Successfully retrieved all the sessions on Redis - $value"
        )
      case _ =>
        result.map(logMessage(_))
        InternalServerError("Failed to retrieve all sessions on Redis")
    }
  }

  def testDeleteSession(): Action[AnyContent] = Action.async {
    implicit request =>
      val result = sessionModel.deleteSession(1)
      result.map {
        case true =>
          result.map(logMessage(_))
          Ok(
            s"Successfully deleted session on Redis"
          )
        case _ =>
          result.map(logMessage(_))
          InternalServerError("Failed to delete session on Redis")
      }
  }

  // -- TOKEN --
  def testTokenCreationAndExpiry(): Action[AnyContent] = Action.async {
    implicit request =>
      val token = createToken(1, "testUser")
      logMessage(validateToken(token))
      Thread.sleep(21000)
      logMessage(validateToken(token))

      Future.successful(
        Ok(token)
      )

  }

  def testTokenValidation(): Action[AnyContent] = Action.async {
    implicit request =>
      // Some(JwtClaim({"userId":123,"username":"testingUsername"}, None, None, None, Some(2741041803), None, None, None))
      val token = validateToken(
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjI3NDEwNDE4MDMsInVzZXJJZCI6MTIzLCJ1c2VybmFtZSI6InRlc3RpbmdVc2VybmFtZSJ9.kBv7V3h2kTUPJil3na-nrDJcFQmzGbRdW0MsO-JKIGE"
      )
      token match {
        case Some(value) =>
          Future.successful(
            Ok(s"Successfully validated this token :  ${token}")
          )
        case None =>
          Future.successful(
            InternalServerError(s"Token validation function failed.")
          )
      }

  }

  def testTokenComparisonWithRedis(): Action[AnyContent] = Action.async {
    implicit request =>
      val userId = 12345
      val token =
        "example_jwt"
      val comparisonResult = sessionModel.compareSessionToken(userId, token)

      comparisonResult.map {
        case true =>
          Ok("The provided token matches the one inside the Redis session")

        case false =>
          InternalServerError(
            "Failed to compare the token with it's Redis counterpart"
          )
      }

  }

  def testProtectedRoute(): Action[AnyContent] = secureAction {
    implicit request =>
      Ok("This is a protected route.")
  }
}
