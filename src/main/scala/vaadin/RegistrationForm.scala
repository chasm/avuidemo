package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import javax.servlet._

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data.Item
import com.vaadin.data.util.BeanItem
import com.vaadin.ui._
import com.vaadin.ui.Button.ClickListener
import com.vaadin.terminal.ExternalResource

import java.util.{Date, UUID}
import java.net.URL

/**
 * The registration form for the agent
 */
class RegistrationForm(user: ContentUser) extends Form with ClickListener {
  protected var isNew: Boolean = false
	protected val submit: Button = new Button("Register", this.asInstanceOf[ClickListener])
  
	setItemDataSource(new BeanItem[ContentUser](user))
  setSizeFull()
  getLayout().setMargin(true)
	setWriteThrough(false)

	val footer = new HorizontalLayout()
	footer.setSpacing(true)
	footer.addComponent(submit)
	footer.setVisible(true)
	footer.setMargin(true, true, true, true)

	setFooter(footer)
	
  setVisibleItemProperties(List("nameFirst", "nameLast", "emailAddress", "password"))

	setFormFieldFactory(new DefaultFieldFactory() {
		override def createField(item: Item, propertyId: Any, uiContext: Component): Field = {
		  val field = propertyId match {
        case "nameFirst" => super.createField(item, propertyId, uiContext)
        case "nameLast" => super.createField(item, propertyId, uiContext)
        case "emailAddress" => super.createField(item, propertyId, uiContext)
        case "password" => super.createField(item, propertyId, uiContext)
    	  case _ => null
		  }
		  field match {
		    case x: TextField =>
		      x.setNullRepresentation("")
		      x
		    case y => y
		  }
		}
	})
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case s if s == submit => if (isValid()) {
        commit()
        val beanItem = this.getItemDataSource().asInstanceOf[BeanItem[ContentUser]]
        val bean = beanItem.getBean().asInstanceOf[ContentUser]
        val id: String = bean.id
        val pw: String = beanItem.getItemProperty("password").getValue().asInstanceOf[String]
        println(beanItem.getItemProperty("password"))
        println("pw: " + pw)
        bean.setExpires(0L)
        bean.setPassword(pw)
        ContentUserDAO.put(bean)
        val user = ContentUserDAO.get(id).getOrElse(new ContentUser())
          println("Found the user: " + user.toString)
        AgentServices.getInstance().logIn(id, pw)
        getWindow().open(new ExternalResource(AgentServices.getInstance().getURL()))
      }
      case _ => println("Huh?")
    }
  }

  override def setItemDataSource(newDataSource: Item) = {
    isNew = false
    
		if (newDataSource != null) {
			super.setItemDataSource(newDataSource)
		}
	}
}
