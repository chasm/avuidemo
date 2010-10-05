package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import javax.servlet._

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data.Item
import com.vaadin.data.validator.{EmailValidator, StringLengthValidator}
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.ui._
import com.vaadin.ui.MenuBar._
import com.vaadin.ui.Window.Notification
import com.vaadin.terminal.{ThemeResource, UserError, ExternalResource, Sizeable, Resource}

import com.nonebetwixt.agent.utilities._
import com.nonebetwixt.agent.utilities.Dimension._

import java.util.{Date, UUID}
import java.net.URL

import reflect.{BeanProperty,BeanDisplayName}

class InvitationForm extends VerticalLayout {
  
  var newuser = new ContentUser()
  var invitationItem = new BeanItem(newuser)
  
  val form = new Form()
  form.setWriteThrough(false)
  form.setInvalidCommitted(false)
  
  form.setFormFieldFactory(new InvitationFieldFactory())
  form.setItemDataSource(invitationItem)
  form.setVisibleItemProperties(List("nameFirst", "nameLast", "emailAddress"))
  
  addComponent(form)
  
  form.setDescription("Use the form below to invite others to create their own Agents and connect with you.")
  
  val buttons = new HorizontalLayout()
  buttons.setSpacing(true)
  
  val send = new Button("Send", new Button.ClickListener() {
    def buttonClick(event: Button#ClickEvent ) {
      try {
        form.commit()
        sendInvitation(newuser)
        println("newuser.id: " + newuser.getId())
        newuser = new ContentUser()
        invitationItem = new BeanItem(newuser)
        form.setItemDataSource(invitationItem)
        form.setVisibleItemProperties(List("nameFirst", "nameLast", "emailAddress"))
      } catch {
        case _ =>
      }
    }
  })
  buttons.addComponent(send)
  
  val discard = new Button("Discard", new Button.ClickListener() {
    def buttonClick(event: Button#ClickEvent) { form.discard() }
  })
  buttons.addComponent(discard)

  form.getFooter().addComponent(buttons)
  form.getFooter().setMargin(true)

  private class InvitationFieldFactory extends DefaultFieldFactory {

    override def createField(item: Item, propertyId: AnyRef, uiContext: Component): Field = {
      var fld = super.createField(item, propertyId, uiContext)
      
      propertyId match {
        case "nameFirst" => 
          val tf: TextField = fld.asInstanceOf[TextField]
          tf.setRequired(true)
          tf.setRequiredError("Please enter a first name")
          tf.setWidth("12em")
          tf.addValidator(new StringLengthValidator("First name must be 1-25 characters", 1, 25, false))
        case "nameLast" => 
          val tf: TextField = fld.asInstanceOf[TextField]
          tf.setRequired(true)
          tf.setRequiredError("Please enter a last name")
          tf.setWidth("12em")
          tf.addValidator(new StringLengthValidator("Last name must be 2-25 characters", 2, 25, false))
        case "emailAddress" => 
          val tf: TextField = fld.asInstanceOf[TextField]
          tf.setRequired(true)
          tf.setRequiredError("Please enter an email address")
          tf.setWidth("12em")
          tf.addValidator(new EmailValidator("Email address must be valid"))
        case _ => null
      }

      fld
    }
  }
  
  private def sendInvitation(newuser: ContentUser) {
    
    ContentUserDAO.put(newuser)
    
    AgentServices.sendEmail(
      newuser.getEmailAddress,
      "An invitation to Agent Services",
      AgentServices.getEmailBody(newuser.getId)
    )
    
    getWindow().showNotification("Invitation Sent", "An invitation was sent to " +
      newuser.getNameFirst + " " + newuser.getNameLast + " at " + newuser.getEmailAddress,
      Notification.TYPE_TRAY_NOTIFICATION)
  }
}
