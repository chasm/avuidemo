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
  setWidth("100%")
  colCenter.setHeight("480px")
  colCenter.addComponent(new ContentPane())
  colCenter.getContent().asInstanceOf[Layout].setMargin(false)
  colCenter.getContent().asInstanceOf[Layout].setSizeFull()
  addComponent(colCenter)
  
}
 
class ContentTagContainer(val collection: java.util.Collection[ContentTag])
  extends BeanItemContainer[ContentTag](collection)
  
object ContentTagContainer {
	def load: Option[ContentTagContainer] = {
	  ContentTagDAO.getAll() match {
	    case Nil => None
	    case cts => Some(new ContentTagContainer(cts))
	  }
	}	  
}
