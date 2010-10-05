package com.biosimilarity.validation.model.trace.Absyn; // Java Package generated by the BNF Converter.

public class Abstraction extends Expression {
  public final ListVariableExpr listvariableexpr_;
  public final Expression expression_;

  public Abstraction(ListVariableExpr p1, Expression p2) { listvariableexpr_ = p1; expression_ = p2; }

  public <R,A> R accept(com.biosimilarity.validation.model.trace.Absyn.Expression.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof com.biosimilarity.validation.model.trace.Absyn.Abstraction) {
      com.biosimilarity.validation.model.trace.Absyn.Abstraction x = (com.biosimilarity.validation.model.trace.Absyn.Abstraction)o;
      return this.listvariableexpr_.equals(x.listvariableexpr_) && this.expression_.equals(x.expression_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.listvariableexpr_.hashCode())+this.expression_.hashCode();
  }


}
