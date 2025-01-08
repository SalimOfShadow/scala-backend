package database.models.users

import SQLCode.Tables
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationModel(db: Database)(implicit ec: ExecutionContext) {

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
    val resultFuture = db.run(
      Users
        .filter(userRow =>
          userRow.username === username && userRow.password === password
        )
        .result
    )
    resultFuture
      .map(userRows => userRows.nonEmpty)
      .recover { case e: Throwable =>
        println(s"An error occurred: ${e.getMessage}")
        false
      }
  }

  // Create a new user
  def createUser(username: String, password: String): Future[Boolean] = {
    println("SHOULD CREATE THE USER ")
    val newUser = Tables.UsersRow(0, username, password)
    val insertAction = Users += (newUser) // 0 for auto-incremented id
    db.run(insertAction)
      .map(_ => true)
      .recover { case e: Throwable =>
        println(s"An error occurred while creating user: ${e.getMessage}")
        false
      }
  }

  def getUserByUsername(username: String): Future[Option[(Int, String,String)]] = {
    // Query the Users table for the given username

    val que = Users
      .filter(_.username === username)
      .map(user => (user.id, user.username,user.password))
      .result
      .headOption
    val query = Users
      .filter(_.username === username)
      .map(user => (user.id, user.password))
      .result
      .headOption

    // Execute the query and map the result
    db.run(que)
      .recover { case e: Throwable =>
        println(s"An error occurred while retrieving user: ${e.getMessage}")
        None // If there's an error, return None
      }
  }


  def getAllUsers(): Future[Seq[(Int, String, String)]] = {
    val query = Users
      .map(user => (user.id, user.username, user.password))  // Select the columns you need
      .result  // Retrieve all rows
    db.run(query)
      .recover { case e: Throwable =>
        println(s"An error occurred while retrieving all users: ${e.getMessage}")
        Seq()  // Return an empty sequence if there's an error
      }
  }

  // Login a user (find user by username)
  def loginUser(username: String): Future[Option[String]] = {
    val query = Users.filter(_.username === username).result.headOption
    db.run(query)
      .map {
        case Some(user) => Some(user.username)
        case None       => None
      }
      .recover { case e: Throwable =>
        println(s"An error occurred during login: ${e.getMessage}")
        None
      }
  }

  // Delete a user by username
  def deleteUser(username: String): Future[Boolean] = {
    val query = Users.filter(_.username === username).delete
    db.run(query)
      .map(rowsAffected => rowsAffected > 0)
      .recover { case e: Throwable =>
        println(s"An error occurred while deleting user: ${e.getMessage}")
        false
      }
  }

  // Ban a user by setting the banned field to true
  def banUser(username: String): Future[Boolean] = {
    val query =
      Users.filter(_.username === username).map(_.username).update(username)
    db.run(query)
      .map(rowsAffected => rowsAffected > 0)
      .recover { case e: Throwable =>
        println(s"An error occurred while banning user: ${e.getMessage}")
        false
      }
  }
}
