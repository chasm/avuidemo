package com.nonebetwixt.agent.ui

import javax.servlet._

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.ui._
import com.vaadin.ui.MenuBar._
import com.vaadin.terminal.{ThemeResource, UserError, ExternalResource, Sizeable, Resource}

import com.nonebetwixt.agent.utilities._
import com.nonebetwixt.agent.utilities.Dimension._

import java.util.{Date, UUID}
import java.net.URL

import reflect.{BeanProperty,BeanDisplayName}

class InvitationManager extends HorizontalLayout with Fragmented {
  
  def getFragment() = "invite"
  setSpacing(true)
  setMargin(true)
  setWidth("100%")
  
  val lpanel = new Panel("Left Column")
  lpanel.setWidth("100%")
  
  val colLeft = new VerticalLayout()
  colLeft.setWidth("100%")
  colLeft.addComponent(lpanel)
  addComponent(colLeft)
  
  val form = new InvitationForm
  
  val cpanel = new Panel("Invite others to connect")
  cpanel.setWidth("100%")
  cpanel.addComponent(form)
  cpanel.addStyleName("cpanel")
  
  val colCenter = new VerticalLayout()
  colCenter.setWidth("100%")
  colCenter.addComponent(cpanel)
  addComponent(colCenter)
  
  val rpanel = new Panel("Right Column")
  rpanel.setWidth("100%")
  
  val colRight = new VerticalLayout
  colRight.setWidth("100%")
  colRight.addComponent(rpanel)
  addComponent(colRight)
  
  setExpandRatio(colLeft, 0.2f)
  setExpandRatio(colCenter, 0.6f)
  setExpandRatio(colRight, 0.2f)
}
