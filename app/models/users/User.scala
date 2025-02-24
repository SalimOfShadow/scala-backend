package models.users

case class User(
    id: Int,
    username: String,
    email: String,
    passwordHash: String,
    passwordSalt: String,
    verified: Option[Boolean],
    createdAt: Option[java.sql.Timestamp],
    lastSeen: Option[java.sql.Timestamp]
)
