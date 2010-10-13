package com.nonebetwixt.agent.rest

import ru.circumflex.core._
import org.slf4j.LoggerFactory
import java.util.UUID._

import com.nonebetwixt.agent.utilities.Helpers._

class UUIDRouter extends RequestRouter("/rest/uuids") {
  
  get("/text") = {
    ctx.contentType = "text/plain"
    getUUIDs("1").mkString("\n")
  }
  
  get("/text/:qty") = {
    ctx.contentType = "text/plain"
    getUUIDs(uri("qty")).mkString("\n")
  }
  
  get("/json") = {
    ctx.contentType = "application/json"
    getUUIDs(uri("qty")).map(u => "{ \"uuid\": \"" + u + "\" }").mkString("[ ",", "," ]")
  }
  
  get("/json/:qty") = {
    ctx.contentType = "application/json"
    getUUIDs(uri("qty")).map(u => "{ \"uuid\": \"" + u + "\" }").mkString("[ ",", "," ]")
  }
  
  get("/xml") = {
    ctx.contentType = "text/xml"
    <uuids>
      <uuid>{getUUIDs("1")}</uuid>
    </uuids>
  }
  
  get("/xml/:qty") = {
    ctx.contentType = "text/xml"
    <uuids>{
      getUUIDs(uri("qty")).map(u => {
        <uuid>{u}</uuid>
      })
    }</uuids>
  }
  
  private def getUUIDs(qty: String): List[String] = {
    val end = try { qty.toInt } catch { case _ =>1 }
    for (i <- 1 to end toList) yield randomUUID.toString
  }
  
}
