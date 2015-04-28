package com.infotechsoft.integration.queryplan.semqa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;


public abstract class QAMultiOp extends QAOperation {  //QA MultiOp inherit the methods from ancestor and also can override the methods inside the methods
	private QAOperators operator;
	private Set<GraphPattern> operands = new LinkedHashSet<GraphPattern>(); // make it a set to ensure uniqueness

	public QAMultiOp(QAOperators operator, GraphPattern... operands) {
		super();
		this.operator = operator;
		List<GraphPattern> opList = Arrays.asList(operands);
		this.operands.addAll(opList);;
		refreshVarsAndTerms();
	}
	public QAMultiOp(QAOperators operator, Set<? extends GraphPattern> operands) {
		super();
		this.operator = operator;
		this.operands.addAll(operands);;
		refreshVarsAndTerms();
	}
	public Set<GraphPattern> getOperands() {
		return operands;
	}

	public void setOperands(GraphPattern... ops) {
		this.operands.clear();
		operands.addAll(Arrays.asList(ops));
		refreshVarsAndTerms();
	}

	public void setOperands(Set<GraphPattern> ops) {
		this.operands.clear();
		operands.addAll(ops);
		refreshVarsAndTerms();
	}

	public QAOperators getOperator() {
		return operator;
	}
	
	public void addOperand(GraphPattern op) {
		this.operands.add(op);
		refreshVarsAndTerms();
	}
	public void addOperands(GraphPattern... ops) {
		operands.addAll(Arrays.asList(ops));
		refreshVarsAndTerms();
	}
	public void addOperands(Set<GraphPattern> ops) {
		operands.addAll(ops);
		refreshVarsAndTerms();
	}
	protected void refreshVarsAndTerms() {
		terms.clear();
		for (GraphPattern gp : operands ) {
			terms.addAll(gp.getUriMentioned());
		}
		vars.clear();
		for (GraphPattern gp : operands ) {
			varsInOps.addAll(gp.getVarsMentionedInOperands());
			vars.addAll(gp.getVarsMentioned());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof QAMultiOp)) {
			return false;
		}
		QAMultiOp multiobj = (QAMultiOp) obj;
		boolean foundAllOps = true;
		// check that all operands are in the multiobj...
		for (GraphPattern ops : getOperands()) {
			boolean foundop = false;
			for (GraphPattern objops : multiobj.getOperands()) {
				if (ops.equals(objops)) {
					foundop = true;
					break;
				}
			}
			if (!foundop) {
				foundAllOps = false;
				break;
			}
		}
		// and also check that all multiobj operands are in this
		boolean foundAllObjOps = true;
		for (GraphPattern objops : multiobj.getOperands()) {
			boolean foundop = false;
			for (GraphPattern ops : getOperands()) {
				if (ops.equals(objops)) {
					foundop = true;
					continue;
				}
			}
			if (!foundop) {
				foundAllObjOps = false;
				break;
			}
		}
		return (this.operator.equals(multiobj.getOperator()) && 
				foundAllOps && foundAllObjOps);
	}
	@Override
	public int hashCode() {
		// use method from Effective Java (2001)
		int result = QAOperation.SEED;
		result = QAOperation.PRIME_NUMBER * result + operator.hashCode();
		for (GraphPattern ops : operands) {
			result = QAOperation.PRIME_NUMBER * result + ops.hashCode();
		}
		return result;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.operator.toString());
		sb.append("(");
		boolean flagfirst = true;
		for (GraphPattern gp : this.getOperands()) {
			if (flagfirst) {
				flagfirst = false;
			}
			else {
				sb.append(", ");
			}
			sb.append(gp.toString());
		}
		sb.append(")");
		return sb.toString();
	}
	protected Set<GraphPattern> copyOperands() throws SemQAException {
		// creates a copy of the operand set - needed to clone/copy the operation itself
		Set<GraphPattern> ret = new LinkedHashSet<GraphPattern>(operands.size());
		//deep copy...
		for (GraphPattern gp : operands) {
		    ret.add(gp.copy()) ;
		}
		return ret;
	}
	@Override
	public boolean contains(GraphPattern gp) {
		if (this.operands.size() == 0) {
			return (gp == null);
		}
		else {
			boolean ret = false;
			for (GraphPattern op : this.getOperands()) {
				if (op.equals(gp) || op.contains(gp)) {
					// set to true if one of the operations is the parameter,
					// or if one of the operations contains it
					ret = true;
					break;
				}
			}
			// if it gets out of the loop without setting the return value, it is not contained
			return ret;
		}
	}
	protected boolean containsAsOperand(GraphPattern gp) {
		if (gp == null) {
			return false;
		}
		boolean containsGp = false;
		Set<GraphPattern> ops = this.getOperands();
		for (GraphPattern op : ops) {
			if (gp.equals(op)) {
				containsGp = true;
				break;
			}
		}
		return containsGp;
	}
	/**
	 * This method returns true only if the parameter is one of the operands of this operation.
	 * @param gp
	 * @return
	 */
	abstract public boolean containsOperand(GraphPattern gp);
}
