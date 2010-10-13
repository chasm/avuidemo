package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._
import scala.collection.immutable.{ListSet, TreeSet}

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}
import java.util.Locale._
import java.text.DateFormat._

@Entity
class Post(uuid: String, ci: String, ui: String, fi: String, rt: String, s: String, b: String) {
  
  def this() = {
    this("","","","","","","")
  }
  
  def this(uuid: String) = {
    this(uuid,"","","","","","")
  }
  
  def this(ci: String, ui: String, fi: String, rt: String, s: String, b: String) = {
    this("",ci,ui,fi,rt,s,b)
  }
  
  @PrimaryKey
  val id: String = try {
    UUID.fromString(uuid).toString
  } catch {
    case _ => UUID.randomUUID.toString
  }
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[Cop], onRelatedEntityDelete=ABORT)
  var copId: String = ci
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[Member], onRelatedEntityDelete=ABORT)
  var userId: String = ui
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[Forum], onRelatedEntityDelete=ABORT)
  var forumId: String = fi
  
  @SecondaryKey(relate=MANY_TO_ONE, relatedEntity=classOf[Post], onRelatedEntityDelete=ABORT)
  var replyToId: String = rt
  
  var isActive: Boolean = true
  var subject: String = s
  var body: String = b
  var posted: Date = new Date()
  
  @scala.transient
  var children: TreeSet[Post] = TreeSet.empty(Ordering.fromLessThan[Post](_.getMillis() < _.getMillis()))
  
  def getId(): String = this.id

  def getCopId(): String = this.copId
  def setCopId(ci: String) = { this.copId = ci }

  def getUserId(): String = this.userId
  def setUserId(ui: String) = { this.userId = ui }

  def getForumId(): String = this.forumId
  def setForumId(fi: String) = { this.forumId = fi }

  def getReplyToId(): String = this.replyToId
  def setReplyToId(rt: String) = { this.replyToId = rt }
  
  def getIsActive(): Boolean = this.isActive
  def setIsActive(ia: Boolean) { this.isActive = ia }

  def getSubject(): String = this.subject
  def setSubject(s: String) = { this.subject = s }

  def getBody(): String = this.body
  def setBody(b: String) = { this.body = b }
  
  def getPosted(): String = dateTimeFormatter.format(this.posted)
  def getMillis(): Long = this.posted.getTime()
  
  def getChildren(): List[Post] = this.children.toList
  def setChildren(c: List[Post]) = {
    this.children = TreeSet.empty(Ordering.fromLessThan[Post](_.getMillis() < _.getMillis()))
    c.map(child => { this.children += child })
  }
  def addChild(c: Post) = { this.children += c }
  def removeChild(c: Post) = { this.children -= c }
  def hasChildren(): Boolean = this.children.nonEmpty
  
  private def dateTimeFormatter = getDateTimeInstance(MEDIUM, MEDIUM, ENGLISH)
  
  override def toString(): String = "Post: " + this.subject + " - " + getPosted()
}

object PostDAO {
  
  def put(post: Post) = DbSession.contentAccessor.postsById.put(post)
  
  def put(posts: List[Post]) {
    val ca = DbSession.contentAccessor
    posts.map(p => {
      ca.postsById.put(p)
    })
  }
  
  def get(id: String): Option[Post] = {
    DbSession.contentAccessor.postsById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[Post] = {
    DbSession.contentAccessor.postsById.entities().toList
  }
  
  def getAllByCopId(copId: String): List[Post] = {
    DbSession.contentAccessor.postsByCopId.subIndex(copId).entities().toList
  }
  
  def getAllByUserId(userId: String): List[Post] = {
    DbSession.contentAccessor.postsByUserId.subIndex(userId).entities().toList
  }
  
  def getAllByForumId(forumId: String): List[Post] = {
    DbSession.contentAccessor.postsByForumId.subIndex(forumId).entities().toList
  }
  
  def getAllByReplyToId(replyToId: String): List[Post] = {
    DbSession.contentAccessor.postsByReplyToId.subIndex(replyToId).entities().toList
  }
  
  def getAllWithRepliesByForumId(forumId: String): List[Post] = {
    var posts = getAllByForumId(forumId).map(p => (p.getId(), p)).toMap
    posts.toList.map(p => {
      val replyToId = p._2.getReplyToId()
      if (replyToId != null) {
        posts(replyToId).addChild(p._2)
      }
    })
    
    val list = posts.map(p => p._2).toList.filter(p => p.replyToId == null)
    list
  }
}
