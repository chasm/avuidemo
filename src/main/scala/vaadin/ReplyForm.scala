package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import javax.servlet._

import scala.collection.JavaConversions._

import com.vaadin._
import com.vaadin.data.Item
import com.vaadin.data.util.BeanItem
import com.vaadin.ui._
import com.vaadin.ui.Button.ClickListener
import com.vaadin.terminal.ExternalResource

import org.vaadin.tinymceeditor._

import java.util.{Date}

import reflect.{BeanProperty,BeanDisplayName}

class ReplyForm(post: Post, parent: PostDisplay, parentId: AnyRef)
    extends CustomComponent with ClickListener {
  val save: Button = new Button("Save", this.asInstanceOf[ClickListener])
  val cancel: Button = new Button("Cancel", this.asInstanceOf[ClickListener])
	
	val member: Option[Member] = MemberDAO.getByCopIdAndUserId(
	  post.getCopId(),
	  AgentServices.getInstance().getCurrentUserId().getOrElse("")
	)
	
  val newPost: Post = new Post(
    post.getCopId(),
    member.map(_.getId()).getOrElse(null),
    post.getForumId(),
    post.getId(),
    post.getSubject(),
    ""
  )
  
  val layout = new VerticalLayout
  layout.setSpacing(false)
  layout.setMargin(false)
  
  val subject = new TextField()
  subject.setValue(newPost.getSubject())
  subject.setWidth("100%")
  
  val body = new TinyMCETextField()
  body.setWidth("100%")
  body.setHeight("240px")
  
  val btnBar = new HorizontalLayout()
  btnBar.setHeight("72px")
	btnBar.setSpacing(true)
	btnBar.addComponent(save)
	btnBar.addComponent(cancel)
	btnBar.setMargin(true)

  layout.addComponent(subject)
  layout.addComponent(body)
  layout.addComponent(btnBar)
  
  def buttonClick(event: Button#ClickEvent) {
    event.getButton() match {
      case s if s == save => 
        newPost.setSubject(subject.getValue().asInstanceOf[String])
        newPost.setBody(body.getValue().asInstanceOf[String])
        PostDAO.put(newPost)
        val p = PostDAO.get(newPost.getId()).getOrElse(new Post())
        val container = parent.container
        val itemId = container.addItem()
        parent.loadPost(newPost, itemId)
        val item = container.getItem(itemId)
        item.getItemProperty("subject").setValue(newPost.getSubject())
        item.getItemProperty("id").setValue(newPost.getId())
        container.setChildrenAllowed(itemId, false)
        container.setChildrenAllowed(parentId, true)
        container.setParent(itemId, parentId)
      case c if c == cancel => {
        parent.loadPost(post, parentId)
      }
      case _ => println("Huh?")
    }
  }
  
  setCompositionRoot(layout)
}
