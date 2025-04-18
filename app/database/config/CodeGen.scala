package database.config

// This will generate SQL code
object CodeGen extends App {
  slick.codegen.SourceCodeGenerator.run(
    profile = "slick.jdbc.PostgresProfile",
    jdbcDriver = "org.postgresql.Driver",
    url =
      "jdbc:postgresql://postgres_db:5432/postgres?user=postgres&password=postgres",
    outputDir = "/home/salim/Desktop/Projects/play-backend/app/database/config",
    pkg = "database.config",
    user = None,
    password = None,
    ignoreInvalidDefaults = true,
    outputToMultipleFiles = false
  )
}
