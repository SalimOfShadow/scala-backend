package models.requests

import play.api.libs.json.{Json, OFormat}

// A sealed trait to capture all possible user requests
sealed trait UserRequest

final case class SignUpRequest(
    username: String,
    email: String,
    password: String
) extends UserRequest

object SignUpRequest {
  implicit val format: OFormat[SignUpRequest] = Json.format[SignUpRequest]
}

final case class LoginRequest(
    usernameOrEmail: String,
    password: String
) extends UserRequest

object LoginRequest {
  implicit val format: OFormat[LoginRequest] = Json.format[LoginRequest]
}

final case class LogoutRequest(
    usernameOrEmail: Option[String],
) extends UserRequest

object LogoutRequest {
  implicit val format: OFormat[LogoutRequest] = Json.format[LogoutRequest]
}
