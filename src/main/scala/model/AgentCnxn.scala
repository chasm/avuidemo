package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class AgentCnxn(li: String, ri: String, ti: String) {
  
  def this() = {
    this("","","")
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=CASCADE)
  var leftId: String = li

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=CASCADE)
  var rightId: String = ri
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentTag], onRelatedEntityDelete=CASCADE)
  var tagId: String = ti
  
  def getId(): String = id

  def getLeftId(): String = this.leftId
  def setLeftId(li: String) = { this.leftId = li }

  def getRightId(): String = this.rightId
  def setRightId(ri: String) = { this.rightId = ri }

  def getTagId(): String = this.tagId
  def setTagId(ti: String) = { this.tagId = ti }
  
  override def toString(): String = this.leftId + " :: " + this.rightId + " (" + this.tagId + ")"
}

object AgentCnxnDAO {
  
  /**
   * Save or update a single AgentCnxn
   *
   * @param agentCnxn The AgentCnxn to save or update
   */
  def put(agentCnxn: AgentCnxn) = DbSession.contentAccessor.agentCnxnsById.put(agentCnxn)
  
  /**
   * Save or update a list of AgentCnxns
   *
   * @param agentCnxns The List[AgentCnxn] to save or update
   */
  def put(agentCnxns: List[AgentCnxn]) {
    val ca = DbSession.contentAccessor
    
    agentCnxns.map(ac => {
      ca.agentCnxnsById.put(ac)
    })
  }
  
  /**
   * Get an AgentCnxn by UUID (as String)
   *
   * @param id The ID of the AgentCnxn to get
   * @return Some(AgentCnxn) or None
   */
  def get(id: String): Option[AgentCnxn] = {
    DbSession.contentAccessor.agentCnxnsById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  /**
   * Get a list of all AgentCnxns
   *
   * @return A List of AgentCnxns
   */
  def getAll(): List[AgentCnxn] = {
    DbSession.contentAccessor.agentCnxnsById.entities().toList
  }
  
  /**
   * Get a list of all AgentCnxns for a specific ContentUser
   *
   * @param userId The ID of the ContentUser whose AgentCnxns will be retrieved
   * @return A List of AgentCnxns
   */
  def getAllByUserId(userId: String): List[AgentCnxn] = {
    DbSession.contentAccessor.agentCnxnsByRightId.subIndex(userId).entities().toList.map(ac => {
      val lid = ac.getLeftId()
      val rid = ac.getRightId()
      ac.setLeftId(rid)
      ac.setRightId(lid)
      ac
    }) ::: DbSession.contentAccessor.agentCnxnsByLeftId.subIndex(userId).entities().toList
  }
  
  /**
   * Get a list of all AgentCnxns for a specific leftId
   *
   * @param leftId The ID of the ContentUser whose AgentCnxns as leftId will be retrieved
   * @return A List of AgentCnxns
   */
  def getAllByLeftId(leftId: String): List[AgentCnxn] = {
    DbSession.contentAccessor.agentCnxnsByLeftId.subIndex(leftId).entities().toList
  }
  
  /**
   * Get a list of all AgentCnxns for a specific rightId
   *
   * @param rightId The ID of the ContentUser whose AgentCnxns as rightId will be retrieved
   * @return A List of AgentCnxns
   */
  def getAllByRightId(rightId: String): List[AgentCnxn] = {
    DbSession.contentAccessor.agentCnxnsByRightId.subIndex(rightId).entities().toList
  }
  
  /**
   * Get a list of all AgentCnxns for a specific tag name
   *
   * @param tagId The ID of the ContentTag whose AgentCnxns will be retrieved
   * @return A List of AgentCnxns
   */
  def getAllByTagId(tagId: String): List[AgentCnxn] = {
    DbSession.contentAccessor.agentCnxnsByTagId.subIndex(tagId).entities().toList
  }
}