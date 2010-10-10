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

class AgentCnxnsContainer(val collection: java.util.Collection[AgentRelationship])
  extends BeanItemContainer[AgentRelationship](collection)
  
object AgentCnxnsContainer {
	def load: Option[AgentCnxnsContainer] = {
	  AgentRelationshipDAO.getAllByUserId(AgentServices.getInstance().getCurrentUserId().getOrElse("none")) match {
	    case Nil => None
	    case acs => Some(new AgentCnxnsContainer(acs))
	  }
	}	  
}

object AgentCnxnsTable {
  val ActionEdit = new Action("Suggest New Relationship")
  val ActionDelete = new Action("Delete Connection")
  val Actions: Array[Action] = Array( ActionEdit, ActionDelete )
}

class AgentCnxnsTable extends VerticalLayout {

  val table = new Table()

  val markedRows: HashSet[AnyRef] = new HashSet()

  addComponent(table)

  table.addStyleName("borderless")
  table.addStyleName("striped")

  table.setWidth("100%")
  // table.setHeight("250px")

  table.setSelectable(true)
  table.setMultiSelect(true)
  table.setImmediate(true)
  table.setColumnReorderingAllowed(true)
  table.setColumnCollapsingAllowed(true)
  
  AgentCnxnsContainer.load.map(c => {
    table.setContainerDataSource(c)
    table.addGeneratedColumn("tags", new TagColumnGenerator())
    table.setVisibleColumns(List("agentId", "agentName", "tags").toArray)
    table.setColumnHeaders(List("ID", "Agent", "Associations").toArray)
    table.setColumnExpandRatio("agentName", 1)
    table.setColumnCollapsed("agentId", true)
  })

  table.addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      AgentCnxnsTable.Actions
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case edit if edit == AgentCnxnsTable.ActionEdit =>
          getWindow().showNotification("Menu Item Selected", "Found: " + edit, Notification.TYPE_TRAY_NOTIFICATION)
        case delete if delete == AgentCnxnsTable.ActionDelete =>
          getWindow().showNotification("Menu Item Selected", "Found: " + delete, Notification.TYPE_TRAY_NOTIFICATION)
        case _ =>
          getWindow().showNotification("Menu Item Selected", "Unknown action.", Notification.TYPE_TRAY_NOTIFICATION)
      }
    }
  })
}
