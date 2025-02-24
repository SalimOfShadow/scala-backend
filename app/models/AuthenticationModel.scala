package models

import com.typesafe.config.{Config, ConfigFactory}
import database.config.Tables
import models.users.User
import org.mindrot.jbcrypt.BCrypt
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Result
import play.api.mvc.Results.{Conflict, InternalServerError, Ok}
import slick.jdbc.PostgresProfile
import utils.ConsoleMessage.logMessage
import utils.JwtUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticationModel @Inject() (
    dbConfigProvider: DatabaseConfigProvider,
    sessionModel: SessionModel
)(implicit
    ec: ExecutionContext
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
    val emptyUser = new User(-1, "", "", "", "", None, None, None)
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
            user.passwordSalt,
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
    logMessage(salt)
    logMessage(salt.length)
    val hashedPassword = BCrypt.hashpw(password, salt)
    val newUser = Tables.UsersRow(
      0,
      username,
      email,
      hashedPassword,
      salt,
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

  def loginUser(
      usernameOrEmail: String,
      password: String
  ): Future[Option[String]] = {
    val userToLogin = retrieveUserInfo(usernameOrEmail)

    userToLogin.flatMap { user =>
      val hashedPassword = BCrypt.hashpw(password, user.passwordSalt)

      validateUser(user.email, hashedPassword).flatMap { isValid =>
        if (!isValid) {
          logMessage("Incorrect credentials")
          Future.successful(None)
        } else {
          sessionModel.getSession(user.id).flatMap {

            case Some(existingToken) =>
              logMessage(
                s"User ${user.username} is already logged in. Returning existing token $existingToken."
              )
              Future.successful(Some(existingToken))

            case None =>
              //If there is no session, create a new one and return the token
              val newToken = JwtUtil.createToken(user.id, user.username)
              sessionModel.storeSession(user.id, newToken).map { stored =>
                if (stored) {
                  logMessage(
                    s"New session created for user ${user.id}. Issuing token: $newToken"
                  )
                  Some(newToken)
                } else {
                  logMessage(s"Failed to store session for user ${user.id}")
                  None
                }
              }
          }
        }
      }
    }
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
