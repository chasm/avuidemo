package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}
import java.util.Locale._
import java.text.DateFormat._

@Entity
class AgentMessage(ui: String, st: String, rt: String, b: String) {
  
  def this() = {
    this("","","","")
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=NULLIFY)
  var userId: String = ui
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=NULLIFY)
  var sentToId: String = st
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[AgentMessage], onRelatedEntityDelete=NULLIFY)
  var replyToId: String = rt
  
  var body: String = b
  var sent: Date = new Date()
  
  def getId(): String = this.id

  def getUserId(): String = this.userId
  def setUserId(ui: String) = { this.userId = ui }

  def getSentToId(): String = this.sentToId
  def setSentToId(st: String) = { this.sentToId = st }

  def getReplyToId(): String = this.replyToId
  def setReplyToId(rt: String) = { this.replyToId = rt }

  def getBody(): String = this.body
  def setBody(b: String) = { this.body = b}
  
  def getSent(): String = dateTimeFormatter.format(this.sent)
  
  private def dateTimeFormatter = getDateTimeInstance(MEDIUM, MEDIUM, ENGLISH)
  
  override def toString(): String = "Message: " + this.body
}

object AgentMessageDAO {
  
  def put(agentMessage: AgentMessage) = DbSession.getContentAccessor().agentMessagesById.put(agentMessage)
  
  def put(agentMessages: List[AgentMessage]) {
    val ca = DbSession.getContentAccessor()
    
    agentMessages.map(am => {
      ca.agentMessagesById.put(am)
    })
  }
  
  def get(id: String): Option[AgentMessage] = {
    DbSession.getContentAccessor().agentMessagesById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[AgentMessage] = {
    DbSession.getContentAccessor().agentMessagesById.entities().toList
  }
  
  def getAllByUserId(userId: String): List[AgentMessage] = {
    DbSession.getContentAccessor().agentMessagesByUserId.subIndex(userId).entities().toList
  }
  
  def getAllBySentToId(sentToId: String): List[AgentMessage] = {
    DbSession.getContentAccessor().agentMessagesBySentToId.subIndex(sentToId).entities().toList
  }
  
  def getAllByReplyToId(replyToId: String): List[AgentMessage] = {
    DbSession.getContentAccessor().agentMessagesByReplyToId.subIndex(replyToId).entities().toList
  }
}
