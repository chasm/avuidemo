package com.nonebetwixt.agent.rest

import ru.circumflex.core._
import org.slf4j.LoggerFactory
import java.util.UUID._

import com.nonebetwixt.agent.model._
import com.nonebetwixt.agent.utilities.Helpers._

class MemberRouter extends RequestRouter("/rest/members") {
  ctx.contentType = "text/xml"
  
  get("") = {
    <members>
      {MemberDAO.getAll().map(member => {
      <member>
        <id>{member.getId()}</id>
        <copId>{member.getCopId()}</copId>
        <userId>{member.getUserId()}</userId>
        <isActive>{member.getIsActive().toString}</isActive>
        <name>{member.getName()}</name>
        <created>{member.getCreated()}</created>
      </member>
      })}
    </members>
  }
  
  get(pr("/:code")) = MemberDAO.get(uri(1)) match {
    case Some(member) =>
      <member>
        <id>{member.getId()}</id>
        <copId>{member.getCopId()}</copId>
        <userId>{member.getUserId()}</userId>
        <isActive>{member.getIsActive().toString}</isActive>
        <name>{member.getName()}</name>
        <created>{member.getCreated()}</created>
      </member>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate member #" + uri(1) + "."}</message>
      </status>
  }
  
  put(pr("/:code")) = {
    val member = MemberDAO.get(uri(1)).getOrElse(new Member(uri(1)))
    
    member.setCopId(param("member[copId]").get)
    member.setUserId(param("member[userId]").get)
    member.setName(param("member[name]").get)
    
    MemberDAO.put(member)
    
    val mbr = MemberDAO.get(member.getId())
    
    mbr match {
      case Some(m) => 
        ctx.statusCode = 201
      
        <status>
          <success>true</success>
          <message>{"Successfully created or updated member #" + uri(1) + "."}</message>
          <uri>{"http://localhost:8180/rest/members/" + uri(1)}</uri>
        </status>
      case None =>
        ctx.statusCode = 400
      
        <status>
          <success>false</success>
          <message>{"Failed to create or update member #" + uri(1) + "."}</message>
        </status> 
    }
  }
  
  delete(pr("/:code")) = MemberDAO.get(uri(1)) match {
    case Some(member) =>
      println("DELETING MEMBER #" + uri(1) + " -- not yet implemented")
      ctx.statusCode = 200
      
      <status>
        <success>true</success>
        <message>{"Successfully deleted member #" + uri(1) + "."}</message>
      </status>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate member #" + uri(1) + "."}</message>
      </status>
  }
}