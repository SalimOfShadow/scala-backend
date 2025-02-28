package controllers

import models.requests.{SignUpRequest, LoginRequest}
import models.{AuthenticationModel, SessionModel}
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc._
import utils.ConsoleMessage.logMessage
import utils.ValidateUser.{validateCreateInput, validateLoginInput}

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject() (
    cc: ControllerComponents,
    authModel: AuthenticationModel,
    sessionModel: SessionModel,
    environment: play.api.Environment,
    configuration: play.api.Configuration
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  /** POST /create-user
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

  def loginUser(): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      request.body.validate[LoginRequest] match {
        case JsSuccess(loginReq, _) =>
          if (
            validateLoginInput(
              Some(loginReq.usernameOrEmail),
              Some(loginReq.password)
            )
          ) {
            authModel
              .loginUser(
                loginReq.usernameOrEmail,
                loginReq.password
              )
              .flatMap {
                case Some(jwt) =>
                  val currentEnv =
                    configuration.underlying.getString("settings.environment")
                  val isSecure = currentEnv != "development"
                  val jwtCookie = new Cookie(
                    name = "sessionToken",
                    value = jwt,
                    path = "/",
                    maxAge = Some(60 * 60 * 24),
                    httpOnly = true,
                    secure = isSecure,
                    domain = None
                  )
                  val userInfoCookie = new Cookie(
                    name = "userInfo",
                    value = loginReq.usernameOrEmail,
                    path = "/"
                  )

                  Future.successful(
                    Ok("Successfully logged in")
                      .withCookies(jwtCookie, userInfoCookie)
                  ) // Return 200 OK with JWT token
                case None =>
                  Future.successful(
                    InternalServerError(
                      "Failed to login. Please check your credentials and try again."
                    )
                  ) // Handle empty result
              }
              .recover { case ex: Exception =>
                logMessage(s"Error login user: ${ex.getMessage}")
                InternalServerError(
                  "Failed to login. Please check your credentials and try again."
                )
              }
          } else {
            logMessage("User input validation failed")
            Future.successful(BadRequest("Invalid request body.."))
          }
        case JsError(errors) =>
          logMessage(s"Invalid request body: $errors")
          Future.successful(BadRequest("Invalid request body."))
      }
  }

  def logoutUser(): Action[JsValue] = Action.async(parse.json) {
    ???
  }
}
