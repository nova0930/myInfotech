package com.infotechsoft.integration.queryplan.semqa;

import java.util.Set;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

public interface GraphPattern {
	/**
	 * Returns true if the parameter is contained (is a sub-graph pattern of) this graph pattern.
	 * @param gp the potential sub-graph pattern
	 * @return true if contained
	 */
	public boolean contains(GraphPattern gp);
	/**
	 * @return a copy of this graph pattern.
	 * @throws SemQAException
	 */
	public GraphPattern copy() throws SemQAException;
	/**
	 * @return a set with all the URIs mentioned inside this graph pattern.
	 */
	public Set<Node_URI> getUriMentioned();
	/**
	 * @return a set with all the variables mentioned inside this graph pattern.
	 */
	public Set<Var> getVarsMentioned();
	/**
	 * @return a set with all the variables mentioned only in the operands of this graph pattern (NOT in value constraints)
	 */
	public Set<Var> getVarsMentionedInOperands();
}
