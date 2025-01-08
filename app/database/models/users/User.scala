package database.models.users

final case class User(id: Option[Int], username: String, password: String)
