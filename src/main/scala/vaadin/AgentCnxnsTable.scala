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

import java.util.{Date, UUID}

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
  
  val tagContainer = LabelTreeContainer.load(
    AgentServices.getInstance().getCurrentUserId().getOrElse("none")
  )
  
  // var tags = new ListSelect("Relationships", tagContainer)
  // tags.setWidth("100%")
  // tags.setRows(8)
  // tags.setMultiSelect(true)
  // tags.setNullSelectionAllowed(true)
  // cnxn match {
  //   case Some(c) => c.getItemProperty("tags").getValue().asInstanceOf[List[ContentTag]]
  //   case None => 
  // }
  
  val tags = new LabelTree(tagContainer)
  
  val tagPanel = new Panel()
  val vl = tagPanel.getContent().asInstanceOf[VerticalLayout]
  tagPanel.setWidth("100%")
  tagPanel.setHeight("240px")
  vl.setSizeUndefined()
  vl.setMargin(true)
  vl.setSpacing(true)
  vl.addComponent(tags)
  
  val message = new TextField("Message")
  message.setRows(5)
  message.setWidth("100%")
  message.setInputPrompt("Type a personal message here.")
  
  val send = new Button("Send", this.asInstanceOf[ClickListener])
  
  val layout = this.getContent().asInstanceOf[VerticalLayout]
  layout.setWidth("240px")
  layout.setSpacing(true)
  layout.setMargin(true)
  layout.addComponent(tagPanel)
  layout.addComponent(message)
  layout.addComponent(send)

  def buttonClick(event: Button#ClickEvent) {
    import Option.{apply => ?} 
    
    event.getButton() match {
      case s if (s == send) => 
        ?(tags.tree.getValue()) match {
          case Some(t) =>
            for {
              id <- t.asInstanceOf[java.util.Set[_]]
              tag = tags.tree.getItem(id).getItemProperty("tag").getValue().asInstanceOf[ContentTag]
              sourceId <- AgentServices.getInstance().getCurrentUserId()
              c <- cnxn
              targetId = c.getItemProperty("agentId").getValue().toString
            } {
              try {
                UUID.fromString(targetId)
                val ac = new AgentConnector()
                ac.requestCnxn(
                  "agent:" + sourceId,
                  "agent:" + targetId,
                  tag,
                  message.getValue().toString,
                  AgentServices.getInstance().getCurrentUser match {
                    case Some(u) => u.getName()
                    case None =>
                      println("Oh, lawdy! We done broke it.")
                      throw new Exception("Must be logged in to request connections.")
                  }
                )
              }
            }
            getWindow().getParent().showNotification("Send this!", "New cnxn: " +
              cnxn.map(c => c.getItemProperty("agentId").getValue().toString) +
              " : " + tags.tree.getValue().asInstanceOf[java.util.Set[String]].toList.mkString("; "),
              Notification.TYPE_TRAY_NOTIFICATION)
            getWindow().getParent().removeWindow(getWindow())
            
          case None =>
            getWindow().getParent().showNotification("No relationship", "A relationship must be selected.",
            Notification.TYPE_TRAY_NOTIFICATION)
        }
      case _ => 
        getWindow().getParent().removeWindow(getWindow())
    }
  }
}

object AgentCnxnsTable {
  val ActionViewContent = new Action("View Content")
  val ActionEdit = new Action("Suggest New Relationship")
  val ActionDelete = new Action("Delete Connection")
  val Actions: Array[Action] = Array( ActionViewContent, ActionEdit, ActionDelete )
}

class AgentCnxnsTable extends VerticalLayout {

  val table = new Table()

  addComponent(table)

  table.addStyleName("borderless")
  table.addStyleName("striped")

  table.setWidth("100%")

  table.setSelectable(true)
  table.setMultiSelect(true)
  table.setImmediate(true)
  table.setColumnReorderingAllowed(true)
  table.setColumnCollapsingAllowed(true)
  
  val optCon = AgentCnxnsContainer.load
  
  optCon.map(c => {
    table.setContainerDataSource(c)
    table.addGeneratedColumn("tagLabels", new TagColumnGenerator())
    table.setVisibleColumns(List("agentId", "agentName", "tagLabels").toArray)
    table.setColumnHeaders(List("ID", "Agent", "Relationships").toArray)
    table.setColumnExpandRatio("agentName", 1)
    table.setColumnCollapsed("agentId", true)
  })

  table.addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      AgentCnxnsTable.Actions
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case view if view == AgentCnxnsTable.ActionViewContent =>
          getWindow().addWindow(new ContentWindow(
            table.getItem(target).getItemProperty("agentName").toString + "'s Content",
            table.getItem(target).getItemProperty("agentId").toString,
            optCon match {
              case Some(c) =>
                c.getItem(target).getBean().getTags()
              case None => Nil
            }
          ))
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
