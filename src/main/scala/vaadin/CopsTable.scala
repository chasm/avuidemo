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
  val ActionOpen = new Action("Open")
  val ActionEditAlias = new Action("Edit Alias")
  val ActionEdit = new Action("Edit")
  val ActionDelete = new Action("Delete")
  val Actions: Array[Action] = Array( ActionOpen, ActionEditAlias, ActionEdit, ActionDelete )
}

class CopsTable(tabsheet: TabSheet) extends VerticalLayout with ClickListener {
  setSizeFull()
  setMargin(true)
  setSpacing(true)

  val table = new Table()

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
      CopsTable.Actions
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case open if open == CopsTable.ActionOpen =>
          val value = container.getItem(target)
          if (value != null) {
            val cop: Cop = value.getBean().asInstanceOf[Cop]
            val tab: ForumTabSheet = new ForumTabSheet(cop.getId())
            tabsheet.addStyleName("borderless")
            tabsheet.addTab(tab, cop.getName(), null).setClosable(true)
            tabsheet.setSelectedTab(tab)
          }
        case editAlias if editAlias == CopsTable.ActionEditAlias =>  
          getWindow().showNotification("Edit Alias", "Edit this dude's alias.", Notification.TYPE_TRAY_NOTIFICATION)
        case edit if edit == CopsTable.ActionEdit =>
          getWindow().showNotification("Edit", "Edit the cop.", Notification.TYPE_TRAY_NOTIFICATION)
        case delete if delete == CopsTable.ActionDelete =>
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
    
    val lbl = new Label("This action will delete this community of practice. Once deleted, communities are not recoverable.")
      
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
