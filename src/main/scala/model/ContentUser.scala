package com.nonebetwixt.agent.model

import scala.collection.JavaConversions._
import scala.collection.immutable.ListSet

import com.sleepycat.persist.model.{Entity, PrimaryKey, SecondaryKey}
import com.sleepycat.persist.model.Relationship._
import com.sleepycat.persist.model.DeleteAction._
import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityCursor, EntityJoin}

import java.util.{Date, UUID}

import org.apache.commons.codec.digest.DigestUtils

@Entity
class ContentUser(uuid: String, nf: String, nl: String, ea: String, pw: String) {
  
  def this() = {
    this(UUID.randomUUID.toString,"","","","")
  }
  
  @PrimaryKey
  var id: String = uuid
  
  var isActive: Boolean = true
  var nameFirst: String = nf
  var nameLast: String = nl
  var emailAddress: String = ea
  var password: String = UUID.randomUUID.toString
  var passwordSalt: String = UUID.randomUUID.toString
  var expires: Date = new Date(new Date().getTime() + 432000000L)
  setPassword(pw)
  
  def getId(): String = id
  
  def getIsActive(): Boolean = this.isActive
  def setIsActive(ia: Boolean) { this.isActive = ia }
  
  def getNameFirst(): String = this.nameFirst
  def setNameFirst(nf: String) { this.nameFirst = nf }
  
  def getNameLast(): String = this.nameLast
  def setNameLast(nl: String) { this.nameLast = nl }
  
  def getEmailAddress(): String = this.emailAddress
  def setEmailAddress(ea: String) { this.emailAddress = ea }
  
  def getExpires(): Long = this.expires.getTime()
  def setExpires(t: Long) { this.expires = new Date(t) }
  
  def setPassword(pw: String) {
    if (pw.length > 5) {
      // this.passwordSalt = UUID.randomUUID.toString
      // this.password = DigestUtils.sha512Hex(pw + this.passwordSalt)
      this.password = pw
    }
  }
  
  def getPassword(): String = this.password
  
  def authenticate(pw: String): Boolean = {
    this.password == pw // DigestUtils.sha512Hex(pw + this.passwordSalt)
  }
  
  def getName(): String = this.nameFirst + " " + this.nameLast
  
  override def toString(): String = getName()
}

object ContentUserDAO {
  
  def put(contentUser: ContentUser) = DbSession.getContentAccessor().contentUsersById.put(contentUser)
  
  def put(contentUsers: List[ContentUser]) {
    val ca = DbSession.getContentAccessor()
    
    contentUsers.map(cu => {
      ca.contentUsersById.put(cu)
    })
  }
  
  def get(id: String): Option[ContentUser] = {
    DbSession.getContentAccessor().contentUsersById.get(id) match {
      case null => None
      case i => Some(i)
    }
  }
  
  def getAll(): List[ContentUser] = {
    DbSession.getContentAccessor().contentUsersById.entities().toList
  }
}