// -*- mode: Scala;-*- 
// Filename:    Agentrification.scala 
// Authors:     lgm                                                    
// Creation:    Thu Jun 17 13:58:04 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.validation
import java.net.URI
import scala.collection.mutable._

trait PersistentMessenger {
  def backing : Agent
}

object StdMsgTypes {
  // Messages
  type AJustStringRequest = JustifiedRequest[String,String]
  type AJustStringResponse = JustifiedResponse[String,String]  

}

case class RStringMessenger(
  override val name : URI,
  override val backing : Agent
) extends RMessenger[String,String](
  name,
  new ListBuffer[StdMsgTypes.AJustStringRequest](),
  new ListBuffer[StdMsgTypes.AJustStringResponse](),
  Some( new LinkedHashMap[URI,Socialite[String,String]]() ),
  AStringTraceMonitor
) with PersistentMessenger
