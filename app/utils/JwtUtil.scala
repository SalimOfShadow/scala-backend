package utils
import com.typesafe.config.{Config, ConfigFactory}
import pdi.jwt.JwtAlgorithm.HS256
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtOptions}
import play.api.libs.json.Json
import play.api.mvc.Cookie
import utils.ConsoleMessage.logMessage

import java.time.Instant
import scala.util.{Failure, Success}

object JwtUtil {
  private val config: Config =
    ConfigFactory.load()

  private val secretKey = config.getString("jwt.secret")
//  private val expirationTime = 10
  private val expirationTime =
    config
      .getString("jwt.expirationTime")
      .toInt // token should expire in  15 minutes
  private val algo = JwtAlgorithm.HS256

  // TODO - implement JWT caching ( https://www.reddit.com/r/webdev/comments/d8baek/storing_jwt_in_redis/ )

  def createToken(
      userId: Int,
      username: String
  ): String = {
    val claim = JwtClaim(
      content = Json.obj("userId" -> userId, "username" -> username).toString,
      expiration = Some(Instant.now.getEpochSecond + expirationTime)
    )
    val encodedJwtToken = Jwt.encode(claim, secretKey, HS256)
    encodedJwtToken
  }

  def validateToken(token: String): Option[JwtClaim] = {
    val decodedJwt = Jwt.decode(token, secretKey, Seq(algo)).toOption
    logMessage(decodedJwt)
    decodedJwt
  }

  def issueJwtCookie(
      configuration: play.api.Configuration,
      jwt: String
  ): Cookie = {
    val currentEnv =
      configuration.underlying.getString("settings.environment")
    val isSecure = currentEnv != "development"
    val jwtCookie = new Cookie(
      name = "sessionToken",
      value = jwt,
      path = "/",
      maxAge = Some(60 * 60 * 24),
      httpOnly = true,
      secure = isSecure,
      domain = None
    )
    jwtCookie
  }

  // If the token has expired, we extract the payload and perform a redis lookup
  // TODO - Use this to validate the cookie from the client
  def extractPayloadFromExpiredToken(
      token: String
  ): Option[(String, String)] = {
    Jwt.decodeRawAll(
      token,
      // This options allow decoding even if the token has expired
      options = JwtOptions(
        signature = false,
        expiration = false
      )
    ) match {
      case Success((header, payload, signature)) =>
        val jsonPayload = Json.parse(payload)
        val userId = (jsonPayload \ "userId").asOpt[Int]
        val username = (jsonPayload \ "username").asOpt[String]
        (userId, username) match {
          case (Some(id), Some(username)) => Some(id.toString, username)
          case _                          => None
        }
      case Failure(_) =>
        None
    }
  }
}
