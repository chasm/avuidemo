package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

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

class ContentManager extends HorizontalLayout with Fragmented {
  
  def getFragment() = "content"
  setSpacing(true)
  setMargin(true)
  setWidth("100%")
  
  val colCenter = new Panel()
  colCenter.setWidth("100%")
  colCenter.setHeight("480px")
  colCenter.addComponent(new ContentPane())
  colCenter.getContent().asInstanceOf[Layout].setMargin(false)
  colCenter.getContent().asInstanceOf[Layout].setSizeFull()
  addComponent(colCenter)
  
}

class ContentWindow(caption: String, agentId: String, tags: List[ContentTag]) extends Window() {
  setWidth("702px")
  center()
  
  println("")
  println("opening the damn content window")
  println("caption: " + caption)
  println("agentId: " + agentId)
  println("tags: " + tags.mkString("; "))
  println("")
  
  val items = ContentItemDAO.getAllByUserIdAndTags(agentId, tags)
  val tree = new ContentTree(items)
  
  val treePanel = new Panel(caption)
  treePanel.setSizeFull()
  treePanel.getContent().setSizeFull()
  treePanel.getContent().asInstanceOf[VerticalLayout].setMargin(false)
  treePanel.addComponent(tree)
  treePanel.setScrollable(true)
  treePanel.addStyleName("borderless")
  
  addComponent(treePanel)
}