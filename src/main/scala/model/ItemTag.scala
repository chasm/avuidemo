package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class ItemTag(ii: String, ti: String) {
  
  def this() = {
    this("","")
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentItem], onRelatedEntityDelete=CASCADE)
  var itemId: String = ii

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentTag], onRelatedEntityDelete=CASCADE)
  var tagId: String = ti
  
  def getId(): String = id

  def getItemId(): String = this.itemId
  def setItemId(ii: String) = { this.itemId = ii}

  def getTagId(): String = this.tagId
  def setTagId(ti: String) = { this.tagId = ti}
  
  override def toString(): String = this.itemId + " (" + this.tagId + ")"
}

object ItemTagDAO {
  
  def put(itemTag: ItemTag) = DbSession.contentAccessor.itemTagsById.put(itemTag)
  
  def put(itemTags: List[ItemTag]) {
    val ca = DbSession.contentAccessor
    
    itemTags.map(it => {
      ca.itemTagsById.put(it)
    })
  }
  
  def getAll(): List[ItemTag] = {
    DbSession.contentAccessor.itemTagsById.entities().toList
  }
  
  def getAllByItemId(itemId: String): List[ItemTag] = {
    DbSession.contentAccessor.itemTagsByItemId.subIndex(itemId).entities().toList
  }
  
  def getTagIdsByItemId(itemId: String): List[String] = {
    getAllByItemId(itemId).map(_.tagId)
  }
  
  def deleteById(id: String) {
    DbSession.contentAccessor.itemTagsById.delete(id)
  }
  
  def delete(itemTag: ItemTag) {
    deleteById(itemTag.getId())
  }
  
  def delete(itemTags: List[ItemTag]) {
    itemTags.map(itemTag => {
      deleteById(itemTag.getId())
    })
  }
}