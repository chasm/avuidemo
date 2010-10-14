package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._
import scala.collection.immutable.{ListSet, TreeSet}

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}
import java.awt.Color

import com.biosimilarity.lift.model.zipper._
import com.biosimilarity.lift.model.store._

object ContentTag {
  def toLabel(contentTags: List[ContentTag]): CnxnLabel[String,String] = {
    new CnxnBranch[String,String](
      "label",
      contentTags.map(c => {
        recurseTags(c)
      })
    )
  }
  
  def toLabel(contentTag: ContentTag): CnxnLabel[String,String] = {
    if (contentTag.hasChildren) {
      new CnxnBranch[String,String](
        "label",
        contentTag.getChildren().map(c => {
          recurseTags(c)
        })
      )
    } else {
      new CnxnBranch[String,String](
        "label",
        List(
          new CnxnLeaf[String,String](
            contentTag.getName()
          )
        )
      )
    }
  }
  
  private def recurseTags(contentTag: ContentTag): CnxnLabel[String,String] = {
    if (contentTag.hasChildren) {
      new CnxnBranch[String,String](
        contentTag.getName(),
        contentTag.getChildren().map(c => {
          recurseTags(c)
        })
      )
    } else {
      new CnxnLeaf[String,String](
        contentTag.getName()
      )
    }
  }
}

@Entity
case class ContentTag(ui: String, pi: String, n: String, a: String, p: Int, f: Int, b: Int) extends java.lang.Comparable[ContentTag] {
  
  def this() = {
    this("","","","",0,-1,-16777216)
  }
  
  def this(ui: String, pi: String, n: String, a: String, p: Int) = {
    this(ui,pi,n,a,0,-1,-16777216)
  }
  
  @PrimaryKey
  val id: String = UUID.randomUUID.toString

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentUser], onRelatedEntityDelete=CASCADE)
  var userId: String = ui

  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[ContentTag], onRelatedEntityDelete=CASCADE)
  var parentId: String = pi
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var name: String = n
  
  @SecondaryKey(relate=MANY_TO_ONE)
  var abbr: String = a
  
  var foreground: Int = f
  var background: Int = b
  
  var position: Int = p
  
  @scala.transient
  var children: TreeSet[ContentTag] = TreeSet.empty(Ordering.fromLessThan[ContentTag](_.getPosition() < _.getPosition()))
  
  def getId(): String = id

  def getUserId(): String = this.userId
  def setUserId(ui: String) = { this.userId = ui}

  def getParentId(): String = this.parentId
  def setParentId(pi: String) = { this.parentId = pi}
  
  def getName(): String = this.name
  def setName(n: String) { this.name = n }
  
  def getAbbr(): String = this.abbr
  def setAbbr(a: String) { this.abbr = a }
  
  def getForeground(): Int = this.foreground
  def setForeground(f: Int) { this.foreground = f }
  def getForegroundColor(): Color = new Color(this.foreground)
  
  def getBackground(): Int = this.background
  def setBackground(b: Int) { this.background = b }
  def getBackgroundColor(): Color = new Color(this.background)
  
  def getChildren(): List[ContentTag] = this.children.toList
  def setChildren(c: List[ContentTag]) = {
    this.children = TreeSet.empty(Ordering.fromLessThan[ContentTag](_.getPosition() < _.getPosition()))
    c.map(child => { this.children += child })
  }
  def addChild(c: ContentTag) = { this.children += c }
  def removeChild(c: ContentTag) = { this.children -= c }
  def hasChildren(): Boolean = this.children.nonEmpty

  def getPosition(): Int = this.position
  def setPosition(p: Int) = { this.position = p}
  
  def compareTo(ct: ContentTag): Int = {
    this.getName().compareTo(ct.getName())
  }
  
  override def toString(): String = this.name + " (" + this.abbr + ")"
  
  def toSpan(): String = {
    val f: Color = this.getForegroundColor()
    val b: Color = this.getBackgroundColor()
    
    "<span title=\"" + this.name + "\" style=\"" + 
      "color: rgb(" + f.getRed() + "," + f.getGreen() + "," + f.getBlue() + "); " +
      "background-color: rgb(" + b.getRed() + "," + b.getGreen() + "," + b.getBlue() + ")" +
      "\">" + this.abbr + "</span>"
  }
}

object ContentTagDAO {
  
  def put(contentTag: ContentTag) = DbSession.contentAccessor.contentTagsById.put(contentTag)
  
  def put(contentTags: List[ContentTag]) {
    val ca = DbSession.contentAccessor
    
    contentTags.map(ct => {
      ca.contentTagsById.put(ct)
    })
  }
  
  def get(id: String): Option[ContentTag] = {
    DbSession.contentAccessor.contentTagsById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[ContentTag] = {
    DbSession.contentAccessor.contentTagsByName.entities().toList
  }
  
  def getAllByUserId(userId: String): List[ContentTag] = {
    DbSession.contentAccessor.contentTagsByUser.subIndex(userId).entities().toList
      .sorted(Ordering.fromLessThan[ContentTag](_.getPosition() < _.getPosition()))
  }
  
  def getByNamesByUserId(names: List[String], userId: String): List[ContentTag] = {
    getAllByUserId(userId).filter(t => {
      names.contains(t.getName())
    })
  }
  
  def getByAbbrsByUserId(abbrs: List[String], userId: String): List[ContentTag] = {
    getAllByUserId(userId).filter(t => {
      abbrs.contains(t.getAbbr())
    })
  }
  
  def getByAbbr(abbr: String): Option[ContentTag] = {
    DbSession.contentAccessor.contentTagsByAbbr.get(abbr) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAllWithChildrenByUserId(userId: String): List[ContentTag] = {
    var tags = getAllByUserId(userId).map(ct => (ct.getId(), ct)).toMap
    
    tags.toList.map(ct => {
      val parentId = ct._2.getParentId()
      if (parentId != null) { tags(parentId).addChild(ct._2) }
    })
    
    val list = tags.map(ct => ct._2).toList.filter(ct => ct.parentId == null)
    list
  }
  
  def deleteById(id: String) {
    DbSession.contentAccessor.contentTagsById.delete(id)
  }
  
  def delete(contentTag: ContentTag) {
    deleteById(contentTag.getId())
  }
  
  def delete(contentTags: List[ContentTag]) {
    contentTags.map(contentTag => {
      deleteById(contentTag.getId())
    })
  }
}