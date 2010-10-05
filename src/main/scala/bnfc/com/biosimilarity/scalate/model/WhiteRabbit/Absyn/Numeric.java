package com.biosimilarity.scalate.model.WhiteRabbit.Absyn; // Java Package generated by the BNF Converter.

public class Numeric extends ValueExpr {
  public final Integer integer_;

  public Numeric(Integer p1) { integer_ = p1; }

  public <R,A> R accept(com.biosimilarity.scalate.model.WhiteRabbit.Absyn.ValueExpr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Numeric) {
      com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Numeric x = (com.biosimilarity.scalate.model.WhiteRabbit.Absyn.Numeric)o;
      return this.integer_.equals(x.integer_);
    }
    return false;
  }

  public int hashCode() {
    return this.integer_.hashCode();
  }


}
