package controllers

import models.requests.SignUpRequest
import models.users.AuthenticationModel
import play.api.libs.json.{JsError, JsSuccess}

import javax.inject._
import play.api.mvc._
import utils.ConsoleMessage.logMessage
import utils.ValidateUser.validateInput

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject() (
    cc: ControllerComponents,
    authModel: AuthenticationModel
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

  /** POST /test
    * Expects JSON with "username" and "password".
    * Inserts a new user into the database, then retrieves and logs all users.
    */
  def createUser(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        json.validate[SignUpRequest] match {
          case JsSuccess(signUpReq, _) =>
            val validationResult = validateInput(
              signUpReq.username,
              signUpReq.email,
              signUpReq.password
            )
            if (validationResult) {
              val createOperationResult: Future[Result] =
                authModel.createUser(
                  signUpReq.username,
                  signUpReq.email,
                  signUpReq.password
                )
              createOperationResult
            } else {
              logMessage("Failed to create the user")
              Future.successful(
                InternalServerError("Failed to create the user")
              )
            }
          case JsError(error) =>
            Future.successful {
              logMessage(s"Invalid request body. ${error}")
              BadRequest(s"Invalid request body.")
            }
        }
      case None =>
        logMessage("Invalid request body.")
        Future.successful(BadRequest("Invalid request body."))
    }
  }
}
