package com.nonebetwixt.agent.utilities

import com.vaadin.Application 
import com.vaadin.ui._
import java.util.Date

class SButtonClickListener(action: Button#ClickEvent => Unit) extends Button.ClickListener {
  def buttonClick(event: Button#ClickEvent): Unit = {
    action(event)
  }
}

class SButton(text: String, action: Button#ClickEvent => Unit) extends Button(text, new SButtonClickListener(action))

class SWindowCloseListener(action: Window#CloseEvent => Unit) extends Window.CloseListener {
  def windowClose(event: Window#CloseEvent) = {
    action(event)
  }
}

class Dimension(private val value: Number) {
  def pixels : String = value + "px"
  def percent : String = value + "%"
}

object Dimension {
  implicit def intToDimension(value: Int): Dimension = new Dimension(value)    
  implicit def doubleToDimension(value: Double): Dimension = new Dimension(value)
}
