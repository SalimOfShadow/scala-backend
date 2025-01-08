package database.models.users

import SQLCode.Tables._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AuthenticationModel(db: Database)(implicit ec: ExecutionContext) {
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
        false // Return `false` if an exception occurs.
      }
  }

  def connectedToTheDb(): Future[Boolean] = {
    val username = "postgres"
    val password = "postgres"
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
        val newUser = User(id = UUID, name = "asdo", email = "asdkop")
        db.run(Users.insertOrUpdate(newUser))
        false // Return `false` if an exception occurs.
      }
  }
  //  def connectedToTheDb(): Future[Boolean] = {
//    val query =
//      sql"SELECT 1".as[Int] // Runs a simple query to check connectivity
//
//    db.run(query)
//      .map(_ =>
//        true
//      ) // If the query runs successfully, the database is connected
//      .recover { case e: Throwable =>
//        println(s"Database connection check failed: ${e.getMessage}")
//        false // Return `false` if there's an exception
//      }
//  }

  def createUser(username: String, password: String): Boolean = {
    ???
  }
  def loginUser(username: String): Boolean = {
    ???
  }
  def deleteUser(username: String): Boolean = {
    ???
  }
  def banUser(username: String): Boolean = {
    ???
  }
}
