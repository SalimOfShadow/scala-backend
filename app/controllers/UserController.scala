package controllers

import database.models.users.AuthenticationModel

import javax.inject._
import play.api.mvc._
import utils.ValidateUser.isNewUserValid

import scala.concurrent.{ExecutionContext, Future}

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
              for {
                insertResult <- authModel.createUser(username, password)
                users <- authModel.getAllUsers
              } yield {
                if (insertResult) {
                  println(s"User '$username' created successfully.")
                  println(s"Current users: $users")
                  Ok(
                    s"User '$username' created successfully. Current users: $users"
                  )
                } else {
                  InternalServerError("Failed to create user.")
                }
              }
            } else {
              Future.successful{
                BadRequest("Invalid request body")
              }
            }
          case _ =>
            Future.successful(
              BadRequest("Missing username or password in JSON.")
            )
        }
      case None =>
        Future.successful(BadRequest("Expected JSON data."))
    }
  }
}
