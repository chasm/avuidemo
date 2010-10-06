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

import scala.collection.JavaConversions._

import java.util.UUID

class ContentPane extends VerticalLayout with Property.ValueChangeListener {
  setSizeFull()
  setMargin(false)
  setSpacing(false)
  setSizeFull()
  
  val container = ContentTreeContainer.load()
  val treeLayout = new SortableContentTree(container, this)
  val tree = treeLayout.getTree()
  
  val treePanel = new Panel("Manage Your Content")
  treePanel.setSizeFull()
  treePanel.setContent(treeLayout)
  treePanel.setScrollable(true)
  treePanel.addStyleName("borderless")
  
  var ciDisplay = new ContentItemDisplay(container, tree)
  ciDisplay.addStyleName("borderless")
  
  val hsPanel = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL)
  hsPanel.setSplitPosition(320, UNITS_PIXELS)
  hsPanel.addStyleName(Runo.SPLITPANEL_SMALL)
  hsPanel.setFirstComponent(treePanel)
  hsPanel.setSecondComponent(ciDisplay)
  hsPanel.setSizeFull()
  hsPanel.setMargin(false)

  def valueChange(event: ValueChangeEvent) {
    val itemId = event.getProperty().getValue()
    if (itemId != null) {
      val selected = container.getItem(itemId)
      if (selected != null) {
        val ciId: String = selected.getItemProperty("id").getValue().asInstanceOf[String]
        val item = ContentItemDAO.get(ciId).getOrElse(new ContentItem())
        ciDisplay.loadViewer(selected, itemId)
      }
    } else {
      getWindow().showNotification("Item Click", "But nothing found!", Notification.TYPE_TRAY_NOTIFICATION)
    }
  }
  
  tree.addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      SortableContentTree.Actions
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      val item = tree.getItem(target)
      action match {
        case x if (x == SortableContentTree.ActionAdd) => 
          ciDisplay.loadNew(target)
          getWindow().showNotification("Add!", "Adding a child item to " +
            tree.getItem(target).getItemProperty("name").getValue().toString,
            Notification.TYPE_TRAY_NOTIFICATION)
        case y if (y == SortableContentTree.ActionDelete) => 
          getWindow().showNotification("Delete!", "Deleting item " +
            tree.getItem(target).getItemProperty("name").getValue().toString,
            Notification.TYPE_TRAY_NOTIFICATION)
        case _ => 
          getWindow().showNotification("Whoops!", "Unknown action",
            Notification.TYPE_TRAY_NOTIFICATION)
      }
    }
  })
  
  addComponent(hsPanel)
  
  // Find the first child and select and expand the node
  val rootItems = tree.rootItemIds().toList
  if (rootItems.nonEmpty) {
    val children = tree.getChildren(rootItems.head).toList
    if (children.nonEmpty) {
      tree.select(children.head)
      tree.expandItem(rootItems.head)
      tree.expandItem(children.head)
    }
  }
}

class ContentItemDisplay(ctr: ContentTreeContainer, tree: Tree) extends VerticalLayout with ClickListener {
  setMargin(false)
  setSizeFull()
  val container = ctr
  val save = new Button("Save", this.asInstanceOf[ClickListener])
  val update = new Button("Update", this.asInstanceOf[ClickListener])
  val edit = new Button("Edit", this.asInstanceOf[ClickListener])
  var item: Item = null
  var id: AnyRef = null
  
  var tags: ListSelect = null
  
  def loadEditor(ci: Item, itemId: AnyRef) {
    removeAllComponents()
    item = ci
    id = itemId
    
    val nameLbl = new Label("Label")
    
    val name = new TextField(ci.getItemProperty("name"))
    name.setMaxLength(32)
    
    val tagsLbl = new Label("Tags")
    
    val tagContainer = ContentTagContainer.load.getOrElse(new ContentTagContainer(List()))
    
    tags = new ListSelect()
    tags.setWidth("180px")
    tags.setRows(3)
    tags.setMultiSelect(true)
    tags.setNullSelectionAllowed(true)
    ContentTagDAO.getAll().map(ct => {
      tags.addItem(ct.getName())
    })
    item.getItemProperty("tags").getValue().asInstanceOf[java.util.TreeSet[ContentTag]].toList.map(ct => {
      tags.select(ct.getName())
    })
    
    val header = new HorizontalLayout()
    header.setMargin(false)
    header.setSpacing(true)
    header.setWidth("100%")
    header.addComponent(nameLbl)
    header.addComponent(name)
    header.addComponent(tagsLbl)
    header.addComponent(tags)
    header.addComponent(update)
    header.setComponentAlignment(update, Alignment.TOP_RIGHT)
    
    val rta = new RichTextArea("Content", ci.getItemProperty("value"))
    rta.setWidth("100%")
    rta.setHeight("320px")
    
    val panel = new Panel(ci.getItemProperty("name").getValue().asInstanceOf[String])
    panel.setSizeFull()
    panel.addStyleName("borderless")
    panel.addComponent(header)
    panel.addComponent(rta)
    panel.getContent().asInstanceOf[VerticalLayout].setSizeFull()
    
    addComponent(panel)
  }
  
  def loadNew(parentItemId: AnyRef) {
    removeAllComponents()
    val parent = container.getItem(parentItemId)
    val parentId = parent.getItemProperty("id").getValue().toString
    
    id = container.addItem()
    item = container.getItem(id)
    container.setParent(id, parentItemId)
    container.setChildrenAllowed(id,false)
    
    item.getItemProperty("id").setValue("")
    item.getItemProperty("userId").setValue(AgentServices.getInstance().getCurrentUserId().getOrElse(""))
    item.getItemProperty("parentId").setValue(parentId)
    item.getItemProperty("name").setValue("New Content Item")
    item.getItemProperty("value").setValue("")
    item.getItemProperty("vtype").setValue("String")
    item.getItemProperty("uri").setValue("")
    item.getItemProperty("position").setValue(ContentItemDAO.getAllByParentId(parentId).toList.size)
    item.getItemProperty("tags").setValue(new java.util.TreeSet[ContentItem]())
    
    val nameLbl = new Label("Label")
    
    val name = new TextField(item.getItemProperty("name"))
    name.setMaxLength(32)
    
    val tagsLbl = new Label("Tags")
    
    val tagContainer = ContentTagContainer.load.getOrElse(new ContentTagContainer(List()))
    
    tags = new ListSelect()
    tags.setWidth("180px")
    tags.setRows(3)
    tags.setMultiSelect(true)
    tags.setNullSelectionAllowed(true)
    ContentTagDAO.getAll().map(ct => {
      tags.addItem(ct.getName())
    })
    
    val header = new HorizontalLayout()
    header.setMargin(false)
    header.setSpacing(true)
    header.setWidth("100%")
    header.addComponent(nameLbl)
    header.addComponent(name)
    header.addComponent(tagsLbl)
    header.addComponent(tags)
    header.addComponent(save)
    header.setComponentAlignment(save, Alignment.TOP_RIGHT)
    
    val rta = new RichTextArea("Content", item.getItemProperty("value"))
    rta.setWidth("100%")
    rta.setHeight("320px")
    
    val panel = new Panel(item.getItemProperty("name").getValue().asInstanceOf[String])
    panel.setSizeFull()
    panel.addStyleName("borderless")
    panel.addComponent(header)
    panel.addComponent(rta)
    panel.getContent().asInstanceOf[VerticalLayout].setSizeFull()
    
    addComponent(panel)
  }
  
  def loadViewer(ciId: String, itemId: AnyRef) {
    val ci: Item = container.getItem(ciId)
    loadViewer(ci, itemId)
  }
  
  def loadViewer(ci: Item, itemId: AnyRef) {
    removeAllComponents()
    item = ci
    id = itemId
    
    val tags = try {
      new Label(item.getItemProperty("tags").getValue().asInstanceOf[java.util.Set[ContentTag]].toList.map(t => {
        "<div class=\"tag_" + t.getAbbr() + "\"><span>" + t.getAbbr() + "</span> " + t.getName() + "</div>"
      }).mkString(" "), Label.CONTENT_XHTML)
    } catch {
      case e => e.printStackTrace
      new Label("")
    }
    
    val header = new HorizontalLayout()
    header.setSpacing(true)
    header.setMargin(false)
    header.setWidth("100%")
    header.setHeight("40px")
    header.addComponent(tags)
    header.addComponent(edit)
    header.setComponentAlignment(edit, Alignment.TOP_RIGHT)
    header.setExpandRatio(tags, 1.0f)
    
    val value = new Label(item.getItemProperty("value"), Label.CONTENT_XHTML)
    
    val panel = new Panel(item.getItemProperty("name").getValue().asInstanceOf[String])
    panel.setWidth("100%")
    panel.setHeight("480px")
    panel.setScrollable(true)
    panel.addComponent(header)
    panel.addComponent(value)
    
    val layout = panel.getContent().asInstanceOf[VerticalLayout]
    layout.addStyleName("borderless")
    layout.setWidth("100%")
    layout.setExpandRatio(value, 1.0f)
    
    addComponent(panel)
  }
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case u if (u == update) => 
        val thisId = item.getItemProperty("id").getValue().asInstanceOf[String]
        val newTags = tags.getValue().asInstanceOf[java.util.Set[String]].toList
        val thisItem = ContentItemDAO.get(thisId).map(x => {
          x.setUserId( item.getItemProperty("userId").getValue().asInstanceOf[String] )
          x.setParentId( item.getItemProperty("parentId").getValue().asInstanceOf[String] )
          x.setName( item.getItemProperty("name").getValue().asInstanceOf[String] )
          val curValue = item.getItemProperty("value").getValue().asInstanceOf[String]
          x.setValue( curValue )
          x.setVtype( if (curValue == null || curValue.trim() == "") "Label" else "String" )
          x.setUri( item.getItemProperty("uri").getValue().asInstanceOf[String] )
          x.setPosition( item.getItemProperty("position").getValue().asInstanceOf[String].toInt )
          
          ContentItemDAO.put(x)
          val id = x.getId()
          ItemTagDAO.delete(ItemTagDAO.getByItemId(id))
          ItemTagDAO.put(newTags.map(t => {
            new ItemTag(id, t)
          }))
          
          val newCTs = ContentTagDAO.getByNames(newTags)
          x.setTags( newCTs )
          item.getItemProperty("tags").setValue(x.getTags())
        })
        loadViewer(item, id)
        
      case s if (s == save) => 
        val thisId = item.getItemProperty("id").getValue().asInstanceOf[String]
        val newTags = tags.getValue().asInstanceOf[java.util.Set[String]].toList
        
        val newContentItem = new ContentItem()
        newContentItem.setUserId( item.getItemProperty("userId").getValue().asInstanceOf[String] )
        newContentItem.setParentId( item.getItemProperty("parentId").getValue().asInstanceOf[String] )
        newContentItem.setName( item.getItemProperty("name").getValue().asInstanceOf[String] )
        val curValue = item.getItemProperty("value").getValue().asInstanceOf[String]
        newContentItem.setValue( curValue )
        newContentItem.setVtype( if (curValue == null || curValue.trim() == "") "Label" else "String" )
        newContentItem.setUri( item.getItemProperty("uri").getValue().asInstanceOf[String] )
        newContentItem.setPosition( item.getItemProperty("position").getValue().asInstanceOf[String].toInt )
        
        ContentItemDAO.put(newContentItem)
        val newId = newContentItem.getId()
        ItemTagDAO.put(newTags.map(t => {
          new ItemTag(newId, t)
        }))
        
        val newCTs = ContentTagDAO.getByNames(newTags)
        newContentItem.setTags( newCTs )
        item.getItemProperty("tags").setValue(newContentItem.getTags())
        tree.select(id)

      case e if (e == edit) => {
        loadEditor(item, id)
      }
      case _ => println("Huh?")
    }
  }
  
}

object ContentTreeContainer {
  def load(): ContentTreeContainer = {
    val userId = AgentServices.getInstance().getCurrentUser.getOrElse(new ContentUser()).getId()
    val items = ContentItemDAO.getAllWithChildrenAndTagsByUserId(userId)
    
    val ctc: ContentTreeContainer = new ContentTreeContainer()
    
    ctc.addContainerProperty("id", classOf[String], null)
    ctc.addContainerProperty("userId", classOf[String], null)
    ctc.addContainerProperty("parentId", classOf[String], null)
    ctc.addContainerProperty("name", classOf[String], null)
    ctc.addContainerProperty("value", classOf[String], null)
    ctc.addContainerProperty("vtype", classOf[String], null)
    ctc.addContainerProperty("uri", classOf[String], null)
    ctc.addContainerProperty("position", classOf[String], null)
    ctc.addContainerProperty("tags", classOf[java.util.Set[ContentTag]], null)
    
    def addItemsRecursively(container: ContentTreeContainer, items: List[ContentItem], parentId: AnyRef) {
      items.map(ci => {
        val itemId = container.addItem()
        val item = container.getItem(itemId)
        println("ci: " + ci.getName() + " - " + ci.getPosition().toString)
        item.getItemProperty("id").setValue(ci.getId())
        item.getItemProperty("userId").setValue(ci.getUserId())
        item.getItemProperty("parentId").setValue(ci.getParentId())
        item.getItemProperty("name").setValue(ci.getName())
        item.getItemProperty("value").setValue(ci.getValue())
        item.getItemProperty("vtype").setValue(ci.getVtype())
        item.getItemProperty("uri").setValue(ci.getUri())
        item.getItemProperty("position").setValue(ci.getPosition())
        item.getItemProperty("tags").setValue(ci.getTags())
        
        if (parentId != null) {
          container.setParent(itemId, parentId)
        }
        
        if (ci.hasChildren()) {
          container.setChildrenAllowed(itemId, true)
          addItemsRecursively(container, ci.getChildren(), itemId)
        } else {
          container.setChildrenAllowed(itemId, false)
        }
      })
    }  

    addItemsRecursively(ctc, items, null)
    
    ctc
  }
}

class ContentTreeContainer extends HierarchicalContainer


// class ContentTree extends Tree {
//   setImmediate(true)
//   setItemCaptionPropertyId("name")
//   setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY)
//   setSelectable(true)
//   addStyleName("borderless")
//   setSizeFull()
//   
//   addActionHandler(new Action.Handler() {
//     def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
//       ContentTree.Actions
//     }
// 
//     def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
//       getWindow().showNotification("Action!", "An action (" + action.toString + ") from " +
//         sender.toString + " to " + target.toString,
//         Notification.TYPE_TRAY_NOTIFICATION)
//     }
//   })
// 
//   // Expand whole tree
//   rootItemIds().toList.map(id => {
//     expandItemsRecursively(id)
//   })
// }