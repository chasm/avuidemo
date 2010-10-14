package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import java.net.URI
import java.util.{Date, UUID}

import com.biosimilarity.lift._
import com.biosimilarity.lift.model._
import com.biosimilarity.lift.model.store._
import com.biosimilarity.lift.lib._

case class Cntnt(id: String, agentId: String, name: String, value: String, position: Int, parentId: String)

object CntntStore extends TermStore[String,String,String,Cntnt]

class ContentRouter {
  val TTL: Long = 7 * 24 * 60 * 60 * 1000  // Requests last a week
  
  def putContent(source: String, tag: ContentTag, contentItem: ContentItem) {
    
    println("")
    println("-----")
    println("putContent:")
    println("source: " + source)
    println("tag: " + tag)
    println("contentItem: " + contentItem)
    println("")
    
    val label = new CnxnBranch[String,String](
      "contentItem",
      List(
        new CnxnLeaf[String,String]("\"" + source + "\""),
        ContentTag.toLabel(tag)
      )
    )
    
    CntntStore.put(label, Cntnt(
      contentItem.getId(),
      contentItem.getUserId(),
      contentItem.getName(),
      contentItem.getValue(),
      contentItem.getPosition(),
      contentItem.getParentId()
    ))
  }

  def getContent(target: String, tag: ContentTag) {
    
    println("")
    println("  +++++")
    println("  getContent:")
    println("  target: " + target)
    println("  tag: " + tag)
    println("")
    
    val qry = new CnxnBranch[String,String](
      "contentItem",
      List(
        new CnxnLeaf[String,String]("\"" + target + "\""),
        ContentTag.toLabel(tag)
      )
    )
    
    val handleCntntResponse = (v: Option[CntntStore.Resource]) => {
      println("")
      println("handleCntntResponse reset:")
      println(v.getOrElse("\nCAN'T GRAB IT.\n"))
      println("")
      v
    }
    
    println("  qry: " + qry.toString)
    println("  handleCntntResponse: " + handleCntntResponse.toString)
    println("  +++++")
    println("")
    
    CntntStore.get(qry, handleCntntResponse)
  }
}
