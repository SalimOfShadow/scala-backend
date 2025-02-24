package SQLGeneratedTables

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends Tables {
  val profile: slick.jdbc.JdbcProfile = slick.jdbc.PostgresProfile
}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for
  // tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Users.schema

  /** Entity class storing rows of table Users
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param username Database column username SqlType(varchar), Length(20,true)
   *  @param email Database column email SqlType(varchar), Length(300,true)
   *  @param passwordHash Database column password_hash SqlType(varchar), Length(200,true)
   *  @param passwordSalt Database column password_salt SqlType(varchar), Length(16,true)
   *  @param verified Database column verified SqlType(bool), Default(Some(false))
   *  @param createdAt Database column created_at SqlType(timestamp)
   *  @param lastSeen Database column last_seen SqlType(timestamp) */
  case class UsersRow(id: Int, username: String, email: String, passwordHash: String, passwordSalt: String, verified: Option[Boolean] = Some(false), createdAt: Option[java.sql.Timestamp], lastSeen: Option[java.sql.Timestamp])
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Boolean]], e3: GR[Option[java.sql.Timestamp]]): GR[UsersRow] = GR{
    prs => import prs._
    (UsersRow.apply _).tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<?[Boolean], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends profile.api.Table[UsersRow](_tableTag, "users") {
    def * = ((id, username, email, passwordHash, passwordSalt, verified, createdAt, lastSeen)).mapTo[UsersRow]
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(username), Rep.Some(email), Rep.Some(passwordHash), Rep.Some(passwordSalt), verified, createdAt, lastSeen)).shaped.<>({r=>import r._; _1.map(_=> (UsersRow.apply _).tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7, _8)))}, (_:Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username SqlType(varchar), Length(20,true) */
    val username: Rep[String] = column[String]("username", O.Length(20,varying=true))
    /** Database column email SqlType(varchar), Length(300,true) */
    val email: Rep[String] = column[String]("email", O.Length(300,varying=true))
    /** Database column password_hash SqlType(varchar), Length(200,true) */
    val passwordHash: Rep[String] = column[String]("password_hash", O.Length(200,varying=true))
    /** Database column password_salt SqlType(varchar), Length(16,true) */
    val passwordSalt: Rep[String] = column[String]("password_salt", O.Length(16,varying=true))
    /** Database column verified SqlType(bool), Default(Some(false)) */
    val verified: Rep[Option[Boolean]] = column[Option[Boolean]]("verified", O.Default(Some(false)))
    /** Database column created_at SqlType(timestamp) */
    val createdAt: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("created_at")
    /** Database column last_seen SqlType(timestamp) */
    val lastSeen: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("last_seen")

    /** Uniqueness Index over (email) (database name users_email_key) */
    val index1 = index("users_email_key", email, unique=true)
    /** Uniqueness Index over (username) (database name users_username_key) */
    val index2 = index("users_username_key", username, unique=true)
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}
