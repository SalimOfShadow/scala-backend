package controllers

import database.Connection
import database.models.users.AuthenticationModel
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class DatabaseController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {


  def load = Action { implicit request =>
    Ok(views.html.index())
  }
}
