package com.nonebetwixt.agent

import ru.circumflex.core._
import ru.circumflex.freemarker.FTL._
import java.text.SimpleDateFormat
import java.util.Date
import org.slf4j.LoggerFactory

class Main extends RequestRouter {

  val log = LoggerFactory.getLogger("com.nonebetwixt.agent")

  get(pr("/login/:code")) = {
    redirect("/#" + uri(1))
  }
  
  // substitutes a UUID regex for :code in paths
  private def pr(path: String) = {
    path.replaceAll(
      ":code",
      "(?i:[a-f0-9]{8}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{12})"
    ).r
  }

}