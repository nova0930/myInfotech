package com.infotechsoft.integration.queryplan.semqa.sparql;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.infotechsoft.integration.queryplan.semqa.Filter;
import com.infotechsoft.integration.queryplan.semqa.GraphPattern;
import com.infotechsoft.integration.queryplan.semqa.Join;
import com.infotechsoft.integration.queryplan.semqa.LeftJoin;
import com.infotechsoft.integration.queryplan.semqa.Optional;
import com.infotechsoft.integration.queryplan.semqa.Or;
import com.infotechsoft.integration.queryplan.semqa.QAQuery;
import com.infotechsoft.integration.queryplan.semqa.TriplePattern;
import com.infotechsoft.integration.queryplan.semqa.ValueConstraint;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;
import com.infotechsoft.integration.queryplan.semqa.utils.NodeUtils;
import com.infotechsoft.integration.queryplan.semqa.utils.ValueConstraintUtils;

/**
 * Represents a singleton.
 */

public class SemQAToSPARQL {

	/**
	 * Holds singleton instance
	 */
	private static SemQAToSPARQL instance;

	/**
	 * prevents instantiation
	 */
	private SemQAToSPARQL() {
		// prevent creation
	}

	/**
	 * Returns the singleton instance.
	 @return	the singleton instance
	 */
	static public SemQAToSPARQL getInstance() {
		if (instance == null) {
			instance = new SemQAToSPARQL();
		}
		return instance;
	}
	private Triple triplePatternToTriple(TriplePattern tp) throws SemQAException {
		Node s = NodeUtils.copyNode(tp.getSubject());
		Node p = NodeUtils.copyNode(tp.getProperty());
		Node o = NodeUtils.copyNode(tp.getObject());
		Triple ret = new Triple(s, p, o);
		return ret;
	}
	private Element triplePatternToTripleBlock(TriplePattern tp) throws SemQAException {
		// use when need a block with a single pattern
		Triple t = triplePatternToTriple(tp);
		ElementTriplesBlock block = new ElementTriplesBlock();
		block.addTriple(t);
		return block;
	}
	private Element filterToFilter(Filter f) throws SemQAException {
		Element ret;
		ValueConstraint vc = f.getConstraint();
		Expr expr = ValueConstraintUtils.deepCopyExpr(vc.getExpr());
		Element efilter = new ElementFilter(expr);
		// now get the operand
		GraphPattern op = f.getOperand();
		Element opelm = graphPatternToElement(op);
		if (opelm instanceof ElementGroup) {
			// add the filter to the group
			((ElementGroup) opelm).addElement(efilter);
			ret = opelm; // and return it
		}
		else {
			// create a new Element Group, and add the operand and the filter
			ElementGroup group = new ElementGroup();
			group.addElement(opelm);
			group.addElement(efilter);
			ret = group;
		}
		return ret; // which needs to be put in a group together with its operand
	}
	private Element orToUnion(Or or) throws SemQAException {
		Set<GraphPattern> ops = or.getOperands();
		ElementUnion ret = new ElementUnion();
		for (GraphPattern op : ops) {
			ret.addElement(graphPatternToElement(op));
		}
		return ret;
	}
	private Element joinToGroup(Join join) throws SemQAException {
		Set<GraphPattern> ops = join.getOperands();
		// first find out if there are any triple blocks inside the join
		Set<TriplePattern> tps = new LinkedHashSet<TriplePattern>(ops.size());
		Set<GraphPattern> complex = new LinkedHashSet<GraphPattern>(ops.size());
		for (GraphPattern op : ops) {
			if (op instanceof TriplePattern) {
				tps.add((TriplePattern) op);
			}
			else {
				complex.add(op);
			}
		}
		ElementGroup group = new ElementGroup();
		if (tps.size() > 0) {
			// so now, we build a triple block with the triple patterns
			ElementTriplesBlock block = new ElementTriplesBlock();
			for (TriplePattern tp : tps) {
				block.addTriple(triplePatternToTriple(tp));
			}
			group.addElement(block);
		}
		// and then process the complex stuff
		Element ret;
		if (complex.size() > 0) {
			for (GraphPattern gp : complex) {
				ret = graphPatternToElement(gp);
				group.addElement(ret);
			}
		}
		// so finally, just make sure that we do not have just one element group inside another
		List<Element> elms = group.getElements(); 
		if ((elms.size() == 1) && (elms.get(0) instanceof ElementGroup)) {
			return elms.get(0);
		}
		else {
			return group;
		}
	}
	private Element leftJoinToGroup(LeftJoin lj) throws SemQAException {
		// create an element group with the base and the optional parts
		ElementGroup group = new ElementGroup();
		// first, process the non-optional side of the left join
		GraphPattern base = lj.getBaseOperand();
		Element baseElm = graphPatternToElement(base);
		// if the base is a group, collapse it
		if (baseElm instanceof ElementGroup) {
			List<Element> baseGroupElms = ((ElementGroup) baseElm).getElements();
			for (Element bge : baseGroupElms) {
				group.addElement(bge);
			}
		}
		else { // otherwise, just add it
			group.addElement(baseElm);
		}
		// then, process all the optionals, and create an optional element of each
		Set<GraphPattern> opts = lj.getOptionalOperands();
		int i = 0;
		Element optPart;
		ElementOptional optElm;
		for (GraphPattern opt : opts) {
			if (opt instanceof Optional) {
				Optional optOpt = (Optional) opt;
				GraphPattern gp = optOpt.getOperand();
				optPart = graphPatternToElement(gp);
				ValueConstraint vc = optOpt.getConstraint();
				if (vc != null) {
					// if a value constraint exists in the optional, put the filter element together with the optional part in a group  
					ElementFilter f = new ElementFilter(ValueConstraintUtils.deepCopyExpr(vc.getExpr()));
					ElementGroup g = graphPatternsToGroup(optPart, f);
					optElm = new ElementOptional(g);
				}
				else {
					// if no value constraint, just create an element optional with the optional part
					optElm = new ElementOptional(optPart);
				}
				group.addElement(optElm); 
			}
			else {
				throw new SemQAException("LEFTJOIN should only have Optional objects in its optional side.");
			}
		}
		return group;
	}
	private Element graphPatternToElement(GraphPattern gp) throws SemQAException {
		Element ret;
		if (gp instanceof Filter) {
			ret = filterToFilter((Filter) gp);
		}
		else if (gp instanceof Or) {
			ret = orToUnion((Or) gp);
		}
		else if (gp instanceof Join) {
			ret = joinToGroup((Join) gp);
		}
		else if (gp instanceof LeftJoin) {
			ret = leftJoinToGroup((LeftJoin) gp);
		}
//		else if (gp instanceof VariableMapper) {
////			throw new SemQAException("graphPatternToElement: VARMAP not implemented - " + gp);
//			System.err.println("[WARNING] graphPatternToElement: VARMAP not implemented - should map " 
//					+ ((VariableMapper) gp).mapAsString());
//			return graphPatternToElement(((VariableMapper) gp).getOperand());
//		}
		else if (gp instanceof TriplePattern) {
			ret = triplePatternToTripleBlock((TriplePattern) gp);
		}
		else {
			throw new SemQAException("semQAToSPARQL: unknown operation " + gp.toString());
		}
		return ret;
	}
	private ElementGroup graphPatternsToGroup(Element... elms) throws SemQAException {
		ElementGroup ret = new ElementGroup();
		for (Element elm : elms) {
			ret.addElement(elm);
		}
		return ret;
	}
	/**
	 * Generates a query from a graph pattern instead of from a complete SemQA query
	 * @param gp
	 * @return
	 * @throws SemQAException
	 */
	public Query semQAToSparql(GraphPattern gp) throws SemQAException {
		Element elm = graphPatternToElement(gp);
		Query sparqlq = new Query();
		sparqlq.setQuerySelectType();
		// see if we need to set the variables
		sparqlq.setQueryPattern(elm);
		return sparqlq;
	}
	/**
	 * Generates a SPARQL query from a SemQA query
	 * @param q
	 * @return
	 * @throws SemQAException
	 */
	public Query semQAToSparql(QAQuery q) throws SemQAException {
		GraphPattern top = q.getGraphPattern();
		Query sparqlq = semQAToSparql(top);
		return sparqlq;
	}
}