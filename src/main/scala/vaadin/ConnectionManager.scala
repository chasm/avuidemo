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

class ConnectionManager extends HorizontalLayout with Fragmented {
  
  def getFragment() = "cnxns"
  setSpacing(true)
  setMargin(true)
  setWidth("100%")
  
  val lpanel = new Panel("Left Column")
  lpanel.setWidth("100%")
  
  val colLeft = new VerticalLayout()
  colLeft.setWidth("100%")
  colLeft.addComponent(lpanel)
  addComponent(colLeft)
  
  val icpanel = new Panel("Internet connections")
  icpanel.setWidth("100%")
  icpanel.addStyleName("cpanel")
  icpanel.getContent().asInstanceOf[VerticalLayout].setMargin(false)
  icpanel.addComponent(new InternetCnxnsTable())
  
  val acpanel = new Panel("Agent connections")
  acpanel.setWidth("100%")
  acpanel.addStyleName("cpanel")
  acpanel.getContent().asInstanceOf[VerticalLayout].setMargin(false)
  acpanel.addComponent(new AgentCnxnsTable())
  
  val colCenter = new VerticalLayout()
  colCenter.setWidth("100%")
  colCenter.addComponent(icpanel)
  colCenter.addComponent(acpanel)
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