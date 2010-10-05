package com.nonebetwixt.agent.ui

import javax.servlet._

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.ui._
import com.vaadin.ui.MenuBar._
import com.vaadin.terminal.{ThemeResource, UserError, ExternalResource, Sizeable, Resource}

import com.nonebetwixt.agent.utilities._
import com.nonebetwixt.agent.utilities.Dimension._

import java.util.{Date, UUID}
import java.net.URL

import reflect.{BeanProperty,BeanDisplayName}

class IntroductionForm extends VerticalLayout {
  
  val form = new Form()
  form.setDescription("Use the form below to introduce a friend to other friends.")
  val personToIntroduce = new ComboBox("Introduce")
  personToIntroduce.addItem("")
  personToIntroduce.addItem("Tom")
  personToIntroduce.addItem("Dick")
  personToIntroduce.addItem("Harry")
  personToIntroduce.setWidth("12em")
  
  val personsIntroduced = new ListSelect("To")
  personsIntroduced.addItem("Tom")
  personsIntroduced.addItem("Dick")
  personsIntroduced.addItem("Harry")
  personsIntroduced.addItem("Bob")
  personsIntroduced.addItem("Carol")
  personsIntroduced.addItem("Ted")
  personsIntroduced.addItem("Alice")
  personsIntroduced.setRows(3)
  personsIntroduced.setNullSelectionAllowed(false)
  personsIntroduced.setMultiSelect(true)
  personsIntroduced.setImmediate(true)
  personsIntroduced.setWidth("12em")

  form.addField("personToIntroduce", personToIntroduce)
  form.addField("personsIntroduced", personsIntroduced)
  
  form.setFooter(new VerticalLayout())
  
  val okbar = new HorizontalLayout()
  okbar.setSizeUndefined()
  okbar.setSpacing(true)
  form.getFooter().addComponent(okbar)
  
  okbar.addComponent(new Button("OK", form, "commit"))
  okbar.addComponent(new Button("Discard", form, "discard"))
  
  addComponent(form)
  
}