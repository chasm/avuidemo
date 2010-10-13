package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class CnxnTag(ii: String, tn: String) {
  
  def this() = {
    this("","")
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[InternetCnxn], onRelatedEntityDelete=CASCADE)
  var cnxnId: String = ii

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentTag], onRelatedEntityDelete=CASCADE)
  var tagName: String = tn
  
  def getId(): String = this.id

  def getCnxnId(): String = this.cnxnId
  def setCnxnId(ii: String) = { this.cnxnId = ii}

  def getTagName(): String = this.tagName
  def setTagName(tn: String) = { this.tagName = tn}
  
  override def toString(): String = this.cnxnId + " (" + this.tagName + ")"
}

object CnxnTagDAO {
  
  def put(cnxnTag: CnxnTag) = DbSession.contentAccessor.cnxnTagsById.put(cnxnTag)
  
  def put(cnxnTags: List[CnxnTag]) {
    val ca = DbSession.contentAccessor
    
    cnxnTags.map(it => {
      ca.cnxnTagsById.put(it)
    })
  }
  
  def getAll(): List[CnxnTag] = {
    DbSession.contentAccessor.cnxnTagsById.entities().toList
  }
  
  def getAllByCnxnId(cnxnId: String): List[CnxnTag] = {
    DbSession.contentAccessor.cnxnTagsByCnxnId.subIndex(cnxnId).entities().toList
  }
  
  def getAllByTagName(tagName: String): List[CnxnTag] = {
    DbSession.contentAccessor.cnxnTagsByTagName.subIndex(tagName).entities().toList
  }
}