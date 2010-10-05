package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class ItemTag(ii: String, tn: String) {
  
  def this() = {
    this("","")
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentItem], onRelatedEntityDelete=CASCADE)
  var itemId: String = ii

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentTag], onRelatedEntityDelete=CASCADE)
  var tagName: String = tn
  
  def getId(): String = id

  def getItemId(): String = this.itemId
  def setItemId(ii: String) = { this.itemId = ii}

  def getTagName(): String = this.tagName
  def setTagName(tn: String) = { this.tagName = tn}
  
  override def toString(): String = this.itemId + " (" + this.tagName + ")"
}

object ItemTagDAO {
  
  def put(itemTag: ItemTag) = DbSession.getContentAccessor().itemTagsById.put(itemTag)
  
  def put(itemTags: List[ItemTag]) {
    val ca = DbSession.getContentAccessor()
    
    itemTags.map(it => {
      ca.itemTagsById.put(it)
    })
  }
  
  def getAll(): List[ItemTag] = {
    DbSession.getContentAccessor().itemTagsById.entities().toList
  }
  
  def getByItemId(itemId: String): List[ItemTag] = {
    DbSession.getContentAccessor().itemTagsByItemId.subIndex(itemId).entities().toList
  }
  
  def getTagNamesByItemId(itemId: String): List[String] = {
    getByItemId(itemId).map(_.tagName)
  }
  
  def deleteById(id: String) {
    DbSession.getContentAccessor().itemTagsById.delete(id)
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