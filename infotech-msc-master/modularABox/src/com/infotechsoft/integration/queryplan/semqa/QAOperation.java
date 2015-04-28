package com.infotechsoft.integration.queryplan.semqa;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;


public abstract class QAOperation implements GraphPattern {
	public static final int PRIME_NUMBER = 37;
	public static final int SEED = 17;
	protected Set<Node_URI> terms = new LinkedHashSet<Node_URI>();
	protected Set<Var> varsInOps = new LinkedHashSet<Var>();
	protected Set<Var> vars = new LinkedHashSet<Var>();

	abstract public QAOperators getOperator();
	
	public Set<Node_URI> getUriMentioned() {
		return terms;
	}

	public Set<Var> getVarsMentioned() {
		return vars;
	}
	public Set<Var> getVarsMentionedInOperands() {
		return varsInOps;
	}

	
}
