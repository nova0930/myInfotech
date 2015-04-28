package com.infotechsoft.integration.queryplan.semqa.sparql;


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.*;
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
import com.infotechsoft.integration.queryplan.semqa.sparql.*;
import com.infotechsoft.integration.queryplan.semqa.utils.NodeUtils;
import com.infotechsoft.integration.queryplan.semqa.utils.OperationUtils;
import com.infotechsoft.integration.queryplan.semqa.utils.ValueConstraintUtils;

/**
 * Represents a singleton.
 */

public class SPARQLToSemQA {

	/**
	 * Holds singleton instance
	 */
	private static SPARQLToSemQA instance;

	/**
	 * prevents instantiation
	 */
	private SPARQLToSemQA() {
		// prevent creation
	}

	/**
	 * Returns the singleton instance.
	 @return	the singleton instance
	 */
	static public SPARQLToSemQA getInstance() {
		if (instance == null) {
			instance = new SPARQLToSemQA();
		}
		return instance;
	}
	private GraphPattern unionToOr(ElementUnion union) throws SemQAException {
		List<Element> children = union.getElements();
		if ((children == null) || (children.size() == 0)) {
			throw new SemQAException(": cannot have a UNION with no children");
		}
		if (children.size() == 1) {
			GraphPattern res = sparqlToSemQA(children.get(0));
			return res;
		}
		else {
			Set<GraphPattern> childSemQA = new LinkedHashSet<GraphPattern>();
			// create a set of the first list of children to process
			for (Element child : children) {
				childSemQA.add(sparqlToSemQA(child));
			}
			Or ret = new Or(childSemQA);
			return ret;
		}
	}
//	private Or leftJoinToJoinOr(GraphPattern gp, ElementOptional optional) throws SemQAException {
//		// converts a left join to the Or of a Join, see semQA paper
//		// because of SPARQL syntactic notation, must get the non-optional side as parameter
//		GraphPattern right = sparqlToSemQA(optional.getOptionalElement());
//		Join join = new Join(gp, right);
//		return new Or(join, gp);
//	}
	private Filter convertToSemQAFilter(GraphPattern gp, ElementFilter filter) {
		// converts a filter in SPARQL syntax to a SemQA filter
		Expr filterExpr = ValueConstraintUtils.deepCopyExpr(filter.getExpr()); // create a copy of the expression
		ValueConstraint vc = new ValueConstraint(filterExpr);
		Filter ret = new Filter(gp, vc);
		return ret;
	}
	private GraphPattern convertSetToJoin(Set<GraphPattern> childSemQA) {
		Set<GraphPattern> current = childSemQA;
		if (current.size() == 1) { // if only one element, return that element
			return current.toArray(new GraphPattern[1])[0];
		}
		else {
			// let's flatten joins here
			Set<GraphPattern> toJoin = new LinkedHashSet<GraphPattern>();
			boolean repeat = false;
			for (GraphPattern gp : childSemQA) {
				if (OperationUtils.isJoinOp(gp)) {
					toJoin.addAll(((Join) gp).getOperands());
					repeat = true;
				}
				else {
					toJoin.add(gp);
				}
			}
			GraphPattern ret; 
			if (repeat) {
				ret = convertSetToJoin(toJoin);
			}
			else {
				ret = new Join(toJoin);
			}
			return ret;
		}
	}
	private LeftJoin convertToLeftJoin(GraphPattern gp, ElementOptional optional) throws SemQAException {
		// converts a left join to the Or of a Join, see semQA paper
		// because of SPARQL syntactic notation, must get the non-optional side as parameter
		GraphPattern right = sparqlToSemQA(optional.getOptionalElement());
		return new LeftJoin(gp, right);
	}
	private LeftJoin convertToLeftJoin(GraphPattern base, Set<GraphPattern> optionals) throws SemQAException {
		return new LeftJoin(base, optionals);
	}
	private ValueConstraint getConstraintFromFilters(Set<ElementFilter> filters) {
		// generates a single value constraint by ANDing multiple expressions in filters
		Expr left = null;
		for (ElementFilter filter : filters) {
			Expr filterExpr = ValueConstraintUtils.deepCopyExpr(filter.getExpr()); // create a copy of the expression
			if (left == null) {
				left = filterExpr;
			}
			else {
				left = new E_LogicalAnd(left, filterExpr);
			}
		}
		return new ValueConstraint(left);
	}
	private Optional optionalToOptional(ElementOptional optional) throws SemQAException {
		// converts an ElementOptional, which may include a filter, to a semQA Optional object
		Element elm = optional.getOptionalElement();
		GraphPattern gp;
		ValueConstraint vc;
		if (elm instanceof ElementGroup) {
			List<Element> children = ((ElementGroup) elm).getElements();
			Set<GraphPattern> childSemQA = new LinkedHashSet<GraphPattern>();
			Set<Optional> optionals = new LinkedHashSet<Optional>();
			Set<ElementFilter> filters = new LinkedHashSet<ElementFilter>();
			for (Element child : children) {
				if (child instanceof ElementFilter) {
					filters.add((ElementFilter) child);
				}
				else if (child instanceof ElementOptional) {
					// if child is an optional, need to get the graph pattern for the optional
					// and add it to the list of optional graph patterns
					optionals.add(optionalToOptional((ElementOptional) child));
//					childSemQA.add(optionalToOptional((ElementOptional) child));
				}
				else {
					childSemQA.add(sparqlToSemQA(child));
				}
			}
			GraphPattern baseGp = convertSetToJoin(childSemQA);
			if (childSemQA.size() == 0) {
				throw new SemQAException("semQA: OPTIONAL without non-optional side not supported.");
			}
			if (optionals.size() > 0) {
				// if there are optional elements, create a left join
				// convert the non-filter non-optional graph patterns to a Join if necessary
				// then, each optional element is part of a left join
				gp = new LeftJoin(baseGp, optionals);
			}
			else {
				// no optionals, just return the join of the graph patterns
				gp = baseGp;
			}
			if (filters.size() > 0) {
				vc = getConstraintFromFilters(filters);
			}
			else {
				vc = null;
			}
		}
		else {
			gp = sparqlToSemQA(elm); // if it is not a group
			vc = null;
		}
		return new Optional(gp, vc);
		
	}
	private GraphPattern groupToSemQA(ElementGroup group) throws SemQAException {
		List<Element> children = group.getElements();
		if ((children == null) || (children.size() == 0)) {
			throw new SemQAException(": cannot have a GROUP with no children");
		}
		if (children.size() == 1) {
			Element elm = children.get(0);
			if (elm instanceof ElementOptional) {
				throw new SemQAException("semQA: OPTIONAL without non-optional side not supported.");
			}
			GraphPattern res = sparqlToSemQA(elm);
			return res;
		}
		else {
			Set<GraphPattern> childSemQA = new LinkedHashSet<GraphPattern>();
			Set<Optional> optionals = new LinkedHashSet<Optional>();
			Set<ElementFilter> filters = new LinkedHashSet<ElementFilter>();
			// create a set of the first list of children to process
			int i = 0;
			for (Element child : children) {
				if (child instanceof ElementOptional) {
					// if child is an optional, need to get the graph pattern for the optional
					// and add it to the list of optional graph patterns
					optionals.add(optionalToOptional((ElementOptional) child));
					
				}
				else if (child instanceof ElementFilter) {
					// if it is a filter, need to set aside in order to create a filter over the result
					filters.add((ElementFilter) child);
				}
				else {
					// accumulate the non-optional, non-filter parts of this group
					childSemQA.add(sparqlToSemQA(child));
				}
			}
			GraphPattern noFilters;
			if (childSemQA.size() == 0) {
				throw new SemQAException("semQA: OPTIONAL without non-optional side not supported.");
			}
			GraphPattern main = convertSetToJoin(childSemQA);
			if (optionals.size() > 0) {
				// if there are optional elements, create a left join
				// convert the non-filter non-optional graph patterns to a Join if necessary
				// then, each optional element is part of a left join
				noFilters = new LeftJoin(main, optionals);
			}
			else {
				// no optionals, just return the join of the graph patterns
				noFilters = main;
			}
			// process all the filters one by one over the join
			GraphPattern filtered = noFilters;
			if (filters.size() > 0) {
				for (ElementFilter filter : filters) {
					filtered = convertToSemQAFilter(filtered, filter);
				}
			}
			// and finally return the filtered result
			return filtered;
		}
	}
	private GraphPattern triplesBlockToJoin(ElementTriplesBlock block) throws SemQAException {
		BasicPattern children = block.getPattern();
		if ((children == null) || (children.size() == 0)) {
			throw new SemQAException(": cannot have a GROUP with no children");
		}
		if (children.size() == 1) {
			GraphPattern res = sparqlTripleToSemQATriple(children.get(0));
			return res;
		}
		else {
			Set<GraphPattern> childSemQA = new LinkedHashSet<GraphPattern>();
			// create a set of the first list of children to process
			for (Triple child : children) {
				childSemQA.add(sparqlTripleToSemQATriple(child));
			}
			GraphPattern ret = convertSetToJoin(childSemQA);
			return ret;
		}
	}
	private GraphPattern pathBlockToJoin(ElementPathBlock block) throws SemQAException {
	        PathBlock children = block.getPattern();
                if ((children == null) || (children.size() == 0)) {
                        throw new SemQAException(": cannot have a GROUP with no children");
                }
                if (children.size() == 1) {
                        GraphPattern res = sparqlTripleToSemQATriple(children.get(0));
                        return res;
                }
                else {
                        Set<GraphPattern> childSemQA = new LinkedHashSet<GraphPattern>();
                        // create a set of the first list of children to process
                        for (TriplePath child : children.getList()) {
                                childSemQA.add(sparqlTripleToSemQATriple(child));
                        }
                        GraphPattern ret = convertSetToJoin(childSemQA);
                        return ret;
                }
        }
	private TriplePattern sparqlTripleToSemQATriple(Triple t) throws SemQAException {
		
		TriplePattern tp = new TriplePattern(
				NodeUtils.copyNode(t.getSubject()),
				NodeUtils.copyNode(t.getPredicate()),
				NodeUtils.copyNode(t.getObject()));
		return tp;
	}
	private TriplePattern sparqlTripleToSemQATriple(TriplePath t) throws SemQAException {
            
                TriplePattern tp = new TriplePattern(
                                NodeUtils.copyNode(t.getSubject()),
                                NodeUtils.copyNode(t.getPredicate()),
                                NodeUtils.copyNode(t.getObject()));
                return tp;
        }
	private GraphPattern sparqlToSemQA(Element elm) throws SemQAException {
		// for now, we handle ElementGroup, ElementTriplesBlock, ElementFilter, ElementUnion, ElementOptional
		// left unhandled: named graphs and subqueries, as well as ARQ-specific stuff (i.e., UNSAID).
		GraphPattern ret;
		if (elm instanceof ElementGroup) {
			ret = groupToSemQA((ElementGroup) elm);
		}
		else if (elm instanceof ElementUnion) {
			ret = unionToOr((ElementUnion) elm);
		}
		else if (elm instanceof ElementTriplesBlock) {
			ret = triplesBlockToJoin((ElementTriplesBlock) elm);
		}
                else if (elm instanceof ElementPathBlock) {
                        ret = pathBlockToJoin((ElementPathBlock) elm);
                }
		else if (elm instanceof ElementFilter) {
			throw new SemQAException("semQA: FILTER without pattern unsupported");
		}
		else if (elm instanceof ElementOptional) {
			throw new SemQAException("semQA: OPTIONAL without non-optional pattern unsupported");
		}
		else {
			throw new SemQAException("semQA: unsupported element: " + elm.toString());
		}
		return ret;
	}
	
	public QAQuery sparqlToSemQA(Query q) throws SemQAException {
		Element top = q.getQueryPattern();
		GraphPattern gp = sparqlToSemQA(top);
		QAQuery qa = new QAQuery(gp);
		return qa;
	}
}