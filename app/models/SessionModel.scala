package models

import com.redis._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionModel @Inject() ()(implicit ec: ExecutionContext) {

  private val redisPool = new RedisClientPool("localhost", 6379)

  def storeSession(
      userId: Int,
      sessionToken: String,
      expiration: Int = 60 * 60 * 24
  ): Future[Boolean] = Future {
    redisPool.withClient { client =>
      client.setex(s"session:$userId", expiration, sessionToken)
    }
  }

  def getSession(userId: Int): Future[Option[String]] = Future {
    redisPool.withClient { client =>
      client.get(s"session:$userId")
    }
  }
  def getAllSession(): Future[Option[String]] = Future {
    redisPool.withClient { client =>
      client.get(s"session:12345")
    }
  }

  def deleteSession(userId: Int): Future[Boolean] = Future {
    redisPool.withClient { client =>
      val result = client.del(s"session:$userId")
      result match {
        case Some(value) => value > 0
      }
    }
  }
}
