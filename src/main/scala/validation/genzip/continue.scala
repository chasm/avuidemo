// -*- mode: Scala;-*- 
// Filename:    continue.scala 
// Authors:     lgm                                                    
// Creation:    Mon May 31 21:56:30 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.validation.genzip

object MonadDefns {
  type MonadLike = { 
    def map[A,B]( f : A => B )
    def flatMap[M[_],A,B]( f : A => M[B] )
    def filter[A]( p : A => Boolean )    
  }
  type MonadXFormLike = {
    def lift[ML[_],MU[_],A]( m : ML[A] ) : MU[A]
  }
}

trait StateT[S,M[_],A]{
  def runState( s : S ) : M[(A,S)]
  def evalState( s : S ) : M[A]
  def get : StateT[S,M,S]
  def put( s : S ) : StateT[S,M,Unit]
  
  def map[B]( f : A => B )
  def flatMap[B]( f : A => StateT[S,M,B] )
  def filter( p : A => Boolean )

  def lift( c : M[A] ) : StateT[S,M,A]
}

trait CC[R,M[_],A] {
  def k2P : K[R,M,A,_] => StateT[Int,M,A]
}

trait Prompt[R,A] {
  def level : Int
}

class CPrompt[R,A](
  override val level : Int
) extends Prompt[R,A] {
}

trait P[R,M[_],A] {
  self : StateT[Int,M,A] =>
    def stateT : StateT[Int,M,A]
    def runP() : M[(Int,A)] 
    def newPrompt() = {
      for( n <- get ) yield{ put( n+1 ); new CPrompt( n ) }
    }
}

trait Frame[M[_],R,A,B]{
  def a2CC : A => CC[R,M,B]
}

trait K[R,M[_],A,B]{
  def frame : Frame[M,R,A,B]
  def r : R
  def a : A
  def b : B

  def map[C]( f : A => C )
  def flatMap[C]( f : A => K[R,M,A,C] )
  def filter( p : A => Boolean )

  def lift( m : M[A] ) : CC[R,M,A]
}

trait SubK[R,M[_],A,B] extends K[R,M,A,B]{
}

trait ControlOps[R,M[_],A] {
  def appk[B]( k : K[R,M,A,B], a : A ) : StateT[Int,M,A]
  def runCC( cc : CC[R,M,A] ) : M[A]
  def newPrompt( ) : CC[R,M,Prompt[R,A]]
  def pushPrompt(
    prompt : Prompt[R,A], cc : CC[R,M,A]
  ) : CC[R,M,A]
  def letSubK[B](
    prompt : Prompt[R,B],
    subk : SubK[R,M,A,B] => CC[R,M,B]
  ) : CC[R,M,A]
  def pushSubK[B](
    prompt : Prompt[R,B],
    subk : CC[R,M,A] 
  ) : CC[R,M,B]
  def promptP( f : Prompt[R,A] => CC[R,M,A] ) : CC[R,M,A]
  def shiftP[B](
    p : Prompt[R,B],
    f : (CC[R,M,A] => CC[R,M,B]) => CC[R,M,B]
  ) : CC[R,M,A]
}
