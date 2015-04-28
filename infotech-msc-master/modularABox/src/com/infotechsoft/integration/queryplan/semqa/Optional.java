package com.infotechsoft.integration.queryplan.semqa;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;
import com.infotechsoft.integration.queryplan.semqa.utils.OperationUtils;

public class Optional implements GraphPattern {
	private GraphPattern operand;
	private ValueConstraint constraint;
	protected Set<Node_URI> terms = new LinkedHashSet<Node_URI>();
	protected Set<Var> vars = new LinkedHashSet<Var>();
	protected Set<Var> varsInOps = new LinkedHashSet<Var>();

	public Optional(GraphPattern graphPattern, ValueConstraint constraint) {
		super();
		this.operand = graphPattern;
		this.constraint = constraint;
		refreshVarsAndTerms();
	}

	public Optional(GraphPattern graphPattern) {
		this(graphPattern, null);
	}

	public GraphPattern getOperand() {
		return operand;
	}

	public void setOperand(GraphPattern graphPattern) {
		this.operand = graphPattern;
		refreshVarsAndTerms();
	}

	public ValueConstraint getConstraint() {
		return constraint;
	}
	public void setConstraint(ValueConstraint constraint) {
		this.constraint = constraint;
		refreshVarsAndTerms();
	}

	protected void refreshVarsAndTerms() {
		terms.clear();
		terms.addAll(operand.getUriMentioned());
		vars.clear();
		varsInOps.addAll(operand.getVarsMentioned()); // only operand added to VarsInOps
		vars.addAll(varsInOps);
		if (constraint != null) {
			terms.addAll(constraint.getUriMentioned());
			vars.addAll(constraint.getVarsMentioned());
		}
	}

	@Override
	public Set<Node_URI> getUriMentioned() {
		return terms;
	}

	@Override
	public Set<Var> getVarsMentioned() {
		return vars;
	}
	@Override
	public Set<Var> getVarsMentionedInOperands() {
		return varsInOps;
	}
	@Override
	public boolean contains(GraphPattern gp) {
		if (gp == null) return false;
		if (gp.equals(operand)) return true;
		if (gp.equals(constraint)) return true;
		return false;
	}

	@Override
	public GraphPattern copy() throws SemQAException {
	        //handle null constraint...
		return new Optional(operand.copy(), (ValueConstraint) (constraint == null ? null : constraint.copy()));
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Optional) {
			Optional objOpt = (Optional) obj;
			// ensure that both elements are equal
			return  
					((this.operand == null) ? 
							(objOpt.getOperand() == null) :
							this.operand.equals(objOpt.getOperand()))  &&
					((this.constraint == null) ? 
							(objOpt.getConstraint() == null) :
							this.constraint.equals(objOpt.getConstraint()));
		}
		else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		// use method from Effective Java (2001)
		int result = QAOperation.SEED;
		result = QAOperation.PRIME_NUMBER * result + operand.hashCode();
		if(constraint != null) {
			result = QAOperation.PRIME_NUMBER * result + constraint.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("OPT(");
		sb.append(operand.toString());
		if (constraint != null) {
			sb.append(", ");
			sb.append(constraint.toString());
		}
		sb.append(')');
		return sb.toString();
	}
	
	public static Optional createOptional(GraphPattern gp, ValueConstraint vc) {
		if (OperationUtils.isOptionalOp(gp)) {
			Optional opt = (Optional) gp;
			GraphPattern gpin = opt.getOperand();
			ValueConstraint vcin = opt.getConstraint();
			ValueConstraint vcout = vc; // by default, just use the incoming vc
			if (vcin != null) {
				Expr exp = vc.getExpr(); 
				Expr expin = vcin.getExpr();
				Expr exprnew = new E_LogicalAnd(exp, expin); // make an and of the two value constraints coming in
				vcout = new ValueConstraint(exprnew); // create a new value constraint
			}
			return new Optional(gpin, vcout);
		}
		else {
			return new Optional(gp, vc);
		}
	}

}
