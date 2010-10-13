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
  val src: URI,
  val timestamp: Date,
  val ttl: Long,
  val cnxnResponseLabel: CnxnLabel[String,String]
) extends CnxnQry

case class CnxnResp(
  val tag: CnxnLabel[String,String],
  val src: URI,
  val timestamp: Date,
  val ttl: Long,
  val accepted: Boolean
) extends CnxnQry

object CnxnStore extends TermStore[String,String,String,String] {
  var Test: String = ""
}

class AgentConnector {
  val TTL: Long = 7 * 24 * 60 * 60 * 1000  // Requests last a week
  
  /**
   * Request a new connection
   *
   * @param sourceId The source agent's uuid as a string
   * @param targetId The target agent's uuid as a string
   * @param tag The relationship type requested
   */
  def requestCnxn(source: String, target: String, tag: String) {
    
    println("")
    println("-----")
    println("requestCnxn:")
    println("source: " + source)
    println("target: " + target)
    println("tag: " + tag)
    println("")
    
    val cnxn: CnxnBranch[String,String] = new CnxnBranch[String,String](
      "associations",
      List(
        new CnxnLeaf[String,String]( tag )
      )
    )
    
    // val label = new CnxnBranch[String,String](
    //   "cnxnRequest",
    //   List(
    //     new CnxnBranch[String,String](
    //       "target",
    //       List( new CnxnLeaf[String,String]("\"" + target + "\""))
    //     )
    //   )
    // )
    
    val label = new CnxnLeaf[String,String]("\"" + target + "\"")
    
    val cnxnResponseLabel: CnxnLabel[String,String] = new CnxnBranch[String,String](
      "responses",
      List(
        new CnxnLeaf[String, String]( "\"" + source + "\"" ),
        new CnxnLeaf[String, String]( "\"" + target + "\"" )
      )
    )
    
    // val payload = CnxnStore.Ground(CnxnReq(
    //   cnxn,
    //   new URI(source),
    //   new Date(),
    //   AgentConnector.TTL,
    //   cnxnResponseLabel
    // ))
    
    val payload = source
    
    println("label: " + label.toString)
    println("cnxnResponseLabel: " + cnxnResponseLabel.toString)
    println("payload: " + payload.toString)
    println("")
    
    CnxnStore.put(label, payload)
    
    // val qry = new CnxnCtxtBranch[String,String,String](
    //   "responses",
    //   List(
    //     new CnxnCtxtLeaf[String,String,String]( Left( "\"" + source + "\"" ) ),
    //     new CnxnCtxtLeaf[String,String,String]( Left( "\"" + target + "\"" ) )
    //   )
    // )
    // 
    // val handleCnxnResponse = (v: Option[CnxnStore.Resource]) => {
    //   v match {
    //     case Some(x) => println("\nFIRST LEVEL: " + x.toString + "\n")
    //     case None => println("\nNOTHING\n")
    //   }
    //   v
    // }
    // 
    // println("qry: " + qry.toString)
    // println("handleCnxnResponse: " + handleCnxnResponse.toString)
    // println("-----")
    // println("")
    // 
    // CnxnStore.get(qry, handleCnxnResponse)
  }
  
  
  // Some(
  //   RBound(
  //     Some(
  //       Ground(
  //         CnxnReq(
  //           List(
  //             Right(
  //               List(
  //                 Left(Close Friend)
  //               )
  //             )
  //           ),
  //           agent:82841319-c51d-4b35-abfb-0a5ce84680e0,
  //           Wed Oct 13 01:37:03 GMT-03:00 2010,
  //           604800000,
  //           List(
  //             Right(
  //               List(
  //                 Left(
  //                   &quot;agent:82841319-c51d-4b35-abfb-0a5ce84680e0&quot;
  //                 )
  //               )
  //             ), 
  //             Right(
  //               List(
  //                 Left(
  //                   &quot;agent:00000000-1111-2222-3333-444444444444&quot;
  //                 )
  //               )
  //             )
  //           )
  //         )
  //       )
  //     ),
  //     Some(org.prolog4j.tuprolog.TuPrologSolution@1240158)
  //   )
  // )
  
  
  
  
  
  
  
  /**
   * Listen for connection requests
   *
   * @param sourceId The source agent's uuid as a string
   */
  def listenForCnxnRequests(source: String) {
    
    println("")
    println("  +++++")
    println("  listenForCnxnRequests:")
    println("  source: " + source)
    println("")
    
    // val qry = new CnxnCtxtBranch[String,String,String](
    //   "cnxnRequest",
    //   List(
    //     new CnxnCtxtBranch[String,String,String](
    //       "target",
    //       List(
    //         new CnxnCtxtLeaf[String,String,String]( Left( "\"" + source + "\"" ) )
    //       )
    //     )
    //   )
    // )
    
    val qry = new CnxnLeaf[String,String]("\"" + source + "\"")
    
    val handleCnxnResponse = (v: Option[CnxnStore.Resource]) => {
      println("\n    I AM AGENT " + AgentServices.getInstance().getCurrentUserId().getOrElse("crapola"))
      println("    GOT A RESPONSE! " + v.toString)
      listenForCnxnRequests(source)
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
  def respondToCnxnRequest(source: String, target: String, cnxn: CnxnLabel[String,String], accepted: Boolean) {
    
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
    
    // val payload = CnxnStore.Ground(CnxnResp(
    //   cnxn,
    //   new URI(source),
    //   new Date(),
    //   AgentConnector.TTL,
    //   accepted
    // ))
    
    val payload = source
    
    println("label: " + label.toString)
    println("payload: " + payload.toString)
    println("*****")
    println("")
    
    CnxnStore.put(label, payload)
  }
}
