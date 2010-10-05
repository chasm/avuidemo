package com.nonebetwixt.agent.ui

import javax.servlet._

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.ui._
import com.vaadin.ui.MenuBar._
import com.vaadin.terminal.{ThemeResource, UserError, ExternalResource, Sizeable, Resource}
import com.vaadin.ui.TabSheet._

import com.nonebetwixt.agent.utilities._
import com.nonebetwixt.agent.utilities.Dimension._

import java.util.{Date, UUID}
import java.net.URL

import reflect.{BeanProperty,BeanDisplayName}

class CopManager extends HorizontalLayout with Fragmented {
  
  def getFragment() = "cop"
  setSpacing(true)
  setMargin(true)
  setWidth("100%")
  setHeight("480px")
  
  val tabsheet = new TabSheet()
  tabsheet.setSizeFull()
  tabsheet.addStyleName("borderless")

  val copsTable = new CopsTable(tabsheet)
  copsTable.setSizeFull()
  tabsheet.addTab(copsTable, "Communities", null)
  
  addComponent(tabsheet)
}