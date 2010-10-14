package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class CnxnTag(ii: String, ti: String) {
  
  def this() = {
    this("","")
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[InternetCnxn], onRelatedEntityDelete=CASCADE)
  var cnxnId: String = ii

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentTag], onRelatedEntityDelete=CASCADE)
  var tagId: String = ti
  
  def getId(): String = this.id

  def getCnxnId(): String = this.cnxnId
  def setCnxnId(ii: String) = { this.cnxnId = ii}

  def getTagId(): String = this.tagId
  def setTagId(ti: String) = { this.tagId = ti}
  
  override def toString(): String = this.cnxnId + " (" + this.tagId + ")"
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
  
  def getAllByTagId(tagId: String): List[CnxnTag] = {
    DbSession.contentAccessor.cnxnTagsByTagId.subIndex(tagId).entities().toList
  }
}