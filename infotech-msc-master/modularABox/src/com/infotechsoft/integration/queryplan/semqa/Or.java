package com.infotechsoft.integration.queryplan.semqa;

import java.util.Set;

import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

public class Or extends QAMultiOp {

	public Or(GraphPattern... operands) {
		super(QAOperators.OR, operands);
	}
	public Or(Set<? extends GraphPattern> operands) {
		super(QAOperators.OR, operands);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Or) {
			return super.equals(obj);
		}
		else {
			return false;
		}
	}
	@Override
	public GraphPattern copy() throws SemQAException {
		return new Or(this.copyOperands());
		
	}
	@Override
	public boolean containsOperand(GraphPattern gp) {
//		boolean containsGp = this.getOperands().contains(gp);
		// for some reason, the contains method is not using the equals method - maybe that is how it always works
		// use a loop and the equals method instead
		if (gp == null) {
			return false;
		}
		boolean containsAsOp = containsAsOperand(gp);
		if (gp instanceof Or) {
			// if the graph pattern is a join, we should see if the operands of the parameter
			// are a subset of the operands of this join
			if (containsAsOp) {
				return true;
			}
			else {
				Or orGp = (Or) gp;
				boolean containsAllGps = true;
				for (GraphPattern orOp : orGp.getOperands()) {
					// all operands must be contained
					containsAllGps = containsAllGps & containsAsOperand(orOp);
				}
				return containsAllGps;
			}
		}
		else {
			// otherwise, just see if the graph pattern is one of the operands
			return containsAsOp;
		}
	}
}
