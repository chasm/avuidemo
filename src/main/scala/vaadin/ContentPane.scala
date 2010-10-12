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

import org.vaadin.tinymceeditor._

import scala.collection.JavaConversions._

import java.util.UUID

class ContentPane extends VerticalLayout with Property.ValueChangeListener {
  setSizeFull()
  setMargin(false)
  setSpacing(false)
  setSizeFull()
  
  val userId = AgentServices.getInstance().getCurrentUser.getOrElse(new ContentUser()).getId()
  val items = ContentItemDAO.getAllWithChildrenAndTagsByUserId(userId)
  val tree = new ContentTree(items)
  
  val treePanel = new Panel("Manage Your Content")
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
        val item = ContentItemDAO.get(ciId).getOrElse(new ContentItem())
        getWindow().showNotification("Item Click", "Found: " + item.toString, Notification.TYPE_TRAY_NOTIFICATION)
      }
    } else {
      getWindow().showNotification("Item Click", "But nothing found!", Notification.TYPE_TRAY_NOTIFICATION)
    }
  }
}

class ViewContentWindow(tree: ContentTree, itemId: AnyRef) extends Window {
  val item: Item = tree.getItem(itemId)
  setCaption(item.getItemProperty("name").getValue().asInstanceOf[String])
  
  val value: Label = new Label(
    item.getItemProperty("valueLabel").getValue().asInstanceOf[Label].getValue().toString,
    Label.CONTENT_XHTML
  )
  
  val tags = try {
    new Label(item.getItemProperty("tags").getValue().asInstanceOf[List[ContentTag]].map(t => {
      "<div class=\"tag_" + t.getAbbr() + "\"><span>" + t.getAbbr() + "</span> " + t.getName() + "</div>"
    }).mkString(" "), Label.CONTENT_XHTML)
  } catch {
    case e => e.printStackTrace
    new Label("")
  }
  
  setSizeUndefined()
  
  val layout = getContent().asInstanceOf[VerticalLayout]
  layout.setSpacing(true)
  layout.setMargin(true)
  layout.addComponent(tags)
  layout.addComponent(value)
  layout.setExpandRatio(value, 1)
  layout.setWidth("640px")
  
  setModal(true)
}

class NewContentWindow(tree: ContentTree, parentItemId: AnyRef)
    extends Window with ClickListener {
  
  setWidth("702px")
  setHeight("460px")
  center()
  
  val parent = parentItemId match {
    case null => None
    case id => tree.getItem(id) match {
      case null => None
      case item => Some(item)
    }
  }
  
  val nameLbl = new Label("Item")
  nameLbl.setWidth("40px")
  
  val name = new TextField()
  name.setMaxLength(32)
  parent match {
    case Some(p) => setCaption("Add item below " + p.getItemProperty("name").getValue().asInstanceOf[String])
    case None => setCaption("Add item")
  }
  
  val tagsLbl = new Label("Relationships")
  tagsLbl.setWidth("80px")
  
  val tagContainer = ContentTagContainer.load.getOrElse(new ContentTagContainer(List()))
  
  var tags = new ListSelect()
  tags.setWidth("180px")
  tags.setRows(3)
  tags.setMultiSelect(true)
  tags.setNullSelectionAllowed(true)
  ContentTagDAO.getAll().map(ct => {
    tags.addItem(ct.getName())
  })
  parent match {
    case Some(p) => p.getItemProperty("tags").getValue().asInstanceOf[List[ContentTag]].map(t => {
      tags.select(t.getName())
    })
    case None => tags.select("Public/Anyone")
  }
  
  val save = new Button("Save", this.asInstanceOf[ClickListener])
  
  val header = new HorizontalLayout()
  header.setMargin(false)
  header.setSpacing(true)
  header.setWidth("100%")
  header.addComponent(nameLbl)
  header.addComponent(name)
  header.addComponent(tagsLbl)
  header.addComponent(tags)
  header.addComponent(save)
  header.setExpandRatio(tagsLbl, 1f)
  header.setComponentAlignment(save, Alignment.TOP_RIGHT)
  
  val rta = new TinyMCETextField()
  rta.setWidth("100%")
  rta.setHeight("320px")
  
  val layout = this.getContent().asInstanceOf[VerticalLayout]
  layout.setSizeFull()
  layout.addComponent(header)
  layout.addComponent(rta)
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case s if (s == save) => 
        val obj = new ContentItem()
        val objId = obj.getId()
        val itemId = tree.addItem()
        val item = tree.getItem(itemId)
        val newTags = tags.getValue().asInstanceOf[java.util.Set[String]].toList
        
        item.getItemProperty("id").setValue( obj.getId() )
        obj.setUserId( AgentServices.getInstance().getCurrentUserId().getOrElse("") )
        item.getItemProperty("userId").setValue( obj.getUserId() )
        obj.setName( name.getValue().asInstanceOf[String] )
        item.getItemProperty("name").setValue( obj.getName() )
        
        val curValue = rta.getValue().toString
        obj.setValue( curValue )
        item.getItemProperty("value").setValue( curValue )
        item.getItemProperty("valueLabel").setValue( new Label(curValue, Label.CONTENT_XHTML) )
        
        obj.setVtype( if (curValue == null || curValue.trim() == "") "Label" else "String" )
        item.getItemProperty("vtype").setValue( obj.getVtype() )
        obj.setUri( "" )
        item.getItemProperty("uri").setValue( obj.getUri() )
        
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
        
        ContentItemDAO.put(obj)
        tree.objMap += (itemId -> obj)
        ItemTagDAO.put(newTags.map(t => {
          new ItemTag(objId, t)
        }))
        
        val newCTs = ContentTagDAO.getByNames(newTags)
        obj.setTags( newCTs )
        item.getItemProperty("tags").setValue(obj.getTags().toList)
        item.getItemProperty("tagLabel").setValue(new Label(obj.getTagsAsHTML(), Label.CONTENT_XHTML))
        
        tree.recursivelyUpdatePositionsAndParents(tree.rootItemIds().toList.asInstanceOf[List[AnyRef]])

        getWindow().getParent().removeWindow(getWindow())
      case _ => println("Huh?")
    }
  }
}

class EditContentWindow(tree: ContentTree, item: Item, itemId: AnyRef, obj: ContentItem)
    extends Window with ClickListener {
  setWidth("702px")
  setHeight("460px")
  center()
  
  val nameLbl = new Label("Item")
  nameLbl.setWidth("40px")
  
  val name = new TextField(item.getItemProperty("name"))
  name.setMaxLength(32)
  setCaption("Edit " + name.getValue().asInstanceOf[String])
  
  val tagsLbl = new Label("Relationships")
  nameLbl.setWidth("80px")
  
  val tagContainer = ContentTagContainer.load.getOrElse(new ContentTagContainer(List()))
  
  var tags = new ListSelect()
  tags.setWidth("180px")
  tags.setRows(3)
  tags.setMultiSelect(true)
  tags.setNullSelectionAllowed(true)
  ContentTagDAO.getAll().map(ct => {
    tags.addItem(ct.getName())
  })
  item.getItemProperty("tags").getValue().asInstanceOf[List[ContentTag]].map(ct => {
    tags.select(ct.getName())
  })
  
  val update = new Button("Update", this.asInstanceOf[ClickListener])
  
  val header = new HorizontalLayout()
  header.setMargin(false)
  header.setSpacing(true)
  header.setWidth("100%")
  header.addComponent(nameLbl)
  header.addComponent(name)
  header.addComponent(tagsLbl)
  header.addComponent(tags)
  header.addComponent(update)
  header.setExpandRatio(tagsLbl, 1f)
  header.setComponentAlignment(update, Alignment.TOP_RIGHT)
  
  val rta = new TinyMCETextField()
  rta.setWidth("100%")
  rta.setHeight("320px")
  rta.setValue(item.getItemProperty("value").getValue().asInstanceOf[String])
  
  val layout = this.getContent().asInstanceOf[VerticalLayout]
  layout.setSizeFull()
  layout.addComponent(header)
  layout.addComponent(rta)
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case u if (u == update) => 
        val thisId = item.getItemProperty("id").getValue().asInstanceOf[String]
        val newTags = tags.getValue().asInstanceOf[java.util.Set[String]].toList
        val thisItem = ContentItemDAO.get(thisId).map(x => {
          x.setUserId( item.getItemProperty("userId").getValue().asInstanceOf[String] )
          x.setParentId( item.getItemProperty("parentId").getValue().asInstanceOf[String] )
          x.setName( item.getItemProperty("name").getValue().asInstanceOf[String] )
          val curValue = rta.getValue().toString
          x.setValue( curValue )
          item.getItemProperty("value").setValue(curValue)
          item.getItemProperty("valueLabel").setValue(new Label(curValue, Label.CONTENT_XHTML))
          x.setVtype( if (curValue == null || curValue.trim() == "") "Label" else "String" )
          x.setUri( item.getItemProperty("uri").getValue().asInstanceOf[String] )
          x.setPosition( item.getItemProperty("position").getValue().asInstanceOf[String].toInt )
          
          ContentItemDAO.put(x)
          val id = x.getId()
          ItemTagDAO.delete(ItemTagDAO.getAllByItemId(id))
          ItemTagDAO.put(newTags.map(t => {
            new ItemTag(id, t)
          }))
          
          val newCTs = ContentTagDAO.getByNames(newTags)
          x.setTags( newCTs )
          item.getItemProperty("tags").setValue(x.getTags().toList)
          item.getItemProperty("tagLabel").setValue(new Label(x.getTagsAsHTML(), Label.CONTENT_XHTML))
        })
        getWindow().getParent().removeWindow(getWindow())
      case _ => println("Huh?")
    }
  }
}
