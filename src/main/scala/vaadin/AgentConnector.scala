package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import java.net.URI
import java.util.{Date, UUID}

import com.biosimilarity.lift._
import com.biosimilarity.lift.model._
import com.biosimilarity.lift.model.store._
import com.biosimilarity.lift.lib._

trait CnxnQry

case class CnxnReq(
  val tag: CnxnLabel[String,String],
  val message: String,
  val name: String,
  val src: URI,
  val timestamp: Date,
  val ttl: Long,
  val cnxnResponseLabel: CnxnLabel[String,String]
) extends CnxnQry

case class CnxnResp(
  val tag: CnxnLabel[String,String],
  val message: String,
  val name: String,
  val src: URI,
  val timestamp: Date,
  val ttl: Long,
  val accepted: Boolean
) extends CnxnQry

object CnxnStore extends TermStore[String,String,String,CnxnQry]

class AgentConnector {
  val TTL: Long = 7 * 24 * 60 * 60 * 1000  // Requests last a week
  
  /**
   * Request a new connection
   *
   * @param sourceId The source agent's uuid as a string
   * @param targetId The target agent's uuid as a string
   * @param tag The relationship type requested
   */
  def requestCnxn(
    source: String,
    target: String,
    tag: ContentTag,
    message: String,
    name: String
  ) {
    
    println("")
    println("-----")
    println("requestCnxn:")
    println("source: " + source)
    println("target: " + target)
    println("tag: " + tag)
    println("")
    
    val label = new CnxnBranch[String,String](
      "cnxnRequest",
      List( new CnxnLeaf[String,String]("\"" + target + "\"") )
    )
    
    val cnxnResponseLabel: CnxnLabel[String,String] = new CnxnBranch[String,String](
      "responses",
      List(
        new CnxnLeaf[String, String]( "\"" + source + "\"" ),
        new CnxnLeaf[String, String]( "\"" + target + "\"" )
      )
    )
    
    val payload = CnxnStore.Ground(CnxnReq(
      ContentTag.toLabel(tag),
      message,
      name,
      new URI(source),
      new Date(),
      TTL,
      cnxnResponseLabel
    ))
    
    println("label: " + label.toString)
    println("cnxnResponseLabel: " + cnxnResponseLabel.toString)
    println("payload: " + payload.toString)
    println("")
    
    CnxnStore.put(label, payload)
    
    val handleCnxnResponse = (v: Option[CnxnStore.Resource]) => {
      v match {
        case Some(x) => println("\nFIRST LEVEL: " + x.toString + "\n")
        case None => println("\nNOTHING\n")
      }
      v
    }
    
    println("handleCnxnResponse: " + handleCnxnResponse.toString)
    println("-----")
    println("")
    
    CnxnStore.get(cnxnResponseLabel, handleCnxnResponse)
  }

  /**
   * Listen for connection requests
   *
   * @param sourceId The source agent's uuid as a string
   */
  def listenForCnxnRequests( source: String ) {
    
    println("")
    println("  +++++")
    println("  listenForCnxnRequests:")
    println("  source: " + source)
    println("")
    
    val qry = new CnxnBranch[String,String](
      "cnxnRequest",
      List( new CnxnLeaf[String,String]("\"" + source + "\"") )
    )
    
    val handleCnxnResponse = (v: Option[CnxnStore.Resource]) => {
      println(v.toString)
      v
    }
    
    println("  qry: " + qry.toString)
    println("  handleCnxnResponse: " + handleCnxnResponse.toString)
    println("  +++++")
    println("")
    
    CnxnStore.get(qry, handleCnxnResponse)
  }
  
  /**
   * Respond to a connection request
   *
   * @param sourceId The source agent's uuid as a string
   * @param targetId The target agent's uuid as a string
   * @param cnxn The relationship type requested
   * @param accepted True if request accepted; false if not
   */
  def respondToCnxnRequest(
    source: String,
    target: String,
    cnxn: CnxnLabel[String,String],
    message: String,
    accepted: Boolean
  ) {
    
    println("")
    println("*****")
    println("listenForCnxnRequests:")
    println("source: " + source)
    println("target: " + target)
    println("cnxn: " + cnxn.toString)
    println("accepted: " + accepted.toString)
    println("")
    
    val label = new CnxnBranch[String,String](
      "responses",
      List(
        new CnxnLeaf[String,String]( "\"" + source + "\"" ),
        new CnxnLeaf[String,String]( "\"" + target + "\"" )
      )
    )
    
    val payload = CnxnStore.Ground(CnxnResp(
      cnxn,
      message,
      AgentServices.getInstance().getCurrentUser match {
        case Some(u) => u.getName()
        case None => throw new Exception("Must be logged in to request connections.")
      },
      new URI(source),
      new Date(),
      TTL,
      accepted
    ))
    
    println("label: " + label.toString)
    println("payload: " + payload.toString)
    println("*****")
    println("")
    
    CnxnStore.put(label, payload)
  }
}
