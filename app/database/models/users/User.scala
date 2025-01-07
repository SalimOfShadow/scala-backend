package database.models.users

import java.util.UUID

final case class User(
    id: UUID,
    name: String,
    email: String
)


