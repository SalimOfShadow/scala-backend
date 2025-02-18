package utils

import scala.util.chaining._
import scala.util.matching.Regex

object ValidateUser {

  private val usernamePattern: Regex = """^[A-Za-z0-9]\w*$""".r
  private val emailPattern =
    """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
  private val passwordPattern: Regex =
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$".r

  private def isUsernameValid(username: String): Boolean =
    usernamePattern.findFirstMatchIn(username) match {
      case Some(value) =>
        value.matched.length >= 4 && value.matched.length <= 20
      case None => false
    }

  private def isEmailValid(email: String): Boolean =
    emailPattern.findFirstMatchIn(email) match {
      case Some(value) =>
        value.matched.length >= 5 && value.matched.length <= 254
      case None => false
    }
  private def isPasswordValid(password: String): Boolean =
    passwordPattern.findFirstMatchIn(password) match {
      case Some(value) =>
        value.matched.length >= 8 && value.matched.length <= 100
      case None => false
    }

  def validateCreateInput(
      username: Option[String],
      email: Option[String],
      password: Option[String]
  ): Boolean = {

    val usernameValid = username.exists(isUsernameValid)
    val emailValid = email.exists(isEmailValid)
    val passwordValid = password.exists(isPasswordValid)

    if (usernameValid && emailValid && passwordValid) true else false
  }

}
