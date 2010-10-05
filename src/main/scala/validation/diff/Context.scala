// -*- mode: Scala;-*- 
// Filename:    Context.scala 
// Authors:     lgm                                                    
// Creation:    Thu Mar  4 20:07:13 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.differential

case class Context[Name, NSeq <: NmSeq[Name]](
  override val self : RegularType[Name,NSeq]
)
extends RegularType[Name, NSeq] with Proxy {
  override def support = self.support
}

trait Contextual[Name, NSeq <: NmSeq[Name]]
extends Differential[Name,NSeq]  {
  def holePunch( support : NSeq )(
    x : Name, regularType : RegularType[Name,NSeq]
  ) : Context[Name,NSeq] = {
    fresh match {
      case None => throw new Exception( "out of names" )
      case Some( cX ) => {
	val fixRT =
	  RegularFixPt[Name,NSeq](
	    (fresh match {
	      case None =>
		throw new Exception( "out of names" )
	      case Some( fX ) => fX
	    }),
	    regularType,
	    support
	  )
	Context[Name,NSeq](
	  RegularFixPt[Name,NSeq](
	    cX,
	    RegularSum[Name,NSeq](
	      List(
		RegularUnity[Name,NSeq]( support ),
		RegularProduct[Name,NSeq](
		  List(
		    RegularFPEnv[Name,NSeq](
		      x,
		      partial( x, regularType ),
		      fixRT,
		      support
		    ),
		    RegularMention[Name,NSeq](
		      cX,
		      support
		    )
		  ),
		  support
		)
	      ),
	      support
	    ),
	    support
	  )
	)
      }
    }
  }
}

