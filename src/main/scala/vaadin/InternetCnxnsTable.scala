package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin.data.{Item, Property}
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.event.Action
import com.vaadin.terminal.ThemeResource
import com.vaadin.ui._
import com.vaadin.ui.Window.Notification
import com.vaadin.ui.Button.ClickListener

import org.vaadin.tinymceeditor._

import scala.collection.mutable.HashSet
import scala.collection.JavaConversions._

class InternetCnxnsContainer(val collection: java.util.Collection[InternetCnxn])
  extends BeanItemContainer[InternetCnxn](collection)
  
object InternetCnxnsContainer {
	def load: Option[InternetCnxnsContainer] = {
	  InternetCnxnDAO.getAllWithContentTagsByUserId(AgentServices.getInstance().getCurrentUserId().getOrElse("none")) match {
	    case Nil => None
	    case ics => Some(new InternetCnxnsContainer(ics))
	  }
	}	  
}

object InternetCnxnsTable {
  val ActionEdit = new Action("Edit")
  val ActionRemove = new Action("Remove")
  val Actions: Array[Action] = Array( ActionEdit, ActionRemove )
}

class InternetCnxnsTable extends VerticalLayout {

  val table = new Table()

  addComponent(table)

  table.addStyleName("borderless")
  table.addStyleName("striped")

  table.setWidth("100%")
  table.setHeight("150px")

  table.setSelectable(true)
  table.setMultiSelect(true)
  table.setImmediate(true)
  table.setColumnReorderingAllowed(true)
  table.setColumnCollapsingAllowed(true)
  
  InternetCnxnsContainer.load.map(c => {
    table.setContainerDataSource(c)
    table.addGeneratedColumn("tags", new TagColumnGenerator())
    table.setVisibleColumns(List("id", "site", "uri", "alias", "tags").toArray)
    table.setColumnHeaders(List("ID", "Site", "URI", "Alias", "Associations").toArray)
    table.setColumnExpandRatio("alias", 1)
    table.setColumnCollapsed("id", true)
  })

  table.addActionHandler(new Action.Handler() {
    def getActions(target: AnyRef, sender: AnyRef): Array[Action] = {
      InternetCnxnsTable.Actions
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case edit if edit == InternetCnxnsTable.ActionEdit =>
          getWindow().showNotification("Edit Cop", "Edit this cop.", Notification.TYPE_TRAY_NOTIFICATION)
        case remove if remove == InternetCnxnsTable.ActionRemove =>
          getWindow().showNotification("Remove Cop", "Remove this cop.", Notification.TYPE_TRAY_NOTIFICATION)
        case _ =>
          getWindow().showNotification("Error", "Unrecognized action.", Notification.TYPE_TRAY_NOTIFICATION)
      }
    }
  })
  
  private class EditInternetCnxnWindow(table: InternetCnxnsTable, item: Item, itemId: AnyRef, obj: Cop)
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
    rta.setValue(item.getItemProperty("desc").getValue().asInstanceOf[String])

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

  
  private class ConfirmDeletionWindow(table: Table, itemId: AnyRef) extends Window with ClickListener {
    setWidth("360px")
    setHeight("144px")
    setCaption("Are you sure?")
    center()
    
    val lbl = new Label("This action will cancel your membership in this community of practice.")
      
    val remove = new Button("Remove", this.asInstanceOf[ClickListener])
    val cancel = new Button("Cancel", this.asInstanceOf[ClickListener])
    
    val hl = new HorizontalLayout()
    hl.setSizeUndefined()
    hl.setMargin(true)
    hl.setSpacing(true)
    hl.addComponent(remove)
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
        case r if (r == remove) => 
          table.removeItem(itemId)
          close()
        case _ => close()
      }
    }
  }
}

class TagColumnGenerator extends Table.ColumnGenerator {
  def generateCell(source: Table, itemId: AnyRef, columnId: AnyRef): Component = {
    val prop: Property = source.getItem(itemId).getItemProperty("tags")
    val tags = prop.getValue() match {
      case null => ""
      case ts => 
        ts.asInstanceOf[List[ContentTag]]
          .map(t => <span class={"tag_" + t.getAbbr()} title={t.getName()}>{t.getAbbr()}</span>).mkString("")
    }
    new Label(tags, Label.CONTENT_XHTML)
  }
}