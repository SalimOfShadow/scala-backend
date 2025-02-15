package database.models.users

import SQLCode.Tables
import org.mindrot.jbcrypt.BCrypt
import org.postgresql.util.PSQLException

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile
import utils.ConsoleMessage.logMessage

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

  // Check if the database is connected
  def connectedToTheDb(): Future[Boolean] = {
    val query = sql"SELECT 1".as[Int]
    db.run(query)
      .map(_ => true)
      .recover { case e: Throwable =>
        println(s"Database connection check failed: ${e.getMessage}")
        false
      }
  }

  // Validate user login by checking the username and password
  def validateUser(username: String, password: String): Future[Boolean] = {
    db.run(
      Users
        .filter(userRow =>
          userRow.username === username && userRow.password === password
        )
        .result
    ).map(_.nonEmpty)
      .recover { case e: Throwable =>
        println(s"An error occurred: ${e.getMessage}")
        false
      }
  }

  // Create a new user
  def createUser(username: String, password: String): Future[Boolean] = {
    val salt = BCrypt.gensalt()
    val hashedPassword = BCrypt.hashpw(password, salt)
    val newUser = Tables.UsersRow(0, username, hashedPassword)
    db.run(Users += newUser)
      .map(_ => true)
      .recover {
        case e: PSQLException if e.getSQLState == "23505" =>
          logMessage("User already exists")
          false
        case e: Throwable =>
          logMessage(s"An error occurred while creating user: ${e.getMessage}")
          false
      }
  }

  def getAllUsers: Future[Seq[(Int, String, String)]] = {
    db.run(Users.map(user => (user.id, user.username, user.password)).result)
      .recover { case e: Throwable =>
        println(
          s"An error occurred while retrieving all users: ${e.getMessage}"
        )
        Seq()
      }
  }
}
