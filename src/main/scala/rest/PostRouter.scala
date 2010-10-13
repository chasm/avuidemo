package com.nonebetwixt.agent.rest

import ru.circumflex.core._
import org.slf4j.LoggerFactory
import java.util.UUID._

import com.nonebetwixt.agent.model._
import com.nonebetwixt.agent.utilities.Helpers._

class PostRouter extends RequestRouter("/rest/posts") {
  ctx.contentType = "text/xml"
  
  get("") = {
    <posts>
      {PostDAO.getAll().map(post => {
      <post>
        <id>{post.getId()}</id>
        <copId>{post.getCopId()}</copId>
        <userId>{post.getUserId()}</userId>
        <forumId>{post.getForumId()}</forumId>
        <replyToId>{post.getReplyToId()}</replyToId>
        <isActive>{post.getIsActive().toString}</isActive>
        <subject>{post.getSubject()}</subject>
        <body>{post.getBody()}</body>
        <posted>{post.getPosted()}</posted>
      </post>
      })}
    </posts>
  }
  
  get(pr("/:code")) = PostDAO.get(uri(1)) match {
    case Some(post) =>
      <post>
        <id>{post.getId()}</id>
        <copId>{post.getCopId()}</copId>
        <userId>{post.getUserId()}</userId>
        <forumId>{post.getForumId()}</forumId>
        <replyToId>{post.getReplyToId()}</replyToId>
        <isActive>{post.getIsActive().toString}</isActive>
        <subject>{post.getSubject()}</subject>
        <body>{post.getBody()}</body>
        <posted>{post.getPosted()}</posted>
      </post>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate post #" + uri(1) + "."}</message>
      </status>
  }
  
  put(pr("/:code")) = {
    val post = PostDAO.get(uri(1)).getOrElse(new Post(uri(1)))
    
    post.setCopId(param("post[copId]").get)
    post.setUserId(param("post[userId]").get)
    post.setForumId(param("post[forumId]").get)
    post.setReplyToId(param("post[replyToId]").get)
    post.setSubject(param("post[subject]").get)
    post.setBody(param("post[body]").get)
    
    PostDAO.put(post)
    
    val pst = PostDAO.get(post.getId())
    
    pst match {
      case Some(p) => 
        ctx.statusCode = 201
      
        <status>
          <success>true</success>
          <message>{"Successfully created or updated post #" + uri(1) + "."}</message>
          <uri>{"http://localhost:8180/rest/posts/" + uri(1)}</uri>
        </status>
      case None =>
        ctx.statusCode = 400
      
        <status>
          <success>false</success>
          <message>{"Failed to create or update post #" + uri(1) + "."}</message>
        </status> 
    }
  }
  
  delete(pr("/:code")) = PostDAO.get(uri(1)) match {
    case Some(post) =>
      println("DELETING POST #" + uri(1) + " -- not yet implemented")
      ctx.statusCode = 200
      
      <status>
        <success>true</success>
        <message>{"Successfully deleted post #" + uri(1) + "."}</message>
      </status>
    case _ => 
      ctx.statusCode = 404
      
      <status>
        <success>false</success>
        <message>{"Unable to locate post #" + uri(1) + "."}</message>
      </status>
  }
}