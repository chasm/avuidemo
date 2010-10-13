package com.nonebetwixt.agent.rest

import ru.circumflex.core._
import org.slf4j.LoggerFactory

import javax.servlet.ServletContext
import javax.servlet.http.HttpServlet

class Main extends RequestRouter {

  val log = LoggerFactory.getLogger("com.nonebetwixt.agent.rest")

  get("/") = "Home page..."
  
  new CopRouter
  new MemberRouter
  new ForumRouter
  new PostRouter
  new UUIDRouter
}
