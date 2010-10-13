package com.nonebetwixt.agent.rest

import ru.circumflex.core._
import org.slf4j.LoggerFactory
import java.util.UUID._

import com.nonebetwixt.agent.model._
import com.nonebetwixt.agent.utilities.Helpers._

class CopRouter extends RequestRouter("/rest/cops") {
  ctx.contentType = "text/xml"
  
  get("") = {
    <cops>
      {CopDAO.getAll().map(cop => {
      <cop>
        <id>{cop.getId()}</id>
        <userId>{cop.getUserId()}</userId>
        <isActive>{cop.getIsActive().toString}</isActive>
        <name>{cop.getName()}</name>
        <desc>{cop.getDesc()}</desc>
        <created>{cop.getCreated()}</created>
      </cop>
      })}
    </cops>
  }
  
  get(pr("/:code")) = CopDAO.get(uri(1)) match {
    case Some(cop) =>
      <cop>
        <id>{cop.getId()}</id>
        <userId>{cop.getUserId()}</userId>
        <isActive>{cop.getIsActive().toString}</isActive>
        <name>{cop.getName()}</name>
        <desc>{cop.getDesc()}</desc>
        <created>{cop.getCreated()}</created>
      </cop>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate cop #" + uri(1) + "."}</message>
      </status>
  }
  
  put(pr("/:code")) = {
    val cop = CopDAO.get(uri(1)).getOrElse(new Cop(uri(1)))
    
    cop.setUserId(param("cop[userId]").get)
    cop.setName(param("cop[name]").get)
    cop.setDesc(param("cop[desc]").get)
    
    CopDAO.put(cop)
    
    val member = new Member(cop.getId(), cop.getUserId(), param("cop[username]").get)
    
    MemberDAO.put(member)
    
    val mbr = MemberDAO.get(member.getId())
    
    mbr match {
      case Some(m) => 
        ctx.statusCode = 201
      
        <status>
          <success>true</success>
          <message>{"Successfully created or updated cop #" + uri(1) + "."}</message>
          <uri>{"http://localhost:8180/rest/cops/" + uri(1)}</uri>
        </status>
      case None =>
        ctx.statusCode = 400
      
        <status>
          <success>false</success>
          <message>{"Failed to create or update cop #" + uri(1) + "."}</message>
        </status> 
    }
  }
  
  delete(pr("/:code")) = CopDAO.get(uri(1)) match {
    case Some(cop) =>
      println("DELETING COP #" + uri(1) + " -- not yet implemented")
      ctx.statusCode = 200
      
      <status>
        <success>true</success>
        <message>{"Successfully deleted cop #" + uri(1) + "."}</message>
      </status>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate cop #" + uri(1) + "."}</message>
      </status>
  }
}