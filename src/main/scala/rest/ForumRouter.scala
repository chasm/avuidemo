package com.nonebetwixt.agent.rest

import ru.circumflex.core._
import org.slf4j.LoggerFactory
import java.util.UUID._

import com.nonebetwixt.agent.model._
import com.nonebetwixt.agent.utilities.Helpers._

class ForumRouter extends RequestRouter("/rest/fora") {
  ctx.contentType = "text/xml"
  
  get("") = {
    <fora>
      {ForumDAO.getAll().map(forum => {
      <forum>
        <id>{forum.getId()}</id>
        <copId>{forum.getCopId()}</copId>
        <userId>{forum.getUserId()}</userId>
        <isActive>{forum.getIsActive().toString}</isActive>
        <name>{forum.getName()}</name>
        <desc>{forum.getDesc()}</desc>
        <created>{forum.getCreated()}</created>
      </forum>
      })}
    </fora>
  }
  
  get(pr("/:code")) = ForumDAO.get(uri(1)) match {
    case Some(forum) =>
      <forum>
        <id>{forum.getId()}</id>
        <copId>{forum.getCopId()}</copId>
        <userId>{forum.getUserId()}</userId>
        <isActive>{forum.getIsActive().toString}</isActive>
        <name>{forum.getName()}</name>
        <desc>{forum.getDesc()}</desc>
        <created>{forum.getCreated()}</created>
      </forum>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate forum #" + uri(1) + "."}</message>
      </status>
  }
  
  put(pr("/:code")) = {
    val forum = ForumDAO.get(uri(1)).getOrElse(new Forum(uri(1)))
    
    forum.setCopId(param("forum[copId]").get)
    forum.setUserId(param("forum[userId]").get)
    forum.setName(param("forum[name]").get)
    forum.setDesc(param("forum[desc]").get)
    
    ForumDAO.put(forum)
    
    val frm = ForumDAO.get(forum.getId())
    
    frm match {
      case Some(f) => 
        ctx.statusCode = 201
      
        <status>
          <success>true</success>
          <message>{"Successfully created or updated forum #" + uri(1) + "."}</message>
          <uri>{"http://localhost:8180/rest/fora/" + uri(1)}</uri>
        </status>
      case None =>
        ctx.statusCode = 400
      
        <status>
          <success>false</success>
          <message>{"Failed to create or update forum #" + uri(1) + "."}</message>
        </status> 
    }
  }
  
  delete(pr("/:code")) = ForumDAO.get(uri(1)) match {
    case Some(forum) =>
      println("DELETING FORUM #" + uri(1) + " -- not yet implemented")
      ctx.statusCode = 200
      
      <status>
        <success>true</success>
        <message>{"Successfully deleted forum #" + uri(1) + "."}</message>
      </status>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate forum #" + uri(1) + "."}</message>
      </status>
  }
}