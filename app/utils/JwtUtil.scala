package utils
import com.typesafe.config.{Config, ConfigFactory}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.libs.json.Json

import java.time.Instant

object JwtUtil extends App {

  private val secretKey = config.getString("jwt.secret")
  private val expirationTime =
    config
      .getString("jwt.expirationTime")
      .toInt // token should expire in  1 day
  private val algo = JwtAlgorithm.HS256

  def createToken(
      userId: Int,
      username: String
  ): String = {
    val claim = JwtClaim(
      content = Json.obj("userId" -> userId, "username" -> username).toString,
      expiration = Some(Instant.now.getEpochSecond + 20)
    )
    Jwt.encode(claim, secretKey, algo)
  }

  def validateToken(token: String): Option[JwtClaim] = {
    Jwt.decode(token, secretKey, Seq(algo)).toOption
  }

}
