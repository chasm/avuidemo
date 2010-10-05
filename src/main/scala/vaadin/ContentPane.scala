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
  
  val tree = new ContentTree()
  tree.setSizeFull()
  val container = ContentTreeContainer.load()
  tree.setContainerDataSource(container)
  tree.addListener(this)
  
  val treePanel = new Panel("Manage Your Content")
  treePanel.setSizeFull()
  treePanel.getContent().asInstanceOf[Layout].setSizeFull()
  treePanel.addComponent(tree)
  treePanel.setScrollable(true)
  treePanel.addStyleName("borderless")
  
  var ciDisplay = new ContentItemDisplay(container)
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

class ContentItemDisplay(ctr: ContentTreeContainer) extends VerticalLayout with ClickListener {
  setMargin(false)
  setSizeFull()
  val container = ctr
  val save = new Button("Save", this.asInstanceOf[ClickListener])
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
    tags.setWidth("100%")
    tags.setRows(3)
    tags.setMultiSelect(true)
    tags.setNullSelectionAllowed(true)
    tags.setContainerDataSource(tagContainer)
    tags.setPropertyDataSource(ci.getItemProperty("tags"))
    
    val header = new HorizontalLayout()
    header.setMargin(false)
    header.setSpacing(true)
    header.setSizeUndefined()
    header.addComponent(nameLbl)
    header.addComponent(name)
    header.addComponent(tagsLbl)
    header.addComponent(tags)
    header.addComponent(save)
    
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
    
    val value = new Label(item.getItemProperty("value"))
    value.setWidth("100%")
    value.setHeight("100%")
    
    val panel = new Panel(item.getItemProperty("name").getValue().asInstanceOf[String])
    panel.addComponent(header)
    panel.addComponent(value)
    panel.setSizeFull()
    
    val layout = panel.getContent().asInstanceOf[VerticalLayout]
    layout.addStyleName("borderless")
    layout.setSizeFull()
    layout.setExpandRatio(value, 1.0f)
    
    addComponent(panel)
  }
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case s if s == save => 
        val thisId = item.getItemProperty("id").getValue().asInstanceOf[String]
        val thisItem = ContentItemDAO.get(thisId).map(x => {
          x.setUserId( item.getItemProperty("userId").getValue().asInstanceOf[String] )
          x.setParentId( item.getItemProperty("parentId").getValue().asInstanceOf[String] )
          x.setName( item.getItemProperty("name").getValue().asInstanceOf[String] )
          x.setValue( item.getItemProperty("value").getValue().asInstanceOf[String] )
          x.setVtype( item.getItemProperty("vtype").getValue().asInstanceOf[String] )
          
          // ContentItemDAO.put(thisItem)
          val itemTags = ItemTagDAO.getByItemId(x.getId())
          println("Old Tags: ")
          itemTags.map(it => println(it.getId() + ": " + it.getItemId() + " (" + it.getTagName + ")"))
          val newTags = item.getItemProperty("tags").getValue().asInstanceOf[java.util.Set[ContentTag]].toList
          println("New Tags: ")
          newTags.map(it => println(it.getName() + ": " + it.getAbbr()))
          println("Wait for it...")
          tags.getValue().asInstanceOf[java.util.Set[ContentTag]].toList.map(t => println(t.getName()))
        })

      case e if e == edit => {
        getWindow().showNotification("Item Click", "Edit button: " + event.toString, Notification.TYPE_TRAY_NOTIFICATION)
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

    val rootItemId = ctc.addItem()
    val rootItem = ctc.getItem(rootItemId)
    rootItem.getItemProperty("id").setValue(UUID.randomUUID.toString)
    rootItem.getItemProperty("name").setValue("Content")
    rootItem.getItemProperty("vtype").setValue("ROOT")
    addItemsRecursively(ctc, items, rootItemId)
    
    ctc
  }
}

class ContentTreeContainer extends HierarchicalContainer

object ContentTree {
  // Actions for the context menu
  private val ActionAdd: Action = new Action("Add child item")
  private val ActionDelete: Action = new Action("Delete")
  private val Actions: Array[Action] = Array(ActionAdd, ActionDelete)
}

class ContentTree extends Tree {
  setImmediate(true)
  setItemCaptionPropertyId("name")
  setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY)
  setSelectable(true)
  addStyleName("borderless")
  setSizeFull()
  
  addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      ContentTree.Actions
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      getWindow().showNotification("Action!", "An action (" + action.toString + ") from " +
        sender.toString + " to " + target.toString,
        Notification.TYPE_TRAY_NOTIFICATION)
    }
  })

  // Expand whole tree
  rootItemIds().toList.map(id => {
    expandItemsRecursively(id)
  })
}