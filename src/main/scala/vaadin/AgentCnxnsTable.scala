package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin.data.{Item, Property}
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.event.Action
import com.vaadin.terminal.ThemeResource
import com.vaadin.ui._

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
  val ActionMark = new Action("Mark")
  val ActionUnmark = new Action("Unmark")
  val ActionLog = new Action("Save")
  val ActionsUnmarked: Array[Action] = Array( ActionMark, ActionLog )
  val ActionsMarked: Array[Action] = Array( ActionUnmark, ActionLog )
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
      if (markedRows.contains(target)) {
        Array(AgentCnxnsTable.ActionMark)
      } else {
        Array(AgentCnxnsTable.ActionUnmark)
      }
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case mark if mark == AgentCnxnsTable.ActionMark =>
          markedRows.add(target)
          table.requestRepaint()
        case unmark if unmark == AgentCnxnsTable.ActionUnmark =>
          markedRows.remove(target)
          table.requestRepaint()
        case _ =>
      }
    }
  })
}
