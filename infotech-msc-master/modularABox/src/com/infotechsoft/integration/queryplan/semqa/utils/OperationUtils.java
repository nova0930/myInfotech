package com.infotechsoft.integration.queryplan.semqa.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;
import com.infotechsoft.integration.queryplan.semqa.Filter;
import com.infotechsoft.integration.queryplan.semqa.GraphPattern;
import com.infotechsoft.integration.queryplan.semqa.Join;
import com.infotechsoft.integration.queryplan.semqa.LeftJoin;
import com.infotechsoft.integration.queryplan.semqa.Optional;
import com.infotechsoft.integration.queryplan.semqa.Or;
import com.infotechsoft.integration.queryplan.semqa.QAMultiOp;
import com.infotechsoft.integration.queryplan.semqa.QAQuery;
import com.infotechsoft.integration.queryplan.semqa.TriplePattern;
import com.infotechsoft.integration.queryplan.semqa.ValueConstraint;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

public class OperationUtils {
	private OperationUtils() {
		// do not allow instantiation
	}
	public static boolean isTriplePattern(GraphPattern op) {
		return (op instanceof TriplePattern);
	}
	public static boolean isValueConstraint(GraphPattern op) {
		return (op instanceof ValueConstraint);
	}
	public static boolean isFilterOp(GraphPattern op) {
		return (op instanceof Filter);
	}
	public static boolean isMultiOp(GraphPattern gp) {
		return (gp instanceof QAMultiOp);
	}
	public static boolean isJoinOp(GraphPattern gp) {
		return (gp instanceof Join);
	}
	public static boolean isOrOp(GraphPattern gp) {
		return (gp instanceof Or);
	}
	public static boolean isLeftJoinOp(GraphPattern gp) {
		return (gp instanceof LeftJoin);
	}
	public static boolean isOptionalOp(GraphPattern gp) {
		return (gp instanceof Optional);
	}
	public static boolean isQAQuery(GraphPattern gp) {
		return (gp instanceof QAQuery);
	}
	/**
	 * Determines if any element in the first set is contained in the second set.
	 * @param <T> the type of the elements in the sets
	 * @param s1 the set that contains the elements to be checked
	 * @param s2 the set in which elements may be contained
	 * @return true if any element in the first set is contained in the second set, false otherwise. Also returns false if either set is null.
	 */
	public static <T> boolean anyContainedIn(Set<T> s1, Set<T> s2) {
		if ((s1 == null) || (s2 == null)) {
			return false;
		}
		for (T o : s1) {
			if (s2.contains(o)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks if any Var from gp is contained in the graph pattern otherGp
	 * @param gp
	 * @param setOfGps
	 * @return true if any var is contained
	 */
	public static boolean anyVarContainedIn(GraphPattern gp, GraphPattern otherGp) {
		if ((gp == null) || (otherGp == null)) {
			return false;
		}
		Set<Var> gpvars = gp.getVarsMentioned();
		Set<Var> othervars = otherGp.getVarsMentioned();
		return anyContainedIn(gpvars, othervars);
	}
	/**
	 * Checks if any Var from gp is contained in any of the graph patterns in the set 
	 * @param gp
	 * @param setOfGps
	 * @return true if any var is contained
	 */
	public static boolean anyVarContainedIn(GraphPattern gp, Set<GraphPattern> setOfGps) {
		if ((gp == null) || (setOfGps == null) || (setOfGps.size() == 0)) {
			return false;
		}
		for (GraphPattern other : setOfGps) {
			if (anyVarContainedIn(gp, other)) {
				return true;
			}
		}
		return false;
	}
	public static QAMultiOp createMultiJoinOp(GraphPattern op1, GraphPattern op2) {
		QAMultiOp ret = new Join(op1, op2);
		return ret;
	}
	public static QAMultiOp createMultiOrOp(GraphPattern op1, GraphPattern op2) {
		QAMultiOp ret = new Or(op1, op2);
		return ret;
	}
	private static Set<GraphPattern> getNonOrOps(Or gp) {
		Set<GraphPattern> retOps = new LinkedHashSet<GraphPattern>();
		Set<GraphPattern> ops = ((Or) gp).getOperands();
		Set<GraphPattern> res;
		for (GraphPattern op : ops) {
			if (isOrOp(op)) { // if it is an or, then get all its operands and add them
				res = getNonOrOps((Or) op);
				retOps.addAll(res);
			}
			else { // if it is not an or, then add it by itself
				retOps.add(op);
			}
		}
		return retOps;
	}
	private static Set<Set<GraphPattern>> crossProduct(Set<Set<GraphPattern>> ops1, Set<GraphPattern> ops2, boolean isLeftJoin) {
		// the sets are as follows:
		// for the two-level set, the top level is an "OR", the second level is an "AND" or "LEFTJOIN optionals" of graph patterns
		// for the one-level set, it must be an "OR" of graph patterns
		Set<Set<GraphPattern>> ret = new LinkedHashSet<Set<GraphPattern>>();
		if ((ops1 == null) && (ops2 == null)) {
		}
		else if ((ops2 == null) || (ops2.size() == 0)) {
			ret = ops1; // just return the incoming set
		}
		else if ((ops1 == null) || (ops1.size() == 0)) {
			// must separate each element of the second set into one set each for the return set
			for (GraphPattern gp2 : ops2) {
				Set<GraphPattern> setgp2 = new LinkedHashSet<GraphPattern>(1);
				setgp2.add(gp2);
				ret.add(setgp2);
			}
		}
		else {
			for (Set<GraphPattern> setOfGps1 : ops1) {
				for (GraphPattern gp2 : ops2) {
					Set<GraphPattern> term = new LinkedHashSet<GraphPattern>();
					term.addAll(setOfGps1);
					if (isLeftJoin) {
						term.add(new Optional(gp2, null)); // need to make it an Optional
					}
					else {
						term.add(gp2);
					}
					ret.add(term);
				}
			}
		}
		return ret;
	}
	private static Join createJoinFromSet(Set<GraphPattern> ops) {
		if (ops == null) {
			return null;
		}
		Set<GraphPattern> term = new LinkedHashSet<GraphPattern>();
		for (GraphPattern gp : ops) {
			if (isJoinOp(gp)) {
				term.addAll(((Join) gp).getOperands());
			}
			else {
				term.add(gp);
			}
		}
		return new Join(term);
	}
		
	private static QAMultiOp distributeJoinOrLeftJoinInsideOr(QAMultiOp q) throws SemQAException {
		if (isJoinOp(q)) {
			return distributeJoinInsideOr((Join) q);
		}
		else if (isLeftJoinOp(q)) {
			return distributeLeftJoinInsideOr((LeftJoin) q);
		}
		else {
			throw new SemQAException(": cannot distribute if not a Join or left join - " + q);
		}
	}
	private static QAMultiOp distributeJoinInsideOr(Join q) {
		Set<GraphPattern> ops = q.getOperands();
		Set<Set<GraphPattern>> distSet = new LinkedHashSet<Set<GraphPattern>>(ops.size());
		for (GraphPattern op : ops) {
			Set<GraphPattern> nonOrOps;
			if (isOrOp(op)) {
				nonOrOps = getNonOrOps((Or) op); 
			}
			else {
				// not the most efficient but if not we need to change the type inside the set of sets
				nonOrOps = new LinkedHashSet<GraphPattern>(1); // singleton as a set
				nonOrOps.add(op);
			}
			distSet.add(nonOrOps);
		}
		Set<Set<GraphPattern>> res = new LinkedHashSet<Set<GraphPattern>>();
		for (Set<GraphPattern> dist : distSet) {
			res = crossProduct(res, dist, false);
		}
		// now res contains a set of graph pattern sets, each of which represents a join to put in an Or
		Set<GraphPattern> setOfJoins = new LinkedHashSet<GraphPattern>();
		for (Set<GraphPattern> joinSet : res) {
			Join j = createJoinFromSet(joinSet);
			setOfJoins.add(j);
		}
		QAMultiOp ret = new Or(setOfJoins);
		return ret;
	}
	private static Set<LeftJoin> distributeBaseInsideOr(LeftJoin q) throws SemQAException {
		GraphPattern base = q.getBaseOperand();
		Set<GraphPattern> opts = q.getOptionalOperands();
		// if the base is composed or Or'ed elements, then distribute them
		if (isOrOp(base)) {
			Set<GraphPattern> nonOrOps = getNonOrOps((Or) base);
			// each of the or ops needs to be leftjoined with the rest of the leftjoin
			Set<LeftJoin> leftJoinOps = new LinkedHashSet<LeftJoin>(nonOrOps.size());
			for (GraphPattern nonOrOp : nonOrOps) {
				LeftJoin lj = new LeftJoin(nonOrOp, opts);
				leftJoinOps.add(lj);
			}
			return leftJoinOps;
		}
		else {
			Set<LeftJoin> leftJoinOps = new LinkedHashSet<LeftJoin>(1);
			leftJoinOps.add(q);
			return leftJoinOps; // no change - return the input graph pattern in a set
		}
	}
	private static QAMultiOp distributeLeftJoinInsideOr(LeftJoin q) throws SemQAException {
		GraphPattern base = q.getBaseOperand();
		// get all the optional operands
		Set<GraphPattern> ops = q.getOptionalOperands();
		Set<Set<GraphPattern>> distSet = new LinkedHashSet<Set<GraphPattern>>(ops.size());
		for (GraphPattern op : ops) {
			Set<GraphPattern> nonOrOps;
			if (isOrOp(op)) {
				nonOrOps = getNonOrOps((Or) op); 
			}
			else {
				// not the most efficient but if not we need to change the type inside the set of sets
				nonOrOps = new LinkedHashSet<GraphPattern>(1); // singleton as a set
				nonOrOps.add(op);
			}
			distSet.add(nonOrOps);
		}
		// the cross product of all Or'ed optional operands will be the optional operands of a new left join
		Set<Set<GraphPattern>> res = new LinkedHashSet<Set<GraphPattern>>();
		for (Set<GraphPattern> dist : distSet) {
			res = crossProduct(res, dist, true);
		}
		// create optionals for each cross product
		// now res contains a set of optional operands - need to put it together with the base
		Set<LeftJoin> leftJoins = new LinkedHashSet<LeftJoin>();
		for (Set<GraphPattern> opts : res) {
			LeftJoin lj = new LeftJoin(base, opts);
			if (isOrOp(base)) {
				Set<LeftJoin> distlj = distributeBaseInsideOr(lj);
				leftJoins.addAll(distlj);
			}
			else {
				leftJoins.add(lj);
			}
		}
		QAMultiOp ret;
		if (leftJoins.size() == 1) {
			ret = leftJoins.toArray(new LeftJoin[1])[0];
		}
		else {
			ret = new Or(leftJoins); 
		}

		return ret;
	}
	private static boolean isJoinOrLeftJoinAndContainsOr(QAMultiOp mop) {
		if (!(isJoinOp(mop) || isLeftJoinOp(mop))) {
			return false;
		}
		boolean atLeastOneOr = false;
		for (GraphPattern op : mop.getOperands()) {
			if (isOrOp(op)) {
				atLeastOneOr = true;
				break;
			}
		}
		return atLeastOneOr;
	}
	private static QAMultiOp distributeJoinInsideLeftJoin(Join q) throws SemQAException {
		Set<GraphPattern> ops = q.getOperands();
		Set<GraphPattern> nonLeftJoins = new LinkedHashSet<GraphPattern>(ops.size());
		Set<LeftJoin> leftJoins = new LinkedHashSet<LeftJoin>(ops.size());
		// first, separate the left joins
		for (GraphPattern op : ops) {
			if (isLeftJoinOp(op)) {
				leftJoins.add((LeftJoin) op); 
			}
			else {
				nonLeftJoins.add(op);
			}
		}
		if (leftJoins.size() == 0) {
			// if no left joins, just return the incoming join
			return q;
		}
		else {
			if (ops.size() == 1) {
				// special case: only one operand, and it must be a left join
				return (LeftJoin) ops.toArray(new GraphPattern[1])[0];
			}
			else {
				// now, add all the bases to join with the non-left joins
				// and aggregate the optionals in a single set
				Set<GraphPattern> optionals = new LinkedHashSet<GraphPattern>(ops.size());
				for (LeftJoin lj : leftJoins) {
					nonLeftJoins.add(lj.getBaseOperand());
					optionals.addAll(lj.getOptionalOperands());
				}
				// finally, return a left join
				// note: there must be at least two operands in the nonleftjoins, since the special case has been taken care of
				// and there is at least one optional, since there is at least one left join
				Join j = new Join(nonLeftJoins);
				LeftJoin lj = new LeftJoin(j, optionals);
				return lj;
			}
		}
	}
	private static boolean isJoinAndContainsLeftJoin(QAMultiOp mop) {
		if (!isJoinOp(mop)) {
			return false;
		}
		boolean atLeastOneLeftJoin = false;
		for (GraphPattern op : mop.getOperands()) {
			if (isLeftJoinOp(op)) {
				atLeastOneLeftJoin = true;
				break;
			}
		}
		return atLeastOneLeftJoin;
	}
	public static GraphPattern applyQueryDistribution(GraphPattern mop) throws SemQAException {
		if (isMultiOp(mop)) { // only operate on binary operations, otherwise, just skip
			QAMultiOp mopres;
			mopres = (QAMultiOp) mop;
			// distribute Ors inside both joins or left joins
			if (isJoinOrLeftJoinAndContainsOr(mopres)) { 
				mopres = distributeJoinOrLeftJoinInsideOr(mopres); // distribute the join if possible
				if (mopres == null) {
					mopres = (QAMultiOp) mop; // if not distributed, then just redistribute inside the original
				}
			}
			QAMultiOp mopres2 = mopres;
			// distribute left joins inside joins
			if (isJoinAndContainsLeftJoin(mopres2)) { 
				mopres = distributeJoinInsideLeftJoin((Join) mopres2); // distribute the join if possible
				if (mopres == null) {
					mopres = mopres2; // if not distributed, then just redistribute the previous result
				}
			}
			// the following is traversing the tree left-order first. Study to see if we need to 
			// traverse bottom-first or top-first instead.
			Set<GraphPattern> ops = mopres.getOperands(); 
			Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>(ops.size());
			for (GraphPattern op : ops) {
				GraphPattern gp = applyQueryDistribution(op);
				if (gp != null) {
					newOps.add(gp);
				}
				else {
					newOps.add(op);
				}
			}
			mopres.setOperands(newOps);
			return mopres;
		}
		else if (isFilterOp(mop)) {
			Filter f = (Filter) mop;
			GraphPattern innerOp = f.getOperand();
			GraphPattern res = applyQueryDistribution(innerOp);
			if (res != null) {
				f.setOperand(res);
			}
			return f;
		}
		else if (isOptionalOp(mop)) {
			Optional opt = (Optional) mop;
			GraphPattern innerOp = opt.getOperand();
			GraphPattern res = applyQueryDistribution(innerOp);
			if (res != null) {
				opt.setOperand(res);
			}
			return opt;
		}
		else {
			return null; // no query distribution performed, just return null
		}
	}
	public static void applyQueryDistribution(QAQuery q) throws SemQAException {
		GraphPattern element = q.getGraphPattern();
		if (element != null) {
			// distribute the element
			GraphPattern res = applyQueryDistribution(element); 
			if (res != null) {
				q.setGraphPattern(res); // and, if changed, set the result as the top element
			}
		}
	}
	private static GraphPattern findMaximumContainedPattern(Set<GraphPattern> ops) {
		Map<GraphPattern, Integer> numberOfSupers = new LinkedHashMap<GraphPattern, Integer>(ops.size());
		for (GraphPattern op1 : ops) {
			// count the number of other graph patterns in which this pattern is contained
			int count = 0;
			for (GraphPattern op2 : ops) {
				if (!(op1 == op2) && isJoinOp(op2) && ((Join) op2).containsOperand(op1)) {
					// op1 contained in op2 but not the same actual gp
					count++; // increase the count
				}
			}
			// set the number of counts
			numberOfSupers.put(op1, count);
		}
		int max = 0;
		GraphPattern maxContained = null;
		for (Map.Entry<GraphPattern, Integer> entry : numberOfSupers.entrySet()) {
			int entryVal = entry.getValue();
			if (entryVal > max) {
				max = entryVal;
				maxContained = entry.getKey();
			}
		}
		return maxContained;
	}
	private static boolean isContainedIn(GraphPattern opIn, Set<GraphPattern> ops) {
		if (opIn == null) {
			return false;
		}
		for (GraphPattern op : ops) {
			if (opIn.equals(op)) {
				return true;
			}
		}
		return false; // got here without finding any matches
	}
	private static GraphPattern getJoinPatternWithoutCommon(Join orig, GraphPattern common) {
		// removes the "common" graph pattern from the original, and returns a new one
		// assume that we already know that the common is contained in the orig
		Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>();
		// for some reason, removeAll does not work (see below)
		// so we will add one by one
		//		 newOps.addAll(orig.getOperands());
		Set<GraphPattern> origOps = ((Join) orig).getOperands();
		if (isJoinOp(common)) {
			//			 newOps.removeAll(((Join) common).getOperands());
			// for some reason, removeAll does not always work
			// it may have something to do with the implementation or use of the equals method
			// we will add one by one instead
			Set<GraphPattern> commonOps = ((Join) common).getOperands();
			for (GraphPattern origOp : origOps) {
				// if the origOp is contained in common, do not add to the return, otherwise add it
				if (!isContainedIn(origOp, commonOps)) {
					newOps.add(origOp);
				}
			}
		}
		else {
			// otherwise, it can only be one of the operands
			//			 newOps.remove(common);
			// see on the other side of the if, we will add one by one
			for (GraphPattern origOp : origOps) {
				// if the origOp is contained in common, do not add to the return, otherwise add it
				if (!common.equals(origOp)) {
					newOps.add(origOp);
				}
			}
		}
		if (newOps.size() == 1) {
			// if only one element, return that element
			return newOps.toArray(new GraphPattern[1])[0];
		}
		else {
			// otherwise, return a Join of the remaining elements
			return new Join(newOps);
		}
	}
	private static GraphPattern nestLeftJoinsIfPossible(LeftJoin ljIn) throws SemQAException {
		// the input pattern represents LeftJoin(base, rest).
		// if the pattern is of the form
		// LeftJoin(base, Or(LeftJoin(p2, p3), p4))
		// then we must check:
		// if p4 = p3, then this resolves to 
		// LeftJoin(LeftJoin(base, p2), p3)  
		// if p4 = Or(p3, p5, ..., pn), then this resolves to
		// Or(LeftJoin(LeftJoin(base, p2), p3), Join(base, p5), ..., Join(base, pn))
		// if p3 = Or(p4, p5, ..., pn), then this resolves to
		// Or(LeftJoin(LeftJoin(base, p2), p4), Join(base, p2, p5), ... Join(base, p2, pn))
		// otherwise, just leave as is
		GraphPattern base = ljIn.getBaseOperand();
		GraphPattern rest = ljIn.getFirstOptionalOperand();
		if (isOrOp(rest)) {
			Set<GraphPattern> ops = ((Or) rest).getOperands();
			GraphPattern[] opsArray = ops.toArray(new GraphPattern[ops.size()]);
			if (opsArray.length > 0) {
				GraphPattern firstOp = opsArray[0];
				GraphPattern p4;
				if (opsArray.length  == 1) {
					// there was only one element in the optional side, so do not change
					return null;
				}
				else if (opsArray.length  == 2) {
					// there was only one more element besides the first (left join)
					p4 = opsArray[1];
				}
				else {
					// create an Or of the rest of the operators to configure as the "rest" of the Or
					Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>(ops.size());
					newOps.addAll(ops);
					newOps.remove(firstOp);
					p4 = new Or(newOps);
				}
				// TODO this assumes that the left join is always the first operator - we may need to make this more robust
				if (isLeftJoinOp(firstOp)) {
					LeftJoin ljInner = (LeftJoin) firstOp;
					Set<GraphPattern> innerOps = ljInner.getOperands();
					if (innerOps.size() != 2) {
						throw new SemQAException("nestLeftJoinsIfPossible: left join contains too many operands - " + rest);
					}
					GraphPattern[] innerOpsArray = innerOps.toArray(new GraphPattern[innerOps.size()]);
					GraphPattern p2 = innerOpsArray[0];
					GraphPattern p3 = innerOpsArray[1];
					LeftJoin first = null;
					Set<GraphPattern> p5TopN; // to hold remaining operands (p5 to pn)
					Set<GraphPattern> p5TopNResolved = new LinkedHashSet<GraphPattern>();
					Or restOfPs = null;
					GraphPattern restOfPsProc = null;
					boolean exactEquals = false;
					if (p3.equals(p4)) {
						// first case, p3 = p4
						first = new LeftJoin(new LeftJoin(base, p2), p3);
						exactEquals = true;
					}
					else if (isOrOp(p4) && ((Or) p4).containsOperand(p3)) {
						// second case, p4 = Or(p3, p5, ..., pn)
						first = new LeftJoin(ljInner, p3);
						p5TopN = ((Or) p4).getOperands();
						GraphPattern temp;
						for (GraphPattern pk : p5TopN) {
							temp = new Join(base, pk);
							p5TopNResolved.add(temp);
						}

					}
					else if (isOrOp(p3) && ((Or) p3).containsOperand(p4)) {
						// third case, p3 = Or(p4, p5, ..., pn) 
						first = new LeftJoin(ljInner, p4);
						p5TopN = ((Or) p3).getOperands();
						p5TopNResolved.add(first);
						GraphPattern temp;
						for (GraphPattern pk : p5TopN) {
							temp = new Join(base, p2, pk);
							p5TopNResolved.add(temp);
						}
					}
					if (first == null) { // not the right pattern, just return null
						return null;
					}
					else {
						// the first (which is a left join) must be now processed to see if more nesting is possible
						GraphPattern newFirst = convertToNestedLeftJoins(first);
						if (newFirst == null) {
							newFirst = first;
						}
						// now, process the restOfPs
						if (exactEquals) {
							return newFirst;
						}
						else {
							restOfPs = new Or(p5TopNResolved);
							// reprocess the rest of Ps to see if there are any more left joins there
							restOfPsProc = convertToLeftJoin(restOfPs);
							if (restOfPsProc == null) {
								// nothing was processed, return with the original Or
								Set<GraphPattern> newp5 = new LinkedHashSet<GraphPattern>(p5TopNResolved.size() + 1);
								newp5.add(newFirst);
								newp5.addAll(restOfPs.getOperands());
								return new Or(newp5);
							}
							else {
								// something was processed
								if (isOrOp(restOfPsProc)) {
									// consolidate in a single Or
									Set<GraphPattern> newp5 = new LinkedHashSet<GraphPattern>(p5TopNResolved.size() + 1);
									newp5.add(newFirst);
									newp5.addAll(((Or) restOfPsProc).getOperands());
									return new Or(newp5);
								}
								else {
									return new Or(newFirst, restOfPsProc);
								}
							}
						}
					}
				}
				else {
					return null; // no change if the first operand of the Or is not a LeftJoin
				}
			}
			else {
				throw new SemQAException("nestLeftJoinsIfPossible: optional operand does not contain any elements - " + rest);
			}
		}
		else {
			return null; // no change if the optional side is not an Or
		}
	}
//	private static Or convertLeftJoinToJoinOr(LeftJoin lj) throws SemQAException {
//		GraphPattern baseOp = lj.getBaseOperand();
//		Set<GraphPattern> optOps = lj.getOptionalOperands();
//		Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>(optOps.size() + 1);
//		newOps.add(baseOp);
//		newOps.addAll(optOps);
//		Join join = new Join(newOps);
//		Or or = new Or(join, baseOp);
//		return or;
//	}
//	public static GraphPattern convertAllLeftJoinsToJoinOrs(int depth, GraphPattern gp) throws SemQAException {
//		GraphPattern newOp, ret;
//		System.out.println("convertAllLeftJoinsToJoinOrs: at depth " + depth);
//		long timeInit = System.currentTimeMillis();
//		if (isMultiOp(gp)) {
//			QAMultiOp bop = (QAMultiOp) gp;
//			// proceed through all operands, even if a left join (will do the base operand first)
//			Set<GraphPattern> ops = bop.getOperands();
//			Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>(ops.size());
//			for (GraphPattern op : ops) {
//				newOp = convertAllLeftJoinsToJoinOrs(depth + 1, op);
//				if (newOp != null) {
//					newOps.add(newOp);
//				}
//				else {
//					newOps.add(op);
//				}
//			}
//			bop.setOperands(newOps);
//			// after traversing the tree, convert this element if it is an optional
//			GraphPattern newBop; 
//			if (isLeftJoinOp(bop)) {
//				newBop = convertLeftJoinToJoinOr((LeftJoin) bop);
//			}
//			else {
//				newBop = bop;
//			}
//			ret = newBop;
//		}
//		else if (isFilterOp(gp)) {
//			// for a filter, just convert inside its one operand
//			Filter f = (Filter) gp;
//			GraphPattern op = f.getOperand();
//			newOp = convertAllLeftJoinsToJoinOrs(depth + 1, op);
//			if (newOp != null) {
//				f.setOperand(newOp);
//			}
//			ret = f; 
//		}
//		else if (VarMapperUtils.isVariableMapper(gp)) {
//			VariableMapper vm = (VariableMapper) gp;
//			GraphPattern op = vm.getOperand();
//			newOp = convertAllLeftJoinsToJoinOrs(depth + 1, op);
//			if (newOp != null) {
//				vm.setOperand(newOp);
//			}
//			ret = vm;
//		}
//		else if (isTriplePattern(gp)) {
//			ret = null; // no need to flatten
//		}
//		else {
//			throw new SemQAException("convertAllLeftJoinsToJoinOrs: unnknown operation " + gp);
//		}
//		long timeEnd = System.currentTimeMillis();
//		System.out.println("convertAllLeftJoinsToJoinOrs: iteration " + depth + "; total elapsed time this iteration: " + (timeEnd - timeInit));
//		return ret;
//		
//	}
//	public static void convertAllLeftJoinsToJoinOrs(QAQuery q) throws SemQAException {
//		GraphPattern element = q.getGraphPattern();
//		if (element != null) {
//			// convert all left joins
//			GraphPattern res = convertAllLeftJoinsToJoinOrs(0, element);
//			if (res != null) {
//				q.setGraphPattern(res); // and, if changed, set the result as the top element
//			}
//		}
//	}
	private static GraphPattern convertToLeftJoin(Or or) throws SemQAException {
		// first, find if any graph pattern is contained in one or more of the others
		Set<GraphPattern> orOps = or.getOperands();
		if (orOps.size() <= 1) {
			return null; // nothing to convert if only one (or zero) operands
		}
		// now, get the pattern contained in the largest number of other patterns
		GraphPattern maxContained = findMaximumContainedPattern(orOps);
		if (maxContained == null) {
			// no pattern was found to be contained in any others
			return null;
		}
		else {
			// create a new LeftJoin if appropriate
			// first, find the operands which contain the maximum contained
			// and put in a set the "remainder" of those operands 
			// (i.e., the part that is not part of the "root" operand)
			// and put those that do not contain the "root" in a separate set.
			// do not set "root" of the left join into any of the two sets
			Set<GraphPattern> ljOps = new LinkedHashSet<GraphPattern>();
			Set<GraphPattern> otherOps = new LinkedHashSet<GraphPattern>();
			for (GraphPattern op : orOps) {
				if (!op.equals(maxContained)) {
					if (isJoinOp(op) && ((Join) op).containsOperand(maxContained)) {
						ljOps.add(getJoinPatternWithoutCommon((Join) op, maxContained));
					}
					else {
						otherOps.add(op);
					}
				}
			}
			// now, create a left join
			GraphPattern lj = null;
			if (ljOps.size() == 0) {
				// if no operands for the left join, do not set anything
			}
			else if (ljOps.size() == 1) {
				// if only one operand remaining, use this one as the other side of the left join
				GraphPattern other = ljOps.toArray(new GraphPattern[1])[0];
				lj = new LeftJoin(maxContained, new Optional(other, null));
			}
			else {
				// if more than one operand, they must be Or'ed 
				Or ljOr = new Or(ljOps);
				lj = new LeftJoin(maxContained, new Optional(ljOr, null));
			}
			// and finally, create an or with the left join (if exists) and the remaining graph patterns
			if (otherOps.size() == 0) {
				return lj; // even if null, in which case nothing was done
			}
			else {
				// add the left join to the Or, and return the Or
				// note: ensure that the left join goes out first
				Set<GraphPattern> finalOps = new LinkedHashSet<GraphPattern>(otherOps.size() + 1);
				if (lj != null) {
					finalOps.add(lj);
				}
				finalOps.addAll(otherOps);
				return new Or(finalOps);
			}
		}
	}
	private static GraphPattern convertToNestedLeftJoins(LeftJoin lj) throws SemQAException {
		// take the left join and see if a nested left join is possible
		// so, first convert the optional side of the left join into a left join if possible
		GraphPattern res;
		GraphPattern optSide = lj.getFirstOptionalOperand();
		if (isOrOp(optSide)) {
			GraphPattern convOptSide = convertToLeftJoin((Or) optSide);
			if (convOptSide != null) {
				// put the converted side as the new optional operand
				Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>(2);
				newOps.add(lj.getBaseOperand());
				newOps.add(convOptSide);
				lj.setOperands(newOps);
				// and now try to nest it
				GraphPattern nestedLj = nestLeftJoinsIfPossible(lj);
				if (nestedLj == null) {
					// not possible, just use the leftJoin
					res = lj;
				}
				else {
					res = nestedLj; 
				}
			}
			else {
				// Or in the optional side but could not convert to a new left join, just use leftjoin
				res = lj;
			}

		}
		else {
			// no Or in the optional side means that there is no possibility of nesting, so just use leftjoin
			res = lj;
		}
		return res;
	}
	public static GraphPattern convertAllOrsToLeftJoins(Or gp) throws SemQAException {
		// first, convert to a LeftJoin
		GraphPattern conv = convertToLeftJoin(gp);
		LeftJoin lj;
		Set<GraphPattern> remainder = null;
		if (conv != null) {
			if (isLeftJoinOp(conv)) {
				lj = (LeftJoin) conv; // ONLY got a left join
			}
			else if (isOrOp(conv)) {
				Set<GraphPattern> ops = ((Or) conv).getOperands(); 
				GraphPattern ljgp = ops.toArray(new GraphPattern[ops.size()])[0];
				if (isLeftJoinOp(ljgp)) {
					lj = (LeftJoin) ljgp;
					remainder = new LinkedHashSet<GraphPattern>();
					remainder.addAll(ops);
					remainder.remove(ljgp);
				}
				else {
					throw new SemQAException("convertToNestedLeftJoins: expected a LeftJoin in the first operand of \n" + conv + "\nafter converting from " + gp);
				}
			}
			else {
				throw new SemQAException("convertToNestedLeftJoins: expected either a LeftJoin or Or in \n" + conv + "\nafter converting from " + gp);
			}
		}
		else { // did not convert, just return null
			return null;
		}
		GraphPattern conv2 = convertToNestedLeftJoins(lj);
		GraphPattern res;
		if (conv2 == null) {
			res = lj;
		}
		else {
			res = conv2;
		}
		// now we need to restore the rest of the Or if it exists
		if (remainder == null) {
			return res;
		}
		else {
			Set<GraphPattern> retOps = new LinkedHashSet<GraphPattern>(remainder.size() + 1);
			if (isOrOp(res)) {
				// create a single or
				Set<GraphPattern> resOps = ((Or) res).getOperands();
				retOps.addAll(resOps); // add these first to preserve order
				retOps.addAll(remainder);
			}
			else { // add the result to the rest of the operands
				retOps.add(res);
				retOps.addAll(remainder);
			}
			// and return an Or
			return new Or(retOps);
		}
	}
	private static Set<GraphPattern> getOperandsIfJoinOrSingleton(GraphPattern base) {
		Set<GraphPattern> baseOps;
		if (isJoinOp(base)) {
			baseOps = ((Join) base).getOperands();
		}
		else {
			baseOps = Collections.singleton(base);
		}
		return baseOps;
	}

	private static Set<GraphPattern> findPatternContainedInAllOpsOfOr(Or or) {
		Set<GraphPattern> ops = or.getOperands();
		Set<GraphPattern> commonGps = new LinkedHashSet<GraphPattern>(ops.size());
		// this is best done with an array:
		GraphPattern[] opsArray = ops.toArray(new GraphPattern[ops.size()]);
		GraphPattern base = opsArray[0];
		Set<GraphPattern> baseOps = getOperandsIfJoinOrSingleton(base);
		for (GraphPattern baseOp : baseOps) {
			boolean foundInAll = true;
			for (int i = 1; i < opsArray.length; i++) {
				if (isJoinOp(opsArray[i])) {
					if (!((Join) opsArray[i]).containsOperand(baseOp)) {
						foundInAll = false;
						break;
					}
				}
				else {
					if (!opsArray[i].equals(baseOp)) {
						foundInAll = false;
						break;
					}
				}
			}
			if (foundInAll) {
				commonGps.add(baseOp);
			}
		}
		return commonGps;
	}
	private static GraphPattern unDistributeOr(Or or) throws SemQAException {
		Set<GraphPattern> commons = findPatternContainedInAllOpsOfOr(or);
		GraphPattern commonGp;
		if (commons.size() == 0) {
			return or; // do not change if no commnos
		}
		else if (commons.size() == 1) {
			commonGp = commons.toArray(new GraphPattern[1])[0];
		}
		else {
			commonGp = new Join(commons); // this will be the "common" join
		}
		Set<GraphPattern> ops = or.getOperands();
		// first need a trick to avoid using an "identity" on the or
		boolean singleTerm = false;
		for (GraphPattern op : ops) {
			if (op.equals(commonGp)) {
				// the common is equal to one of the terms
				singleTerm = true;
				break;
			}
		}
		GraphPattern toRestore = null;
		GraphPattern[] commonArray = commons.toArray(new GraphPattern[commons.size()]);
		if (singleTerm) {
			// must get one of the commonGps - but if only one, then cannot get common
			if (commons.size() == 1) {
				return or; // should not continue processing, just return the input
			}
			else {
				toRestore = commonArray[0];
				if (commons.size() == 2) {
					commonGp = commonArray[1];
				}
				else {
					commons.remove(toRestore);
					commonGp = new Join(commons);
				}
			}
		}
		// now we create the operation to return
		Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>(ops.size());
		for (GraphPattern op : ops) {
			if (isJoinOp(op)) {
				GraphPattern res = getJoinPatternWithoutCommon((Join) op, commonGp);
				if (isMultiOp(res)) {
					int numberOps = ((QAMultiOp) res).getOperands().size();
					if (numberOps <= 1) {
						throw new RuntimeException("ERROR: only " + numberOps + " operands in " + res + 
								" after taking common " + commonGp + " from " + op);
					}
				}
				newOps.add(res);
			}
			else {
				throw new SemQAException("extractCommonsFromOr: to extract commons here all internal patterns should be joins - " + or);
			}
		}
		// and now we return the equivalent:
		Set<GraphPattern> commonGpOps = isMultiOp(commonGp) ? 
				((QAMultiOp) commonGp).getOperands() : 
					Collections.singleton(commonGp);
				Set<GraphPattern> retOps = new LinkedHashSet<GraphPattern>(commonGpOps.size() + 1);
				retOps.addAll(commonGpOps);
				retOps.add(new Or(newOps));
				return new Join(retOps);
	}

	private static GraphPattern unDistributeAllOrs(GraphPattern gp) throws SemQAException {
		GraphPattern res1;
		// first, go to the bottom of the tree from the graph pattern
		if (OperationUtils.isFilterOp(gp)) {
			// get operand and go deeper in tree
			Filter f = (Filter) gp;
			res1 = unDistributeAllOrs(f.getOperand());
			if (res1 != null) {
				f.setOperand(res1);
			}
		}
		else if (OperationUtils.isOptionalOp(gp)) {
			// get operand and go deeper in tree
			Optional opt = (Optional) gp;
			res1 = unDistributeAllOrs(opt.getOperand());
			if (res1 != null) {
				opt.setOperand(res1);
			}
		}
		else if (OperationUtils.isTriplePattern(gp)) {
			// do nothing and just continue
		}
		else if (OperationUtils.isMultiOp(gp)) {
			// go deeper in tree over all operands
			QAMultiOp mop = (QAMultiOp) gp;
			Set<GraphPattern> ops = mop.getOperands();
			Set<GraphPattern> newOps = new LinkedHashSet<GraphPattern>(ops.size());
			for (GraphPattern op : ops) {
				res1 = unDistributeAllOrs(op);
				if (res1 != null) {
					newOps.add(res1);
				}
				else {
					newOps.add(op);
				}
			}
			mop.setOperands(newOps);
		}
//		else if (VarMapperUtils.isVariableMapper(gp)) {
//			VariableMapper vm = (VariableMapper) gp;
//			res1 = unDistributeAllOrs(vm.getOperand());
//			if (res1 != null) {
//				vm.setOperand(res1);
//			}
//		}
		else { // no other operations
			throw new SemQAException("convertAllToLeftJoins: unknown operation - " + gp);
		}
		// now, process this particular graph pattern if appropriate
		if (OperationUtils.isOrOp(gp)) {
			GraphPattern conv = unDistributeOr((Or) gp);
			if (conv != null) {
				return conv;
			}
		}
		// either the operation is not an Or, or it did not find any left joins to create
		return null;
	}
	/**
	 * This procedure finds factors to undistribute an Or, so that
	 * Or(Join(p1, p2), Join(p1, p3)...,Join(p1, pn))
	 * becomes
	 * Join(p1, Or(p2, p3, ... pn))
	 * @param q
	 * @throws SemQAException
	 */
	public static void unDistributeAllOrs(QAQuery q) throws SemQAException {
		GraphPattern top = q.getGraphPattern();
		if (top != null) {
			GraphPattern res = unDistributeAllOrs(top);
			if (res != null) {
				q.setGraphPattern(res);
			}
		}
		else {
			// do nothing, leave a null as the top element
		}
	}
}
