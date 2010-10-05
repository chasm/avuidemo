package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

@Entity
class ContentTag(n: String, a: String) extends java.lang.Comparable[ContentTag] {
  
  def this() = {
    this("","")
  }
  
  @PrimaryKey
  var name: String = n
  
  @SecondaryKey(relate=ONE_TO_ONE)
  var abbr: String = a
  
  def getName(): String = this.name
  def setName(n: String) { this.name = n }
  
  def getAbbr(): String = this.abbr
  def setAbbr(a: String) { this.abbr = a }
  
  def compareTo(ct: ContentTag): Int = {
    this.getName().compareTo(ct.getName())
  }
  
  override def toString(): String = this.name + " (" + this.abbr + ")"
}

object ContentTagDAO {
  
  def put(contentTag: ContentTag) = DbSession.getContentAccessor().contentTagsByName.put(contentTag)
  
  def put(contentTags: List[ContentTag]) {
    val ca = DbSession.getContentAccessor()
    
    contentTags.map(ct => {
      ca.contentTagsByName.put(ct)
    })
  }
  
  def get(name: String): Option[ContentTag] = {
    DbSession.getContentAccessor().contentTagsByName.get(name) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[ContentTag] = {
    DbSession.getContentAccessor().contentTagsByName.entities().toList
  }
  
  def getByAbbr(abbr: String): Option[ContentTag] = {
    DbSession.getContentAccessor().contentTagsByAbbr.get(abbr) match {
      case null => None
      case i => Some(i)
    }
  }
}