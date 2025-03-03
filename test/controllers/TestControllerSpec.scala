package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class TestControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {
  "TestController GET" should {

    "validate a JWT token" in {
      val controller: TestController = inject[TestController]
      val home = controller.testTokenValidation().apply(FakeRequest(GET, "/"))
      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(
        "Successfully validated this token :  Some(JwtClaim({\"userId\":123,\"username\":\"testingUsername\"}, None, None, None, Some(2741041803), None, None, None))"
      )
    }

    "store a session on Redis" in {
      val controller: TestController = inject[TestController]
      val home = controller.testStoreSession().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(
        "Successfully stored a session on Redis"
      )
    }

    "retrieve a session from Redis" in {
      val controller: TestController = inject[TestController]
      val home = controller.testSessionRetrieval().apply(FakeRequest(GET, "/"))
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(
        "Successfully retrieved a session on Redis - "
      )
    }
    "retrieve all sessions from Redis" in {
      val controller: TestController = inject[TestController]
      val home = controller.testAllSession().apply(FakeRequest(GET, "/"))
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(
        "Successfully retrieved all the sessions on Redis "
      )
    }
    "compare JWT token with the one stored inside a Redis session" in {
      val controller: TestController = inject[TestController]
      val home =
        controller.testTokenComparisonWithRedis().apply(FakeRequest(GET, "/"))
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(
        "The provided token matches the one inside the Redis session"
      )
    }

    //    "delete a sessions from Redis" in {
    //      val controller: TestController = inject[TestController]
    //      val home = controller.testDeleteSession().apply(FakeRequest(GET, "/"))
    //      contentType(home) mustBe Some("text/plain")
    //      contentAsString(home) must include(
    //        s"Successfully deleted session on Redis"
    //      )
    //    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }
  }
}
