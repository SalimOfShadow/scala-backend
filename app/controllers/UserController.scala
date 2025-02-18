package controllers

import models.requests.SignUpRequest
import models.{AuthenticationModel, SessionModel}
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc._
import utils.ConsoleMessage.logMessage
import utils.ValidateUser.validateCreateInput

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject() (
    cc: ControllerComponents,
    authModel: AuthenticationModel,
    sessionModel: SessionModel
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

  /** POST /test
    * Expects JSON with "username", "email" and "password".
    * Inserts a new user into the database
    */
  def createUser(): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      request.body.validate[SignUpRequest] match {
        case JsSuccess(signUpReq, _) =>
          if (
            validateCreateInput(
              Some(signUpReq.username),
              Some(signUpReq.email),
              Some(signUpReq.password)
            )
          ) {
            authModel
              .createUser(
                signUpReq.username,
                signUpReq.email,
                signUpReq.password
              )
              .map { result =>
                result
              }
              .recover { case ex: Exception =>
                logMessage(s"Error creating user: ${ex.getMessage}")
                InternalServerError(
                  "An error occurred while creating the user."
                )
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
//  def loginUser(): Action[JsValue] = Action.async(parse.json) {
//    implicit request =>
//      request.body.validate[LoginRequest] match {
//        case JsSuccess(loginReq, _) =>
//          if (
//            validateUserInput(
//              Some(loginReq.usernameOrEmail),
//              Some(loginReq.usernameOrEmail),
//              Some(loginReq.password)
//            )
//          ) {
//            authModel
//              .loginUser(
//                loginReq.usernameOrEmail,
//                loginReq.password
//              )
//              .map { result =>
//                result
//              }
//              .recover { case ex: Exception =>
//                logMessage(s"Error creating user: ${ex.getMessage}")
//                InternalServerError(
//                  "An error occurred while creating the user."
//                )
//              }
//          } else {
//            logMessage("User input validation failed")
//            Future.successful(BadRequest("Invalid user input."))
//          }
//
//        case JsError(errors) =>
//          logMessage(s"Invalid request body: $errors")
//          Future.successful(BadRequest("Invalid request body."))
//      }
//  }
}
