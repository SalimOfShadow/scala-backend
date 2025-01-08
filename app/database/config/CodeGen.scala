package database.config

// This will generate SQL code
object CodeGen extends App {
  slick.codegen.SourceCodeGenerator.run(
    profile = "slick.jdbc.PostgresProfile",
    jdbcDriver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/users?user=postgres&password=postgres",
    outputDir = "/home/salim/Desktop/Projects/scala-backend/app/",
    pkg = "SQLCode",
    user = None,
    password = None,
    ignoreInvalidDefaults = true,
    outputToMultipleFiles = false
  )
}
