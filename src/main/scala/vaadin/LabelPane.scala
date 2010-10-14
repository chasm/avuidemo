package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin.data.{Item, Property}
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.event.Action
import com.vaadin.ui._
import com.vaadin.ui.Window.Notification
import com.vaadin.data.util.HierarchicalContainer
import com.vaadin.terminal.Sizeable._
import com.vaadin.terminal.ThemeResource
import com.vaadin.ui.themes._
import com.vaadin.ui.Button.ClickListener

import com.vaadin.addon.colorpicker._
import com.vaadin.addon.colorpicker.events.ColorChangeEvent

import scala.collection.JavaConversions._

import java.util.UUID
import java.awt.Color

class LabelPane extends VerticalLayout with Property.ValueChangeListener {
  setSizeFull()
  setMargin(false)
  setSpacing(false)
  setSizeFull()
  
  val userId = AgentServices.getInstance().getCurrentUser.getOrElse(new ContentUser()).getId()
  val items = ContentTagDAO.getAllWithChildrenByUserId(userId)
  val tree = new LabelTreeTable(items)
  
  val treePanel = new Panel("Manage Your Labels")
  treePanel.setSizeFull()
  treePanel.getContent().setSizeFull()
  treePanel.getContent().asInstanceOf[VerticalLayout].setMargin(false)
  treePanel.addComponent(tree)
  treePanel.setScrollable(true)
  treePanel.addStyleName("borderless")
  
  addComponent(treePanel)

  def valueChange(event: ValueChangeEvent) {
    val itemId = event.getProperty().getValue()
    if (itemId != null) {
      val selected = tree.getItem(itemId)
      if (selected != null) {
        val ciId: String = selected.getItemProperty("id").getValue().asInstanceOf[String]
        val item = ContentTagDAO.get(ciId).getOrElse(new ContentTag())
        getWindow().showNotification("Item Click", "Found: " + item.toString, Notification.TYPE_TRAY_NOTIFICATION)
      }
    } else {
      getWindow().showNotification("Item Click", "But nothing found!", Notification.TYPE_TRAY_NOTIFICATION)
    }
  }
}

class NewLabelWindow(tree: LabelTreeTable, parentItemId: AnyRef)
    extends Window with ClickListener {
  
  setWidth("240px")
  setHeight("200px")
  center()
  
  val parent = parentItemId match {
    case null => None
    case id => tree.getItem(id) match {
      case null => None
      case item => Some(item)
    }
  }
  
  parent match {
    case Some(p) => setCaption("Add tag under " + p.getItemProperty("name").getValue().asInstanceOf[String])
    case None => setCaption("Add tag")
  }
  
  val name = new TextField("Name")
  name.setMaxLength(32)
  
  val abbr = new TextField("Abbr")
  abbr.setMaxLength(2)
  
  // val fcp: ColorPicker = new ColorPicker("Foreground", Color.WHITE)
  // var foreground: Color = null

  // Add a color change listener to the color picker
  // fcp.addListener(new ColorPicker.ColorChangeListener() {
  //   override def colorChanged(event: ColorChangeEvent) {
  //     foreground = event.getColor()
  //   }
  // })
  
  // val bcp: ColorPicker = new ColorPicker("Background", Color.BLACK)
  // var background: Color = null

  // Add a color change listener to the color picker
  // bcp.addListener(new ColorPicker.ColorChangeListener() {
  //   override def colorChanged(event: ColorChangeEvent) {
  //     background = event.getColor()
  //   }
  // })

  val save = new Button("Save", this.asInstanceOf[ClickListener])
  
  val hl = new HorizontalLayout()
  hl.setSpacing(true)
  hl.setMargin(false)
  // hl.addComponent(fcp)
  // hl.addComponent(bcp)
  hl.addComponent(save)
  
  val layout = this.getContent().asInstanceOf[VerticalLayout]
  layout.setSizeFull()
  layout.setMargin(true)
  layout.setSpacing(true)
  layout.addComponent(name)
  layout.addComponent(abbr)
  layout.addComponent(hl)
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case s if (s == save) => 
        val obj = new ContentTag()
        val objId = obj.getId()
        val itemId = tree.addItem()
        val item = tree.getItem(itemId)
        
        item.getItemProperty("id").setValue( obj.getId() )
        obj.setUserId( AgentServices.getInstance().getCurrentUserId().getOrElse("") )
        item.getItemProperty("userId").setValue( obj.getUserId() )
        obj.setName( name.getValue().asInstanceOf[String] )
        item.getItemProperty("name").setValue( obj.getName() )
        obj.setAbbr( abbr.getValue().asInstanceOf[String] )
        item.getItemProperty("abbr").setValue( obj.getAbbr() )
        
        parent match {
          case Some(p) => item.getItemProperty("parentId").setValue(
              p.getItemProperty("id").getValue().asInstanceOf[String]
            )
            obj.setParentId(item.getItemProperty("parentId").getValue().toString)
            val pos = try {
              tree.getChildren(parentItemId).toList.length
            } catch {
              case _ => 0
            }
            obj.setPosition(pos)
            item.getItemProperty("position").setValue(pos)
            tree.setChildrenAllowed(parentItemId, true)
            tree.setParent(itemId, parentItemId)
          case None => item.getItemProperty("parentId").setValue(null)
            val pos = tree.rootItemIds().toList.length
            obj.setPosition(pos)
            item.getItemProperty("position").setValue(pos)
        }
        
        tree.setChildrenAllowed(itemId, false)
        
        ContentTagDAO.put(obj)
        tree.objMap += (itemId -> obj)
        
        tree.recursivelyUpdatePositionsAndParents(tree.rootItemIds().toList.asInstanceOf[List[AnyRef]])

        getWindow().getParent().removeWindow(getWindow())
      case _ => println("Huh?")
    }
  }
}

class EditLabelWindow(tree: LabelTreeTable, item: Item, itemId: AnyRef, obj: ContentTag)
    extends Window with ClickListener {
  setWidth("300px")
  setHeight("300px")
  center()
  
  val name = new TextField("Name",item.getItemProperty("name"))
  name.setMaxLength(32)
  setCaption("Edit " + name.getValue().asInstanceOf[String])
  
  val abbr = new TextField("Abbr",item.getItemProperty("abbr"))
  abbr.setMaxLength(2)
  
  val update = new Button("Update", this.asInstanceOf[ClickListener])
  
  val layout = this.getContent().asInstanceOf[VerticalLayout]
  layout.setSizeFull()
  layout.setMargin(true)
  layout.setSpacing(true)
  layout.addComponent(name)
  layout.addComponent(abbr)
  layout.addComponent(update)
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case u if (u == update) => 
        val thisId = item.getItemProperty("id").getValue().asInstanceOf[String]
        val thisItem = ContentTagDAO.get(thisId).map(x => {
          x.setUserId( item.getItemProperty("userId").getValue().asInstanceOf[String] )
          x.setParentId( item.getItemProperty("parentId").getValue().asInstanceOf[String] )
          x.setName( item.getItemProperty("name").getValue().asInstanceOf[String] )
          x.setAbbr( item.getItemProperty("abbr").getValue().asInstanceOf[String] )
          x.setPosition( item.getItemProperty("position").getValue().asInstanceOf[String].toInt )
          
          ContentTagDAO.put(x)
      })
      getWindow().getParent().removeWindow(getWindow())
      case _ => println("Huh?")
    }
  }
}
