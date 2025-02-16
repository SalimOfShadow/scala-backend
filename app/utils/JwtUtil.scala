package utils
import com.typesafe.config.{Config, ConfigFactory}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.libs.json.Json

import java.time.Instant

object JwtUtil {
  val config: Config = ConfigFactory.load()
  private val secretKey = config.getString("jwt.secret")
  private val expirationTime = 60 * 60 * 24 // token should expire in  1 day
  private val algo = JwtAlgorithm.HS256

  def createToken(
      userId: Int,
      username: String
  ): String = {
    val claim = JwtClaim(
      content = Json.obj("userId" -> userId, "username" -> username).toString(),
      expiration = Some(Instant.now().getEpochSecond + expirationTime)
    )
    Jwt.encode(claim, secretKey, algo)
  }

  def validateToken(token: String): Option[JwtClaim] = {
    Jwt.decode(token, secretKey, Seq(algo)).toOption
  }
}
