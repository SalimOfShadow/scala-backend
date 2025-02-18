package controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.ValidateUser.validateCreateInput

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class ValidateUserSpec  extends AnyWordSpec with Matchers {

  "validateCreateUser" should   {

    "return true if all params are present and correct" in {
      val username = Some("validUsername")
      val email = Some("valid@email.com")
      val password = Some("validPassword1!")

      val actual = validateCreateInput(username,email,password)
      actual shouldBe true
    }

    "return false if even a single param is missing" in {
      val username = Some("validUsername")
      val password = Some("validPassword1!")

      val actual = validateCreateInput(username,None,password)
      actual shouldBe false
    }

    "return false if the username is invalid" in {
      val username = Some("!sv")
      val email = Some("valid@email.com")
      val password = Some("validPassword1!")

      val actual = validateCreateInput(username,email,password)
      actual shouldBe false
    }

    "return false if the email is invalid" in {
      val username = Some("validUsername")
      val email = Some("invalidEmail.com")
      val password = Some("validPassword1!")

      val actual = validateCreateInput(username,email,password)
      actual shouldBe false
    }

    "return false if the password is invalid" in {
      val username = Some("validUsername")
      val email = Some("valid@email.com")
      val password = Some("invalidPassword")

      val actual = validateCreateInput(username,email,password)
      actual shouldBe false
    }
  }
}
