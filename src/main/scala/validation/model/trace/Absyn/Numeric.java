package com.biosimilarity.validation.model.trace.Absyn; // Java Package generated by the BNF Converter.

public class Numeric extends ValueExpr {
  public final Integer integer_;

  public Numeric(Integer p1) { integer_ = p1; }

  public <R,A> R accept(com.biosimilarity.validation.model.trace.Absyn.ValueExpr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof com.biosimilarity.validation.model.trace.Absyn.Numeric) {
      com.biosimilarity.validation.model.trace.Absyn.Numeric x = (com.biosimilarity.validation.model.trace.Absyn.Numeric)o;
      return this.integer_.equals(x.integer_);
    }
    return false;
  }

  public int hashCode() {
    return this.integer_.hashCode();
  }


}
