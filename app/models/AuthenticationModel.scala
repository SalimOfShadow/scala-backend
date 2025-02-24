package models

import com.typesafe.config.{Config, ConfigFactory}
import models.users.User
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

  // Get the secrets
  private val config: Config =
    ConfigFactory.load()
  private val secretKey = config.getString("settings.secret")

  // Get the database configuration
  private val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._

  // Reference to the Users table from generated code
  val Users = Tables.Users

  private def retrieveUserInfo(usernameOrEmail: String): Future[User] = {
    val emptyUser = new User(0, "", "", "", None, None, None)
    db.run(
      Users
        .filter(userRow =>
          userRow.username === usernameOrEmail || userRow.email === usernameOrEmail
        )
        .result
    ).map { users =>
      users.headOption
        .map(user =>
          new User(
            user.id,
            user.username,
            user.email,
            user.passwordHash,
            user.verified,
            user.createdAt,
            user.lastSeen
          )
        )
        .getOrElse(emptyUser)
    }.recover { case e: Throwable =>
      println(s"An error occurred: ${e.getMessage}")
      emptyUser
    }
  }

  private def validateUser(
      usernameOrEmail: String,
      hashedPassword: String
  ): Future[Boolean] = {
    db.run(
      Users
        .filter(userRow =>
          userRow.username === usernameOrEmail && userRow.passwordHash === hashedPassword || userRow.email === usernameOrEmail && userRow.passwordHash === hashedPassword
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

  def loginUser(usernameOrEmail: String, password: String): Future[Boolean] = {
    val userToLogin = retrieveUserInfo(usernameOrEmail)

    // TODO - Before i call this, i would need to extract the Future[User] from userToLogin
    val loginResult = validateUser(usernameOrEmail, password).map(isValid =>
      if (isValid) {

        true
      } else false
    )
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
