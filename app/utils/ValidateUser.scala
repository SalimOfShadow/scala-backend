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
    usernamePattern
      .findFirstMatchIn(username)
      .exists(u => u.matched.length >= 4 && u.matched.length <= 20)

  private def isEmailValid(email: String): Boolean =
    emailPattern
      .findFirstMatchIn(email)
      .exists(e => e.matched.length >= 5 && e.matched.length <= 254)

  private def isPasswordValid(password: String): Boolean = {
    passwordPattern
      .findFirstMatchIn(password)
      .exists(p => p.matched.length >= 8 && p.matched.length <= 100)
  }

  def validateCreateInput(
      username: Option[String],
      email: Option[String],
      password: Option[String]
  ): Boolean = {

    val usernameValid = username.exists(isUsernameValid)
    val emailValid = email.exists(isEmailValid)
    val passwordValid = password.exists(isPasswordValid)
    usernameValid && emailValid && passwordValid}

  def validateLoginInput(
      usernameOrEmail: Option[String],
      password: Option[String]
  ): Boolean = {
    val usernameOrEmailValid = usernameOrEmail.exists(value =>
      isUsernameValid(value) || isEmailValid(value)
    )
    val passwordValid = password.exists(isPasswordValid)

    usernameOrEmailValid && passwordValid
  }
}
