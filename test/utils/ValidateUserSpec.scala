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

  "ValidateUser" should   {

    "return true if all params are present and correct" in {
      val username = Some("vasds2")
      val email = Some("valid@email.com")
      val password = Some("validPassword1!")

      val actual = validateCreateInput(username,email,password)

      actual shouldBe true
    }
  }
}
