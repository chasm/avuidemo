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

import scala.collection.JavaConversions._

class TopicsManager(forumId: String) extends VerticalLayout with Property.ValueChangeListener {
  setWidth("100%")
  setHeight("380px")
  setMargin(true)
  
  val tree = new TopicTree()
  tree.setSizeFull()
  val container = TopicTreeContainer.load(forumId)
  tree.setContainerDataSource(container)
  tree.addListener(this)
  val treePanel = new Panel("Topics")
  treePanel.addComponent(tree)
  treePanel.addStyleName("borderless")
  treePanel.setSizeFull()
  treePanel.getContent().asInstanceOf[Layout].setSizeFull()
  
  var postDisplay = new PostDisplay(container)
  postDisplay.addStyleName("borderless")
  
  val hsPanel = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL)
  hsPanel.setSplitPosition(320, UNITS_PIXELS)
  hsPanel.addStyleName(Runo.SPLITPANEL_SMALL)
  hsPanel.setFirstComponent(treePanel)
  hsPanel.setSecondComponent(postDisplay)
  hsPanel.setSizeFull()
  hsPanel.setMargin(false)
  
  val panel = new Panel
  panel.addComponent(hsPanel)
  panel.getContent().asInstanceOf[Layout].setMargin(false)
  panel.getContent().asInstanceOf[Layout].setSizeFull()
  panel.setSizeFull()

  def valueChange(event: ValueChangeEvent) {
    val itemId = event.getProperty().getValue()
    if (itemId != null) {
      val selected = container.getItem(itemId)
      if (selected != null) {
        val postId: String = selected.getItemProperty("id").getValue().asInstanceOf[String]
        postDisplay.loadPost(postId, itemId)
      }
    } else {
      getWindow().showNotification("Item Click", "But nothing found!", Notification.TYPE_TRAY_NOTIFICATION)
    }
  }
  
  addComponent(panel)
}

class PostDisplay(ctr: TopicTreeContainer) extends VerticalLayout {
  setMargin(false)
  setSizeFull()
  val container = ctr
  
  def loadEditor(post: Post, itemId: AnyRef) {
    removeAllComponents()
    
    val panel = new Panel("Reply to " + post.getSubject())
    panel.setHeight("380px")
    panel.setWidth("100%")
    panel.getContent().asInstanceOf[Layout].setMargin(false)
    panel.addComponent(new ReplyForm(post, this, itemId))
    addComponent(panel)
  }
  
  def loadPost(postId: String, itemId: AnyRef) {
    val post: Post = PostDAO.get(postId).getOrElse(new Post())
    loadPost(post, itemId)
  }
  
  def loadPost(post: Post, itemId: AnyRef) {
    removeAllComponents()
    val member: Member = MemberDAO.get(post.getUserId()).getOrElse(new Member())
    val msgBody: Label = new Label(post.getBody(), Label.CONTENT_XHTML)
    msgBody.setSizeFull()

    val panel = new Panel(if (post.getCopId() != "") post.getSubject() else "Message")
    panel.setHeight("310px")
    panel.setWidth("100%")
    panel.addStyleName("borderless")
    panel.addComponent(msgBody)
    panel.getContent().asInstanceOf[Layout].setSizeFull()
    addComponent(panel)

    val btnBar = new HorizontalLayout()
    btnBar.setWidth("100%")
    btnBar.setHeight("70px")
    btnBar.setMargin(true)
    btnBar.setSpacing(true)
    val memberName = new Label(member.getName())
    btnBar.addComponent(memberName)
    btnBar.addComponent(new Label(post.getPosted()))
    val replyBtn = new Button("Reply")
    replyBtn.addListener(new Button.ClickListener() {
      def buttonClick(event: Button#ClickEvent) {
        loadEditor(post, itemId)
      }
    })
    btnBar.addComponent(replyBtn)
    btnBar.setExpandRatio(memberName, 1.0f);
    addComponent(btnBar)
  }
  
}

object TopicTreeContainer {
  def load(forumId: String): TopicTreeContainer = {
    val posts = PostDAO.getAllWithRepliesByForumId(forumId)

    val ttc: TopicTreeContainer = new TopicTreeContainer()
  
    ttc.addContainerProperty("subject", classOf[String], null)
    ttc.addContainerProperty("id", classOf[String], null)
    
    def addItemsRecursively(container: TopicTreeContainer, items: List[Post], parentId: AnyRef) {
      items.map(p => {
        val itemId = container.addItem()
        val item = container.getItem(itemId)
        item.getItemProperty("subject").setValue(p.getSubject())
        item.getItemProperty("id").setValue(p.getId())
        
        if (parentId != null) {
          container.setParent(itemId, parentId)
        }
        
        if (p.hasChildren()) {
          container.setChildrenAllowed(itemId, true)
          addItemsRecursively(container, p.getChildren(), itemId)
        } else {
          container.setChildrenAllowed(itemId, false)
        }
      })
    }  

    addItemsRecursively(ttc, posts, null)
    
    ttc
  }
}

class TopicTreeContainer extends HierarchicalContainer

object TopicTree {
  // Actions for the context menu
  private val ActionAdd: Action = new Action("Add child item")
  private val ActionDelete: Action = new Action("Delete")
  private val Actions: Array[Action] = Array(ActionAdd, ActionDelete)
}

class TopicTree extends Tree {
  setImmediate(true)
  setItemCaptionPropertyId("subject")
  setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY)
  setSelectable(true)
  addStyleName("borderless")
  setSizeFull()
  
  addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      TopicTree.Actions
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