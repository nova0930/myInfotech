package com.infotechsoft.integration.queryplan.semqa;

import java.util.Set;

import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

public class Join extends QAMultiOp {

	public Join(GraphPattern... operands) {
		super(QAOperators.JOIN, operands);
	}
	public Join(Set<? extends GraphPattern> operands) {
		super(QAOperators.JOIN, operands);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Join) {
			return super.equals(obj);
		}
		else {
			return false;
		}
	}
	@Override
	public GraphPattern copy() throws SemQAException {
		return new Join(this.copyOperands());
		
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
		if (gp instanceof Join) {
			// if the graph pattern is a join, we should see if the operands of the parameter
			// are a subset of the operands of this join
			if (containsAsOp) {
				return true;
			}
			else {
				Join joinGp = (Join) gp;
				boolean containsAllGps = true;
				for (GraphPattern joinOp : joinGp.getOperands()) {
					// all operands must be contained
					containsAllGps = containsAllGps & containsAsOperand(joinOp);
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
