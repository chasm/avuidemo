// package com.nonebetwixt.agent.ui
// 
// import com.vaadin.data.{Item, Property}
// import com.vaadin.data.Property.ValueChangeEvent
// import com.vaadin.event.Action
// import com.vaadin.ui._
// 
// import scala.collection.JavaConversions._
// 
// class ContentTree extends HorizontalLayout with Action.Handler {
// 
//   // Actions for the context menu
//   private val ACTION_ADD = new Action("Add child item")
//   private val ACTION_DELETE = new Action("Delete")
//   private val ACTIONS: Array[Action] = Array(ACTION_ADD, ACTION_DELETE)
// 
//   private val tree = new Tree("Managed Content")
// 
//   setSpacing(true)
//   addComponent(tree)
//   
//   // HorizontalLayout editBar
//   // private TextField editor
//   // private Button change
// 
//   // tree.setContainerDataSource()
// 
//   tree.addActionHandler(this)
//   tree.setImmediate(true)
// 
//   tree.setItemCaptionPropertyId("name")
//   tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY)
// 
//   tree.rootItemIds().toList.map(id => {
//     tree.expandItemsRecursively(id)
//   })
// 
//   /*
//    * Returns the set of available actions
//    */
//   def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
//     ACTIONS
//   }
// 
//   /*
//    * Handle actions
//    */
//   def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
//     if (action == ACTION_ADD) {
//       tree.setChildrenAllowed(target, true)
//       tree.expandItem(target)
// 
//       val itemId = tree.addItem()
//       tree.setParent(itemId, target)
//       tree.setChildrenAllowed(itemId, false)
// 
//       val item: Item = tree.getItem(itemId)
//       val name: Property = item.getItemProperty("name")
//       name.setValue("New Item")
// 
//     } else if (action == ACTION_DELETE) {
//       val parent = tree.getParent(target)
//       tree.removeItem(target)
//       if (parent != null && tree.getChildren(parent).size() == 0) {
//         tree.setChildrenAllowed(parent, false)
//       }
//     }
//   }
// }