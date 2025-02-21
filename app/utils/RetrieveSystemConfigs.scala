package utils

import com.typesafe.config.{Config, ConfigFactory};

object RetrieveSystemConfigs {
  private val config: Config = ConfigFactory.load()
  private val defaultSystemPassword = config.getString("settings.defaultSecret")
  def validateSystemCredentials(): Either[String, Unit] = {

    val jwtPwds = sys.env.getOrElse("SECRET_KEY", "")
//    sys.env.getOrElse(envVariable, defaultValue)
//    if (!is_valid(statement)) Left("syntax is not valid") else Right(())

  }
}
