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

import scala.collection.JavaConversions._

object LabelTree {
  // Actions for the context menu
  // val ActionAdd: Action = new Action("Add child item")
  // val ActionDelete: Action = new Action("Delete")
  // val Actions: Array[Action] = Array(ActionAdd, ActionDelete)
}

class LabelTree(container: HierarchicalContainer) extends VerticalLayout {
  setMargin(false)
  setSpacing(false)
  setWidth("100%")

  val tree = new Tree("Relationships")
  tree.setImmediate(true)
  tree.setItemCaptionPropertyId("name")
  tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY)
  tree.setContainerDataSource(container)
  // tree.addListener(listener)
  tree.setSelectable(true)
  tree.setMultiSelect(true)
  tree.addStyleName("borderless")
  tree.setSizeUndefined()

  // Expand all nodes
  tree.rootItemIds().toList.map(id => {
    tree.expandItemsRecursively(id)
  })
  // tree.setDragMode(Tree.TreeDragMode.NODE)
  // tree.setDropHandler(new TreeSortDropHandler(tree, container))

  addComponent(tree)
  
  def getTree(): Tree = tree
    
  // private class TreeSortDropHandler(val tree: Tree, container: HierarchicalContainer) extends DropHandler {
  // 
  //   def getAcceptCriterion(): AcceptCriterion = {
  //     AcceptAll.get()
  //   }
  // 
  //   def drop(dropEvent: DragAndDropEvent) {
  //     val t: Transferable = dropEvent.getTransferable()
  // 
  //     if (t.getSourceComponent() == tree && t.isInstanceOf[DataBoundTransferable]) {
  //       val dropData: Tree#TreeTargetDetails = dropEvent.getTargetDetails().asInstanceOf[Tree#TreeTargetDetails]
  //       val sourceItemId: AnyRef = t.asInstanceOf[DataBoundTransferable].getItemId()
  //       val targetItemId: AnyRef = dropData.getItemIdOver()
  //       val location: VerticalDropLocation = dropData.getDropLocation()
  //       val parentId: AnyRef = tree.getParent(sourceItemId)
  //       
  //       try {
  //         if (location == VerticalDropLocation.MIDDLE) tree.setChildrenAllowed(targetItemId, true)
  //         moveNode(sourceItemId, targetItemId, location)
  //         if (!tree.hasChildren(parentId)) tree.setChildrenAllowed(parentId, false)
  //         if (!tree.hasChildren(sourceItemId)) tree.setChildrenAllowed(sourceItemId, false)
  //         recursivelyUpdatePositionsAndParents(tree.rootItemIds().toList.asInstanceOf[List[AnyRef]])
  //       } catch {
  //         case e => e.printStackTrace
  //       }
  //     }
  //   }
  //   
  //   def recursivelyUpdatePositionsAndParents(items: List[AnyRef]) {
  //     items.zipWithIndex.map(x => {
  //       val item = tree.getItem(x._1)
  //       val id = item.getItemProperty("id").getValue().toString
  //       val oldPos = item.getItemProperty("position").getValue().asInstanceOf[String].toInt
  //       val newPos = x._2
  //       
  //       if (oldPos != newPos) {
  //         ContentItemDAO.get(id).map(obj => {
  //           obj.setPosition(newPos)
  //           ContentItemDAO.put(obj)
  //           item.getItemProperty("position").setValue(newPos)
  //         })
  //       }
  //       
  //       if (tree.hasChildren(x._1)) {
  //         recursivelyUpdatePositionsAndParents(tree.getChildren(x._1).toList.asInstanceOf[List[AnyRef]])
  //       }
  //     })
  //   }
  // 
  //   private def moveNode(sourceItemId: AnyRef, targetItemId: AnyRef, location: VerticalDropLocation) {
  //     val container: HierarchicalContainer = tree.getContainerDataSource().asInstanceOf[HierarchicalContainer]
  // 
  //     if (location == VerticalDropLocation.MIDDLE) {
  //       if (container.setParent(sourceItemId, targetItemId) && container.hasChildren(targetItemId)) {
  //         // move first in the container
  //         container.moveAfterSibling(sourceItemId, null)
  //       }
  //     } else if (location == VerticalDropLocation.TOP) {
  //       val parentId: AnyRef = container.getParent(targetItemId)
  //       if (container.setParent(sourceItemId, parentId)) {
  //         // reorder only the two items, moving source above target
  //         container.moveAfterSibling(sourceItemId, targetItemId)
  //         container.moveAfterSibling(targetItemId, sourceItemId)
  //       }
  //     } else if (location == VerticalDropLocation.BOTTOM) {
  //       val parentId: AnyRef = container.getParent(targetItemId)
  //       if (container.setParent(sourceItemId, parentId)) {
  //         container.moveAfterSibling(sourceItemId, targetItemId)
  //       }
  //     }
  //   }
  // }
}

object LabelTreeContainer {
  def load(userId: String): LabelTreeContainer = {
    val contentTags = ContentTagDAO.getAllWithChildrenByUserId(userId)

    val ltc: LabelTreeContainer = new LabelTreeContainer()
    
    ltc.addContainerProperty("id", classOf[String], null)
    ltc.addContainerProperty("userId", classOf[String], null)
    ltc.addContainerProperty("parentId", classOf[String], null)
    ltc.addContainerProperty("name", classOf[String], null)
    ltc.addContainerProperty("abbr", classOf[String], null)
    ltc.addContainerProperty("position", classOf[String], null)
    ltc.addContainerProperty("foreground", classOf[String], null)
    ltc.addContainerProperty("background", classOf[String], null)
    ltc.addContainerProperty("tag", classOf[ContentTag], null)
    
    def addItemsRecursively(container: LabelTreeContainer, items: List[ContentTag], parentId: AnyRef) {
      items.map(t => {
        val itemId = container.addItem()
        val item = container.getItem(itemId)
        item.getItemProperty("id").setValue(t.getId())
        item.getItemProperty("userId").setValue(t.getUserId())
        item.getItemProperty("parentId").setValue(t.getParentId())
        item.getItemProperty("name").setValue(t.getName())
        item.getItemProperty("abbr").setValue(t.getAbbr())
        item.getItemProperty("position").setValue(t.getPosition())
        item.getItemProperty("foreground").setValue(t.getForeground())
        item.getItemProperty("background").setValue(t.getBackground())
        item.getItemProperty("tag").setValue(t)
        
        if (parentId != null) {
          container.setParent(itemId, parentId)
        }
        
        if (t.hasChildren()) {
          container.setChildrenAllowed(itemId, true)
          addItemsRecursively(container, t.getChildren(), itemId)
        } else {
          container.setChildrenAllowed(itemId, false)
        }
      })
    }  

    addItemsRecursively(ltc, contentTags, null)
    
    ltc
  }
}

class LabelTreeContainer extends HierarchicalContainer
