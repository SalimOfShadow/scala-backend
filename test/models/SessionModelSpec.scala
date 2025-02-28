// TODO - MAKE THIS WORK

package models

import com.redis._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar

class SessionModelSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  // Mock Redis client and pool
  val mockRedisClient: RedisClient = mock[RedisClient]
  val mockRedisPool: RedisClientPool = mock[RedisClientPool]

  // Test instance of SessionModel using the mock Redis pool
  val sessionModel: SessionModel = new SessionModel {
    override val redisPool: RedisClientPool = mockRedisPool
  }

  // Helper method to mock RedisPool behavior correctly
  def mockWithClient(response: Option[Long]): Unit = {
    when(mockRedisPool.withClient(any[RedisClient => Any])).thenAnswer {
      invocation =>
        val clientFunction =
          invocation.getArgument[RedisClient => Any](0) // Fixed method name
        clientFunction(mockRedisClient) // Apply function to mock RedisClient
        response // Return the expected response
    }
  }

  "SessionModel" should {
    "deleteSession" should {

      "delete the session from Redis and return true" in {
        sessionModel.storeSession(12345, "TEST", "testEmail", "jwt-example")
        sessionModel.deleteSession(12345).map { result =>
          result shouldBe true
        }
      }
    }
  }
}
