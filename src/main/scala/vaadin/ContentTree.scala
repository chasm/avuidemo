package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin.data.{Item, Property}
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.util.HierarchicalContainer
import com.vaadin.event.{DataBoundTransferable, Transferable, Action}
import com.vaadin.event.dd.{DragAndDropEvent, DropHandler}
import com.vaadin.event.dd.acceptcriteria.{AcceptAll, AcceptCriterion}
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation
import com.vaadin.ui._
import com.vaadin.ui.Window.Notification
import com.vaadin.ui.Button.ClickListener

import com.vaadin.addon.treetable._

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap

import java.util.UUID

object ContentTree {
  // Actions for the context menu
  val ActionAddChild: Action = new Action("Add Child")
  val ActionAddSibling: Action = new Action("Add Sibling")
  val ActionView: Action = new Action("View")
  val ActionEdit: Action = new Action("Edit")
  val ActionDelete: Action = new Action("Delete")
  val Actions: Array[Action] = Array(ActionAddChild, ActionAddSibling, ActionView, ActionEdit, ActionDelete)
}

class ContentTree(objs: List[ContentItem]) extends TreeTable {
  val tree = this
  var objMap: HashMap[AnyRef, ContentItem] = HashMap.empty
  
  addContainerProperty("id", classOf[String], "ID")
  addContainerProperty("userId", classOf[String], "Owner")
  addContainerProperty("parentId", classOf[String], "Parent")
  addContainerProperty("name", classOf[String], "Item")
  addContainerProperty("value", classOf[String], "Value")
  addContainerProperty("valueLabel", classOf[Label], "Value")
  addContainerProperty("vtype", classOf[String], "Type")
  addContainerProperty("uri", classOf[String], "URI")
  addContainerProperty("tags", classOf[List[ContentTag]], "Tags")
  addContainerProperty("tagLabel", classOf[Label], "Tags")
  addContainerProperty("position", classOf[String], "Position")
  
  addContentItems(objs, null)
  
  setSizeFull()
  setColumnExpandRatio("name", 0.3f)
  setColumnExpandRatio("valueLabel", 0.6f)
  setColumnExpandRatio("tagLabel", 0.1f)
  setImmediate(true)
  setItemCaptionPropertyId("name")
  setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY)
  setSelectable(true)
  addStyleName("striped")
  setVisibleColumns(List("name", "valueLabel", "tagLabel").toArray)
  setColumnHeaderMode(Table.COLUMN_HEADER_MODE_EXPLICIT)
  setColumnHeader("name", "Label")
  setColumnHeader("valueLabel", "Content")
  setColumnHeader("tagLabel", "Tags")
  setColumnAlignment("tagLabel", Table.ALIGN_RIGHT)
  
  // Expand all nodes
  recursivelySetCollapsed(rootItemIds(), false)
  setDragMode(Table.TableDragMode.ROW)
  setDropHandler(new TreeTableSortDropHandler(this))
  
  addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      ContentTree.Actions
    }
  
    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      val item = getItem(target)
      action match {
        case addChild if (addChild == ContentTree.ActionAddChild) => 
          getWindow().addWindow(new NewContentWindow(tree, target))
        case addSibling if (addSibling == ContentTree.ActionAddSibling) => 
          getWindow().addWindow(new NewContentWindow(tree, tree.getParent(target)))
        case viewItem if (viewItem == ContentTree.ActionView) => 
          getWindow().addWindow(new ViewContentWindow(tree, target))
        case editItem if (editItem == ContentTree.ActionEdit) => 
          getWindow().addWindow(new EditContentWindow(tree, item, target, objMap(target)))
        case delItem if (delItem == ContentTree.ActionDelete) => 
          getWindow().addWindow(new ConfirmDeletionWindow(tree, target))
        case _ => 
          getWindow().showNotification("Whoops!", "Unknown action",
            Notification.TYPE_TRAY_NOTIFICATION)
      }
    }
  })
  
  private class ConfirmDeletionWindow(tree: ContentTree, itemId: AnyRef) extends Window with ClickListener {
    setWidth("360px")
    setHeight("144px")
    setCaption("Are you sure?")
    center()
    
    val lbl = new Label("This action will delete this content item and all child "+
      "content items. Once deleted, content is not recoverable.")
      
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
          tree.deleteBranch(itemId)
          close()
        case _ => getWindow().getParent().removeWindow(getWindow())
      }
    }
  }
    
  def recursivelyUpdatePositionsAndParents(itemIds: List[AnyRef]) {
    itemIds.zipWithIndex.map(x => {
      val itemId = x._1
      val newPos = x._2
      
      val item = tree.getItem(itemId)
      val objId = item.getItemProperty("id").getValue().toString
      
      val oldParentId = item.getItemProperty("parentId").getValue() match {
        case null => null
        case parentId => parentId.toString
      }
      
      val oldPos = try {
        item.getItemProperty("position").getValue().asInstanceOf[String].toInt
      } catch {
        case _ => -1
      }
      
      val newParentId = tree.getParent(itemId) match {
        case parent: Integer => tree.getItem(parent).getItemProperty("id").getValue().toString
        case _ => null
      }
      
      if (oldPos != newPos || oldParentId != newParentId) {
        
        ContentItemDAO.get(objId).map(obj => {
          if (oldPos != newPos) {
            obj.setPosition(newPos)
            item.getItemProperty("position").setValue(newPos)
          }
          
          if (oldParentId != newParentId) {
            obj.setParentId(newParentId)
            item.getItemProperty("parentId").setValue(newParentId)
          }
          
          ContentItemDAO.put(obj)
        })
      }
      
      if (tree.hasChildren(itemId)) {
        recursivelyUpdatePositionsAndParents(tree.getChildren(itemId).toList.asInstanceOf[List[AnyRef]])
      }
    })
  }
  
  def deleteBranch(itemId: AnyRef) {
    // Must recurse first, sadly
    if (tree.hasChildren(itemId)) {
      tree.getChildren(itemId).toList.asInstanceOf[List[AnyRef]].map(z => deleteBranch(z.asInstanceOf[AnyRef]))
    }
    
    val obj = objMap(itemId)
    val objId = obj.getId()
    
    // Delete all
    tree.removeItem(itemId)
    
    ContentItemDAO.delete(obj)
  }
  
  private class TreeTableSortDropHandler(tree: ContentTree) extends DropHandler {

    def getAcceptCriterion(): AcceptCriterion = {
      AcceptAll.get()
    }

    def drop(dropEvent: DragAndDropEvent) {
      val t: Transferable = dropEvent.getTransferable()

      if (t.getSourceComponent() == tree && t.isInstanceOf[DataBoundTransferable]) {
        val dropData: AbstractSelect#AbstractSelectTargetDetails =
          dropEvent.getTargetDetails().asInstanceOf[AbstractSelect#AbstractSelectTargetDetails]
        val sourceItemId: AnyRef = t.asInstanceOf[DataBoundTransferable].getItemId()
        val targetItemId: AnyRef = dropData.getItemIdOver()
        val location: VerticalDropLocation = dropData.getDropLocation()
        val parentId: AnyRef = tree.getParent(sourceItemId)
        
        try {
          if (location == VerticalDropLocation.MIDDLE) tree.setChildrenAllowed(targetItemId, true)
          moveNode(sourceItemId, targetItemId, location)
          if (!tree.hasChildren(parentId)) tree.setChildrenAllowed(parentId, false)
          if (!tree.hasChildren(sourceItemId)) tree.setChildrenAllowed(sourceItemId, false)
          tree.recursivelyUpdatePositionsAndParents(tree.rootItemIds().toList.asInstanceOf[List[AnyRef]])
        } catch {
          case e => e.printStackTrace
        }
      }
    }

    private def moveNode(sourceItemId: AnyRef, targetItemId: AnyRef, location: VerticalDropLocation) {
      val container: HierarchicalContainer = tree.getContainerDataSource().asInstanceOf[HierarchicalContainer]

      if (location == VerticalDropLocation.MIDDLE) {
        if (container.setParent(sourceItemId, targetItemId) && container.hasChildren(targetItemId)) {
          // move first in the container
          container.moveAfterSibling(sourceItemId, null)
        }
      } else if (location == VerticalDropLocation.TOP) {
        val parentId: AnyRef = container.getParent(targetItemId)
        if (container.setParent(sourceItemId, parentId)) {
          // reorder only the two items, moving source above target
          container.moveAfterSibling(sourceItemId, targetItemId)
          container.moveAfterSibling(targetItemId, sourceItemId)
        }
      } else if (location == VerticalDropLocation.BOTTOM) {
        val parentId: AnyRef = container.getParent(targetItemId)
        if (container.setParent(sourceItemId, parentId)) {
          container.moveAfterSibling(sourceItemId, targetItemId)
        }
      }
    }
  }
  
  def recursivelySetCollapsed(items: java.util.Collection[_], collapsed: Boolean) {
    items.asInstanceOf[java.util.Collection[AnyRef]].toList.map(id => {
      if (hasChildren(id)) {
        setCollapsed(id, false)
        recursivelySetCollapsed(getChildren(id), collapsed)
      }
    })
  }
  
  def addContentItems(objs: List[ContentItem], parentId: AnyRef) {
    objs.map(obj => {
      val itemId = this.addItem()
      println("Adding content item " + obj.getName() + " with tags " + obj.getTags().toList.mkString("; "))
      this.objMap += (itemId -> obj)
      this.getContainerProperty(itemId, "id").setValue(obj.getId())
      this.getContainerProperty(itemId, "userId").setValue(obj.getUserId())
      this.getContainerProperty(itemId, "parentId").setValue(obj.getParentId())
      this.getContainerProperty(itemId, "name").setValue(obj.getName())
      this.getContainerProperty(itemId, "value").setValue(obj.getValue())
      this.getContainerProperty(itemId, "valueLabel").setValue(new Label(obj.getValue(), Label.CONTENT_XHTML))
      this.getContainerProperty(itemId, "vtype").setValue(obj.getVtype())
      this.getContainerProperty(itemId, "uri").setValue(obj.getUri())
      this.getContainerProperty(itemId, "tags").setValue(obj.getTags().toList)
      this.getContainerProperty(itemId, "tagLabel").setValue(new Label(obj.getTagsAsHTML(), Label.CONTENT_XHTML))
      this.getContainerProperty(itemId, "position").setValue(obj.getPosition())
      
      if (parentId != null) {
        this.setParent(itemId, parentId)
      }
      
      if (obj.hasChildren()) {
        this.setChildrenAllowed(itemId, true)
        this.addContentItems(obj.getChildren(), itemId)
      } else {
        this.setChildrenAllowed(itemId, false)
      }
    })
  }
}
