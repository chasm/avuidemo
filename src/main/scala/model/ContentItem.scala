package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._
import scala.collection.immutable.{ListSet, TreeSet}

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class ContentItem(ui: String, pi: String, n: String, v: String, vt: String, u: String, p: Int) {
  
  def this() = {
    this("","","","","","",0)
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=CASCADE)
  var userId: String = ui

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentItem], onRelatedEntityDelete=CASCADE)
  var parentId: String = pi
  
  var name: String = n
  var value: String = v
  var vtype: String = vt
  var uri: String = u
  var position: Int = p
  
  @scala.transient
  var children: TreeSet[ContentItem] = TreeSet.empty(Ordering.fromLessThan[ContentItem](_.getPosition() < _.getPosition()))
  
  @scala.transient
  var tags: TreeSet[ContentTag] = TreeSet.empty(Ordering.fromLessThan[ContentTag](_.getAbbr() < _.getAbbr()))
  
  def getId(): String = id

  def getUserId(): String = this.userId
  def setUserId(ui: String) = { this.userId = ui}

  def getParentId(): String = this.parentId
  def setParentId(pi: String) = { this.parentId = pi}

  def getName(): String = this.name
  def setName(n: String) = { this.name = n}

  def getValue(): String = this.value
  def setValue(v: String) = { this.value = v}

  def getVtype(): String = this.vtype
  def setVtype(vt: String) = { this.vtype = vt}

  def getUri(): String = this.uri
  def setUri(u: String) = { this.uri = u}

  def getPosition(): Int = this.position
  def setPosition(p: Int) = { this.position = p}
  
  def getChildren(): List[ContentItem] = this.children.toList
  def setChildren(c: List[ContentItem]) = {
    this.children = TreeSet.empty(Ordering.fromLessThan[ContentItem](_.getPosition() < _.getPosition()))
    c.map(child => { this.children += child })
  }
  def addChild(c: ContentItem) = { this.children += c }
  def removeChild(c: ContentItem) = { this.children -= c }
  def hasChildren(): Boolean = this.children.nonEmpty
  
  def getTags(): java.util.TreeSet[ContentTag] = new java.util.TreeSet(this.tags.toList)
  def setTags(t: List[ContentTag]) = {
    this.tags = TreeSet.empty(Ordering.fromLessThan[ContentTag](_.getAbbr() < _.getAbbr()))
    t.map(tag => { this.tags += tag })
  }
  def addTag(t: ContentTag) = { this.tags += t }
  def removeTag(t: ContentTag) = { this.tags -= t }
  def hasTags(): Boolean = this.tags.nonEmpty
  
  def getTagsAsHTML(): String = this.tags.toList.map(t => {
    "<span class=\"tag_" + t.getAbbr() + "\" title=\"" + t.getName() + "\">" + t.getAbbr() + "</span>"
  }).mkString(" ")
  
  override def toString(): String = this.name + ": " + this.value + " (" + this.vtype + ")"
}

object ContentItemDAO {
  
  def put(contentItem: ContentItem) = {
    println("VALUE: " + contentItem.getValue())
    DbSession.getContentAccessor().contentItemsById.put(contentItem)
  }
  
  def put(contentItems: List[ContentItem]) {
    val ca = DbSession.getContentAccessor()
    
    contentItems.map(ci => {
      ca.contentItemsById.put(ci)
    })
  }
  
  def get(id: String): Option[ContentItem] = {
    DbSession.getContentAccessor().contentItemsById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[ContentItem] = {
    DbSession.getContentAccessor().contentItemsById.entities().toList
  }
  
  def getAllByUserId(userId: String): List[ContentItem] = {
    DbSession.getContentAccessor().contentItemsByUser.subIndex(userId).entities().toList
      .sorted(Ordering.fromLessThan[ContentItem](_.getPosition() < _.getPosition()))
  }
  
  def getAllByParentId(parentId: String): List[ContentItem] = {
    DbSession.getContentAccessor().contentItemsByParent.subIndex(parentId).entities().toList
  }
  
  def getAllWithChildrenAndTagsByUserId(userId: String): List[ContentItem] = {
    var tags = ContentTagDAO.getAll().map(ct => (ct.getName(), ct)).toMap
    var items = getAllByUserId(userId).map(ci => (ci.getId(), ci)).toMap
    val its = ItemTagDAO.getAll()
    its.map(it => {
      if (items.contains(it.getItemId())) {
        items(it.getItemId()).addTag(tags(it.getTagName()))
      }
    })
    
    items.toList.map(ci => {
      val parentId = ci._2.getParentId()
      if (parentId != null) { items(parentId).addChild(ci._2) }
    })
    
    val list = items.map(ci => ci._2).toList.filter(ci => ci.parentId == null)
    list
  }
  
  def deleteById(id: String) {
    DbSession.getContentAccessor().contentItemsById.delete(id)
  }
  
  def delete(contentItem: ContentItem) {
    deleteById(contentItem.getId())
  }
  
  def delete(contentItems: List[ContentItem]) {
    contentItems.map(contentItem => {
      deleteById(contentItem.getId())
    })
  }
  
}