package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._
import scala.collection.immutable.ListSet
import scala.collection.mutable.HashMap


import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

class AgentRelationship(ai: String, an: String) {
  def this() = this("","")
  
  var agentId: String = ai
  var agentName: String = an
  var tags: ListSet[ContentTag] = ListSet.empty
  
  def getAgentId: String = this.agentId
  def setAgentId(ai: String) = { this.agentId = ai }
  
  def getAgentName: String = this.agentName
  def setAgentName(an: String) = { this.agentName = an }
  
  def getTags: List[ContentTag] = this.tags.toList
  def setTags(ts: List[ContentTag]) = {
    this.tags = ListSet.empty
    ts.map(t => { this.tags += t })
  }
  def addTag(t: ContentTag) = { this.tags += t }
  def removeTag(t: ContentTag) = { this.tags -= t }
}

object AgentRelationshipDAO {
  
  def getAllByUserId(userId: String): List[AgentRelationship] = {
    val tags = ContentTagDAO.getAll().map(t => (t.getName(), t)).toMap                 // map tagName to tag
    val users = ContentUserDAO.getAll().map(u => (u.getId(), u.getName())).toMap       // map userId to user name
    val rels: HashMap[String,AgentRelationship] = HashMap.empty                        // empty hash map for agent relationships
    AgentCnxnDAO.getAllByUserId(userId).map(c => {
      val aId = c.getRightId()
      if (rels.contains(aId)) { 
        rels(aId).tags += tags(c.getTagName())
      } else {
        val r = new AgentRelationship(aId, users(aId))
        r.tags += tags(c.getTagName())
        rels += aId -> r
      }
    })
    rels.toList.map(x => x._2)                                                         // fill map with relationships combining tags
  }
  
}