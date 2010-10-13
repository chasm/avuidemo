package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._
import scala.collection.immutable.ListSet

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}
import java.util.Locale._
import java.text.DateFormat._

@Entity
class Forum(uuid: String, ci: String, ui: String, n: String, d: String, ms: ListSet[String]) {
  
  def this() = {
    this("","","","","",ListSet.empty)
  }
  
  def this(uuid: String) = {
    this(uuid,"","","","",ListSet.empty)
  }
  
  def this(ci: String, ui: String, n: String, d: String, ms: ListSet[String]) = {
    this("",ci,ui,n,d,ms)
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
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var isActive: Boolean = true
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var name: String = n
  var desc: String = d
  var moderators: Array[String] = ms.toArray
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

  def getDesc(): String = this.desc
  def setDesc(d: String) = { this.desc = d}

  def getModerators(): ListSet[String] = {
    var ls: ListSet[String] = ListSet.empty
    this.moderators.toList.map(m => ls += m)
    ls
  }
  def setModerators(ms: ListSet[String]) = { this.moderators = ms.toArray}
  
  def getCreated(): String = dateTimeFormatter.format(this.created)
  
  private def dateTimeFormatter = getDateTimeInstance(MEDIUM, MEDIUM, ENGLISH)
  
  override def toString(): String = "Forum: " + this.name
}

object ForumDAO {
  
  def put(forum: Forum) = DbSession.contentAccessor.foraById.put(forum)
  
  def put(fora: List[Forum]) {
    val ca = DbSession.contentAccessor
    
    fora.map(f => {
      ca.foraById.put(f)
    })
  }
  
  def get(id: String): Option[Forum] = {
    DbSession.contentAccessor.foraById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[Forum] = {
    DbSession.contentAccessor.foraById.entities().toList
  }
  
  def getAllByCopId(copId: String): List[Forum] = {
    DbSession.contentAccessor.foraByCopId.subIndex(copId).entities().toList
  }
  
  def getAllByUserId(userId: String): List[Forum] = {
    DbSession.contentAccessor.foraByUserId.subIndex(userId).entities().toList
  }
  
  def getAllByName(name: String): List[Forum] = {
    DbSession.contentAccessor.foraByName.subIndex(name).entities().toList
  }
  
  def getAllByIsActive(isActive: Boolean): List[Forum] = {
    DbSession.contentAccessor.foraByIsActive.subIndex(isActive).entities().toList
  }
  
  def getAllByCopIdAndIsActive(copId: String, isActive: Boolean): List[Forum] = {
    val join = new EntityJoin(DbSession.contentAccessor.foraById)
    
    join.addCondition(DbSession.contentAccessor.foraByCopId, copId)
    join.addCondition(DbSession.contentAccessor.foraByIsActive, isActive)
    
    join.entities().toList
  }
}