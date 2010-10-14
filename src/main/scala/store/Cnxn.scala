// -*- mode: Scala;-*- 
// Filename:    PathStore.scala 
// Authors:     lgm                                                    
// Creation:    Wed Aug 25 16:34:16 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.lift.model.store

import com.biosimilarity.lift.model.zipper._
import scala.collection.SeqProxy
import java.net.URI

trait CnxnLabel[Namespace,Tag]
extends Tree[Tag] with SeqProxy[Either[Tag,CnxnLabel[Namespace,Tag]]] {
  def up( tOrC : Either[Tag,CnxnLabel[Namespace,Tag]] ): List[Tag] = {
    tOrC match {
      case Left( t ) => List( t )
      case Right( CnxnBranch( ns, lbls ) ) => {
      	( List[Tag]() /: lbls.flatMap( _.self ) )(
      	  {
      	    ( acc, e ) => {
      	      acc ++ up( e )
      	    }
      	  }
      	)
      }
    }
  }

  def atoms : Seq[Tag] = { this flatMap( up ) }
}

class CnxnLeaf[Namespace,Tag]( val tag : Tag )
extends TreeItem[Tag]( tag ) with CnxnLabel[Namespace,Tag] {
  override def self = List( Left( tag ) )
}

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
) extends TreeSection[Tag]( labels ) with CnxnLabel[Namespace,Tag] {
  override def self = labels.map( Right( _ ) )
}

object CnxnBranch {
  def unapply[Namespace,Tag](
    cnxnBranch : CnxnBranch[Namespace,Tag]
  ) : Option[( Namespace, List[CnxnLabel[Namespace,Tag]] )] = {
    Some( ( cnxnBranch.nameSpace, cnxnBranch.labels ) )
  }
}

trait CnxnCtxtLabel[Namespace,Var,Tag]
extends CnxnLabel[Either[Namespace,Var],Either[Tag,Var]] {
  type U =
    Either[
      Either[Tag,Var],
      CnxnLabel[Either[Namespace,Var],Either[Tag,Var]]
    ]
  override def up( tOrC : U )
   : List[Either[Tag,Var]] = {
    tOrC match {
      case Left( t ) => List( t )
      case Right( CnxnCtxtLeaf( tOrV ) ) => List( tOrV )
      case Right( CnxnCtxtBranch( ns, lbls ) ) => {
	val selves : List[U] = lbls.flatMap( _.self )
	( List[Either[Tag,Var]]() /: selves )(
	  {
	    ( acc, e ) => {
	      acc ++ up( e )
	    }
	  }
	)
      }
    }
  }  

  def names : Seq[Either[Tag,Var]] = {
    atoms filter(
      {
	( ctxtLbl ) => {
	  ctxtLbl match { 
	    case Left( _ ) => false
	    case Right( _ ) => true
	  }
	}
      }
    )
  }
}

class CnxnCtxtLeaf[Namespace,Var,Tag]( val tag : Either[Tag,Var] )
extends CnxnCtxtLabel[Namespace,Var,Tag] {
  override def self = List( Left( tag ) )
}

object CnxnCtxtLeaf {
  def unapply[Namespace,Var,Tag](
    cnxnCtxtLeaf : CnxnCtxtLeaf[Namespace,Var,Tag]
  ) : Option[( Either[Tag,Var] )] = {
    Some( ( cnxnCtxtLeaf.tag ) )
  }
}

class CnxnCtxtBranch[Namespace,Var,Tag](
  val nameSpace : Namespace,
  val labels : List[CnxnCtxtLabel[Namespace,Var,Tag]]
) extends CnxnCtxtLabel[Namespace,Var,Tag] {
  override def self = labels.map( Right( _ ) )
}

object CnxnCtxtBranch {
  def unapply[Namespace,Var,Tag](
    cnxnCtxtBranch : CnxnCtxtBranch[Namespace,Var,Tag]
  ) : Option[( Namespace, List[CnxnCtxtLabel[Namespace,Var,Tag]] )] = {
    Some( ( cnxnCtxtBranch.nameSpace, cnxnCtxtBranch.labels ) )
  }
}

trait CnxnCtxtInjector[Namespace,Var,Tag] {
  def injectLabel( cLabel : CnxnLabel[Namespace,Tag] )
  : CnxnCtxtLabel[Namespace,Var,Tag] = {
    cLabel match {
      case cLeaf : CnxnLeaf[Namespace,Tag] =>
	inject( cLeaf )
      case cBranch : CnxnBranch[Namespace,Tag] =>
	inject( cBranch )
    }
  }
  def inject( cLabel : CnxnLeaf[Namespace,Tag] )
  : CnxnCtxtLabel[Namespace,Var,Tag] = {
    new CnxnCtxtLeaf( Left( cLabel.tag ) )
  }
  def inject( cLabel : CnxnBranch[Namespace,Tag] )
  : CnxnCtxtLabel[Namespace,Var,Tag] = {
    new CnxnCtxtBranch(
      cLabel.nameSpace,
      cLabel.labels.map( injectLabel( _ ) )
    )
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

