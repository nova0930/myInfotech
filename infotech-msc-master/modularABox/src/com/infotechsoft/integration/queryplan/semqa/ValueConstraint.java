package com.infotechsoft.integration.queryplan.semqa;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;
import com.infotechsoft.integration.queryplan.semqa.utils.ValueConstraintUtils;



public class ValueConstraint implements GraphPattern {
	private Expr expr;

	public ValueConstraint(Expr topElement) {
		super();
		this.expr = topElement;
	}

	public Expr getExpr() {
		return expr;
	}

	public void setExpr(Expr topElement) {
		this.expr = topElement;
	}

	@Override
	public Set<Node_URI> getUriMentioned() {
		// TODO should fix this to return the nodes mentioned inside the expression
		return new HashSet<Node_URI>(0); // the terms of the ValueConstraint are not necessary
	}

	@Override
	public Set<Var> getVarsMentioned() {
		return expr.getVarsMentioned();
	}
	@Override
	public Set<Var> getVarsMentionedInOperands() {
		return new LinkedHashSet<Var>(); // no operands in a value constraint, return an empty set to avoid NPE problems
	}
	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof ValueConstraint) &&
				(this.expr.equals(((ValueConstraint) obj).getExpr()))
				);
	}

	@Override
	public int hashCode() {
		// use method from Effective Java (2001)
		int result = QAOperation.SEED;
		result = QAOperation.PRIME_NUMBER * result + expr.hashCode();
		return result;
	}
	@Override
	public String toString() {
		return expr.toString();
	}
	
	@Override
	public GraphPattern copy() throws SemQAException {
		Expr newExpr = ValueConstraintUtils.deepCopyExpr(expr); // fixes a bug in the E_OneOf.deepCopy method
		return new ValueConstraint(newExpr); 
	}

	@Override
	public boolean contains(GraphPattern gp) {
		// a value constraint cannot contain any graph pattern
		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new ValueConstraint(this.expr);
	}
}
