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

class ForaContainer(val collection: java.util.Collection[Forum])
  extends BeanItemContainer[Forum](collection)
  
object ForaContainer {
	def load(copId: String): Option[ForaContainer] = {
	  ForumDAO.getAllByCopIdAndIsActive(copId, true) match {
	    case Nil => None
	    case cs => Some(new ForaContainer(cs))
	  }
	}	  
}

object ForaTable {
  val ActionMark = new Action("Mark")
  val ActionUnmark = new Action("Unmark")
  val ActionLog = new Action("Save")
  val ActionsUnmarked: Array[Action] = Array( ActionMark, ActionLog )
  val ActionsMarked: Array[Action] = Array( ActionUnmark, ActionLog )
}

class ForaTable(copId: String, tabsheet: TabSheet) extends VerticalLayout {

  val table = new Table()

  val markedRows: HashSet[AnyRef] = new HashSet()

  addComponent(table)

  table.addStyleName("striped")

  table.setWidth("100%")
  table.setHeight("300px")

  table.setSelectable(true)
  table.setMultiSelect(false)
  table.setImmediate(true)
  table.setColumnReorderingAllowed(true)
  table.setColumnCollapsingAllowed(true)
  
  ForaContainer.load(copId).map(c => {
    table.setContainerDataSource(c)
    table.setVisibleColumns(List("id", "name", "desc").toArray)
    table.setColumnHeaders(List("ID", "Forum", "Description").toArray)
    table.setColumnExpandRatio("desc", 1)
    table.setColumnCollapsed("id", true)
  })

  table.addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      if (markedRows.contains(target)) {
        Array(ForaTable.ActionMark)
      } else {
        Array(ForaTable.ActionUnmark)
      }
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case mark if mark == ForaTable.ActionMark =>
          markedRows.add(target)
          table.requestRepaint()
        case unmark if unmark == ForaTable.ActionUnmark =>
          markedRows.remove(target)
          table.requestRepaint()
        case _ =>
      }
    }
  })
  
  table.addListener(new Property.ValueChangeListener() {
    def valueChange(event: ValueChangeEvent) {
      val value = event.getProperty().getValue()
      if (value != null) {
        val forum: Forum = value.asInstanceOf[Forum]
        val tab = new TopicsManager(forum.getId())
        tabsheet.addTab(tab, forum.getName(), null).setClosable(true)
        tabsheet.setSelectedTab(tab)
      }
    }
  })
}
