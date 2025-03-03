package models

import com.redis._
import com.typesafe.config.{Config, ConfigFactory}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionModel @Inject() ()(implicit ec: ExecutionContext) {
  val config: Config = ConfigFactory.load()
  private val expirationTime = config.getString("redis.expirationTime").toInt

//  private val expirationTime = 40
  protected val redisPool = new RedisClientPool("localhost", 6379)

  def storeSession(
      userId: Int,
      username: String,
      email: String,
      jwtIssued: String
  ): Future[Boolean] = Future {
    val sessionKey = s"session:$userId"
    val sessionData = Map(
      "userId" -> userId.toString,
      "username" -> username,
      "email" -> email,
      "last_jwt_issued" -> jwtIssued
    )
    redisPool.withClient { client =>
      val setResult = client.hmset(sessionKey, sessionData)
      if (setResult) {
        client.expire(sessionKey, expirationTime)
      }
      setResult
    }
  }

  def getSession(userId: Int): Future[Option[Map[String, String]]] = Future {
    redisPool.withClient { client =>
      val fetchedUser = client.hgetall(s"session:$userId")
      refreshSession(userId)
      fetchedUser
    }
  }

  def getAllSessions: Future[Option[Map[String, String]]] = Future {
    redisPool.withClient { client =>
      client.hgetall(s"session:1") // TODO - FIX THIS
    }
  }

  private def refreshSession(userId: Int): Future[Boolean] = Future {
    val sessionKey = s"session:$userId"

    redisPool.withClient { client =>
      val result = client.expire(sessionKey, expirationTime)
      result
    }
  }

  def updateLastJwtIssued(userId: Int, newJwt: String): Future[Boolean] =
    Future {
      val sessionKey = s"session:$userId"
      redisPool.withClient { client =>
        if (client.exists(sessionKey)) {
          client.hset(sessionKey, "last_jwt_issued", newJwt)
          true
        } else {
          false // Session expired or doesn't exist
        }
      }
    }

  /** Compares a session token for authentication */
  def compareSessionToken(userId: Int, token: String): Future[Boolean] = {
    getSession(userId).map {
      case Some(sessionData) if sessionData.nonEmpty => {
        sessionData.get("last_jwt_issued").contains(token)
      }
      case _ => {
        print(s"comparison failed for user $userId")
        false
      }
    }
  }

  def deleteSession(userId: Int): Future[Boolean] = Future {
    redisPool.withClient { client =>
      val result = client.del(s"session:$userId").exists(_ > 0)
      result
    }
  }
}
