package controllers

import database.Connection
import database.models.users.AuthenticationModel
import play.api._
import play.api.mvc._
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class UserController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def createUser(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      Future {
        request.body match {
          case AnyContentAsFormUrlEncoded(params) =>
            println(s"urlEncoded = $params")
            Ok(s"Params were found! $params")

          case mp @ AnyContentAsMultipartFormData(_) =>
            println(s"multipart = ${mp.asFormUrlEncoded}")
            Ok(s"Multipart params were found! ${mp.asFormUrlEncoded}")

          case json @ AnyContentAsJson(_) =>
            json.asJson match {
              case Some(jsonValue) =>
                val username = (jsonValue \ "username")
                val password = (jsonValue \ "password")

                if (username.isDefined) {
                  val newDb = Database.forConfig("slick.dbs.default")
                  val userModel = new AuthenticationModel(newDb)
                  val isUserValid = userModel.validateUser(
                    username.toString,
                    password.toString
                  )

                  isUserValid.onComplete {
                    case Success(isUserValid) =>
                      println(s"User is valid: $isUserValid")
                    case Failure(exception) =>
                      println(s"An error occurred: ${exception.getMessage}")
                  }

                  println(s"json content found : ${jsonValue}")
                  println(s"isUsernameDefined is ${username.isDefined}")
                  Ok("ok")
                } else {
                  BadRequest("404 - Bad Request.")
                }

              case None =>
                BadRequest("404 - Bad Request.")
            }

          case _ =>
            BadRequest("Unsupported content type.")
        }
      }
  }

  def connectedToTheDb(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val resultFuture =
        new AuthenticationModel(Connection.db).connectedToTheDb()

      resultFuture
        .map { isUserValid =>
          println(s"User is valid: $isUserValid")
          Ok(s"User is valid: $isUserValid")
        }
        .recover { case exception =>
          println(s"An error occurred: ${exception.getMessage}")
          InternalServerError(
            "An error occurred while connecting to the database."
          )
        }
  }

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.index())
  }
}
