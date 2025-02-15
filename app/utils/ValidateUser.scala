package utils

import scala.util.matching.Regex

object ValidateUser {
  def validateInput(
      username: String,
      email: String,
      password: String
  ): Boolean = {
    val numberPattern: Regex = "[0-9]".r
    val emailPattern =
      """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
    val usernamePattern: Regex = "^[A-Za-z0-9]\\w*$".r

    // TODO - Add more robust checks like : contains lowercase/uppercase, has at least 1 special char etc...
    val isPasswordValid: Boolean =
      numberPattern.findFirstMatchIn(password) match {
        case Some(_) =>
          password.length >= 8 && password.length <= 100
        case None => false
      }

    val isEmailValid: Boolean = emailPattern.findFirstMatchIn(email) match {
      case Some(_) =>
        email.length >= 5 && email.length <= 254
      case None => false
    }

    val isUsernameValid: Boolean =
      usernamePattern.findFirstMatchIn(username) match {
        case Some(value) =>
          value.matched.length >= 4 && value.matched.length <= 20
        case None => false
      }

    if (isUsernameValid && isEmailValid && isPasswordValid) true else false
  }
}
