// -*- mode: Scala;-*- 
// Filename:    SpecialKMessenger.scala 
// Authors:     lgm                                                    
// Creation:    Wed Aug 25 13:58:10 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.lift.model.store

import com.biosimilarity.lift.model.agent._
import com.biosimilarity.lift.model.msg._
import com.biosimilarity.lift.lib._

import net.liftweb.amqp._

import scala.collection.mutable._
import scala.util.continuations._ 

import scala.actors.Actor
import scala.actors.Actor._

import com.rabbitmq.client._

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver

import java.net.URI
import java.util.UUID

trait DistributedTermSpaceMsg[Namespace,Var,Tag,Value]
trait DistributedTermSpaceRequest[Namespace,Var,Tag,Value]
extends DistributedTermSpaceMsg[Namespace,Var,Tag,Value]
trait DistributedTermSpaceResponse[Namespace,Var,Tag,Value]
extends DistributedTermSpaceMsg[Namespace,Var,Tag,Value]

case class DGetRequest[Namespace,Var,Tag,Value](
  path : CnxnCtxtLabel[Namespace,Var,Tag]
) extends DistributedTermSpaceRequest[Namespace,Var,Tag,Value]
case class DGetResponse[Namespace,Var,Tag,Value](
  path : CnxnCtxtLabel[Namespace,Var,Tag],
  value : Value
) extends DistributedTermSpaceResponse[Namespace,Var,Tag,Value]
case class DFetchRequest[Namespace,Var,Tag,Value](
  path : CnxnCtxtLabel[Namespace,Var,Tag]
) extends DistributedTermSpaceRequest[Namespace,Var,Tag,Value]
case class DFetchResponse[Namespace,Var,Tag,Value](
  path : CnxnCtxtLabel[Namespace,Var,Tag],
  value : Value
) extends DistributedTermSpaceResponse[Namespace,Var,Tag,Value]
case class DPutRequest[Namespace,Var,Tag,Value](
  path : CnxnCtxtLabel[Namespace,Var,Tag],
  value : Value
) extends DistributedTermSpaceRequest[Namespace,Var,Tag,Value]
case class DPutResponse[Namespace,Var,Tag,Value](
  path : CnxnCtxtLabel[Namespace,Var,Tag]
) extends DistributedTermSpaceResponse[Namespace,Var,Tag,Value]


trait AgentsOverAMQP[Namespace,Var,Tag,Value] {
  type DReq = DistributedTermSpaceRequest[Namespace,Var,Tag,Value]
  type DRsp = DistributedTermSpaceResponse[Namespace,Var,Tag,Value]
  type JTSReq = JustifiedRequest[DReq,DRsp]
  type JTSRsp = JustifiedResponse[DReq,DRsp]

  object IdVendor extends UUIDOps {
    def getURI() = {
      new URI( "com", "biosimilarity", getUUID().toString )
    }
  }

  object AnAMQPTraceMonitor extends TraceMonitor[DReq,DRsp]  

  class AMQPAgent(
    alias : URI
  ) extends ReflectiveMessenger[DReq,DRsp](
    alias,
    new ListBuffer[JTSReq](),
    new ListBuffer[JTSRsp](),
    Some( new LinkedHashMap[URI,Socialite[DReq,DRsp]]),
    AnAMQPTraceMonitor
  )
  with UUIDOps {
    override def validateTarget( msg : {def to : URI} ) : Boolean = {
      // Put URI filtering behavior here
      true
    }
    
    override def validateAcquaintance( msg : {def from : URI} ) : Boolean = {
      // Put Requestor filtering behavior here
      nameSpace match {
	case None => false
	case Some( map ) => true
      }
    }

    override def handleWithContinuation(
      request : JTSReq,
      k : Status[JTSReq] => Status[JTSReq]
    ) = {
      //println( "handling: " + request )
      request match {
	case JustifiedRequest(
	  msgId, trgt, src, lbl, body, None
	) => { 
	  // Handle a justified request with no initiating response
	}
	case _ => {
	  // Handle a justified request with an initiating response
	}
      }
      JReqStatus(
	request,
	true,
	None,
	Some( k )
      )
    }  
  }
  
}

trait EndPoint[Namespace,Var,Tag,Value] {
  def location : URI
  def handleRequest( 
    dmsg : JustifiedRequest[DistributedTermSpaceRequest[Namespace,Var,Tag,Value],DistributedTermSpaceResponse[Namespace,Var,Tag,Value]]
  ) : Boolean
  def handleResponse( 
    dmsg : DistributedTermSpaceResponse[Namespace,Var,Tag,Value]
  ) : Boolean
}

class AgentTwistedPair[Namespace,Var,Tag,Value](
  src : EndPoint[Namespace,Var,Tag,Value],
  trgt : EndPoint[Namespace,Var,Tag,Value]
) extends AgentsOverAMQP[Namespace,Var,Tag,Value]
with AbstractJSONAMQPListener
with UUIDOps {
  
  implicit def endPointAsURI(
    ep : EndPoint[Namespace,Var,Tag,Value]
  ) : URI = {
    ep.location
  }

  type JSONListener = AMQPAgent  

  case object _jsonListener
  extends AMQPAgent( trgt ) {
    override def handleWithContinuation(
      request : JTSReq,
      k : Status[JTSReq] => Status[JTSReq]
    ) = {
      //println( "handling: " + request )
      request match {
	case JustifiedRequest(
	  msgId, mtrgt, msrc, lbl, body, None
	) => { 
	  // Handle a justified request with no initiating response	  
	  JReqStatus(
	    request,
	    src.handleRequest( request ),
	    None,
	    Some( k )
	  )
	}
	case _ => {
	  // Handle a justified request with an initiating response
	  JReqStatus(
	    request,
	    src.handleRequest( request ),
	    None,
	    Some( k )
	  )
	}
      }      
    }

    override def handleWithContinuation(
      response : JTSRsp,
      k : Status[JTSRsp] => Status[JTSRsp]
    ) = {
      //println( "handling: " + request )
      response match {
	case JustifiedResponse(
	  msgId, mtrgt, msrc, lbl, body, just
	) => { 	  
	  JRspStatus(
	    response,
	    src.handleResponse( body ),
	    None,
	    Some( k )
	  )
	}
	case _ => {
	  throw new Exception(
	    "Unexpected message type : " + response.getClass
	  )
	}
      }      
    }

    override def act () {
      nameSpace match {
	case None => {
	  logError( name, this, NoNamespace() )
	}
	case Some( map ) => {
	  receive {
	    case msg@AMQPMessage( cntnt : String ) => {
	      val h2o = rehydrate( cntnt ) 
	      h2o match { 
		case Left( jreq ) => this ! jreq 
		case Right( jrsp ) => this ! jrsp
	      }
	    }
	    case jr@JustifiedRequest(
	      m, p, d, t,
	      f : DReq,
	      c : Option[Response[AbstractJustifiedRequest[DReq,DRsp],DRsp]]
	    ) => {	    
	      val jrJSON : JustifiedRequest[DReq,DRsp]
	      = jr.asInstanceOf[JustifiedRequest[DReq,DRsp]]
    
	      if ( validate( jrJSON ) ) {
		println( "calling handle on " + jr )
		reset {
		  shift {
		    ( k : Status[JustifiedRequest[DReq,DRsp]] => Status[JustifiedRequest[DReq,DRsp]] )
		  => {
		    k( handleWithContinuation( jrJSON, k ) )
		  }
		  }
		}
	      }
	      act()
	    }
	    case jr@JustifiedResponse(
	      m, p, d, t,
	      f : DRsp,
	      c : Option[Request[AbstractJustifiedResponse[DReq,DRsp],DReq]]
	    ) =>  {
	      val jrJSON : JustifiedResponse[DReq,DRsp]
	      = jr.asInstanceOf[JustifiedResponse[DReq,DRsp]]
	      if ( validate( jrJSON ) ) {
		println( "calling handle on " + jr )
		reset {
		  shift {
		    ( k : Status[JustifiedResponse[DReq,DRsp]] => Status[JustifiedResponse[DReq,DRsp]] )
		  => {
		    k( handleWithContinuation( jrJSON, k ) )
		  }
		  }
		}
	      }
	      act()
	    }
	    case ir@InspectRequests( t, f ) => {
	      if ( validate( ir ) ) {
		println( "calling handle on " + ir )
		handle( ir )
	      }
	      act()
	    }
	    case ir@InspectResponses( t, f ) => {
	      if ( validate( ir ) ) {
		println( "calling handle on " + ir )
		handle( ir )
	      }
	      act()
	    }
	    case ir@InspectNamespace( t, f ) => {
	      if ( validate( ir ) ) {
		println( "calling handle on " + ir )
		handle( ir )
	      }
	      act()
	    }
	  }
	}
      }    
    }
  }

  override def jsonListener() : JSONListener = {
    _jsonListener
  }  

  override def host : String = { trgt.getHost }

  case object _jsonSender
  extends JSONAMQPSender(
    rabbitFactory(),
    host,
    5672,
    "mult",
    "routeroute"
  )
  _jsonSender.start
  
  def jsonSender() : JSONAMQPSender = {
    _jsonSender
  }

  def send( contents : DReq ) : Unit = {
    val jr = JustifiedRequest[DReq,DRsp](
      getUUID(),
      trgt,
      src,
      getUUID(),
      contents,
      None
    )

    _jsonSender ! AMQPMessage(
      new XStream( new JettisonMappedXmlDriver() ).toXML( jr )
    )
  }

  def send( contents : DRsp ) : Unit = {
    val jr = JustifiedResponse[DReq,DRsp](
      getUUID(),
      src,
      trgt,
      getUUID(),
      contents,
      None
    )

    _jsonSender ! AMQPMessage(
      new XStream( new JettisonMappedXmlDriver() ).toXML( jr )
    )
  }

  def rehydrate( contents: String ) :
  Either[
    JustifiedRequest[DReq,DRsp],
    JustifiedResponse[DReq,DRsp]
  ] = {
    val msg =
      new XStream( new JettisonMappedXmlDriver() ).fromXML( contents )
    msg match {
      case jreq : JustifiedRequest[DReq,DRsp] => {
	Left[JustifiedRequest[DReq,DRsp],JustifiedResponse[DReq,DRsp]]( jreq )
      }
      case jrsp : JustifiedResponse[DReq,DRsp] => {
	Right[JustifiedRequest[DReq,DRsp],JustifiedResponse[DReq,DRsp]]( jrsp )
      }
      case _ => {
	throw new Exception(
	  "unexpected message type : " + msg.getClass
	)
      }
    }
  }
  
}


