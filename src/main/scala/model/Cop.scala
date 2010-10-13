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
class Cop(uuid: String, su: String, n: String, d: String) {
  
  def this() = {
    this("","","","")
  }
  
  def this(uuid: String) = {
    this(uuid,"","","")
  }
  
  def this(su: String, n: String, d: String) = {
    this("",su,n,d)
  }
  
  @PrimaryKey
  val id: String = try {
    UUID.fromString(uuid).toString
  } catch {
    case _ => UUID.randomUUID.toString
  }
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=ABORT)
  var userId: String = su
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var isActive: Boolean = true
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var name: String = n
  var desc: String = d
  var created: Date = new Date()
  
  def getId(): String = this.id

  def getUserId(): String = this.userId
  def setUserId(ui: String) = { this.userId = ui}
  
  def getIsActive(): Boolean = this.isActive
  def setIsActive(ia: Boolean) { this.isActive = ia }

  def getName(): String = this.name
  def setName(n: String) = { this.name = n}

  def getDesc(): String = this.desc
  def setDesc(d: String) = { this.desc = d}
  
  def getCreated(): String = dateTimeFormatter.format(this.created)
  
  private def dateTimeFormatter = getDateTimeInstance(MEDIUM, MEDIUM, ENGLISH)
  
  override def toString(): String = "CoP: " + this.name
}

object CopDAO {
  
  def put(cop: Cop) = DbSession.contentAccessor.copsById.put(cop)
  
  def put(cops: List[Cop]) {
    val ca = DbSession.contentAccessor
    
    cops.map(c => {
      ca.copsById.put(c)
    })
  }
  
  def get(id: String): Option[Cop] = {
    DbSession.contentAccessor.copsById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[Cop] = {
    DbSession.contentAccessor.copsById.entities().toList
  }
  
  def getAllByName(name: String): List[Cop] = {
    DbSession.contentAccessor.copsByName.subIndex(name).entities().toList
  }
  
  def getAllByIsActive(isActive: Boolean): List[Cop] = {
    DbSession.contentAccessor.copsByIsActive.subIndex(isActive).entities().toList
  }
}
  