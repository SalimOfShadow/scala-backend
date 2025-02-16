package controllers

import models.requests.SignUpRequest
import models.users.AuthenticationModel
import play.api.libs.json.{JsError, JsSuccess, JsValue}

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
  def createUser(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpRequest] match {
      case JsSuccess(signUpReq, _) =>
        if (validateInput(signUpReq.username, signUpReq.email, signUpReq.password)) {
          authModel.createUser(signUpReq.username, signUpReq.email, signUpReq.password).map { result =>
            logMessage("User created successfully.")
            result
          }.recover {
            case ex: Exception =>
              logMessage(s"Error creating user: ${ex.getMessage}")
              InternalServerError("An error occurred while creating the user.")
          }
        } else {
          logMessage("User input validation failed")
          Future.successful(BadRequest("Invalid user input."))
        }

      case JsError(errors) =>
        logMessage(s"Invalid request body: $errors")
        Future.successful(BadRequest("Invalid request body."))
    }
  }


}
