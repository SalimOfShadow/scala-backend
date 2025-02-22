package utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ConsoleMessage {
  def logMessage[A](mess: A): Unit = {
    val currentDateTime: LocalDateTime = LocalDateTime.now()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val formattedTime: String = currentDateTime.format(formatter)
    println(s"${formattedTime} |  ${mess}")
  }
}
