package com.biosimilarity.scalate.model.WhiteRabbit.Absyn; // Java Package generated by the BNF Converter.

public abstract class Expression implements java.io.Serializable {
  public abstract <R,A> R accept(Expression.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Sequence p, A arg);
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Application p, A arg);
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Mention p, A arg);
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Value p, A arg);
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Abstraction p, A arg);
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Stipulation p, A arg);

  }

}
