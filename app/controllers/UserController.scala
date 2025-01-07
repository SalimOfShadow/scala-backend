package controllers

import play.api._
import play.api.mvc._

import javax.inject._

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class UserController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {
  def createUser(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      {
        request.body match {
          case AnyContentAsFormUrlEncoded(params) ⇒
            println(s"urlEncoded = $params")
            Ok(s"Params were found! $params")
          case mp @ AnyContentAsMultipartFormData(_) ⇒
            println(s"multipart = ${mp.asFormUrlEncoded}")
            Ok(s"Multipart params were found! ${mp.asFormUrlEncoded}")
          case json @ AnyContentAsJson(_) =>
            val parsedJsonRequest: Result = json.asJson match {
              case Some(jsonValue) => {
                val isUsernameDefined = (jsonValue \ "username").isDefined
                println(s"json content found : ${jsonValue}")
                println(s"isUsernameDefined is $isUsernameDefined")
                Ok("OK")
              }
              case None => BadRequest("404 - Bad Request.")
            }
            parsedJsonRequest
        }
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
