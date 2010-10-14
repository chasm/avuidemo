package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import javax.servlet._
import javax.servlet.http.HttpSession

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.ui._
import com.vaadin.ui.themes.BaseTheme
import com.vaadin.ui.TabSheet._
import com.vaadin.ui.UriFragmentUtility._
import com.vaadin.terminal.{ThemeResource, UserError, ExternalResource, Sizeable, Resource}
import com.vaadin.ui.Window.Notification
import com.vaadin.ui.Window.CloseListener
import com.vaadin.terminal.gwt.server.WebApplicationContext
import com.vaadin.ui.Button.ClickListener
import com.vaadin.service.ApplicationContext

import com.nonebetwixt.agent.utilities._
import com.nonebetwixt.agent.utilities.Dimension._

import org.vaadin.henrik.refresher._

import javax.mail.{Session => MailSession, Transport, Message, PasswordAuthentication, URLName}
import javax.mail.internet._

import java.io.File
import java.net.URL
import java.util.{Date, UUID, Properties}
import java.util.concurrent.{TimeUnit, Executors}

import com.sleepycat.je.{DatabaseException, Environment, EnvironmentConfig}
import com.sleepycat.persist.{EntityStore, StoreConfig}

trait Fragmented {
  def getFragment(): String
}

class AgentServices extends Application with ApplicationContext.TransactionListener {
  private var currentUserId: Option[String] = None
  private var currentUser: Option[ContentUser] = None
  private var currentExpiration: Option[Long] = None
  setSuperuser()
  
  
  override def init() {
    setTheme("agent")
    getContext().addTransactionListener(this)
    setSuperuser()
    var main = new AgentLoginWindow()
    // main.center()
    setMainWindow(main)
  }
  
  def transactionStart(application: Application, o: Object) {
    if (application == AgentServices.this) {
      AgentServices.currentApplication.set(this)
    }
  }

  def transactionEnd(application: Application, o: Object) {
    if (application == AgentServices.this) {
      AgentServices.currentApplication.set(null)
      AgentServices.currentApplication.remove()
    }
  }
  
  def logIn(userId: String, pw: String) {
    ContentUserDAO.get(userId) match {
      case Some(u) => u.authenticate(pw) match {
        case true =>
          currentUserId = Some(u.id)
          currentUser = Some(u)
          currentExpiration = Some(new Date().getTime + AgentServices.loginExpires)
          println("Login successfull for " + userId)
          loadProtectedResources()
          println("listenForCnxnRequests! agent:" + u.id)
          val ac = new AgentConnector()
          ac.listenForCnxnRequests("agent:" + u.id)
        case _ =>
          println("Login failure for " + userId)
          currentUser = None
          currentUserId = None
          currentExpiration = None
      }
      case None => 
        println("Can't find " + userId)
        currentUser = None
        currentUserId = None
        currentExpiration = None
    }
  }
  
  def logOut() {
    currentExpiration = None
    currentUser = None
    currentUserId = None
    close()
  }

  private def loadProtectedResources() {
    setMainWindow(new AgentWindow())
  }
  
  def getCurrentExpiration(): Option[Long] = currentExpiration
  def getCurrentUserId(): Option[String] = currentUserId
  
  def getCurrentUser: Option[ContentUser] = {
    currentUser match {
      case Some(u) => currentUser
      case None => currentUserId match {
        case Some(id) => ContentUserDAO.get(id)
        case None => None
      }
    }
  }
  
  def setSuperuser() {
    ContentUserDAO.get(AgentServices.suId) match {
      case None => 
        val su = new ContentUser(AgentServices.suId, "Fred", "Fisher", "fcfisher@fcfisher.com", "betwixt")
        su.setExpires(0L)
        ContentUserDAO.put(su)
        println("Setting Fred: " + ContentUserDAO.get(AgentServices.suId).isDefined.toString)
        TestData.loadTestData(AgentServices.suId)
        println("Putting test data.")
      case Some(u) =>
    }
  }
  
  def isSuperuser(): Boolean = { currentUserId.getOrElse("1") == AgentServices.suId }
  
  def notLoggedIn: Boolean = currentExpiration match {
    case Some(expires) => 
      val now = new Date().getTime()
      if (expires + AgentServices.loginExpires > now) {
        currentExpiration = Some(now)
        false
      } else {
        true
      }
    case None => true
  }

  def isLoggedIn(): Boolean = {
    !notLoggedIn
  }
  
  def getTitle(): String = {
    currentUser match {
      case Some(u) => u.nameFirst + "'s Agent"
      case None => "Agent Services"
    }
  }
}

object AgentServices {
  val currentApplication: ThreadLocal[AgentServices] = new ThreadLocal[AgentServices]()
  
  val invitationTimeout: Long = 432000000L
  val loginExpires: Long = 7200000L
  val suId: String = "00000000-1111-2222-3333-444444444444"
  val cmFile: File = new File("db/cm")
  
  def getInstance(): AgentServices = {
    AgentServices.currentApplication.get()
  }
  
  val pwa = new PasswordAuthentication("charles@munat.com","123456789")
  val props = System.getProperties.clone.asInstanceOf[Properties]
  props.put("mail.smtp.host", "localhost")
  val mailSession = MailSession.getDefaultInstance(props)

  def sendEmail(address: String, subject: String, body: String) {
    val message = new MimeMessage( mailSession )

    message.setFrom( new InternetAddress( "webmaster@nonebetwixt.net" ) )
    message.setRecipients( Message.RecipientType.TO, address )
    message.setSubject( subject )
    message.setText( body )
    println( "Sent email: " + message.toString )
    Transport.send( message )
  }

  def getEmailBody(uuid: String): String = {"""
We'd like to invite you to beta test our new Agent Services demo. To accept this invitation, follow this link:

http://nonebetwixt.net/#""" + uuid + """

This is the link you will use to log in to Agent Services to access your Agent. Simply follow the above link
and set a password to get started using your Agent.
"""}
}
