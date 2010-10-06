package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin.data.{Item, Property}
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.event.Action
import com.vaadin.terminal.ThemeResource
import com.vaadin.ui._
import com.vaadin.ui.Window.Notification
import com.vaadin.ui.Button.ClickListener

import scala.collection.mutable.HashSet
import scala.collection.JavaConversions._

class CopsContainer(val collection: java.util.Collection[Cop])
  extends BeanItemContainer[Cop](collection)
  
object CopsContainer {
	def load: Option[CopsContainer] = {
	  CopDAO.getAllByIsActive(true) match {
	    case Nil => None
	    case cs => Some(new CopsContainer(cs))
	  }
	}	  
}

object CopsTable {
  val ActionMark = new Action("Mark")
  val ActionUnmark = new Action("Unmark")
  val ActionLog = new Action("Save")
  val ActionsUnmarked: Array[Action] = Array( ActionMark, ActionLog )
  val ActionsMarked: Array[Action] = Array( ActionUnmark, ActionLog )
}

class CopsTable(tabsheet: TabSheet) extends VerticalLayout with ClickListener {
  setSizeFull()
  setMargin(true)
  setSpacing(true)

  val table = new Table()

  val markedRows: HashSet[AnyRef] = new HashSet()

  table.addStyleName("striped")

  table.setWidth("100%")
  table.setHeight("360px")

  table.setSelectable(true)
  table.setMultiSelect(false)
  table.setImmediate(true)
  table.setColumnReorderingAllowed(true)
  table.setColumnCollapsingAllowed(true)
  
  val container = CopsContainer.load.getOrElse(new CopsContainer(CopDAO.getAll()))
  table.setContainerDataSource(container)
  table.setVisibleColumns(List("id", "name", "desc").toArray)
  table.setColumnHeaders(List("ID", "Community", "Description").toArray)
  table.setColumnExpandRatio("desc", 1)
  table.setColumnCollapsed("id", true)
  
  addComponent(table)
  
  val addBtn = new Button("Create a new CoP", this.asInstanceOf[ClickListener])
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case a if a == addBtn => 
        getWindow().addWindow(new CopsWindow(container))
      case _ => println("Huh?")
    }
  }
  
  addComponent(addBtn)

  table.addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      if (markedRows.contains(target)) {
        Array(CopsTable.ActionMark)
      } else {
        Array(CopsTable.ActionUnmark)
      }
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case mark if mark == CopsTable.ActionMark =>
          markedRows.add(target)
          table.requestRepaint()
        case unmark if unmark == CopsTable.ActionUnmark =>
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
        val cop: Cop = value.asInstanceOf[Cop]
        val tab: ForumTabSheet = new ForumTabSheet(cop.getId())
        tabsheet.addStyleName("borderless")
        tabsheet.addTab(tab, cop.getName(), null).setClosable(true)
        tabsheet.setSelectedTab(tab)
      }
    }
  })
}
