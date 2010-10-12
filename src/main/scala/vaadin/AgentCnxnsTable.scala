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

class NewAgentCnxnWindow(caption: String, val cnxn: Option[Item]) extends Window(caption) with ClickListener {
  def this(cnxn: Option[Item]) {
    this("Suggest a new relationship", cnxn)
  }
  
  setSizeUndefined()
  center()
  
  val tagsLbl = new Label("Relationships")
  tagsLbl.setWidth("80px")
  
  val tagContainer = ContentTagContainer.load.getOrElse(new ContentTagContainer(List()))
  
  var tags = new ListSelect()
  tags.setWidth("180px")
  tags.setRows(8)
  tags.setMultiSelect(true)
  tags.setNullSelectionAllowed(true)
  ContentTagDAO.getAll().map(ct => {
    tags.addItem(ct.getName())
  })
  cnxn match {
    case Some(c) => c.getItemProperty("tags").getValue().asInstanceOf[List[ContentTag]].map(t => {
      tags.select(t.getName())
    })
    case None => tags.select("Public/Anyone")
  }
  
  val send = new Button("Send", this.asInstanceOf[ClickListener])
  
  val layout = this.getContent().asInstanceOf[VerticalLayout]
  layout.setWidth("240px")
  layout.setSpacing(true)
  layout.setMargin(true)
  layout.addComponent(tags)
  layout.addComponent(send)

  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case s if (s == send) => 
        getWindow().getParent().showNotification("Send this!", "New cnxn: " + cnxn.map(c => c.getItemProperty("agentId").getValue().toString) +
          " : " + tags.getValue().asInstanceOf[java.util.Set[String]].toList.mkString("; "), Notification.TYPE_TRAY_NOTIFICATION)
        getWindow().getParent().removeWindow(getWindow())
      case _ => 
        getWindow().getParent().removeWindow(getWindow())
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
          getWindow().addWindow(new NewAgentCnxnWindow(Some(table.getItem(target))))
        case delete if delete == AgentCnxnsTable.ActionDelete =>
          getWindow().addWindow(new ConfirmDeletionWindow(table, target))
        case _ =>
          getWindow().showNotification("Menu Item Selected", "Unknown action.", Notification.TYPE_TRAY_NOTIFICATION)
      }
    }
  })
  
  private class ConfirmDeletionWindow(table: Table, itemId: AnyRef) extends Window with ClickListener {
    setWidth("360px")
    setHeight("144px")
    setCaption("Are you sure?")
    center()
    
    val lbl = new Label("This action will delete this agent connection. Once deleted, connections are not recoverable " +
      "and can be reëstablished only with the consent of the other party.")
      
    val delete = new Button("Delete", this.asInstanceOf[ClickListener])
    val cancel = new Button("Cancel", this.asInstanceOf[ClickListener])
    
    val hl = new HorizontalLayout()
    hl.setSizeUndefined()
    hl.setMargin(true)
    hl.setSpacing(true)
    hl.addComponent(delete)
    hl.addComponent(cancel)
    
    val layout = getContent().asInstanceOf[VerticalLayout]
    layout.setSizeFull()
    layout.setMargin(true)
    layout.setSpacing(true)
    layout.addComponent(lbl)
    layout.addComponent(hl)
    layout.setExpandRatio(lbl, 1.0f)

    def buttonClick(event: Button#ClickEvent) {
      event.getButton() match {
        case d if (d == delete) => 
          table.removeItem(itemId)
          close()
        case _ => close()
      }
    }
  }
}
