package com.infotechsoft.integration.queryplan.semqa;

import com.infotechsoft.integration.queryplan.semqa.ValueConstraint;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

public class Filter extends QAOperation {

	protected QAOperators operator = null;
	protected GraphPattern operand = null;
	protected GraphPattern constraint = null;
	
	public Filter(GraphPattern operand1, ValueConstraint operand2) {
//		super(QAOperators.FILTER, operand1, operand2); 
		super();
		this.operator = QAOperators.FILTER;
		this.operand = operand1;
		this.constraint = operand2;
		refreshVarsAndTerms();
	}
	protected void refreshVarsAndTerms() {
		terms.clear();
		terms.addAll(operand.getUriMentioned());
		terms.addAll(constraint.getUriMentioned());
		vars.clear();
		varsInOps.addAll(operand.getVarsMentioned()); // only operand added to VarsInOps
		vars.addAll(varsInOps);
		if (constraint != null) {
			terms.addAll(constraint.getUriMentioned());
			vars.addAll(constraint.getVarsMentioned());
		}
		}
		
	public QAOperators getOperator() {
		return operator;
	}
	public void setOperator(QAOperators operator) {
		this.operator = operator;
	}
	public GraphPattern getOperand() {
		return operand;
	}
	public void setOperand(GraphPattern operand1) {
		this.operand = operand1;
		refreshVarsAndTerms();
	}
	public ValueConstraint getConstraint() {
		return (ValueConstraint) constraint;
	}

	public void setConstraint(ValueConstraint operand2) {
		this.constraint = operand2;
		this.refreshVarsAndTerms();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (obj instanceof Filter) {
			return ( 
				this.operator.equals(((Filter) obj).getOperator()) &&
					(this.operand.equals(((Filter) obj).getOperand())) &&
					(this.constraint.equals(((Filter) obj).getConstraint()))
				);
		}
		else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		// use method from Effective Java (2001)
		int result = QAOperation.SEED;
		result = QAOperation.PRIME_NUMBER * result + operator.hashCode();
		result = QAOperation.PRIME_NUMBER * result + operand.hashCode();
		result = QAOperation.PRIME_NUMBER * result + constraint.hashCode();
		return result;
	}

	@Override
	public GraphPattern copy() throws SemQAException {
		return new Filter(operand.copy(), (ValueConstraint) constraint.copy());
	}
	@Override
	public String toString() {
		return(this.operator.toString() + "( " + this.operand.toString() + ", " + this.constraint.toString()+ " )");
	}
	@Override
	public boolean contains(GraphPattern gp) {
		// a graph pattern can only be contained within the operand side
		if (this.operand == null) {
			return (gp == null);
		}
		else if (this.operand.equals(gp)) {
			return true;
		}
		else {
			return this.operand.contains(gp);
		}
	}

}
