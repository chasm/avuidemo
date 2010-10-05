// -*- mode: Scala;-*- 
// Filename:    Cnxn.scala 
// Authors:     lgm                                                    
// Creation:    Sun Aug  8 11:59:45 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.validation

import java.net.URI

trait CnxnLabel[Namespace,Tag]
class CnxnLeaf[Namespace,Tag]( val tag : Tag )
extends CnxnLabel[Namespace,Tag]

object CnxnLeaf {
  def unapply[Namespace,Tag](
    cnxnLeaf : CnxnLeaf[Namespace,Tag]
  ) : Option[( Tag )] = {
    Some( ( cnxnLeaf.tag ) )
  }
}

class CnxnBranch[Namespace,Tag](
  val nameSpace : Namespace,
  val labels : List[CnxnLabel[Namespace,Tag]]
) extends CnxnLabel[Namespace,Tag]

object CnxnBranch {
  def unapply[Namespace,Tag](
    cnxnBranch : CnxnBranch[Namespace,Tag]
  ) : Option[( Namespace, List[CnxnLabel[Namespace,Tag]] )] = {
    Some( ( cnxnBranch.nameSpace, cnxnBranch.labels ) )
  }
}

trait Cnxn[Src,Label,Trgt] {
  def src   : Src
  def label : Label
  def trgt  : Trgt
}
class CCnxn[Src,Label,Trgt](
  override val src : Src,
  override val label : Label,
  override val trgt : Trgt
) extends Cnxn[Src,Label,Trgt]

object CCnxn {
  def unapply[Src,Label,Trgt](
    cnxn : CCnxn[Src,Label,Trgt]
  ) : Option[(Src,Label,Trgt)] = {
    Some( ( cnxn.src, cnxn.label, cnxn.trgt ) )
  }  
}

case class StringCnxnLeaf( override val tag : String )
     extends CnxnLeaf[String,String]( tag )

case class StringCnxnBranch(
  override val nameSpace : String,
  override val labels : List[CnxnLabel[String,String]]
) extends CnxnBranch[String,String]( nameSpace, labels )

class RMessengerStrLabeledCnxn[ReqBody,RspBody](
  override val src : RMessenger[ReqBody,RspBody],
  override val label : CnxnLabel[String,String],
  override val trgt : RMessenger[ReqBody,RspBody]
) extends CCnxn[
  RMessenger[ReqBody,RspBody],
  CnxnLabel[String,String],
  RMessenger[ReqBody,RspBody]
]( src, label, trgt )
