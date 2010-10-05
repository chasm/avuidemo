package com.biosimilarity.scalate.model.WhiteRabbit;
import com.biosimilarity.scalate.model.WhiteRabbit.Absyn.*;
/** BNFC-Generated Composition Visitor
*/

public class ComposVisitor<A> implements
  com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Expression.Visitor<com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Expression,A>,
  com.biosimilarity.scalate.model.WhiteRabbit.Absyn.VariableExpr.Visitor<com.biosimilarity.scalate.model.WhiteRabbit.Absyn.VariableExpr,A>,
  com.biosimilarity.scalate.model.WhiteRabbit.Absyn.ValueExpr.Visitor<com.biosimilarity.scalate.model.WhiteRabbit.Absyn.ValueExpr,A>
{
/* Expression */
    public Expression visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Sequence p, A arg)
    {
      Expression expression_1 = p.expression_1.accept(this, arg);
      Expression expression_2 = p.expression_2.accept(this, arg);

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Sequence(expression_1, expression_2);
    }
    public Expression visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Application p, A arg)
    {
      Expression expression_1 = p.expression_1.accept(this, arg);
      Expression expression_2 = p.expression_2.accept(this, arg);

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Application(expression_1, expression_2);
    }
    public Expression visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Mention p, A arg)
    {
      VariableExpr variableexpr_ = p.variableexpr_.accept(this, arg);

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Mention(variableexpr_);
    }
    public Expression visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Value p, A arg)
    {
      ValueExpr valueexpr_ = p.valueexpr_.accept(this, arg);

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Value(valueexpr_);
    }
    public Expression visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Abstraction p, A arg)
    {
      ListVariableExpr listvariableexpr_ = new ListVariableExpr();
      for (VariableExpr x : p.listvariableexpr_) {
        listvariableexpr_.add(x.accept(this,arg));
      }
      Expression expression_ = p.expression_.accept(this, arg);

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Abstraction(listvariableexpr_, expression_);
    }
    public Expression visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Stipulation p, A arg)
    {
      VariableExpr variableexpr_ = p.variableexpr_.accept(this, arg);
      Expression expression_ = p.expression_.accept(this, arg);

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Stipulation(variableexpr_, expression_);
    }

/* VariableExpr */
    public VariableExpr visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Transcription p, A arg)
    {
      Expression expression_ = p.expression_.accept(this, arg);

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Transcription(expression_);
    }
    public VariableExpr visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.AtomLiteral p, A arg)
    {
      String ident_ = p.ident_;

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.AtomLiteral(ident_);
    }

/* ValueExpr */
    public ValueExpr visit(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Numeric p, A arg)
    {
      Integer integer_ = p.integer_;

      return new com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Numeric(integer_);
    }

}