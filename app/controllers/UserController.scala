package controllers

import database.models.users.AuthenticationModel

import javax.inject._
import play.api.mvc._
import utils.ConsoleMessage.logMessage
import utils.ValidateUser.isNewUserValid

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
        val usernameOpt = (json \ "username").asOpt[String]
        val passwordOpt = (json \ "password").asOpt[String]
        (usernameOpt, passwordOpt) match {
          case (Some(username), Some(password)) =>
            val validationResult = isNewUserValid(username, password)
            if (validationResult) {
              val createOperationResult: Future[Boolean] =
                authModel.createUser(username, password)
              createOperationResult.map {
                case true =>
                  logMessage("User created successfully")
                  Ok("User created successfully")
                case false =>
                  logMessage("Failed to create the user")
                  InternalServerError("Failed to create the user")
              }
            } else {
              logMessage("Invalid request body")
              Future.successful {
                BadRequest("Invalid request body")
              }
            }
          case _ =>
            logMessage("Expected JSON data.")
            Future.successful(
              BadRequest("Missing username or password in JSON.")
            )
        }
      case None =>
        logMessage("Expected JSON data.")
        Future.successful(BadRequest("Expected JSON data."))
    }
  }
}
