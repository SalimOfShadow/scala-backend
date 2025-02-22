package controllers

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
    environment: play.api.Environment,
    configuration: play.api.Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  /** GET /test
    * Logs and returns all users from the database.
    */
  def testConnection(): Action[AnyContent] = Action.async { implicit request =>
    authModel.getAllUsers.map { users =>
      println(s"Users: $users")
      Ok(s"Users: $users")
    } recover { case ex: Throwable =>
      println(s"An error occurred: ${ex.getMessage}")
      InternalServerError("Database connection error.")
    }
  }

  def testSession(): Action[AnyContent] = Action.async { implicit request =>
    val result = sessionModel.storeSession(12345, "Token")
    result.onComplete(result => logMessage(result.toString))
    Future.successful(Ok(s"result"))
  }

  def testAllSession(): Action[AnyContent] = Action.async { implicit request =>
    val result = sessionModel.getAllSession
    result.onComplete(result => logMessage(result.toString))
    Future.successful(Ok(s"result"))
  }

  def testPrintSecrets(): Action[AnyContent] = Action.async {
    implicit request =>
      val secret = configuration.underlying.getString("jwt.secret")
      Future.successful(
        Ok(secret)
      )
  }

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
      val token = validateToken("asdjiojsasdjio")
      logMessage(token)
      Future.successful(
        Ok(s"token ${token}")
      )

  }


}
