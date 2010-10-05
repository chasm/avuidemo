package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._
import scala.collection.immutable.ListSet

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class InternetCnxn(ui: String, s: String, u: String, a: String, pw: String) {
  
  def this() = {
    this("","","","","")
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=ABORT)
  var userId: String = ui
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var site: String = s
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var uri: String = u
  var alias: String = a
  var passwd: String = pw
  
  @scala.transient
  var tags: ListSet[ContentTag] = ListSet.empty
  
  def getId(): String = id

  def getUserId(): String = this.userId
  def setUserId(ui: String) = { this.userId = ui}

  def getSite(): String = this.site
  def setSite(s: String) = { this.site = s }

  def getUri(): String = this.uri
  def setUri(u: String) = { this.uri = u }

  def getAlias(): String = this.alias
  def setAlias(a: String) = { this.alias = a }

  def getPasswd(): String = this.passwd
  def setPasswd(pw: String) = { this.passwd = pw }
  
  def getTags: List[ContentTag] = this.tags.toList
  def setTags(ts: List[ContentTag]) = {
    this.tags = ListSet.empty
    ts.map(t => { this.tags += t })
  }
  def addTag(t: ContentTag) = { this.tags += t }
  def removeTag(t: ContentTag) = { this.tags -= t }
  
  override def toString(): String = this.alias + " @ " + this.site + " (" + this.uri + ")"
}

object InternetCnxnDAO {
  
  def put(internetCnxn: InternetCnxn) = DbSession.getContentAccessor().internetCnxnsById.put(internetCnxn)
  
  def put(internetCnxns: List[InternetCnxn]) {
    val ca = DbSession.getContentAccessor()
    
    internetCnxns.map(ic => {
      ca.internetCnxnsById.put(ic)
    })
  }
  
  def get(id: String): Option[InternetCnxn] = {
    DbSession.getContentAccessor().internetCnxnsById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[InternetCnxn] = {
    DbSession.getContentAccessor().internetCnxnsById.entities().toList
  }
  
  def getAllWithContentTagsByUserId(userId: String): List[InternetCnxn] = {
    val tags = ContentTagDAO.getAll().map(t => (t.getName(), t)).toMap
    val cnxns = InternetCnxnDAO.getAllByUserId(userId)
    cnxns.map(cnxn => {
      cnxn.tags = ListSet.empty
      CnxnTagDAO.getAllByCnxnId(cnxn.getId()).map(ct => {
        cnxn.tags += tags(ct.getTagName())
      })
      cnxn
    })
  }
  
  def getAllByUserId(userId: String): List[InternetCnxn] = {
    DbSession.getContentAccessor().internetCnxnsByUserId.subIndex(userId).entities().toList
  }
  
  def getAllBySite(site: String): List[InternetCnxn] = {
    DbSession.getContentAccessor().internetCnxnsBySite.subIndex(site).entities().toList
  }
  
  def getAllByUri(uri: String): List[InternetCnxn] = {
    DbSession.getContentAccessor().internetCnxnsByUri.subIndex(uri).entities().toList
  }
}