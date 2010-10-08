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
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Window.Notification

import com.vaadin.addon.treetable._

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap

import java.util.UUID

class ContentTree(objs: List[ContentItem]) extends TreeTable {
  var objMap: HashMap[AnyRef, ContentItem] = HashMap.empty
  
  addContainerProperty("id", classOf[String], "ID")
  addContainerProperty("name", classOf[String], "Name")
  addContainerProperty("value", classOf[String], "Value")
  addContainerProperty("tags", classOf[Label], "Tags")
  
  addContentItems(objs, null)
  
  setSizeFull()
  setColumnExpandRatio("name", 1)
  setImmediate(true)
  setItemCaptionPropertyId("name")
  setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY)
  setSelectable(true)
  addStyleName("striped")
  setVisibleColumns(List("name", "value", "tags").toArray)
  
  // Expand all nodes
  recursivelySetCollapsed(rootItemIds(), false)
  // setDragMode(Tree.TreeDragMode.NODE)
  // setDropHandler(new TreeSortDropHandler(this, container))
  
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
      this.objMap += (itemId -> obj)
      this.getContainerProperty(itemId, "id").setValue(obj.getId())
      this.getContainerProperty(itemId, "name").setValue(obj.getName())
      this.getContainerProperty(itemId, "value").setValue(obj.getValue())
      this.getContainerProperty(itemId, "tags").setValue(new Label(obj.getTagsAsHTML(), Label.CONTENT_XHTML))
      
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
