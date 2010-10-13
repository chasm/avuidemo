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
 * The log in form for the agent
 */
class AgentLoginForm(code: String) extends FormLayout {
  private val btnLogin = new Button("Log in")
  
  private val userId = new TextField("Agent")
  userId.setValue(code)
  userId.setReadOnly(true)
  userId.setWidth("260px")
  
  private val password = new TextField("Password")
  password.setWidth("260px")
  password.setSecret(true)
  
  setSpacing(true)

  addComponent(userId)
  addComponent(password)
  addComponent(btnLogin)
  
  btnLogin.addListener(new Button.ClickListener() {
    def buttonClick(event: Button#ClickEvent) {
      try {
        AgentServices.getInstance().logIn(code, password.getValue().toString)
        getWindow().open(new ExternalResource(AgentServices.getInstance().getURL()))
      } catch {
        case e => getWindow().showNotification(e.toString())
          e.printStackTrace()
      }
    }
  })
}