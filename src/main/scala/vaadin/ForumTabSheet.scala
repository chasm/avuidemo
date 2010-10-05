package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin.data.{Item, Property}
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.event.Action
import com.vaadin.terminal.ThemeResource
import com.vaadin.ui._
import com.vaadin.ui.Window.Notification

import scala.collection.mutable.HashSet
import scala.collection.JavaConversions._

class ForumTabSheet(copId: String) extends VerticalLayout {
  setSpacing(true)
  setMargin(true)
  setWidth("100%")
  
  val tabsheet = new TabSheet()
  tabsheet.setWidth("100%")
  tabsheet.setStyleName("borderless")

  val foraTable = new ForaTable(copId, tabsheet)
  val layout = new VerticalLayout
  layout.setMargin(true)
  layout.setSizeFull()
  layout.addComponent(foraTable)
  tabsheet.addTab(layout, "Fora", null)
  
  addComponent(tabsheet)
}