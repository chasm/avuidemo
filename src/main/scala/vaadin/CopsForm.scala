package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import java.util.{UUID, Date}

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data._
import com.vaadin.ui._
import com.vaadin.ui.themes._
import com.vaadin.ui.{Table => VTable, Field => VField, _}
import com.vaadin.data.Property.{ValueChangeListener, ValueChangeEvent}
import com.vaadin.data.util.{BeanItemContainer, BeanItem}
import com.vaadin.ui.Button.ClickListener
import com.vaadin.event.Action
import com.vaadin.terminal.Sizeable._

class CopsWindow(container: CopsContainer) extends Window {
  setCaption("Create a new Community of Practice")
  val form = new CopsForm(container)
  
  val layout = getContent().asInstanceOf[VerticalLayout]
  layout.setSpacing(true)
  layout.setMargin(true)
  layout.addComponent(form)
  layout.setWidth("420px")
  layout.setHeight("240px")
  
  setModal(true)
}

class CopsForm(container: CopsContainer) extends Form with ClickListener {
  protected var isNew: Boolean = false
	protected val save: Button = new Button("Save", this.asInstanceOf[ClickListener])
	protected val cancel: Button = new Button("Cancel", this.asInstanceOf[ClickListener])
  protected def newItem: Cop = new Cop(
    AgentServices.getInstance().getCurrentUserId().getOrElse(""), "", ""
  )
	
  setSizeFull()
  getLayout().setMargin(true)
	
	val copname = new TextField("CoP Name")
	copname.setColumns(18)
	val desc = new TextField("Description")
	desc.setColumns(22)
	desc.setRows(3)
	desc.addStyleName("vtextarea")
	val username = new TextField("Username")
	username.setColumns(18)
	
	addField("name", copname)
	addField("username", username)
	addField("desc", desc)

	val footer = new HorizontalLayout()
	footer.setSpacing(true)
	footer.addComponent(save)
	footer.addComponent(cancel)
	footer.setVisible(true)
	footer.setMargin(true)

	setFooter(footer)
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case s if s == save => if (isValid()) {
        println("cop id: " + newItem.getId())
        newItem.setName(copname.getValue().asInstanceOf[String])
        newItem.setDesc(desc.getValue().asInstanceOf[String])
        println("cop id: " + newItem.getId())
        val copId = newItem.getId()
        println("copId: " + copId)
        CopDAO.put(newItem)
        container.addBean(newItem)
        println("cop id: " + newItem.getId())
        val member = new Member(copId, newItem.getUserId(), username.getValue().asInstanceOf[String])
        println("member id: " + member.getId())
        println("member copId: " + member.getCopId())
        println("member userId: " + member.getUserId())
        println("member name: " + member.getName())
        println("copId: " + newItem.getId() + " =? " + member.getCopId())
        // MemberDAO.put(member)
        getWindow().getParent().removeWindow(getWindow())
      }
      case c if c == cancel => {
        getWindow().getParent().removeWindow(getWindow())
      }
      case _ => println("Huh?")
    }
  }
}