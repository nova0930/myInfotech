package com.infotechsoft.integration.queryplan.semqa.utils;

import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Datatype;
import com.hp.hpl.jena.sparql.expr.E_Divide;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_IsBlank;
import com.hp.hpl.jena.sparql.expr.E_IsIRI;
import com.hp.hpl.jena.sparql.expr.E_IsLiteral;
import com.hp.hpl.jena.sparql.expr.E_IsURI;
import com.hp.hpl.jena.sparql.expr.E_Lang;
import com.hp.hpl.jena.sparql.expr.E_LangMatches;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_SameTerm;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.E_UnaryMinus;
import com.hp.hpl.jena.sparql.expr.E_UnaryPlus;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

public class ExprUtils {

	public static boolean isNegation(Expr op) {
		return (op instanceof E_LogicalNot);
	}
	public static boolean isLogicalAnd(Expr op) {
		return (op instanceof E_LogicalAnd);
	}
	public static boolean isLogicalOr(Expr op) {
		return (op instanceof E_LogicalOr);
	}
	public static boolean isLogicalBinary(Expr op) {
		return isLogicalAnd(op) || isLogicalOr(op);
	}
	public static boolean isLogical(Expr op) {
		return isNegation(op) || isLogicalBinary(op);
	}
	public static boolean isFunction(Expr op) { // returns ONLY non-logical functions, i.e., not LogicalNot, LogicalAnd, or LogicalOr
		return (op.isFunction() && !(isNegation(op) || isLogicalAnd(op) || isLogicalOr(op)));
	}
	public static boolean isAtomic(Expr op) { // returns ONLY non-logical functions, i.e., not LogicalNot, LogicalAnd, or LogicalOr
		return (!isFunction(op) && !isLogical(op));
	}
	public static boolean isEquals(Expr op) {
		return (op instanceof E_Equals);
	}
	public static boolean isExprVar(Expr op) {
		return (op instanceof ExprVar);
	}
	public static boolean isNotBound(Expr top) {
		if (isNegation(top)) { // it is a NOT
			Expr next = ((E_LogicalNot) top).getArg();
			boolean ret = (next instanceof E_Bound);
			return ret;
		}
		else {
			return false;
		}
	}
	public static boolean haveSameVars(Expr e1, Expr e2) {
		Set<Var> vars1 = e1.getVarsMentioned();
		Set<Var> vars2 = e2.getVarsMentioned();
		// both variable sets must be the same
		if (vars1.size() != vars2.size()) {
			return false; // must be same size
		}
		boolean sameVars = true;
		for (Var var1 : vars1) {
			if (var1 == null) {
				sameVars = false;
				break;
			}
			boolean found = false;
			for (Var var2 : vars2) {
				if (var2 == null) {
					found = false;
					break;
				}
				if (var1.equals(var2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				sameVars = false;
				break;
			}
		}
		return sameVars;
	}
	/**
	 * For any expression, it returns true if all expression arguments are intrinsic themselves
	 * and if all arguments have the same variables
	 * @param e
	 * @return true if arguments have same variables
	 */
	public static boolean isConsistentVar(Expr e) {
		if (isLogical(e)) {
			if (isNegation(e)) {
				E_LogicalNot neg = (E_LogicalNot) e;
				return isConsistentVar(neg.getArg());
			}
			else {
				ExprFunction2 func = (ExprFunction2) e;
				Expr arg1 = func.getArg1();
				Expr arg2 = func.getArg2();
				return haveSameVars(arg1, arg2) && isConsistentVar(arg1) && isConsistentVar(arg2);
			}
		}
		else {
			// if not logical, it is consistent in itself
			return true;
		}
	}
	public static boolean areContradictory(Expr e1, Expr e2) {
		if ((e1 instanceof E_Equals) && (e2 instanceof E_Equals)) {
			// only in the case where both expressions are equalities
			E_Equals e1eq = (E_Equals) e1;
			Expr e1arg1 = e1eq.getArg1();
			Expr e1arg2 = e1eq.getArg2();
			E_Equals e2eq = (E_Equals) e2;
			Expr e2arg1 = e2eq.getArg1();
			Expr e2arg2 = e2eq.getArg2();
			
			NodeValue n1 = null;
			NodeValue n2 = null;
			
			if ((e1arg1 instanceof ExprVar) && (e1arg2 instanceof NodeValue) && !(e1arg2 instanceof NodeValueNode)) {
				n1 = (NodeValue) e1arg2; 
				if (e1arg1.equals(e2arg1) && (e2arg2 instanceof NodeValue) && !(e2arg2 instanceof NodeValueNode)) {
					// both equalities have the same variable, and have some node value as the other argument
					n2 = (NodeValue) e2arg2;
				}
				else if (e1arg1.equals(e2arg2) && (e2arg1 instanceof NodeValue) && !(e2arg1 instanceof NodeValueNode)) {
					n2 = (NodeValue) e2arg1;
				}
			}
			else if ((e1arg2 instanceof ExprVar) && (e1arg1 instanceof NodeValue) && !(e1arg1 instanceof NodeValueNode)) {
				n1 = (NodeValue) e1arg1; 
				if (e1arg2.equals(e2arg1) && (e2arg2 instanceof NodeValue) && !(e2arg2 instanceof NodeValueNode)) {
					// both equalities have the same variable, and have some node value as the other argument
					n2 = (NodeValue) e2arg2;
				}
				else if (e1arg2.equals(e2arg2) && (e2arg1 instanceof NodeValue) && !(e2arg1 instanceof NodeValueNode)) {
					n2 = (NodeValue) e2arg1;
				}
			}
			if ((n1 != null) && (n2 != null)) {
				try {
					boolean areNotSame = NodeValue.notSameAs(n1, n2);
					return areNotSame; // contradictory iff node values are not the same
				} catch (ExprEvalException e) {
					// unknown if they are or are not the same - cannot know if contradictory
					return false;
				}
				
			}
		}
		return false;
	}
	
	private static E_Function copyFunction(E_Function oldExpr) {
		List<Expr> oldArgs = oldExpr.getArgs();
		ExprList newArgs = new ExprList();
		for (Expr old : oldArgs) {
			newArgs.add(old);
		}
		return new E_Function(oldExpr.getFunctionIRI(), newArgs);
	}
	public static Expr generateNewExpr(Expr oldExpr, Expr... exprs) throws SemQAException {
		if ((oldExpr instanceof ExprFunction1) && (exprs.length != 1)) {
			throw new SemQAException("generateNewExpr: illegal number of arguments to create replacement of " + oldExpr);
		}
		if ((oldExpr instanceof ExprFunction2) && (exprs.length != 2)) {
			throw new SemQAException("generateNewExpr: illegal number of arguments to create replacement of " + oldExpr);
		}
		if (oldExpr instanceof E_Equals) {
			return new E_Equals(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_NotEquals) {
			return new E_NotEquals(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_GreaterThanOrEqual) {
			return new E_GreaterThanOrEqual(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_LessThanOrEqual) {
			return new E_LessThanOrEqual(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_GreaterThan) {
			return new E_GreaterThan(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_LessThan) {
			return new E_LessThan(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_LangMatches) {
			return new E_LangMatches(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_Regex) {
			return new E_Regex(exprs[0], exprs[1], exprs.length == 3 ? exprs[2] : null);
		}
		else if (oldExpr instanceof E_SameTerm) {
			return new E_SameTerm(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_Bound) {
			return new E_Bound(exprs[0]);
		}
		else if (oldExpr instanceof E_LogicalAnd) {
			return new E_LogicalAnd(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_LogicalOr) {
			return new E_LogicalOr(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_LogicalNot) {
			return new E_LogicalNot(exprs[0]);
		}
		else if (oldExpr instanceof E_Multiply) {
			return new E_Multiply(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_Divide) {
			return new E_Divide(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_Add) {
			return new E_Add(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_Subtract) {
			return new E_Subtract(exprs[0], exprs[1]);
		}
		else if (oldExpr instanceof E_IsLiteral) {
			return new E_IsLiteral(exprs[0]);
		}
		else if (oldExpr instanceof E_IsIRI) {
			return new E_IsIRI(exprs[0]);
		}
		else if (oldExpr instanceof E_IsURI) {
			return new E_IsURI(exprs[0]);
		}
		else if (oldExpr instanceof E_IsBlank) {
			return new E_IsBlank(exprs[0]);
		}
		else if (oldExpr instanceof E_Datatype) {
			return new E_Datatype(exprs[0]);
		}
		else if (oldExpr instanceof E_Lang) {
			return new E_Lang(exprs[0]);
		}
		else if (oldExpr instanceof E_UnaryPlus) {
			return new E_UnaryPlus(exprs[0]);
		}
		else if (oldExpr instanceof E_UnaryMinus) {
			return new E_UnaryMinus(exprs[0]);
		}
		else if (oldExpr instanceof E_Str) {
			return new E_Str(exprs[0]);
		}
		else if (oldExpr instanceof E_Function) {
			return copyFunction((E_Function) oldExpr);
		}
		else {
			throw new SemQAException("generateNewExpr: unknown or unsupported function - " + oldExpr);			
		}
	}
}
