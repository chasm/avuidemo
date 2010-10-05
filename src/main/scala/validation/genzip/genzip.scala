// -*- mode: Scala;-*- 
// Filename:    genzip.scala 
// Authors:     lgm                                                    
// Creation:    Sat May 29 23:33:57 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.validation.genzip

trait Zipper[R,M[_],T,D] {
  def term : T
}

class DCZipper[R,M[_],T,D](
  override val term : T,
  val traversal : CC[R,M,(Option[T],D)] => CC[R,M,Zipper[R,M,T,D]]
) extends Zipper[R,M,T,D]

class ZipDone[R,M[_],T,D](
  override val term : T
) extends Zipper[R,M,T,D]

trait ZipperOps[R,M[_],T,D] {
  def zipTerm(
    traversal
    : ( ( T => CC[R,M,( Option[T], D )] ), T )
      => CC[R,M,T],
    term : T
  ) : CC[R,M,Zipper[R,M,T,D]]
  def zipThrough( zipper : Zipper[R,M,T,D] ) : Unit
}
