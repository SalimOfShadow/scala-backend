package models.requests

import play.api.libs.json.{Json, OFormat}

// This seems like a perfect use case for Protobuf

// A sealed trait to capture all possible user requests
sealed trait UserRequest

// A request to sign up a new user
final case class SignUpRequest(
                                username: String,
                                email: String,
                                password: String
                              ) extends UserRequest

object SignUpRequest {
  implicit val format: OFormat[SignUpRequest] = Json.format[SignUpRequest]
}

// A request to log in
final case class LoginRequest(
                               usernameOrEmail: String,
                               password: String
                             ) extends UserRequest

object LoginRequest {
  implicit val format: OFormat[LoginRequest] = Json.format[LoginRequest]
}
