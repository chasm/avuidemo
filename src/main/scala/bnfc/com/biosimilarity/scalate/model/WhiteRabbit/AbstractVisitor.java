package com.biosimilarity.scalate.model.WhiteRabbit;
import com.biosimilarity.scalate.model.WhiteRabbit.Absyn.*;
/** BNFC-Generated Abstract Visitor */
public class AbstractVisitor<R,A> implements AllVisitor<R,A> {
/* Expression */
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Sequence p, A arg) { return visitDefault(p, arg); }

    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Application p, A arg) { return visitDefault(p, arg); }

    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Mention p, A arg) { return visitDefault(p, arg); }
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Value p, A arg) { return visitDefault(p, arg); }
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Abstraction p, A arg) { return visitDefault(p, arg); }
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Stipulation p, A arg) { return visitDefault(p, arg); }

    public R visitDefault(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Expression p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* VariableExpr */
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Transcription p, A arg) { return visitDefault(p, arg); }
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.AtomLiteral p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.VariableExpr p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* ValueExpr */
    public R visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Numeric p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.ValueExpr p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }

}
