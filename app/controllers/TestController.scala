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
      result.onComplete(result => logMessage(result.toString))
      Future.successful(Ok(result.toString))
  }

  def testSessionRetrieval(): Action[AnyContent] = Action.async {
    implicit request =>
      val result = sessionModel.getSession(1)
      result.onComplete(result => logMessage(result.get))
      Future.successful(Ok(result.toString))
  }

  def testAllSession(): Action[AnyContent] = Action.async { implicit request =>
    val result = sessionModel.getAllSessions
    result.onComplete(result => logMessage(result.toString))
    Future.successful(Ok(s"result"))
  }

  def testDeleteSession(): Action[AnyContent] = Action.async {
    implicit request =>
      val result = sessionModel.deleteSession(1)
      result.onComplete(result => logMessage(result.toString))
      Future.successful(Ok(s"result"))
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
      val token = validateToken("asdjiojsasdjio")
      logMessage(token)
      Future.successful(
        Ok(s"token ${token}")
      )

  }

  def testTokenComparisonWithRedis(): Action[AnyContent] = Action.async {
    implicit request =>
      val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NDA5NDk1NjEsInVzZXJJZCI6MSwidXNlcm5hbWUiOiJ0ZXN0VXNlciJ9.55hDZ97g42eRTknxCPnvu_YP6C08jVpBk7MfTcBVapI"
      val comparisonResult = sessionModel.compareSessionToken(1, token)

      val result = comparisonResult.map(result => { logMessage(result) })

      Future.successful(
        Ok("result")
      )

  }

  def testProtectedRoute(): Action[AnyContent] = secureAction {
    implicit request =>
      Ok("This is a protected route.")
  }
}
