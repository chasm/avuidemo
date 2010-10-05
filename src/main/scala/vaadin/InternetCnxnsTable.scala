package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin.data.{Item, Property}
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.util.{BeanItem, BeanItemContainer, IndexedContainer}
import com.vaadin.event.Action
import com.vaadin.terminal.ThemeResource
import com.vaadin.ui._

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
  val ActionMark = new Action("Mark")
  val ActionUnmark = new Action("Unmark")
  val ActionLog = new Action("Save")
  val ActionsUnmarked: Array[Action] = Array( ActionMark, ActionLog )
  val ActionsMarked: Array[Action] = Array( ActionUnmark, ActionLog )
}

class InternetCnxnsTable extends VerticalLayout {

  val table = new Table()

  val markedRows: HashSet[AnyRef] = new HashSet()

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
      if (markedRows.contains(target)) {
        Array(InternetCnxnsTable.ActionMark)
      } else {
        Array(InternetCnxnsTable.ActionUnmark)
      }
    }

    def handleAction(action: Action, sender: AnyRef, target: AnyRef) {
      action match {
        case mark if mark == InternetCnxnsTable.ActionMark =>
          markedRows.add(target)
          table.requestRepaint()
        case unmark if unmark == InternetCnxnsTable.ActionUnmark =>
          markedRows.remove(target)
          table.requestRepaint()
        case _ =>
      }
    }
  })
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