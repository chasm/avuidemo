package com.nonebetwixt.agent.utilities

import ru.circumflex.core._
import org.slf4j.LoggerFactory

import java.text.DateFormat._
import java.util.Locale._

object Helpers {
  // substitutes a UUID regex for :code in paths
  def pr(path: String) = {
    path.replaceAll(
      ":code", 
      "([A-Fa-f0-9]{8}\\-[A-Fa-f0-9]{4}\\-[A-Fa-f0-9]{4}\\-[A-Fa-f0-9]{4}\\-[A-Fa-f0-9]{12})"
    ).r
  }
  
  def timeFromMillis(millis: Long): String = {
    (millis / 3600000).toString + " hrs " + (millis % 3600000) + " mins"
  }
  
  def dateFormatter = getDateInstance(MEDIUM, ENGLISH)
  def timeFormatter = getTimeInstance(SHORT, ENGLISH)
  def dateTimeFormatter = getDateTimeInstance(MEDIUM, MEDIUM, ENGLISH)
}