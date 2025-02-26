package utils
import com.typesafe.config.{Config, ConfigFactory}
import pdi.jwt.JwtAlgorithm.HS256
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.libs.json.Json
import utils.ConsoleMessage.logMessage

import java.time.Instant

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
      expiration = Some(Instant.now.getEpochSecond + expirationTime)
    )
    logMessage(claim)
    Jwt.encode(claim, secretKey, HS256)
  }

  def validateToken(token: String): Option[JwtClaim] = {
    Jwt.decode(token, secretKey, Seq(algo)).toOption
  }
}
