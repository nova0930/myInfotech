package com.infotechsoft.integration.queryplan.semqa.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotOneOf;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.E_OneOfBase;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;

import com.infotechsoft.integration.queryplan.semqa.Filter;
import com.infotechsoft.integration.queryplan.semqa.Join;
import com.infotechsoft.integration.queryplan.semqa.LeftJoin;
import com.infotechsoft.integration.queryplan.semqa.Optional;
import com.infotechsoft.integration.queryplan.semqa.Or;
import com.infotechsoft.integration.queryplan.semqa.GraphPattern;
import com.infotechsoft.integration.queryplan.semqa.QAMultiOp;
import com.infotechsoft.integration.queryplan.semqa.QAQuery;
import com.infotechsoft.integration.queryplan.semqa.ValueConstraint;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

/**
 * Represents a singleton.
 */

/**
 * @author patrick
 *
 */
public class ValueConstraintUtils {

	/**
	 * Holds singleton instance
	 */
	private static ValueConstraintUtils instance;

	/**
	 * prevents instantiation
	 */
	private ValueConstraintUtils() {
		// prevent creation
	}

	/**
	 * Returns the singleton instance.
	 @return	the singleton instance
	 */
	static public ValueConstraintUtils getInstance() {
		if (instance == null) {
			instance = new ValueConstraintUtils();
		}
		return instance;
	}
	private static Expr pushNot(E_LogicalNot uop) {
		Expr ret;
		Expr ins = uop.getArg();
		if (ExprUtils.isLogicalBinary(ins)) {
			ExprFunction2 bop = (ExprFunction2) ins;
			E_LogicalNot left = new E_LogicalNot(bop.getArg1());
			E_LogicalNot right = new E_LogicalNot(bop.getArg2());
			if (ExprUtils.isLogicalAnd(bop)) {
				ret = new E_LogicalOr(left,right);
			}
			else { // it is a disjunction
				ret = new E_LogicalAnd(left,right);
			}
			return ret;
		}
		else if (ExprUtils.isNegation(ins)) { // which means, double negation
			ret = ((E_LogicalNot) ins).getArg(); // just eliminate the double negation
			return ret;
		}
		else if (ExprUtils.isFunction(ins)) {
			return null; // cannot push NOT inside a function
		}
		else { // just an atomic value
			return null; // cannot push NOT inside an atomic
		}
	}
		
	public static void pushAllNots(ValueConstraint vc) throws SemQAException {
		Expr top = vc.getExpr();
		Expr topnew = pushAllNots(top); // if returns a non-null, is a new top element and must be replaced
		if (topnew != null) {
			vc.setExpr(topnew);
		}
	}

	public static Expr pushAllNots(Expr vce) throws SemQAException {
		if (ExprUtils.isAtomic(vce)) {
			return null; // nothing to do for atomics
		}
		else if (ExprUtils.isFunction(vce)) {
			return null; // also, nothing to do with functions
		}
		else if (ExprUtils.isNegation(vce)) {
			Expr vcenew = pushNot((E_LogicalNot) vce);
			Expr vceant = vcenew;
			while (vcenew != null) { // loops while it still finds NOTs
				vceant = vcenew;
				vcenew = pushAllNots(vcenew);
			}
			return vceant;
		}
		else if (ExprUtils.isLogicalBinary(vce)) { // no need to push here, but should push inside each operand
			Expr arg1, arg2;
			ExprFunction2 bop = (ExprFunction2) vce;
			Expr vce1 = pushAllNots(bop.getArg1());
			Expr vce2 = pushAllNots(bop.getArg2());
			arg1 = (vce1 == null) ? bop.getArg1() : vce1; // set the first argument as either the pushed result or the same if no push 
			arg2 = (vce2 == null) ? bop.getArg2() : vce2; // and also with argument 2 
			if ((vce1 == null) && (vce2 == null)) { // if neither changed
				return null; // indicate that nothing changed
			}
			else {
				Expr ret;
				// create the same type of logical but with the new arguments with pushed elements
				// note: may wish to use reflection?
				if (ExprUtils.isLogicalAnd(vce)) {
					ret = new E_LogicalAnd(arg1, arg2);
				}
				else if (ExprUtils.isLogicalOr(vce)) {
					ret = new E_LogicalOr(arg1, arg2);
				}
				else {
					throw new SemQAException("pushAllNots(Expr): unknown logical expression"); // just for checking
				}
				return ret;
			}
		}
		else {
			throw new SemQAException("pushAllNots(Expr): unknown operation");
		}
	}
	/**
	 * @param f
	 * @return the distributed filter, or null if no distribution was made
	 * @throws SemQAException 
	 */
	private static GraphPattern distribFilter(Filter f) throws SemQAException {
		GraphPattern ret;
		GraphPattern op1 = f.getOperand();
		ValueConstraint vc = f.getConstraint();
		pushAllNots(vc); // first, ensure that all nots are pushed to the bottom in the value constraint
		Expr top = vc.getExpr();
//		if ((ExprUtils.isAtomic(top)) || ExprUtils.isFunction(top)) {
		if (ExprUtils.isConsistentVar(top)) {// instead of checking atomic, check if it is 'consistent', i.e., it has the same variables
			ret = null; // no filter to distribute
		}
		else {
			if (ExprUtils.isNegation(top)) {
				Expr arg = ((E_LogicalNot) top).getArg();
//				if (ExprUtils.isAtomic(arg) || ExprUtils.isFunction(arg)) { // no need for further pushing if the top operand is a negation over an atomic or over a function
				if (ExprUtils.isConsistentVar(top)) { // instead of checking atomic, check if it is 'consistent', i.e., it has the same variables
					ret = null;
				}
				else {
					throw new SemQAException("distribFilter(Filter): should push NOTs to leafs before distributing filters!");
				}
			}
			else { // it is a binary op
				// FIX: eliminate processing the first operand when working over a logical AND, since it is not necessary
				// Note: process the second operand first to keep consistency on AND processing from previous versions
				ExprFunction2 bop = (ExprFunction2) top;
				Expr vcop2 = bop.getArg2();
				ValueConstraint vc2 = new ValueConstraint(vcop2);
				Filter second = new Filter(op1.copy(),vc2);
				GraphPattern ressecond = distribFilter(second); // distribute the filter inside the newly created filter, recursively
 				if (ressecond == null) {
 					ressecond = second;
 				}
				Expr vcop1 = bop.getArg1();
				ValueConstraint vc1 = new ValueConstraint(vcop1);
				if (ExprUtils.isLogicalOr(bop)) {
					// distribute filter over the first operand only when faced with an Or, which returns a new graph pattern
					Filter first = new Filter(op1,vc1);
	 				GraphPattern resfirst = distribFilter(first); // distribute the filter inside the newly created filter, recursively
	 				if (resfirst == null) {
	 					resfirst = first;
	 				}
					ret = new Or(resfirst, ressecond);
				}
				else if (ExprUtils.isLogicalAnd(bop)) { // it is a conjunction
//					ret = new Join(resfirst, ressecond); // let's try, instead of creating a join, to create a filter over fileter
					Filter resdouble = new Filter(ressecond, vc1);
					ret = distribFilter(resdouble);
					if (ret == null) {
						ret = resdouble;
					}
				}
				else {
					throw new SemQAException("distribFilter(Filter): unknown logical operation");
				}
			}
		}
		return ret;
	}
	public static GraphPattern distribAllFilters(GraphPattern top) throws SemQAException {
		if (OperationUtils.isFilterOp(top)) {
			// first distribute inside the filter operand
			GraphPattern op1 = ((Filter) top).getOperand();
			GraphPattern ret = distribAllFilters(op1); // redistribute inside the filter
			if (ret != null) {
				((Filter) top).setOperand(ret);
			}
			// and now distribute the filter itself
			GraphPattern gp = distribFilter((Filter) top);
			if (gp == null) {
				return top; // did not distribute, just return the same
			}
			else { 
				return gp;
			}
		}
		else if (OperationUtils.isMultiOp(top)){
			QAMultiOp mop = (QAMultiOp) top;
			Set<GraphPattern> ops;
			if (OperationUtils.isLeftJoinOp(mop)) {
			  // process the base operand separately from the optionals
			  LeftJoin ljop = (LeftJoin) mop; 
			  ops = ljop.getOptionalOperands();
			  GraphPattern base = ljop.getBaseOperand();
			  GraphPattern newBase = distribAllFilters(base);
			  if (newBase != null) {
			    ljop.setBaseOperand(newBase);
			  }
			}
			else {
	      ops= mop.getOperands();
			}
			Set<GraphPattern> opsToRemove = new LinkedHashSet<GraphPattern>(ops.size());
			Set<GraphPattern> opsToAdd = new LinkedHashSet<GraphPattern>(ops.size());
			GraphPattern gpinner;
			for (GraphPattern op : ops) {
				gpinner = distribAllFilters(op); // distributes filters inside binary operand 1
				if (gpinner != null) {
					opsToRemove.add(op);
					opsToAdd.add(gpinner);
				}
			}
			ops.removeAll(opsToRemove);
			ops.addAll(opsToAdd);
			// note - the ops set is the same set obtained from the multiop, no need to set it again
			return top; // and returns the input graph pattern, since it was not changed
		}
		else if (OperationUtils.isOptionalOp(top)) {
			// just distribute the inside operand
			Optional opt = (Optional) top;
			GraphPattern inner = opt.getOperand();
			GraphPattern ret = distribAllFilters(inner);
			if (ret != null) {
				opt.setOperand(ret);
			}
			return opt;
		}
		else if (OperationUtils.isTriplePattern(top)) {
			return top;
		}
		else {
			throw new SemQAException("distribAllFilters: unknown operation, " + top);
		}
	}
	/**
	 * Resolves logical or, and, and not operations inside the filters,
	 * @param q a semQA query
	 * @throws SemQAException
	 */
	public static void distribAllFilters(QAQuery q) throws SemQAException {
		 GraphPattern top = q.getGraphPattern();
		 GraphPattern res = distribAllFilters(top);
		 if (res != top) {
			 q.setGraphPattern(res);
		 }
	}
	/**
	 * creates a value constraint as a Logical Or of the value constraint parameters
	 * @param vcs value constraints to be Ored
	 * @return
	 */
	public static ValueConstraint createLogicalOrOfVCs(ValueConstraint... vcs) {
		// first, get an array of expressions
		Expr[] exprs = new Expr[vcs.length];
		int k = 0;
		for (ValueConstraint vc : vcs) {
			exprs[k++] = vc.getExpr();
		}
		while (exprs.length > 1) {
			int newLimit = (int) Math.round((float) exprs.length / 2.0);
			Expr[] newExprs = new Expr[newLimit];
			int j = 0;
			for (int i = 0; i < (exprs.length - 1); i = i+2) { // skip 2 by 2
				newExprs[j++] = new E_LogicalOr(exprs[i], exprs[i+1]);
			}
			if (Math.floor((float) exprs.length / 2.0) != newLimit) {
				// that is, if exprs.length is odd
				newExprs[j] = exprs[exprs.length - 1];
			}
			exprs = newExprs;
		}
		return new ValueConstraint(exprs[0]);
	}
	
	/**
	 * This method circumvents a bug in ARQ 2.8.7, where E_OneOf.deepcopy results in an NPE
	 * @param in an Expr to be copied
	 * @return a complete copy or clone of the in parameter
	 */
	public static Expr deepCopyExpr(Expr in) {
		Expr out;
		if (in instanceof E_OneOfBase) {
			out = deepCopyOneOfBase((E_OneOfBase) in);
		}
                /*Since the GeneTegra only uses the E_OneOfBase expressions by itself within filters or in parent expression of type E_LogicalAnd or E_LogicalOr... 
                their deepCopy my also be changed. A better solution is required in order to account for all possible uses of the E_OneOfBase expressions. see INT-76
                */
                else if(in instanceof E_LogicalAnd){
                    out = deepCopyLogicalAnd((E_LogicalAnd)in) ;
                }
                else if(in instanceof E_LogicalOr){
                    out = deepCopyLogicalOr((E_LogicalOr)in) ;
                }
		else {
			out = in.deepCopy();
		}
		return out;
	}
	/**
	 * This method circumvents a bug in ARQ 2.8.7, where E_OneOf.deepcopy results in an NPE
	 * @param in a E_OneOf to be copied
	 * @return a complete copy or clone of the in parameter
	 */
	private static E_OneOfBase deepCopyOneOfBase(E_OneOfBase in) {
		Expr lhs = deepCopyExpr(in.getLHS());
		ExprList list = in.getRHS();
		List<Expr> listOfExpr = list.getList();
		List<Expr> newListOfExprs = new ArrayList<Expr>(listOfExpr.size());
		for (Expr expr : listOfExpr) {
			newListOfExprs.add(deepCopyExpr(expr));
		}
		E_OneOfBase ret;
		if (in instanceof E_OneOf) {
			ret = new E_OneOf(lhs, new ExprList(newListOfExprs));
		}
		else if (in instanceof E_NotOneOf) {
			ret = new E_NotOneOf(lhs, new ExprList(newListOfExprs));
		}
		else {
			throw new IllegalArgumentException("deepCopyOneOfBase: unknown operator " + in);
		}
		return ret;
	}

        private static E_LogicalAnd deepCopyLogicalAnd(E_LogicalAnd in) {
                Expr left = deepCopyExpr(in.getArg1()) ;
                Expr right = deepCopyExpr(in.getArg2()) ;
                return new E_LogicalAnd(left, right) ;
        }
        
        private static E_LogicalOr deepCopyLogicalOr(E_LogicalOr in) {
                Expr left = deepCopyExpr(in.getArg1()) ;
                Expr right = deepCopyExpr(in.getArg2()) ;
                return new E_LogicalOr(left, right) ;
        }
}