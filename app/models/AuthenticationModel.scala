package models

import org.mindrot.jbcrypt.BCrypt
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Result
import play.api.mvc.Results.{Conflict, InternalServerError, Ok}
import slick.jdbc.PostgresProfile
import utils.ConsoleMessage.logMessage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticationModel @Inject() (dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext
) {

  // Get the database configuration
  private val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._

  // Reference to the Users table from generated code
  val Users = Tables.Users

  def validateUser(username: String, password: String): Future[Boolean] = {
    db.run(
      Users
        .filter(userRow =>
          userRow.username === username && userRow.passwordHash === password
        )
        .result
    ).map(_.nonEmpty)
      .recover { case e: Throwable =>
        println(s"An error occurred: ${e.getMessage}")
        false
      }
  }

  def createUser(
      username: String,
      email: String,
      password: String
  ): Future[Result] = {
    val salt = BCrypt.gensalt()
    val hashedPassword = BCrypt.hashpw(password, salt)
    val newUser = Tables.UsersRow(
      0,
      username,
      email,
      hashedPassword,
      Some(false),
      None,
      None
    )
    db.run(Users += newUser)
      .map(_ => {
        logMessage("User created successfully")
        Ok("User created successfully")
      })
      .recover {
        case e: PSQLException if e.getSQLState == "23505" =>
          logMessage("User already exists")
          Conflict("User already exists")
        case e: Throwable =>
          logMessage(s"An error occurred while creating user: ${e.getMessage}")
          InternalServerError("Internal Serve Error")
      }
  }

  def loginUser(usernameOrEmail: String,password: String) = {
    ???
  }

  def getAllUsers: Future[Seq[(Int, String, String)]] = {
    db.run(
      Users.map(user => (user.id, user.username, user.passwordHash)).result
    ).recover { case e: Throwable =>
      println(
        s"An error occurred while retrieving all users: ${e.getMessage}"
      )
      Seq()
    }
  }


}
