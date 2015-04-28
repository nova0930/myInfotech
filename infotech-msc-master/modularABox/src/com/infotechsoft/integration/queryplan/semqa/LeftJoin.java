package com.infotechsoft.integration.queryplan.semqa;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;
import com.infotechsoft.integration.queryplan.semqa.utils.OperationUtils;

/**
 * Auxiliary operation where matches with the first operand are required,
 * and matches with the rest of the operands are optional.
 * It has the following semantics:
 * LeftJoin(p1, p2) = Or(p1, Join(p1, p2))
 * Note that the second operand is "optional".
 * For more than two operands:
 * LeftJoin(p1, p2, p3, ... , pn) = LeftJoin(LeftJoin(...(LeftJoin(p1, p2), p3), ...), pn)
 * NOTE that this is not the same as
 * LeftJoin(p1, LeftJoin(p2, LeftJoin(p3, ..., LeftJoin(p[n-1], pn)...)))
 * ]  
 * 2011-04-04: Added a value constraint to handle filter within optional.
 * 
 * @author Patrick Shironoshita
 *
 */
public class LeftJoin extends QAMultiOp {
	
	private GraphPattern baseOperand;

	public LeftJoin(GraphPattern baseOperand, GraphPattern... operands) throws SemQAException  {
		super(QAOperators.LEFTJOIN, operands);
		if (operands.length < 1) {
			throw new SemQAException("LeftJoin must have at least one optional operand.");
		}
		for (GraphPattern operand : operands) {
			if (!OperationUtils.isOptionalOp(operand)) {
				throw new SemQAException("All LeftJoin operands except the base must be optionals.");
			}
		}
		this.baseOperand = baseOperand;
		refreshBaseVarsAndTerms(); // must now also insert the terms and variables from the base (terms and vars from other operands inserted in super)
	}
	public LeftJoin(GraphPattern baseOperand, Set<? extends GraphPattern> operands) throws SemQAException {
		super(QAOperators.LEFTJOIN, operands);
		if (operands.size() < 1) {
			throw new SemQAException("LeftJoin must have at least one optional operand.");
		}
		for (GraphPattern operand : operands) {
			if (!OperationUtils.isOptionalOp(operand)) {
				throw new SemQAException("All LeftJoin operands except the base must be optionals.");
			}
		}
		this.baseOperand = baseOperand;
		refreshBaseVarsAndTerms(); // must now also insert the terms and variables from the base (terms and vars from other operands inserted in super)
	}
	public GraphPattern getBaseOperand() {
		//for LeftJoins, the first operand is important - it is required
		return baseOperand;
	}
	public GraphPattern getFirstOptionalOperand() {
		Set<GraphPattern> ops = this.getOptionalOperands(); 
		return ops.toArray(new GraphPattern[ops.size()])[0];
	}
	public Set<GraphPattern> getOptionalOperands() {
		return super.getOperands(); // the operands are just the optional side
	}
	@Override
	public Set<GraphPattern> getOperands() {
		// overriden to return both the base and the optional operands in a single set
		// the base operand will always be the first operand (guaranteed by the LinkedSet implementation)
		Set<GraphPattern> optOps = super.getOperands();
		Set<GraphPattern> ops = new LinkedHashSet<GraphPattern>(optOps.size() + 1);
		ops.add(this.baseOperand);
		ops.addAll(optOps);
		return ops;
	}
	protected GraphPattern copyBaseOperand() throws SemQAException {
		return this.baseOperand.copy();
	}
	protected Set<GraphPattern> copyOptionalOperands() throws SemQAException {
		return super.copyOperands();
	}
	@Override
	protected Set<GraphPattern> copyOperands() throws SemQAException {
		// returns a single graphpattern with both the base and optional operands on it
		Set<GraphPattern> optOps = this.getOptionalOperands();
		Set<GraphPattern> ret = new LinkedHashSet<GraphPattern>(optOps.size() + 1);
		ret.add(this.copyBaseOperand());
		ret.addAll(this.copyOptionalOperands());
		return ret;
	}
	@Override
	protected void refreshVarsAndTerms() {
		super.refreshVarsAndTerms();
		if (this.baseOperand != null) {
			// add the terms and vars of the base operand
			terms.addAll(this.baseOperand.getUriMentioned());
			vars.addAll(this.baseOperand.getVarsMentioned());
		}
	}
	private void refreshBaseVarsAndTerms() {
		// add the terms and vars of the base operand
		terms.addAll(this.baseOperand.getUriMentioned());
		Set<Var> baseVarsMentioned = this.baseOperand.getVarsMentioned();
		varsInOps.addAll(baseVarsMentioned);
		vars.addAll(baseVarsMentioned);
		
	}
	public void setBaseOperand(GraphPattern op) {
		this.baseOperand = op;
	}
	public void setOptionalOperands(GraphPattern... ops) {
		super.setOperands(ops);
	}
	public void setOptionalOperands(Set<GraphPattern> ops) {
		super.setOperands(ops);
	}
	public void addOptionalOperand(GraphPattern op) {
		super.addOperand(op);
	}
	public void addOptionalOperands(GraphPattern... ops) {
		super.addOperands(ops);
	}
	public void addOptionalOperands(Set<GraphPattern> ops) {
		super.addOperands(ops);
	}
	@Override
	public void setOperands(GraphPattern... ops) {
		// assume the first pattern is the base, the rest are the operands
		this.baseOperand = ops[0];
		Set<GraphPattern> optOps = new LinkedHashSet<GraphPattern>(ops.length -1);
		for (int i = 1; i < ops.length; i++) { // start at 1 to not set the base operand
			optOps.add(ops[i]);
		}
		this.setOptionalOperands(optOps);
	}
	@Override
	public void setOperands(Set<GraphPattern> ops) {
		// assume the first pattern is the base, the rest are the operands
		GraphPattern[] opsArray = ops.toArray(new GraphPattern[ops.size()]); 
		setOperands(opsArray);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LeftJoin) {
			LeftJoin objLj = (LeftJoin) obj;
			// ensure that the base operands are also equal
			return super.equals(objLj) && 
					((this.baseOperand == null) ? 
							(objLj.getBaseOperand() == null) :
							this.baseOperand.equals(objLj.getBaseOperand()));
		}
		else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		// use method from Effective Java (2001)
		int result = super.hashCode();
		result = QAOperation.PRIME_NUMBER * result + baseOperand.hashCode();
		return result;
	}
	@Override
	public GraphPattern copy() throws SemQAException {
		return new LeftJoin(this.copyBaseOperand(), this.copyOptionalOperands());
		
	}
	@Override
	public boolean containsOperand(GraphPattern gp) {
//		boolean containsGp = this.getOperands().contains(gp);
		if (gp == null) {
			return false;
		}
		boolean containsAsOp = containsAsOperand(gp);
		if (gp instanceof LeftJoin) {
			// if the graph pattern is a join, we should see if the operands of the parameter
			// are a subset of the operands of this join
			if (containsAsOp) {
				return true;
			}
			else {
				LeftJoin leftJoinGp = (LeftJoin) gp;
				// the base must be the same
				boolean containsAllGps = this.baseOperand.equals(leftJoinGp.getBaseOperand());
				// and it must contain all the optional operands
				for (GraphPattern leftJoinOp : leftJoinGp.getOptionalOperands()) {
					// all operands must be contained
					containsAllGps = containsAllGps & containsAsOperand(leftJoinOp);
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
