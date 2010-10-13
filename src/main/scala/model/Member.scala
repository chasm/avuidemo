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
class Member(uuid: String, ci: String, ui: String, n: String) {
  
  def this() = {
    this("","","","")
  }
  
  def this(uuid: String) = {
    this(uuid,"","","")
  }
  
  def this(ci: String, ui: String, n: String) = {
    this("",ci,ui,n)
  }
  
  @PrimaryKey
  val id: String = try {
    UUID.fromString(uuid).toString
  } catch {
    case _ => UUID.randomUUID.toString
  }
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[Cop], onRelatedEntityDelete=ABORT)
  var copId: String = ci
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=ABORT)
  var userId: String = ui
  
  var isActive: Boolean = true
  var name: String = n
  var created: Date = new Date()
  
  def getId(): String = this.id

  def getCopId(): String = this.copId
  def setCopId(ci: String) = { this.copId = ci}

  def getUserId(): String = this.userId
  def setUserId(ui: String) = { this.userId = ui}
  
  def getIsActive(): Boolean = this.isActive
  def setIsActive(ia: Boolean) { this.isActive = ia }

  def getName(): String = this.name
  def setName(n: String) = { this.name = n}
  
  def getCreated(): String = dateTimeFormatter.format(this.created)
  
  private def dateTimeFormatter = getDateTimeInstance(MEDIUM, MEDIUM, ENGLISH)
  
  override def toString(): String = "Member: " + this.name
}

object MemberDAO {
  
  def put(member: Member) = DbSession.contentAccessor.membersById.put(member)
  
  def put(members: List[Member]) {
    val ca = DbSession.contentAccessor
    
    members.map(m => {
      ca.membersById.put(m)
    })
  }
  
  def get(id: String): Option[Member] = {
    DbSession.contentAccessor.membersById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[Member] = {
    DbSession.contentAccessor.membersById.entities().toList
  }
  
  def getAllByCopId(copId: String): List[Member] = {
    DbSession.contentAccessor.membersByCopId.subIndex(copId).entities().toList
  }
  
  def getAllByUserId(userId: String): List[Member] = {
    DbSession.contentAccessor.membersByUserId.subIndex(userId).entities().toList
  }
  
  def getByCopIdAndUserId(copId: String, userId: String): Option[Member] = {
    val join = new EntityJoin(DbSession.contentAccessor.membersById)
    
    join.addCondition(DbSession.contentAccessor.membersByCopId, copId)
    join.addCondition(DbSession.contentAccessor.membersByUserId, userId)
    
    join.entities().toList match {
      case Nil => None
      case xs => Some(xs.head)
    }
  }
}

