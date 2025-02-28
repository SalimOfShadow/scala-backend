package utils
import com.typesafe.config.{Config, ConfigFactory}
import pdi.jwt.JwtAlgorithm.HS256
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.libs.json.Json
import utils.ConsoleMessage.logMessage

import java.time.Instant
import scala.util.{Failure, Success}

object JwtUtil {
  private val config: Config =
    ConfigFactory.load()

  private val secretKey = config.getString("jwt.secret")
  private val expirationTime =
    config
      .getString("jwt.expirationTime")
      .toInt // token should expire in  1 day
  private val algo = JwtAlgorithm.HS256

  // TODO - implement JWT caching ( https://www.reddit.com/r/webdev/comments/d8baek/storing_jwt_in_redis/ )

  def createToken(
      userId: Int,
      username: String
  ): String = {
    val claim = JwtClaim(
      content = Json.obj("userId" -> userId, "username" -> username).toString,
      expiration = Some(Instant.now.getEpochSecond + 10)
    )
    val encodedJwtToken = Jwt.encode(claim, secretKey, HS256)
    encodedJwtToken
  }

  def validateToken(token: String): Option[JwtClaim] = {
    val decodedJwt = Jwt.decode(token, secretKey, Seq(algo)).toOption
    logMessage(decodedJwt)

    val payload = {
      decodedJwt.map(claim => Json.parse(claim.content))
    }

    val userId = payload.flatMap(json => (json \ "userId").asOpt[String])
    val username = payload.flatMap(json => (json \ "username").asOpt[String])

    println(decodedJwt)
    println(userId)
    decodedJwt
  }
  // If the token has expired, we extract the payload and perform a redis lookup
  // TODO - Use this to validate the cookie from the client
  def extractFieldsFromExpiredToken(token: String): Option[(String, String)] = {
    Jwt.decodeRawAll(token) match {
      case Success((header, payload, signature)) =>
        val jsonPayload = Json.parse(payload)
        val userId = (jsonPayload \ "userId").asOpt[String]
        val username = (jsonPayload \ "username").asOpt[String]
        (userId, username) match {
          case (Some(id), Some(username)) => Some(id, username)
          case _                          => None
        }
      case Failure(_) =>
        None
    }
  }
}
