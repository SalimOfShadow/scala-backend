package utils

import scala.util.matching.Regex

object ValidateUser {
  def isNewUserValid(username: String, password: String): Boolean = {
    val numberPattern: Regex = "[0-9]".r
    val validUsernamePattern: Regex = "^[A-Za-z0-9]\\w*$".r
    // TODO - Add more robust checks like : contains lowercase/uppecase, has at least 1 special char etc...
    val isPasswordValid: Boolean =
      numberPattern.findFirstMatchIn(password) match {
        case Some(_) =>
          password.length >= 8 && password.length <= 100
        case None => false
      }

    val isUsernameValid: Boolean =
      validUsernamePattern.findFirstMatchIn(username) match {
        case Some(value) =>
          value.matched.length >= 4 && value.matched.length <= 20
        case None => false
      }
    if (isUsernameValid && isPasswordValid) true else false
  }
}
